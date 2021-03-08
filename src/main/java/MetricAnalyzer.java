import com.github.mauricioaniche.ck.CK;
import com.github.mauricioaniche.ck.CKClassResult;
import com.github.mauricioaniche.ck.CKMethodResult;
import com.github.mauricioaniche.ck.CKNotifier;
import db.ClassCommitData;
import db.ExtractMethod;
import db.Project;
import db.RefactoringData;
import gr.uom.java.xmi.decomposition.AbstractCodeFragment;
import gr.uom.java.xmi.decomposition.AbstractCodeMapping;
import gr.uom.java.xmi.decomposition.UMLOperationBodyMapper;
import gr.uom.java.xmi.decomposition.replacement.Replacement;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class MetricAnalyzer {
    public Repository repository;
    public Project project;
    public Git git;
    public RefactoringData currentRefactoringData;
    public ExtractMethod currentExtractMethod;
    public List<ClassCommitData> currentClassCommitData;
    public Logger analyzerLogger = LogManager.getLogger(MetricAnalyzer.class);
    public final String tempDir = "tmpFiles";
    public final String tempRepoDir = "tmp/repo";
    public final String extractedLinesFileName = "ExtractedLines.Java";


    public MetricAnalyzer(Repository repo, Project curProject, Git git){
        this.repository = repo;
        this.project = curProject;
        this.git = git;
    }

    /**
     * @param refactoring The refactoring to be analyzed
     * @param commitId The relevant commitId of the refactoring
     */
    public void handleRefactoring(Refactoring refactoring, String commitId, RevCommit currentCommit ){
        currentExtractMethod = null;
        currentRefactoringData = null;
        currentClassCommitData = null;
        Set<ImmutablePair<String, String>> involvedClasses = refactoring.getInvolvedClassesAfterRefactoring();

        for (ImmutablePair<String, String> classInfo : involvedClasses) {
            // Check if it is a test file
            if(Utils.isTest(classInfo.getLeft())) {
                analyzerLogger.debug(String.format("involved classes are: %s", classInfo.getLeft()));
                analyzerLogger.debug(String.format("commitdate: %s", currentCommit.getCommitTime()));

                currentRefactoringData = new RefactoringData(classInfo.getLeft(), classInfo.getRight(),
                        currentCommit.getName(), refactoring.toString(),
                        refactoring.getRefactoringType().getDisplayName(), currentCommit.getFullMessage(),
                        currentCommit.getAuthorIdent().getWhen(), project);

                // If the refactoring is an extract method we get the relevant data
                // Else we just save it as a 'default' refactoring
                if (refactoring.getRefactoringType().getDisplayName().equals("Extract Method")) {
                    analyzerLogger.debug(String.format("Extract Method found on commit: %s", commitId));
                    currentExtractMethod = new ExtractMethod(currentRefactoringData);
                    // cast refactoring as ExtractOperationRefactoring
                    ExtractOperationRefactoring extractRefactoring = (ExtractOperationRefactoring) refactoring;

                    // Analyze the extracted piece of code
                    analyzeExtraction(extractRefactoring);

                    // Analyze the metrics of the related class and the extracted piece of code
                    analyzeMetrics(extractRefactoring.getSourceOperationAfterExtraction().getName(),
                            extractRefactoring.getExtractedCodeFragmentsToExtractedOperation());

                    // Analyze the lifetime of the related class
                    analyzeLifetimeClass();
                }
            }
            else{
                analyzerLogger.debug("Skipping " + refactoring.getName() + " because it is not a test Refactoring");
                break;
            }
        }
    }

    public void analyzeMetrics(String methodName, Set<AbstractCodeFragment> extractedLines){
        boolean useJars = false;
        int maxAtOnce = 0;
        boolean variablesAndFields = true;

        Path tempDirWithPrefix = null;
        try {
            // Get absolute project location
            Path absolutePath = new File("").toPath().toAbsolutePath();
            // Create temp directory to run CK on
            tempDirWithPrefix = Files.createTempDirectory(absolutePath, tempDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Fetch refactoring class file
        String classLocation = currentExtractMethod.getRefactoringData().fileLoc;
        String[] tempSplit = classLocation.split("/");
        String className = tempSplit[tempSplit.length - 1];
        Path source = new File(tempRepoDir + "/" + classLocation).toPath().toAbsolutePath();
        Path target = new File(tempDirWithPrefix.toString()
                + "/" + className).toPath().toAbsolutePath();

        // Create new file for extracted lines of code
        Utils.createCustomJavaFile(tempDirWithPrefix + "/" + extractedLinesFileName, extractedLines);
        try {
            //Move refactorng class file to temp location
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }


        new CK(useJars, maxAtOnce, variablesAndFields).calculate(tempDirWithPrefix, new CKNotifier() {
            @Override
            public void notify(CKClassResult result) {
                if(result.getClassName().equals("ExtracedLines")){
                    currentExtractMethod.setWmcExtractedLines(result.getWmc());
                } else{
                    currentExtractMethod.setWmcWholeClass(result.getWmc());
                    Optional<CKMethodResult> relMethod = result.getMethod(methodName);
                    if(relMethod.isPresent()){
                        CKMethodResult method = relMethod.get();
                        currentExtractMethod.setMethodName(method.getMethodName());
                        currentExtractMethod.setExtractedMethodLoc(method.getLoc());
                    }
                }
            }

            @Override
            public void notifyError(String sourceFilePath, Exception e) {
                System.err.println("Error in " + sourceFilePath);
                e.printStackTrace(System.err);
            }
        });

        // Clean temp directory
        try {
            Files.walk(tempDirWithPrefix)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
        analyzerLogger.debug("TempDir was removed");
    }

    //TODO Check if the file is moved instead of created
    public void analyzeLifetimeClass(){
        currentClassCommitData = new ArrayList<>();
        String classPath = currentExtractMethod.getRefactoringData().fileLoc;
        Iterable<RevCommit> commits = null;
        try {
            commits =  git.log().addPath(classPath).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        List<RevCommit> orderedList = Utils.reverseIterable(commits);
        for(int i = 0; i < orderedList.size(); i++){
            RevCommit commit = orderedList.get(i);
            Date commitDate = commit.getAuthorIdent().getWhen();
            currentClassCommitData.add(new ClassCommitData(
                    commitDate,
                    i==0,
                    currentExtractMethod.getRefactoringData().commitDate.equals(commitDate)));
        }
    }

    /**
     * @param refactoring The refactoring to be analyzed
     */
    public void analyzeExtraction(ExtractOperationRefactoring refactoring){
        UMLOperationBodyMapper mapper = refactoring.getBodyMapper();
        Set<AbstractCodeMapping> mappings = mapper.getMappings();

        // set the amount of LoC of the extracted piece of code
        currentExtractMethod.setExtractedLines(mappings.size());
        for(AbstractCodeMapping mapping : mappings) {
            AbstractCodeFragment fragment1 = mapping.getFragment1();
            if(mapping.isExact()){
                // TODO test if there is a better way to check assert is in line of code
                currentExtractMethod.setHasAssertInvolved(fragment1.getString().contains("assert"));
                continue;
            } else{
                Set<Replacement> replacements = mapping.getReplacements();
                for(Replacement replacement : replacements) {
                    String valueBefore = replacement.getBefore();
                    analyzerLogger.debug(String.format("before replacement: %s", valueBefore));
                    String valueAfter = replacement.getAfter();
                    analyzerLogger.debug(String.format("after replacement: %s", valueAfter));
                    currentExtractMethod.setTypeOfReplacment(replacement.getType().name());
                }
            }
        }
    }
}
