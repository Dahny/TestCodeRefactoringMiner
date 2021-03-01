import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import gr.uom.java.xmi.UMLOperation;
import gr.uom.java.xmi.UMLType;
import gr.uom.java.xmi.decomposition.AbstractCodeFragment;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.*;
import java.util.ArrayList;
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
                writer.write(fragment.codeRange().getCodeElement());
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

}
