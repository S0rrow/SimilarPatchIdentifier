package LCE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.JGitInternalException;

import org.apache.commons.io.FileUtils;

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

    private int counter = -1;
    private String url; // git url
    private String name; // git repo name
    private String cid; // commit id
    private String filepath; // file path
    private String filename; // file name
    private String result_dir; // result dir
    private String candidate_dir; // candidate dir
    private boolean set = false; // if result dir exist

    public GitLoader() {
        this.url = "";
        this.name = "";
        this.cid = "";
        this.filepath = "";
    }

    public void config(String url, String cid, String filepath) {
        this.url = url;
        this.name = get_repo_name_from_url(url);
        this.cid = cid;
        this.filepath = filepath;
        this.filename = get_file_name_from_path(filepath);
    }

    public void set(String path, String candidate_dir) {
        this.result_dir = path;
        this.candidate_dir = candidate_dir;
        if (result_dir != null && candidate_dir != null)
            set = true;
    }

    public void run() {
        System.out.println(ANSI_YELLOW + "==========================================================");
        System.out.println(ANSI_BLUE + "[info #" + counter + "] > git clone " + url);
        print_debug_info();
        // TODO
    }

    public void count(int counter) {
        this.counter = counter;
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

    private String get_file_name_from_path(String path) {
        String[] path_split = path.split("/");
        return path_split[path_split.length - 1];
    }

    private void print_debug_info() {
        System.out.println(ANSI_BLUE + "[debug] > url : " + ANSI_RESET + url);
        System.out.println(ANSI_BLUE + "[debug] > repo_name : " + ANSI_RESET + name);
        System.out.println(ANSI_BLUE + "[debug] > cid : " + ANSI_RESET + cid);
        System.out.println(ANSI_BLUE + "[debug] > file_name : " + ANSI_RESET + filename);
        System.out.println(ANSI_BLUE + "[debug] > result_dir : " + ANSI_RESET + result_dir);
        System.out.println(ANSI_BLUE + "[debug] > candidate_dir : " + ANSI_RESET + candidate_dir);
    }

    private boolean clone() {
        try{
            System.out.print(ANSI_BLUE + "[debug] > cloning start");
            ProcessBuilder pb = new ProcessBuilder();
            System.out.print(".");
            pb.directory(new File(result_dir));
            System.out.print(".");
            pb.command("git", "clone", url);
            System.out.print(".");
            Process p = pb.start();
            System.out.println(".");
            System.out.println(ANSI_GREEN + "[debug] > cloning done");
            return true;
        } catch(Exception e){
            System.out.println(ANSI_RED + "[error] > " + e.getMessage());
            return false;
        }
    }

    private boolean checkout(String directory){
        try{
            System.out.print(ANSI_BLUE + "[debug] > git checkout : " + cid);
            ProcessBuilder pb = new ProcessBuilder();
            System.out.print(".");
            pb.directory(new File(directory));
            System.out.print(".");
            pb.command("git", "checkout", cid);
            System.out.print(".");
            Process p = pb.start();
            System.out.println(".");
            System.out.println(ANSI_GREEN + "[debug] > git checkout success");
            return true;
        } catch(Exception e){
            System.out.println(ANSI_RED + "[error] > " + e.getMessage());
            return false;
        }
    }

    public boolean load() {
        if (set) {
            if(!clone())
                return false;
            if(!checkout(result_dir + "/" + name))
                return false;
            return true;
        } else {
            return false;
        }
    }

    public void copy(String path1, String path2) {
        File file = new File(path1);
        File file2 = new File(path2);
        try {
            FileInputStream input = new FileInputStream(file);
            FileOutputStream output = new FileOutputStream(file2);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            input.close();
            output.close();
        } catch (IOException e) {
            System.out.println("[debug] > IOException : " + e.getMessage());
        }
    }

    public void purge() {
        try {
            File dir = new File(result_dir);
            File dir2 = new File(candidate_dir);
            FileUtils.cleanDirectory(dir);
            FileUtils.cleanDirectory(dir2);
        } catch (IOException e) {
            System.out.println("[debug] > IOException : " + e.getMessage());
        }
    }
}
