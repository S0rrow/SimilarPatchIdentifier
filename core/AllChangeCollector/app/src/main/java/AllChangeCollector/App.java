/*
    Format of cli <command> <filename> <cli>
 */
package AllChangeCollector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.cli.ParseException;
//import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.logging.log4j.Log4JLogger;
import org.apache.logging.log4j.LogManager;
import org.eclipse.jgit.api.errors.GitAPIException;
import java.util.Properties;

public class App {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    static ArrayList<String> repo_url = new ArrayList<String>(); // clone url for the repository
    static ArrayList<String> repo_name = new ArrayList<String>(); // name of the repository
    static ArrayList<String> repo_list = new ArrayList<String>(); // git dir
    static boolean output = false;
    static boolean help = false;
    static boolean all = false;
    static boolean lce = false;
    static String file_all, file_selected;

    Properties properties;
    static Log4JLogger logger = new Log4JLogger(App.class.getName());

    public static void main(String[] args) throws IOException, ParseException, GitAPIException {
        // cli options
        App main = new App(args);
        main.run(args);
    }

    public App(String[] args) {
        try {
            properties = loadProperties(args[0]);
            output = properties.getProperty("output").equals("true");
            all = properties.getProperty("all").equals("true");
            lce = properties.getProperty("lce").equals("true");
            file_all = properties.getProperty("file_all");
            file_selected = properties.getProperty("file_selected");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run(String[] args) {
        // Options options = createOptions();
        // src/com/google/javascript/jscomp/ControlFlowAnalysis.java
        // ae9ee1dba1a71461b89f05e903a4a627f95af434
        // Closure-14
        // /home/codemodel/turbstructor/M48A2/target/062299_Closure-14/Closure
        GitFunctions gitFunctions = new GitFunctions();
        Gumtree gumtree = new Gumtree();
        String filename = "";
        String filename_lce = "";
        File file;
        File file_lce;
        logger.info("> testing log4j logger");
        if (output) {
            logger.info("> properties : Output is enabled");
            System.out.println(ANSI_YELLOW + "[info] > properties : Output is enabled");
        }
        if (lce) {
            System.out.println(ANSI_YELLOW + "[info] > properties : LCE is enabled");
            filename_lce = file_selected;
            file_lce = new File(filename_lce);
            if (!file_lce.exists()) {
                System.out.println(ANSI_RED + "[error] > properties : LCE file does not exist");
                System.exit(1);
            } else if (!file_lce.isAbsolute()) {
                filename_lce = file_lce.getAbsolutePath();
            }
        }
        if (all) {
            System.out.println(ANSI_YELLOW + "[info] > properties : All is enabled");
            filename = file_all;
            file = new File(filename);
            if (!file.exists()) {
                System.out.println(ANSI_RED + "[error] > properties : All file does not exist");
                System.exit(1);
            } else if (!file.isAbsolute()) {
                filename = file.getAbsolutePath();
            }
        }

        System.out.println(ANSI_YELLOW + "[info] > all : " + all + "\n[info] > lce : " + lce); // DEBUG
        if (all) {
            try {
                extract(filename);
                gitFunctions.clone_repo_jgit(repo_url, repo_list);
                gitFunctions.all_commit(repo_list, repo_name);
                gumtree.runGumtreeForAll(repo_name, repo_list);
            } catch (Exception e) {
                System.out.println(ANSI_RED + "[error] > Exception : " + e.getMessage());
            }
            // Cloning, get commit sha
        }
        if (lce) {
            try {
                extract_lce(filename_lce);
            } catch (Exception e) {
                System.out.println(ANSI_RED + "[error] > Exception : " + e.getMessage());
                System.exit(1);
            }
        }
    }

    private void extract(String file_name) throws FileNotFoundException, IOException {
        System.out.println("======= Extracting File Data =======");
        File file = new File(file_name);
        int count = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                count++;
                String[] result = line.split(" ");
                repo_url.add(result[0]);
                repo_name.add(result[1]);
            }
        }
        System.out.println("Counted repo: " + count + "\n");
    }

    private void extract_lce(String file_name) throws FileNotFoundException, IOException {
        System.out.println("======== Extracting for Given input ============");
        File file = new File(file_name);
        GitFunctions gitFunctions = new GitFunctions();
        Gumtree gumtree = new Gumtree();

        String java_file = "", commit = "", lcs_url = "", lce_name = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                // Format : <file_name(java)> <commit> <git_name> <git_url>
                String[] result = line.split(" ");
                java_file = result[0];
                commit = result[1];
                lce_name = result[2];
                lcs_url = result[3];
            }
        }

        try {
            String repo_git = gitFunctions.clone_designated_lcs(lcs_url, lce_name);
            gumtree.get_changed_file_lce(repo_git, lce_name, commit, java_file);
            gumtree.runGumtreeForLCE(lce_name, repo_git, commit, java_file);

        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    public Properties loadProperties() {
        return loadProperties("D:/repository_d/SPI/core/AllChangeCollector/app/Properties/acc.properties");
    }

    public Properties loadProperties(String path) {
        try {
            System.out.println(ANSI_BLUE + "[status] > loading properties");
            File file = new File(path);
            properties.load(new FileInputStream(file));
            System.out.println(ANSI_GREEN + "[status] > properties loaded");
            return properties;
        } catch (Exception e) {
            System.out.println(ANSI_RED + "[error] > Exception : " + e.getMessage());
            return null;
        }
    }
}