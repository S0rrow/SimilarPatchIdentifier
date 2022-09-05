/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package AllChangeCollector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.*;
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
        String file_name = properties.getProperty("file_name");
        String commit_id = properties.getProperty("commit_id");
        String git_name = properties.getProperty("git_name");
        String git_url = properties.getProperty("git_url");
        String output_dir = properties.getProperty("output_dir");
        boolean doClean = properties.getProperty("doClean").equals("true");
        boolean is_d4j = properties.getProperty("is_d4j").equals("true");

        boolean acc = file_name.equals("") || commit_id.equals("");

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

        if (!is_d4j && !gitFunctions.clone(git_url, output_dir)) {
            logger.error(ANSI_RED + "[fatal] > Failed to clone " + git_url + ANSI_RESET);
            return;
        }
        logger.info(ANSI_GREEN + "[info] > Successfully cloned " + git_url + ANSI_RESET);

        String repo_git = output_dir + "/" + git_name;

        logger.info(ANSI_PURPLE + "[info] > AllChangeCollection : " + acc + ANSI_RESET);
        if (acc) {
            logger.trace(ANSI_YELLOW + "[info] > extracting all diffs" + ANSI_RESET);
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
            int cv_extraction_result = extractor.extract_vector(git_name, gumtree_log, output_dir, true); // overrided
                                                                                                          // method for
                                                                                                          // all diffs
            if (cv_extraction_result == -1) {
                logger.error(ANSI_RED + "[fatal] > Failed to extract change vector" + ANSI_RESET);
                return;
            } else if (cv_extraction_result == 1) {
                logger.error(ANSI_RED + "[fatal] > Failed to extract change vector due to no change" + ANSI_RESET);
                return;
            }
            logger.info(ANSI_GREEN + "[info] > Successfully extracted change vector" + ANSI_RESET);
        } else {
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
        System.exit(0);
    }

    public Properties loadProperties() {
        return loadProperties("../acc.properties");
    }

    public Properties loadProperties(String path) {
        // Format : <file_name(java)> <commit> <git_name> <git_url>
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