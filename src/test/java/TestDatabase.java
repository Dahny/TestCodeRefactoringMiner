import db.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.hibernate.SessionFactory;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;

import java.util.*;

//Improvised test to see if the hibernate database is setup correctly
public class TestDatabase {

    public static void main(String[] args) {
        // Init Database
        SessionFactory sessionFactory = HiberNateSettings.getSessionFactory();
        DatabaseOperations dbOperator = new DatabaseOperations(sessionFactory);

        // Setup Project and example of found test refactoring
        Project project = createFakeProject();
        dbOperator.databaseTransaction(project);
        RefactoringData refData = createFakeRefactoringData(project);
        dbOperator.databaseTransaction(refData);

        // Setup fake data for the Extract Method and relevant metrics
        ExtractMethod em = createFakeExtractMethod(refData);
        List<ClassCommitData> classCommitData = createFakeClassCommitData(em);
        List<RefactoringMethodMetrics> rmm = createFakeMethodMetrics(em);
        List<RefactoringClassMetrics> rcm = createFakeClassMetrics(em);

        // Add metrics to Extract Method
        em.setClassCommitData(new HashSet<>(classCommitData));
        em.setRefactoringMethodMetrics(new HashSet<>(rmm));
        em.setRefactoringClassMetrics(new HashSet<>(rcm));

        // Add Extract Method to database
        dbOperator.databaseTransaction(em);
    }


    public static List<ClassCommitData> createFakeClassCommitData(ExtractMethod em){
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

    public static ExtractMethod createFakeExtractMethod(RefactoringData refData){
        ExtractMethod em = new ExtractMethod(refData);

        em.setExtractedMethodLoc(1);
        em.setExtractedLines(2);
        em.setTypeOfReplacment("repType");
        em.setMethodName("methName");
        em.setWmcWholeClass(3);
        em.setWmcExtractedLines(4);
        return em;
    }

    public static Project createFakeProject(){
        Project project = new Project("testProject");
        MetaData md = new MetaData();
        md.setProject(project);
        project.setMetaData(md);
        md.addToExceptionCount();
        md.addToSkippedRefactoringCount();
        md.addToSkippedRefactoringCount();
        return project;
    }

    public static RefactoringData createFakeRefactoringData(Project project){
        return new RefactoringData("fileloc", "classname", "commitId",
                "summary", "refType", "commitMessage", new Date(),
                project);
    }



    public static List<RefactoringClassMetrics> createFakeClassMetrics(ExtractMethod em){
        List<RefactoringClassMetrics> lrcm = new ArrayList<>();

        RefactoringClassMetrics rcmBefore = new RefactoringClassMetrics();
        rcmBefore.setBefore(true);
        rcmBefore.setLoc(50);
        rcmBefore.setNumberOfAttributes(5);
        rcmBefore.setNumberOfMethodInvocations(12);
        rcmBefore.setNumberOfMethods(5);
        rcmBefore.setWmc(20);
        rcmBefore.setExtractMethod(em);

        RefactoringClassMetrics rcmAfter = new RefactoringClassMetrics();
        rcmAfter.setBefore(false);
        rcmAfter.setLoc(100);
        rcmAfter.setNumberOfAttributes(5);
        rcmAfter.setNumberOfMethodInvocations(12);
        rcmAfter.setNumberOfMethods(5);
        rcmAfter.setWmc(21);
        rcmAfter.setExtractMethod(em);

        lrcm.add(rcmBefore);
        lrcm.add(rcmAfter);
        return lrcm;
    }

    public static List<RefactoringMethodMetrics> createFakeMethodMetrics(ExtractMethod em){
        List<RefactoringMethodMetrics> lrmm = new ArrayList<>();

        RefactoringMethodMetrics rmmBefore = new RefactoringMethodMetrics();
        rmmBefore.setBefore(true);
        rmmBefore.setComplexity(5);
        rmmBefore.setCoupling(9);
        rmmBefore.setInvolvesAssertion(true);
        rmmBefore.setLoc(20);
        rmmBefore.setMethodName("testMethod");
        rmmBefore.setExtractMethod(em);


        RefactoringMethodMetrics rmmAfter = new RefactoringMethodMetrics();
        rmmAfter.setBefore(false);
        rmmAfter.setComplexity(5);
        rmmAfter.setCoupling(9);
        rmmAfter.setInvolvesAssertion(true);
        rmmAfter.setLoc(19);
        rmmAfter.setMethodName("testMethod");
        rmmAfter.setExtractMethod(em);

        lrmm.add(rmmBefore);
        lrmm.add(rmmAfter);
        return lrmm;
    }

}
