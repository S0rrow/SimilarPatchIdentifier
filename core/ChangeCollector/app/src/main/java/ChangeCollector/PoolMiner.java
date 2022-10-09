package ChangeCollector;

import java.io.*;
import java.util.*;


import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;

import ChangeCollector.Extractor;
import ChangeCollector.GitFunctions;

public class PoolMiner{
    public String commit_file_path; // path to the file containing the commit ids
    public String target; // directory path to clone git url repository
    public String hash_id; // hash id of the current execution
    public String result_file_path; // path to the file containing the result

    public static Logger pmLogger = LogManager.getLogger(PoolMiner.class.getName()); // log4j2 logger

    public PoolMiner(String commit_file_path, String hash_id, String target, String result_file_path){
        super();
        this.commit_file_path = commit_file_path;
        this.hash_id = hash_id;
        this.target = target;
        this.result_file_path = result_file_path;
    }

    public boolean run(){
        pmLogger.debug(App.ANSI_PURPLE + "[debug] > PoolMiner.run() called" + App.ANSI_RESET);
        Extractor extractor = new Extractor();
        GitFunctions gitFunctions = new GitFunctions();
        try{
            BufferedReader reader = new BufferedReader(new FileReader(this.commit_file_path));
            
            String line = reader.readLine();
            while(line != null) {
                String[] elements = line.split(",");
                String bic = elements[0];
                String bfc = elements[1];
                String bic_path = elements[2];
                String bfc_path = elements[3];
                String git_url = elements[4];
                String jira_key = elements[5];

                String repo_name = GitFunctions.get_repo_name_from_url(git_url);
                String workspace_dir = this.target + "/" + hash_id;
                String repo_dir = workspace_dir + "/" + repo_name;
                File repo = new File(repo_dir);
                String[] diff = gitFunctions.extract_diff(repo_dir, bfc_path, bfc, bic);
                if(diff == null){
                    pmLogger.error(App.ANSI_RED + "[error] > diff is null" + App.ANSI_RESET);
                    return false;
                }
                String diff_file_path = repo_dir + "/diff.txt";
                String gumtree_log = repo_dir + "/gumtree_log.txt";

                BufferedWriter diff_writer = new BufferedWriter(new FileWriter(diff_file_path));

                for (String diff_line : diff) {
                    diff_writer.write(diff_line);
                    diff_writer.write(" ");
                }
                diff_writer.newLine();
                diff_writer.close();

                if(!extractor.extract_log(repo_dir, bfc_path, gumtree_log)) {
                    pmLogger.error(App.ANSI_RED + "[error] > extractor.extract_log() failed" + App.ANSI_RESET);
                    return false;
                }

                
            }
            reader.close();
            pmLogger.debug(App.ANSI_PURPLE + "[debug] > PoolMiner.run() finished" + App.ANSI_RESET);
        } catch(Exception e) {
            pmLogger.error(App.ANSI_RED + "[error] > Exception : " + e + App.ANSI_RESET);
            return false;
        }
        return true;
    }
}