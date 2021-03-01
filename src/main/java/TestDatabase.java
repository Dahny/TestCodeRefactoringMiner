import db.*;
import org.hibernate.SessionFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
}
