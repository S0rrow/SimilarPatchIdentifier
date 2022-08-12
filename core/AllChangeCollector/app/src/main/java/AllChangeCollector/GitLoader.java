package AllChangeCollector;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.TextProgressMonitor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.*;
import org.apache.logging.log4j.*;

public class GitLoader {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public String name;
    public Logger gitLogger = LogManager.getLogger(GitLoader.class);

    public boolean clone(String url, String path) {
        gitLogger.info("> cloning " + url + " to " + path);
        try {
            name = get_repo_name_from_url(url);
            Git.cloneRepository()
                    .setURI(url)
                    .setDirectory(new java.io.File(path))
                    .setProgressMonitor(new TextProgressMonitor())
                    .call();
            return true;
        } catch (Exception e) {
            System.out.println(ANSI_RED + "[error] > Exception : " + e.getMessage());
            return false;
        }
    }

    public boolean crawl_commit_id(String repo_name, String path) {
        ProcessBuilder pb = new ProcessBuilder("git", "log", "--pretty=format:\"%H\"");
        try {
            pb.directory(new java.io.File(path));
            Process p = pb.start();
            if (!saveResult(p, path)) {
                return false;
            }
            p.waitFor();
            return true;
        } catch (Exception e) {
            gitLogger.error("> Exception : " + e.getMessage());
            return false;
        }
    }

    public boolean get_all_commits(ArrayList<String> repo_list, ArrayList<String> repo_name, String path) {
        try {
            for (int i = 0; i < repo_list.size(); i++) {
                Repository repo = new FileRepository(name);
                Collection<Ref> allRefs = repo.getRefDatabase().getRefs();
                
            }
            return true;
        } catch (Exception e) {
            gitLogger.error("> Exception : " + e.getMessage());
            return false;
        }
    }

    private boolean printResult(Process process) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
            return true;
        } catch (Exception e) {
            gitLogger.error("> Exception : " + e.getMessage());
            return false;
        }
    }

    private boolean saveResult(Process process, String path) {
        try {
            File file = new File(path, "commitID.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            String line = "";
            while ((line = reader.readLine()) != null) {
                line = line.substring(1, line.length() - 1);
                writer.write(line + "\n");
            }
            writer.close();
            reader.close();
            return true;
        } catch (Exception e) {
            gitLogger.error("> Exception : " + e.getMessage());
            return false;
        }
    }

    private String get_repo_name_from_url(String url) {
        String[] url_split = url.split("/");
        for (String split : url_split) {
            if (split.contains(".git")) {
                return split.replace(".git", "");
            }
        }
        return url_split[url_split.length - 1];
    }
}
