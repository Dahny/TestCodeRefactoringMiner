import db.*;
import org.hibernate.SessionFactory;

public class TestDatabase {

    public static void main(String[] args) {
        // Init Database
        SessionFactory sessionFactory = HiberNateSettings.getSessionFactory();
        DatabaseOperations dbOperator = new DatabaseOperations(sessionFactory.openSession());

        Project project = new Project("testProject");
        RefactoringData data = new RefactoringData("fileloc", "classname", "commitId",
                "summary", "refType", "commitMessage", 42,
                project);

        ExtractMethod em = new ExtractMethod(data);
        em.setExtractedMethodLoc(1);
        em.setExtractedLines(2);
        em.setTypeOfReplacment("repType");
        em.setMethodName("methName");
        em.setWmcWholeClass(3);
        em.setWmcExtractedLines(4);

        dbOperator.makeTransaction(em);
    }
}
