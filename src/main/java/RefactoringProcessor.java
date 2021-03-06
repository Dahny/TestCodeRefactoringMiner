import db.DatabaseOperations;
import db.Project;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.hibernate.SessionFactory;
import org.refactoringminer.api.Refactoring;

import java.util.Set;

public class RefactoringProcessor {
    private SessionFactory sessionFactory;
    private Repository currentRepository;
    private MetricAnalyzer metricAnalyzer;
    private Project currentProject;
    private Logger refactoringLogger = LogManager.getLogger(RefactoringProcessor.class);
    private final String tempDir = "tmpFiles";

    public RefactoringProcessor(Repository repository, MetricAnalyzer metricAnalyzer, SessionFactory sessionFactory){
        this.currentRepository = repository;
        this.metricAnalyzer = metricAnalyzer;
        this.currentProject = metricAnalyzer.getCurrentProject();
        this.sessionFactory = sessionFactory;
    }


    /**
     * This method will analyze the relevant classes of the refactoring using the current and parent commit
     * It will check if the refactoring is a test refactoring and if there is only 1 class involved (for now only
     * working with one)
     * Than it will determine if we will analyze the class files or if we just note down the metadata based on the
     * fact whether it is a Extract Method or nor
     *
     * @param refactoring The refactoring to process
     * @param commit The relevant commit of the refactoring
     */
    public void handleRefactoring(Refactoring refactoring, RevCommit commit){
        Set<ImmutablePair<String, String>> classesAfter = refactoring.getInvolvedClassesAfterRefactoring();
        Set<ImmutablePair<String, String>> classesBefore = refactoring.getInvolvedClassesBeforeRefactoring();

        // Checking if whether we go through or skip the refactoring
        if(checkIfTestRefactoring(classesAfter) && checkIfTestRefactoring(classesBefore)){
            //fetch the parent commit for the before classes
            RevCommit commitParent = commit.getParent(0);

            //For now we are only working with refactorings involving one class at the time
            ImmutablePair<String, String> classInfoAfter;
            ImmutablePair<String, String> classInfoBefore;

            // write classes to disk if we are dealing with an Extract Method
            if(refactoring.getRefactoringType().getDisplayName() == "Extract Method"){
                classInfoAfter = writeFilesToDisk(classesAfter, commit, "/after/");
                classInfoBefore = writeFilesToDisk(classesBefore, commitParent, "/before/");
            } else {
                classInfoAfter = classesAfter.iterator().next();
                classInfoBefore = classesBefore.iterator().next();
            }

            // Analyze the refactoring
            metricAnalyzer.handleTestRefactoring(refactoring, commit, classInfoAfter);
            metricAnalyzer.handleTestRefactoring(refactoring, commitParent, classInfoBefore);

            // If refactoring type was in test and of type extract method
            if(metricAnalyzer.currentExtractMethod != null)
                Utils.makeDatabaseTransaction(sessionFactory, metricAnalyzer.currentExtractMethod);
                // if refactoring was in test
            else if(metricAnalyzer.currentRefactoringData != null)
                Utils.makeDatabaseTransaction(sessionFactory, metricAnalyzer.currentRefactoringData);
        }
    }

    /**
     * This method checks whether it makes sense to analyze the refactoring or skip it
     * @param classes The classes to be checked
     * @return whether it makes sense to anaylze this refactoring or skip it
     */
    public boolean checkIfTestRefactoring(Set<ImmutablePair<String, String>> classes) {
        if (classes.size() > 1) {
            refactoringLogger.info("Skipping refactoring because of multiple classes involved");
            currentProject.addToSkippedRefactoringCount();
            return false;
        }

        // knowing it will only have 1 obj
        for (ImmutablePair<String, String> classInfo : classes) {
            if (Utils.isTest(classInfo.getLeft())) {
                return true;
            } else {
                refactoringLogger.info("Skipping refactoring because it's not a test refactoring");
                currentProject.addToSkippedRefactoringCount();
                return false;
            }
        }
        // shouldn't get here
        return false;
    }

    /**
     * Write the classes to local directory
     * @param classes The class file to be written to disk
     * @param commit The relevant commit of the class file
     * @param dirName The temp dir name where the class will be stored
     * @return The info about the class provided by RefMiner
     */
    private ImmutablePair<String, String> writeFilesToDisk(Set<ImmutablePair<String, String>> classes,
                                                           RevCommit commit, String dirName){
        ImmutablePair<String, String> cInfo = null;
        for(ImmutablePair<String, String> classInfo: classes){
            String file = classInfo.getLeft();
            cInfo = classInfo;

            try {
                String source = Utils.readFileFromGit(currentRepository, commit, file);
                String[] tempSplit = file.split("/");
                String className = tempSplit[tempSplit.length - 1];
                Utils.writeFile(tempDir + dirName + className, source);
            } catch(Exception e) {
                e.printStackTrace();
                currentProject.addToExceptionCount();
            }
        }
        return cInfo;
    }

}
