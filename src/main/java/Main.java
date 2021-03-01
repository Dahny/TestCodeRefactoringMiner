import db.DatabaseOperations;
import db.RefactoringData;

import org.apache.commons.io.FileUtils;
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
import java.io.IOException;
import java.util.*;

public class Main {

    public static List<String> repos;
    public static Map<String, List<RefactoringData>> refactoringsInProjects;
    public static Logger mainLogger;
    public static RevCommit currentCommit;
    public static Repository currentRepo;
    public static final String tempRepoDir = "tmp/repo";
    //public static final String tempDir = "tmpfiles/";

    public static void main(String[] args) {
        // Init projects
        repos = Utils.readProjects();

        // Set Logging settings
        setMainLogger();

        // Init git services for miner
        GitService gitService = new GitServiceImpl();
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();

        refactoringsInProjects = new HashMap<>();
        for(String projectName: repos) {
            mainLogger.log(Level.INFO, "cloning repo " + projectName);
            currentRepo = getRepo(projectName, gitService);
            mainLogger.log(Level.INFO, "done cloning " + projectName);
            if(currentRepo == null){
                // This should never happen
                mainLogger.info("Skipping " + projectName + "because fetching the repo caused an exception");
                Utils.removeDirectory(tempRepoDir);
                continue;
            }
            RevWalk walker = Utils.setupRevWalker(currentRepo);
            if(walker == null){
                mainLogger.info("Skipping " + projectName + "because creating the walker caused an exception");
                Utils.removeDirectory(tempRepoDir);
                continue;
            }

            // Init the Analyzer
            MetricAnalyzer analyzer = new MetricAnalyzer(currentRepo, projectName);

            // First commit can't have any refactorings
            try {
                currentCommit = walker.next();
            while (walker.iterator().hasNext()) {
                    currentCommit = walker.next();
                    gitService.checkout(currentRepo, currentCommit.name());

                    mainLogger.info("repo: " + currentRepo.toString());
                    mainLogger.info("currentCommit: " + currentCommit.getName());

                    miner.detectAtCommit(currentRepo, currentCommit.getId().getName(), new RefactoringHandler() {
                        @Override
                        public void handle(String commitId, List<Refactoring> refactorings) {
                            for (Refactoring ref : refactorings) {
                                analyzer.handleRefactoring(ref, commitId, currentCommit);
                            }
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //refactoringsInProjects.put(projectName, analyzer.currentRefactoringData);

            // Clean dir after all commits have ben processed
            removeRepo(projectName);
        }
    }


    public static Repository getRepo(String projectName, GitService gitService){
        try {
            return gitService.cloneIfNotExists(
                    tempRepoDir,
                    projectName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void removeRepo(String projectName){
        try {
            mainLogger.info("removing repo " + projectName);
            FileUtils.deleteDirectory(new File(tempRepoDir));
            mainLogger.info("done removing " + projectName);
        } catch (IOException e) {
            mainLogger.error("Error while removing the repository: " + e.getStackTrace());
        }
    }

    public static void setMainLogger(){
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.INFO);
        mainLogger = LogManager.getLogger(Main.class);
        mainLogger.info("Logger is set!");
    }


}
