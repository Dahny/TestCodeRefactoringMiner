import db.Project;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.refactoringminer.api.Refactoring;

import java.util.Set;

public class RefactoringProcessor {
    public MetricAnalyzer metricAnalyzer;
    public Repository currentRepository;
    public Project currentProject;
    public Logger refactoringLogger = LogManager.getLogger(RefactoringProcessor.class);
    public final String tempDir = "tmpFiles";

    public RefactoringProcessor(Repository repository, MetricAnalyzer metricAnalyzer, Project project){
        this.metricAnalyzer = metricAnalyzer;
        this.currentRepository = repository;
        this.currentProject = project;
    }

    public void handleRefactoring(Refactoring refactoring, RevCommit commit){
        Set<ImmutablePair<String, String>> classesAfter = refactoring.getInvolvedClassesAfterRefactoring();
        Set<ImmutablePair<String, String>> classesBefore = refactoring.getInvolvedClassesBeforeRefactoring();

        // Checking if whether we go through or skip the refactoring
        checkRefactoring(classesAfter);
        checkRefactoring(classesBefore);

        // write the after classes to local directory
        ImmutablePair<String, String> classInfoAfter = null;
        for(ImmutablePair<String, String> classInfo: classesAfter){
            String file = classInfo.getLeft();
            classInfoAfter = classInfo;

            try {
                String source = Utils.readFileFromGit(currentRepository, commit, file);
                String[] tempSplit = file.split("/");
                String className = tempSplit[tempSplit.length - 1];
                Utils.writeFile(tempDir + "/after/" + className, source);
            } catch(Exception e) {
                e.printStackTrace();
                currentProject.addToExceptionCount();
            }
        }

        RevCommit commitParent = commit.getParent(0);

        // write the before classes to local directory
        ImmutablePair<String, String> classInfoBefore = null;
        for (ImmutablePair<String, String> classInfo : classesBefore) {
            String file = classInfo.getLeft();
            classInfoBefore = classInfo;

            try {
                String source = Utils.readFileFromGit(currentRepository, commitParent, file);
                String[] tempSplit = file.split("/");
                String className = tempSplit[tempSplit.length - 1];
                Utils.writeFile(tempDir + "/before/" + className, source);
            } catch(Exception e) {
                e.printStackTrace();
                currentProject.addToExceptionCount();
            }
        }

        metricAnalyzer.handleRefactoring(refactoring, commit, classInfoAfter);
        metricAnalyzer.handleRefactoring(refactoring, commitParent, classInfoBefore);
    }

    /**
     * This method checks whether it makes sense to analyze the refactoring or skip it
     * @param classes The classes to be checked
     * @return whether it makes sense to anaylze this refactoring or skip it
     */
    public boolean checkRefactoring(Set<ImmutablePair<String, String>> classes) {
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

}
