import db.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.hibernate.SessionFactory;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import java.util.*;

public class TestDatabase {

    public static void main(String[] args) {
        // Init Database
        SessionFactory sessionFactory = HiberNateSettings.getSessionFactory();
        DatabaseOperations dbOperator = new DatabaseOperations(sessionFactory);

        Project project = new Project("testProject");
        RefactoringData data = new RefactoringData("fileloc", "classname", "commitId",
                "summary", "refType", "commitMessage", new Date(),
                project);

        ExtractMethod em = new ExtractMethod(data);
        em.setExtractedMethodLoc(1);
        em.setExtractedLines(2);
        em.setTypeOfReplacment("repType");
        em.setMethodName("methName");
        em.setWmcWholeClass(3);
        em.setWmcExtractedLines(4);
        List<ClassCommitData> classCommitData = testMethod(em);

        Set<ClassCommitData> ccddata = new HashSet<>(classCommitData);
        for(ClassCommitData ccd:ccddata)
            System.out.println(ccd.toString());
        em.setClassCommitData(new HashSet<>(classCommitData));

        dbOperator.databaseTransaction(em);

//        Path tempDirWithPrefix = null;
//        try {
//            // Create temp directory to run CK on
//            Path path = new File("").toPath().toAbsolutePath();
//            System.out.println(path);
//            tempDirWithPrefix = Files.createTempDirectory(path, "tmpFiles");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println(tempDirWithPrefix);
//        boolean deleted = tempDirWithPrefix.toFile().delete();
//        System.out.println(deleted);



    }


    public static List<ClassCommitData> testMethod(ExtractMethod em){
        // Init git services for miner
        GitService gitService = new GitServiceImpl();
        Repository repo = null;

        try {
            repo = gitService.cloneIfNotExists(
                    "tmp/repo",
                    "https://github.com/apache/dubbo.git");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Git git = new Git(repo);
        ArrayList<ClassCommitData> data = new ArrayList<>();
        Iterable<RevCommit> commits = null;
        try {
            commits =  git.log().addPath("dubbo-registry-default/src/test/java/com/alibaba/dubbo/registry/dubbo/RegistryDirectoryTest.java").call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        List<RevCommit> orderedList = Utils.reverseIterable(commits);
        for(int i = 0; i < orderedList.size(); i++){
            RevCommit commit = orderedList.get(i);
            Date commitDate = commit.getAuthorIdent().getWhen();
            System.out.println(commit.getAuthorIdent().getWhen());
            ClassCommitData ccd = new ClassCommitData(
                    commitDate,
                    i==0,
                    false);
            ccd.setExtractMethod(em);
            data.add(ccd);
        }
        return data;
    }
}
