package ChangeCollector;

import java.io.*;
import java.util.*;

import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;

import ChangeCollector.Extractor;
import ChangeCollector.GitFunctions;

public class PoolMiner {
    public String commit_file_path; // path to the file containing the commit ids
    public String target; // directory path to clone git url repository
    public String hash_id; // hash id of the current execution
    public String result_file_path; // path to the file containing the result
    public String extracted_commit_file_path;

    public static Logger pmLogger = LogManager.getLogger(PoolMiner.class.getName()); // log4j2 logger

    public PoolMiner(String commit_file_path, String hash_id, String target, String result_file_path) {
        super();
        this.commit_file_path = commit_file_path;
        this.hash_id = hash_id;
        this.target = target;
        this.result_file_path = result_file_path;
        extracted_commit_file_path = target + "/commit_file.csv";
    }

    public boolean run() {
        pmLogger.debug(App.ANSI_PURPLE + "[debug] > PoolMiner.run() called" + App.ANSI_RESET);
        Extractor extractor = new Extractor();
        GitFunctions gitFunctions = new GitFunctions();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(commit_file_path));
            BufferedWriter gumtree_writer = new BufferedWriter(new FileWriter(result_file_path, true));
            BufferedWriter commit_writer = new BufferedWriter(new FileWriter(extracted_commit_file_path, true));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] elements = line.split(",");
                String bic = elements[0];
                String bfc = elements[1];
                String bic_path = elements[2];
                String bfc_path = elements[3];
                int lineBlame = Integer.parseInt(elements[4]);
                int lineFix = Integer.parseInt(elements[5]);
                String git_url = elements[6];
                String jira_key = elements[7];

                String repo_name = GitFunctions.get_repo_name_from_url(git_url);
                String workspace_dir = this.target + "/" + hash_id;
                String repo_dir = workspace_dir + "/" + repo_name;
                File repo = new File(repo_dir);

                if (!repo.exists()) {
                    gitFunctions.clone(git_url, workspace_dir);
                }

                String file_name = bfc_path.split("/")[bfc_path.split("/").length - 1];

                String bbic = gitFunctions.blame(repo_dir, bic_path, lineBlame, lineBlame, bic);

                if (bbic == null) {
                    ArrayList<String> hash_list = gitFunctions.log(repo_dir, bic_path);
                    // check if the bic is the first commit
                    if (hash_list.size() == 1) {
                        pmLogger.error(App.ANSI_RED + "[error] > The bic is the first commit" + App.ANSI_RESET);
                        continue;
                    } else if (hash_list.size() == 0) {
                        pmLogger.error(App.ANSI_RED + "[error] > The bic is not in the repository" + App.ANSI_RESET);
                        continue;
                    }
                }

                String[] diff = gitFunctions.extract_diff(repo_dir, bic_path, bic, bbic);

                if (diff == null || diff.length == 0) {
                    pmLogger.error(App.ANSI_RED + "[error] > diff is null or empty" + App.ANSI_RESET);
                    continue;
                }

                pmLogger.info(App.ANSI_BLUE + "[info] > diff extracted successfully" + App.ANSI_RESET);

                String diff_file_path = repo_dir + "/diff.txt";

                BufferedWriter diff_writer = new BufferedWriter(new FileWriter(diff_file_path));

                for (String diff_line : diff) {
                    diff_writer.write(diff_line);
                    commit_writer.write(diff_line);
                    diff_writer.write(" ");
                    commit_writer.write(",");
                }
                diff_writer.newLine();
                diff_writer.close();

                pmLogger.info(App.ANSI_BLUE + "[info] > diff file generated successfully");// diff file written
                                                                                           // successfully

                if (!extractor.extract_gumtree_log(repo_dir, bic, bbic, bic_path, bic_path, workspace_dir)) {
                    pmLogger.error(App.ANSI_RED + "[error] > extractor failed to extract gumtree log" + App.ANSI_RESET);
                    return false;
                }

                pmLogger.info(App.ANSI_BLUE + "[info] > gumtree log generated successfully" + App.ANSI_RESET);

                String gumtree_log = workspace_dir + "/gumtree_log.txt";

                int cv_extraction_result = extractor.extract_vector_pool(gumtree_log, result_file_path);
                if (cv_extraction_result == -1) {
                    pmLogger.error(App.ANSI_RED + "[error] > extractor failed to extract cv" + App.ANSI_RESET);
                    continue;
                } else if (cv_extraction_result == 1) {
                    pmLogger.error(App.ANSI_RED + "[error] > cv is empty" + App.ANSI_RESET);
                    continue;
                }
                pmLogger.info(App.ANSI_GREEN + "[info] > cv generated successfully" + App.ANSI_RESET);

            }
            reader.close();
            gumtree_writer.close();
            commit_writer.close();

            pmLogger.debug(App.ANSI_PURPLE + "[debug] > PoolMiner.run() finished" + App.ANSI_RESET);
        } catch (Exception e) {
            pmLogger.error(App.ANSI_RED + "[error] > Exception : " + e + App.ANSI_RESET);
            return false;
        }
        return true;
    }
}