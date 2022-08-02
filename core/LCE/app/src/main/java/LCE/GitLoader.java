package LCE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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
    private String cid_before; // commit id before
    private String cid_after; // commit id after
    private String filepath_before; // file path before
    private String filepath_after; // file path (after)
    private String filename; // file name
    private String result_dir; // result dir
    private String candidate_dir; // candidate dir
    private boolean set = false; // if result dir exist

    public GitLoader() {
        this.url = "";
        this.name = "";
        this.cid_before = "";
        this.cid_after = "";
        this.filepath_before = "";
        this.filepath_after = "";
        this.filename = "";
        this.result_dir = "";
        this.candidate_dir = "";
    }

    public void config(String url, String cid_before, String cid_after, String filepath_before, String filepath_after) {
        this.url = url;
        this.name = get_repo_name_from_url(url);
        this.cid_before = cid_before;
        this.cid_after = cid_after;
        this.filepath_before = filepath_before;
        this.filepath_after = filepath_after;
        this.filename = get_file_name_from_path(filepath_before);
    }

    public void set(String path, String candidate_dir) {
        this.result_dir = path;
        this.candidate_dir = candidate_dir;
        if (result_dir != null && candidate_dir != null) {
            set = true;
        }
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
        System.out.println(ANSI_BLUE + "[debug] > cid_before : " + ANSI_RESET + cid_before);
        System.out.println(ANSI_BLUE + "[debug] > cid_after : " + ANSI_RESET + cid_after);
        System.out.println(ANSI_BLUE + "[debug] > file_name : " + ANSI_RESET + filename);
        System.out.println(ANSI_BLUE + "[debug] > result_dir : " + ANSI_RESET + result_dir);
        System.out.println(ANSI_BLUE + "[debug] > candidate_dir : " + ANSI_RESET + candidate_dir);
    }

    private boolean clone(String directory) {
        try{
            System.out.println(ANSI_BLUE + "[debug] > cloning start");
            ProcessBuilder pb = new ProcessBuilder();
            pb.directory(new File(result_dir));
            pb.command("git", "clone", url, directory);
            Process p = pb.start();
            p.waitFor();
            System.out.println(ANSI_GREEN + "[debug] > cloning done");
            return true;
        } catch(Exception e){
            System.out.println(ANSI_RED + "[error] > " + e.getMessage());
            return false;
        }
    }

    private boolean checkout(String directory){
        try{
            System.out.println(ANSI_BLUE + "[debug] > git checkout cid before : " + cid_before);
            ProcessBuilder pb = new ProcessBuilder();
            pb.directory(new File(directory));
            pb.command("git", "checkout", "-f", cid_before);
            Process p = pb.start();
            p.waitFor();
            System.out.println(ANSI_GREEN + "[debug] > git checkout success");
            if(!!copy(result_dir + "/" + name + "_" + counter +"/" + filepath_before, candidate_dir + "/" + "candidate_before_no_" + counter + ".java"))
                System.out.println(ANSI_GREEN + "[debug] > copy success");
            else {
                System.out.println(ANSI_RED + "[error] > copy failed");
                return false;
            }

            System.out.println(ANSI_BLUE + "[debug] > git checkout cid after : " + cid_after);
            pb = new ProcessBuilder();
            pb.directory(new File(directory));
            pb.command("git", "checkout", "-f", cid_after);
            p = pb.start();
            p.waitFor();
            System.out.println(ANSI_GREEN + "[debug] > git checkout success");
            if(!!copy(result_dir + "/" + name + "_" + counter +"/" + filepath_after, candidate_dir + "/" + "candidate_after_no_" + counter + ".java"))
                System.out.println(ANSI_GREEN + "[debug] > copy success");
            else {
                System.out.println(ANSI_RED + "[error] > copy failed");
                return false;
            }
            return true;
        } catch(Exception e){
            System.out.println(ANSI_RED + "\n[error] > " + e.getMessage());
            return false;
        }
    }

    public boolean load() {
        try{
            if (set) {
                System.out.print(ANSI_BLUE + "[debug] > loading");
                if(!clone(result_dir + "/" + name + "_" + counter))
                    return false;
                if(!checkout(result_dir + "/" + name + "_" + counter))
                    return false;
                System.out.println(ANSI_GREEN + "[debug] > loading done");
                return true;
            } else {
                return false;
            }
        } catch (Exception e){
            System.out.println(ANSI_RED + "[error] > " + e.getMessage());
            return false;
        }
    }

    public boolean copy(String path1, String path2) {
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
            return true;
        } catch (IOException e) {
            System.out.println("[debug] > IOException : " + e.getMessage());
            return false;
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
