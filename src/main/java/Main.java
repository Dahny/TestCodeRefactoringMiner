import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import db.ExtractMethod;
import db.RefactoringData;
import gr.uom.java.xmi.decomposition.AbstractCodeFragment;
import gr.uom.java.xmi.decomposition.AbstractCodeMapping;
import gr.uom.java.xmi.decomposition.UMLOperationBodyMapper;
import gr.uom.java.xmi.decomposition.replacement.Replacement;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main {

    public static List<String> repos;
    public static Map<String, List<RefactoringData>> refactoringsInProjects;
    public static List<RefactoringData> currentRefactoringData;
    public static Logger mainLogger;
    public static RevCommit currentCommit;
    public static final String tempDir = "tmp/repo";

    public static void main(String[] args) {
        // Init projects
        repos = Utils.readProjects();

        // Set Logging settings
        setLogger();

        // Init git services for miner
        GitService gitService = new GitServiceImpl();
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();

        Repository repo;
        refactoringsInProjects = new HashMap<>();
        for(String projectName: repos) {
            currentRefactoringData = new ArrayList<>();
            try {
                mainLogger.log(Level.INFO, "cloning repo " + projectName);
                repo = gitService.cloneIfNotExists(
                        tempDir,
                        projectName);
                mainLogger.log(Level.INFO, "done cloning " + projectName);

                RevWalk walker = Utils.setupRevWalker(repo);


                // First commit can't have any refactorings
                currentCommit = walker.next();
                while (walker.iterator().hasNext()) {
                    currentCommit = walker.next();
                    mainLogger.info("repo: " + repo.toString());
                    mainLogger.info("currentCommit: " + currentCommit.getName());

                    miner.detectAtCommit(repo, currentCommit.getId().getName(), new RefactoringHandler() {
                        @Override
                        public void handle(String commitId, List<Refactoring> refactorings) {

                            for (Refactoring ref : refactorings) {
                                handleRefactoring(ref, commitId);
                            }
                        }
                    });
                }
                refactoringsInProjects.put(projectName, currentRefactoringData);

                //CSVWriter

                mainLogger.log(Level.INFO, "removing repo " + projectName);
                // Clean dir after all commits have ben processed
                FileUtils.deleteDirectory(new File(tempDir));
                mainLogger.log(Level.INFO, "done removing " + projectName);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void setLogger(){
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.INFO);
        mainLogger = LogManager.getLogger(Main.class);
        mainLogger.log(Level.INFO, "Logger is set!");
    }

    /**
     * @param refactoring The refactoring to be analyzed
     * @param commitId The relevant commitId of the refactoring
     */
    public static void handleRefactoring(Refactoring refactoring, String commitId){
        Set<ImmutablePair<String, String>> involvedClasses = refactoring.getInvolvedClassesBeforeRefactoring();

        if(involvedClasses.size() > 1){
            // TODO We have te get separate metrics in case more classes are involved?
        } else {
            for (ImmutablePair<String, String> classInfo : involvedClasses) {
                // Check if it is a test file
                if(Utils.isTest(classInfo.getLeft())) {
                    mainLogger.log(Level.DEBUG, String.format("involved classes are: %s", classInfo.getLeft()));
                    mainLogger.log(Level.DEBUG, String.format("commitdate: %s", currentCommit.getCommitTime()));

                    RefactoringData refactoringData = new RefactoringData(commitId, refactoring.toString(),
                            refactoring.getRefactoringType().getDisplayName(),
                            currentCommit.getFullMessage(), currentCommit.getCommitTime());

                    // If the refactoring is an extract method we get the relevant data
                    // Else we just save it as a 'default' refactoring
                    if (refactoring.getRefactoringType().getDisplayName().equals("Extract Method")) {
                        mainLogger.log(Level.DEBUG, String.format("Extract Method found on commit: %s", commitId));
                        ExtractMethod extractMethod = new ExtractMethod(refactoringData);
                        analyzeExtraction((ExtractOperationRefactoring) refactoring, extractMethod);
                    } else {
                        currentRefactoringData.add(refactoringData);
                    }
                }
            }
        }
    }

    public static void analyzeLifetimeClass(){

    }

    /**
     * @param refactoring The refactoring to be analyzed
     * @param extractMethod The extract method data
     */
    public static void analyzeExtraction(ExtractOperationRefactoring refactoring, ExtractMethod extractMethod){
        UMLOperationBodyMapper mapper = refactoring.getBodyMapper();
        Set<AbstractCodeMapping> mappings = mapper.getMappings();

        // set the amount of LoC of the extracted piece of code
        extractMethod.setExtractedLines(mappings.size());
        for(AbstractCodeMapping mapping : mappings) {
            AbstractCodeFragment fragment1 = mapping.getFragment1();
            if(mapping.isExact()){
                // TODO test if there is a better way to check assert is in line of code
                extractMethod.setHasAssertInvolved(fragment1.getString().contains("assert"));
                continue;
            } else{
                mainLogger.log(Level.DEBUG, String.format("original abstract code fragment: %s",
                        fragment1.getString()));
                mainLogger.log(Level.DEBUG, String.format("extracted abstract code fragment: %s",
                        mapping.getFragment2().getString()));
                Set<Replacement> replacements = mapping.getReplacements();
                for(Replacement replacement : replacements) {
                    String valueBefore = replacement.getBefore();
                    mainLogger.log(Level.DEBUG, String.format("before replacement: %s", valueBefore));
                    String valueAfter = replacement.getAfter();
                    mainLogger.log(Level.DEBUG, String.format("after replacement: %s", valueAfter));
                    extractMethod.setTypeOfReplacment(replacement.getType().name());
                }
            }
        }
        // Add the fetched data to the list
        currentRefactoringData.add(extractMethod);
    }

}
