import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import db.DatabaseOperations;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.UMLType;
import gr.uom.java.xmi.decomposition.AbstractCodeFragment;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.hibernate.SessionFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Utils {

    /**
     * @param repo JGit Repository
     * @return initialized JGit walker starting at first commit in master branch
     * @throws IOException
     */
    public static RevWalk setupRevWalker(Repository repo) {
        // TODO Assuming master is always main branch, maybe there is a better way?
        try {
            RevWalk walker = new RevWalk(repo);
            ObjectId first = repo.resolve("master");
            walker.markStart(walker.parseCommit(first));

            // Starting with first commit
            walker.sort(RevSort.REVERSE);
            return walker;
        } catch (IOException e) {
            e.printStackTrace();
        }
        // This should never happen
        return null;
    }


    /**
     * @param filePath The path to the file
     * @return whether we are dealing with a test file or not
     */
    //TODO Atm this is really naive, maybe more checks?
    public static boolean isTest(String filePath) {
        return filePath.toLowerCase().contains("test");
    }


    public static List<String> readProjects() {
        // Fetch the repos from file
        List<String> repos = new ArrayList<>();
        try {
            CSVReader reader = new CSVReader(new FileReader("src/main/data/apache_projects.csv"));
            List<String[]> r = reader.readAll();
            r.forEach(x -> repos.add(x[0]));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvException e) {
            e.printStackTrace();
        }
        return repos;
    }

    public static void removeDirectory(String directory) {
        try {
            FileUtils.deleteDirectory(new File(directory));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void makeDatabaseTransaction(SessionFactory sessionFactory, Object data){
        DatabaseOperations dbOperator = new DatabaseOperations(sessionFactory);
        dbOperator.databaseTransaction(data);
    }

    public static List reverseIterable(Iterable<RevCommit> iterable){
        List list = new ArrayList<>();
        iterable.forEach(list::add);
        Collections.reverse(list);
        return list;
    }

    public static String readFileFromGit (Repository repo, RevCommit commit, String filepath) throws IOException {
        try (TreeWalk walk = TreeWalk.forPath(repo, filepath, commit.getTree())) {
            if (walk != null) {
                byte[] bytes = repo.open(walk.getObjectId(0)).getBytes();
                return new String(bytes, StandardCharsets.UTF_8);
            } else {
                throw new IllegalArgumentException("No path found in " + commit.getName() + ": " + filepath);
            }
        }
    }

    public static String readFileFromGit (Repository repo, String commit, String filepath) throws IOException {
        ObjectId commitId = ObjectId.fromString(commit);
        RevWalk revWalk = new RevWalk(repo);
        RevCommit revCommit = revWalk.parseCommit( commitId );

        return readFileFromGit(repo, revCommit, filepath);
    }


    public static File createCustomJavaFile(String path, Set<AbstractCodeFragment> extractedLines) {
        try {
            File file = new File(path);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            // Let's write a custom java file
            writer.write("Public Class ExtracedLines { " );
            writer.newLine();
            writer.write("public void extractedLines() {");
            writer.newLine();
            for(AbstractCodeFragment fragment: extractedLines){
                writer.write(fragment.getString());
                writer.newLine();
            }
            writer.write("}");
            writer.newLine();
            writer.write("}");
            writer.close();

            return file;
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        //this should never happen
        return null;
    }

    //Write the content to a new file at the given path. Creates a new directory at the path if necessary.
    public static void writeFile(String filePath, Object content) throws FileNotFoundException {
        new File(dirsOnly(filePath)).mkdirs();
        PrintStream ps = new PrintStream(filePath);
        ps.print(content);
        ps.close();
    }

    public static String dirsOnly (String fileName) {
        return new File(fileName).getParent();
    }

}
