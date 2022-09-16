/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package ChangeCollector;

import java.io.*;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

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

    static Logger logger = LogManager.getLogger(App.class.getName());

    public static void main(String[] args) {
        Configurator.setLevel(App.class, Level.TRACE);
        App app = new App();
        Properties properties = args.length > 0 ? app.loadProperties(args[0]) : app.loadProperties();
        app.run(properties);
    }

    public void run(Properties properties) {
        // args designates the path of properties file
        GitFunctions gitFunctions = new GitFunctions();
        Extractor extractor = new Extractor();
        Implemental implemental = new Implemental();
        // properties
        String project_root = properties.getProperty("project_root"); // the root directory of the project
        String file_name = properties.getProperty("file_name"); // file name to extract change vector from
        String commit_id = properties.getProperty("commit_id"); // commit id to extract change vector from
        String git_name = properties.getProperty("git_name"); // repository name : unnecessary if url is given
        String git_url = properties.getProperty("git_url"); // repository url
        String output_dir = properties.getProperty("output_dir"); // output directory
        boolean doClean = properties.getProperty("doClean").equals("true"); // a boolean trigger to determine whether to
                                                                            // clean output directory or not
        String mode = properties.getProperty("mode"); // mode : "repository" or "file" or "defects4j"
        String java_home_8 = properties.getProperty("JAVA_HOME.8"); // directory where jdk 8 is installed
        // Defects4J
        String defects4j_name = properties.getProperty("defects4j_name"); // defects4j bug name
        String defects4j_id = properties.getProperty("defects4j_id"); // defects4j bug id
        String hash_id = properties.getProperty("hash_id"); // hash id of the current execution

        // in case of hash id usage
        if (hash_id != null) {
            output_dir = String.format("%s/%s/%s", output_dir, hash_id, "outputs/ChangeCollector");
        }
        // clean output directory
        if (doClean) {
            logger.debug(ANSI_PURPLE + "[debug] > Cleaning output directory" + ANSI_RESET);
            try {
                FileUtils.deleteDirectory(new File(output_dir));
                if (!new File(output_dir).exists()) {
                    new File(output_dir).mkdirs();
                }
            } catch (Exception e) {
                logger.error(ANSI_RED + "[error] > Exception : " + e.getMessage() + ANSI_RESET);
            }
        }

        // clone repository
        if (!mode.equals("defects4j") && !gitFunctions.clone(git_url, output_dir)) {
            logger.error(ANSI_RED + "[fatal] > Failed to clone " + git_url + ANSI_RESET);
            return;
        }
        // logger.info(ANSI_GREEN + "[info] > Successfully cloned " + git_url +
        // ANSI_RESET);

        String repo_git = output_dir + "/" + git_name;

        logger.trace(ANSI_YELLOW + "[info] > executing ChangeCollector for mode : " + mode + ANSI_RESET);
        // MODE 1 : collect all change vectors from a repository
        if (mode.equals("repository")) {
            // STEP 1 : extract all source code differences between commits within a
            // repository
            ArrayList<String[]> all_diffs = gitFunctions.extract_diff(repo_git);
            if (all_diffs == null || all_diffs.size() == 0) {
                logger.error(ANSI_RED + "[fatal] > Failed to extract diffs" + ANSI_RESET);
                return;
            }
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(new File(output_dir, "diff.txt")));
                for (String[] diff : all_diffs) {
                    for (String line : diff) {
                        writer.write(line);
                        writer.write(" ");
                    }
                    writer.newLine();
                }
                writer.close();
            } catch (Exception e) {
                logger.error(ANSI_RED + "[fatal] > Exception : " + e.getMessage() + ANSI_RESET);
                return;
            }
            logger.info(ANSI_GREEN + "[info] > Successfully extracted diffs" + ANSI_RESET);
            // STEP 2 : extract commit ids and file names from all diffs and write them to a
            // file
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(
                        new File(output_dir, GitFunctions.get_repo_name_from_url(git_url) + "_commit_file.csv")));
                for (String[] diff : all_diffs) {
                    for (String line : diff) {
                        writer.write(line + ",");
                    }
                    writer.write(git_url);
                    writer.newLine();
                }
                writer.close();
            } catch (Exception e) {
                logger.error(ANSI_RED + "[fatal] > Exception : " + e.getMessage() + ANSI_RESET);
                return;
            }
            logger.info(ANSI_GREEN + "[info] > Successfully created " + GitFunctions.get_repo_name_from_url(git_url)
                    + "_commit_file.csv" + ANSI_RESET);

            String diff_path = output_dir + "/diff.txt";

            if (!extractor.extract_log(repo_git, diff_path, output_dir)) {
                logger.error(ANSI_RED + "[fatal] > Failed to extract log" + ANSI_RESET);
                return;
            }
            logger.info(ANSI_GREEN + "[info] > Successfully extracted log" + ANSI_RESET);

            String gumtree_log = output_dir + "/gumtree_log.txt";
            // STEP 3 : extract change vectors from all diffs and write them to a file
            int cv_extraction_result = extractor.extract_vector(git_name, gumtree_log, output_dir, true);
            if (cv_extraction_result == -1) {
                logger.error(ANSI_RED + "[fatal] > Failed to extract change vector" + ANSI_RESET);
                return;
            } else if (cv_extraction_result == 1) {
                logger.error(ANSI_RED + "[fatal] > Failed to extract change vector due to no change" + ANSI_RESET);
                return;
            }
            logger.info(ANSI_GREEN + "[info] > Successfully extracted change vector" + ANSI_RESET);
        }
        // MODE 2 : collect change vector between current commit and before of a single
        // source code file
        else if (mode.equals("file")) {

            // STEP 1 : extract source code differences between current commit and before

            String[] diff = gitFunctions.extract_diff(repo_git, file_name, commit_id);
            if (diff == null) {
                logger.error(ANSI_RED + "[fatal] > Failed to extract diff" + ANSI_RESET);
                return;
            }
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(new File(output_dir, "diff.txt")));
                for (String line : diff) {
                    writer.write(line);
                    writer.write(" ");
                }
                writer.newLine();
                writer.close();
            } catch (Exception e) {
                logger.error(ANSI_RED + "[fatal] > Exception : " + e.getMessage() + ANSI_RESET);
                return;
            }
            logger.info(ANSI_GREEN + "[info] > Extracted diff successfully" + ANSI_RESET);

            // STEP 2 : extract commit ids and file names and write them to a file
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(
                        new File(output_dir, GitFunctions.get_repo_name_from_url(git_url) + "_commit_file.csv")));
                for (String line : diff) {
                    writer.write(line + ",");
                }
                writer.write(git_url);
                writer.newLine();
                writer.close();
            } catch (Exception e) {
                logger.error(ANSI_RED + "[fatal] > Exception : " + e.getMessage() + ANSI_RESET);
                return;
            }
            logger.info(ANSI_GREEN + "[info] > Successfully created " + GitFunctions.get_repo_name_from_url(git_url)
                    + "_commit_file.csv for single file " + file_name + ANSI_RESET);

            // STEP 3 : extract change vector from diff and write it to a file

            String diff_path = output_dir + "/diff.txt";
            if (!extractor.extract_log(repo_git, diff_path, output_dir)) {
                logger.error(ANSI_RED + "[fatal] > Failed to extract gumtree log" + ANSI_RESET);
                return;
            }
            logger.info(ANSI_GREEN + "[info] > Successfully extracted gumtree log" + ANSI_RESET);

            String gumtree_log = output_dir + "/gumtree_log.txt";
            int cv_extraction_result = extractor.extract_vector(git_name, gumtree_log, output_dir);
            if (cv_extraction_result == -1) {
                logger.error(ANSI_RED + "[fatal] > Failed to extract change vector due to exception" + ANSI_RESET);
                return;
            } else if (cv_extraction_result == 1) {
                logger.error(ANSI_RED + "[fatal] > Failed to extract change vector due to no change" + ANSI_RESET);
                return;
            }
            logger.info(ANSI_GREEN + "[info] > Successfully extracted change vector" + ANSI_RESET);
        }
        // MODE 3 : collect change vector between current commit and before from a
        // Defects4J bug
        else if (mode.equals("defects4j")) {
            String[] cid_set = new String[2]; // [0] old cid [1] new cid

            // STEP 1 : extract Defects4J commit ids and file names from given Defects4J
            // name and identifier

            if (!implemental.config(project_root, defects4j_name, Integer.parseInt(defects4j_id), output_dir,
                    java_home_8, hash_id)) {
                logger.error(ANSI_RED + "[fatal] > Failed to configure defects4j" + ANSI_RESET);
                return;
            }
            if (!implemental.preprocess()) {
                logger.error(ANSI_RED + "[fatal] > Failed to preprocess defects4j" + ANSI_RESET);
                return;
            }
            if (!implemental.fetch()) {
                logger.error(ANSI_RED + "[fatal] > Failed to fetch defects4j" + ANSI_RESET);
                return;
            }
            if (!implemental.parse()) {
                logger.error(ANSI_RED + "[fatal] > Failed to parse defects4j" + ANSI_RESET);
                return;
            }
            if (implemental.d4j_ready) {
                cid_set = gitFunctions.blame(implemental.faultyProject, implemental.faultyPath,
                        implemental.faultyLineBlame,
                        implemental.faultyLineFix);
            }
            if (!implemental.cid_config(cid_set[0], cid_set[1])) {
                logger.error(ANSI_RED + "[fatal] > Failed to configure commit ids" + ANSI_RESET);
                return;
            }

            // STEP 2 : extract git diff from given Defects4J bug informations
            String[] diff = gitFunctions.extract_diff(implemental.faultyProject, implemental.faultyPath, cid_set[1],
                    cid_set[0]);
            if (diff == null) {
                logger.error(ANSI_RED + "[fatal] > Failed to extract diff" + ANSI_RESET);
                return;
            }
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(new File(output_dir, "diff.txt")));
                for (String line : diff) {
                    writer.write(line);
                    writer.write(" ");
                }
                writer.newLine();
                writer.close();
            } catch (Exception e) {
                logger.error(ANSI_RED + "[fatal] > Exception : " + e.getMessage() + ANSI_RESET);
                return;
            }
            logger.info(ANSI_GREEN + "[info] > Extracted diff successfully" + ANSI_RESET);
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(
                        new File(output_dir, defects4j_name + "_commit_file.csv")));
                for (String line : diff) {
                    writer.write(line + ",");
                }
                writer.write(implemental.faultyProject);
                writer.newLine();
                writer.close();
            } catch (Exception e) {
                logger.error(ANSI_RED + "[fatal] > Exception : " + e.getMessage() + ANSI_RESET);
                return;
            }
            logger.info(ANSI_GREEN + "[info] > Successfully created " + defects4j_name
                    + "_commit_file.csv for single file " + file_name + ANSI_RESET);

            // STEP 3 : extract change vector from diff and write it to a file
            String diff_path = output_dir + "/diff.txt";
            if (!extractor.extract_log(implemental.faultyProject, diff_path, output_dir)) {
                logger.error(ANSI_RED + "[fatal] > Failed to extract gumtree log" + ANSI_RESET);
                return;
            }
            logger.info(ANSI_GREEN + "[info] > Successfully extracted gumtree log" + ANSI_RESET);

            String gumtree_log = output_dir + "/gumtree_log.txt";
            int cv_extraction_result = extractor.extract_vector(defects4j_name, gumtree_log, output_dir);
            if (cv_extraction_result == -1) {
                logger.error(ANSI_RED + "[fatal] > Failed to extract change vector due to exception" + ANSI_RESET);
                return;
            } else if (cv_extraction_result == 1) {
                logger.error(ANSI_RED + "[fatal] > Failed to extract change vector due to no change" + ANSI_RESET);
                return;
            }
            logger.info(ANSI_GREEN + "[info] > Successfully extracted change vector" + ANSI_RESET);

        } else {
            logger.error(ANSI_RED + "[fatal] > Invalid mode" + ANSI_RESET);
            return;
        }
        System.exit(0);
    }

    public Properties loadProperties() {
        return loadProperties("../cc.properties");
    }

    public Properties loadProperties(String path) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(path));
        } catch (Exception e) {
            logger.error(ANSI_RED + "[error] > Exception : " + e.getMessage());
            return null;
        }
        return properties;
    }
}
