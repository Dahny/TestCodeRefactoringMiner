import db.DatabaseOperations;
import db.HiberNateSettings;
import db.Project;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.hibernate.SessionFactory;
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
    public static Logger mainLogger;
    public static SessionFactory sessionFactory;
    public static DatabaseOperations dbOperator;
    public static RevCommit currentCommit;
    public static Repository currentRepo;
    public static final String tempRepoDir = "tmp/repo";

    public static void main(String[] args) {
        // Init projects
        repos = Utils.readProjects();

        // Set Logging settings
        setMainLogger();

        // Setup database
        setupDatabaseConfig();

        // Init git services for miner
        GitService gitService = new GitServiceImpl();
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();

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
            // set git
            Git git = new Git(currentRepo);

            RevWalk walker = Utils.setupRevWalker(currentRepo);
            if(walker == null){
                mainLogger.info("Skipping " + projectName + "because creating the walker caused an exception");
                Utils.removeDirectory(tempRepoDir);
                continue;
            }

            // Add project to database
            Project curProject = new Project(projectName);
            makeDatabaseTransaction(curProject);

            // Init the Analyzer
            MetricAnalyzer analyzer = new MetricAnalyzer(currentRepo, curProject, git);

            while (walker.iterator().hasNext()) {
                try {
                    currentCommit = walker.next();
                    mainLogger.info("currentCommit: " + currentCommit.getName());
                    mainLogger.info("commitMessage: " + currentCommit.getFullMessage());
                    mainLogger.info("commit: " + currentCommit.getType());
                    mainLogger.info("parent count: " + currentCommit.getParentCount());
                    mainLogger.info("parents length: " + currentCommit.getParents().length);

                    // If first commit or merge commit, skip
                    if (currentCommit.getParentCount() == 0 || currentCommit.getParentCount() > 1)
                        continue;

                    // Check if git status okay for next checkout
                    checkGitStatus(git);

                    //git.checkout(currentRepo, currentCommit.getName());
                    git.checkout().setName(currentCommit.getName()).call();

                    checkGitStatus(git);

                    miner.detectAtCommit(currentRepo, currentCommit.getName(), new RefactoringHandler() {
                        @Override
                        public void handle(String commitId, List<Refactoring> refactorings) {
                            for (Refactoring ref : refactorings) {
                                analyzer.handleRefactoring(ref, commitId, currentCommit);

                                // If refactoring type was in test and of type extract method
                                if(analyzer.currentExtractMethod != null)
                                    makeDatabaseTransaction(analyzer.currentExtractMethod);
                                // if refactoring was in test
                                else if(analyzer.currentRefactoringData != null)
                                    makeDatabaseTransaction(analyzer.currentRefactoringData);
                            }
                        }
                    });
                    currentCommit.reset();
                }
                catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Clean dir after all commits have ben processed
            walker.close();
            currentRepo.close();
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

    public static void makeDatabaseTransaction(Object data){
        mainLogger.debug("Starting database transaction");
        dbOperator = new DatabaseOperations(sessionFactory);
        dbOperator.databaseTransaction(data);
        mainLogger.debug("Database transaction is done, session is closed");
    }

    public static void setupDatabaseConfig(){
        mainLogger.info("Starting Database configuration");
        sessionFactory = HiberNateSettings.getSessionFactory();
        //dbOperator = new DatabaseOperations(sessionFactory);
        mainLogger.info("Database configured");
    }

    public static void checkGitStatus(Git git) throws GitAPIException, InterruptedException {
        Status status = git.status().call();
        if(status.isClean())
            return;
        else
            git.clean().setForce(true).call();
            git.reset().setMode(ResetCommand.ResetType.HARD).call();
            Thread.sleep(100);

    }

    public static void removeRepo(String projectName){
        try {
            mainLogger.info("removing repo " + projectName);
            FileUtils.deleteDirectory(new File(tempRepoDir));
            mainLogger.info("done removing " + projectName);
        } catch (IOException e) {
            mainLogger.error("Error while removing the repository: ");
            e.printStackTrace();
        }
    }

    public static void setMainLogger(){
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.INFO);
        mainLogger = LogManager.getLogger(Main.class);
        mainLogger.info("Logger is set!");
    }


}
