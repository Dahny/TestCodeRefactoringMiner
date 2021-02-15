import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    /**
     * @param repo JGit Repository
     * @return initialized JGit walker starting at first commit in master branch
     * @throws IOException
     */
    public static RevWalk setupRevWalker(Repository repo) throws IOException {
        RevWalk walker = new RevWalk(repo);

        // TODO Assuming master is always main branch, maybe there is a better way?
        ObjectId first = repo.resolve("master");
        walker.markStart(walker.parseCommit(first));
        // Starting with first commit
        walker.sort(RevSort.REVERSE);
        return walker;
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

}
