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
    private boolean set; // if result dir exist

    // d4j
    private String d4j_project_name; // d4j project name
    private int d4j_project_num; // d4j project num

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
        this.set = false;
        this.d4j_project_name = "";
        this.d4j_project_num = -1;
    }

    public void config(String url, String cid_before, String cid_after, String filepath_before, String filepath_after,
            String d4j_name, int d4j_num) {
        this.url = url;
        this.name = get_repo_name_from_url(url);
        this.cid_before = cid_before;
        this.cid_after = cid_after;
        this.filepath_before = filepath_before;
        this.filepath_after = filepath_after;
        this.filename = get_file_name_from_path(filepath_before);
        this.d4j_project_name = d4j_name;
        this.d4j_project_num = d4j_num;
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
        System.out.println(ANSI_BLUE + "[info] > url : " + ANSI_RESET + url);
        System.out.println(ANSI_BLUE + "[info] > repo_name : " + ANSI_RESET + name);
        System.out.println(ANSI_BLUE + "[info] > cid_before : " + ANSI_RESET + cid_before);
        System.out.println(ANSI_BLUE + "[info] > cid_after : " + ANSI_RESET + cid_after);
        System.out.println(ANSI_BLUE + "[info] > file_name : " + ANSI_RESET + filename);
        System.out.println(ANSI_BLUE + "[info] > result_dir : " + ANSI_RESET + result_dir);
        System.out.println(ANSI_BLUE + "[info] > candidate_dir : " + ANSI_RESET + candidate_dir);
    }

    private boolean clone(String directory) {
        try {
            System.out.println(ANSI_BLUE + "[status] > cloning start");
            ProcessBuilder pb = new ProcessBuilder();
            pb.directory(new File(result_dir));
            pb.command("git", "clone", url, directory);
            Process p = pb.start();
            p.waitFor();
            System.out.println(ANSI_GREEN + "[status] > cloning done");
            return true;
        } catch (Exception e) {
            System.out.println(ANSI_RED + "[error] > " + e.getMessage());
            return false;
        }
    }

    private boolean checkout(String directory) {
        try {
            String project = d4j_project_name + "-" + d4j_project_num;
            System.out.println(ANSI_BLUE + "[status] > git checkout cid before : " + cid_before);
            ProcessBuilder pb = new ProcessBuilder();
            pb.directory(new File(directory));
            pb.command("git", "checkout", "-f", cid_before);
            Process p = pb.start();
            p.waitFor();
            System.out.println(ANSI_GREEN + "[status] > git checkout success");
            if (!!copy(result_dir + "/" + name + "_" + counter + "/" + filepath_before,
                    candidate_dir + "/" + project + "rank_" + counter + "old.java"))
                System.out.println(ANSI_GREEN + "[status] > copy success");
            else {
                System.out.println(ANSI_RED + "[error] > copy failed");
                return false;
            }

            System.out.println(ANSI_BLUE + "[status] > git checkout cid after : " + cid_after);
            pb = new ProcessBuilder();
            pb.directory(new File(directory));
            pb.command("git", "checkout", "-f", cid_after);
            p = pb.start();
            p.waitFor();
            System.out.println(ANSI_GREEN + "[status] > git checkout success");
            if (!!copy(result_dir + "/" + name + "_" + counter + "/" + filepath_after,
                    candidate_dir + "/" + project + "rank_" + counter + "_new.java"))
                System.out.println(ANSI_GREEN + "[status] > copy success");
            else {
                System.out.println(ANSI_RED + "[error] > copy failed");
                return false;
            }
            return true;
        } catch (Exception e) {
            System.out.println(ANSI_RED + "[error] > " + e.getMessage());
            return false;
        }
    }

    public boolean load() {
        try {
            if (set) {
                System.out.println(ANSI_BLUE + "[status] > loading");
                if (!clone(result_dir + "/" + name + "_" + counter))
                    return false;
                if (!checkout(result_dir + "/" + name + "_" + counter))
                    return false;
                System.out.println(ANSI_GREEN + "[status] > loading done");
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
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
            System.out.println(ANSI_RED + "[error] > IOException : " + e.getMessage());
            return false;
        }
    }

    public void purge() {
        try {
            File dir = new File(result_dir);
            File dir2 = new File(candidate_dir);
            if (!dir.exists())
                dir.mkdir();
            if (!dir2.exists())
                dir2.mkdir();
            FileUtils.cleanDirectory(dir);
            FileUtils.cleanDirectory(dir2);
        } catch (IOException e) {
            System.out.println(ANSI_RED + "[error] > IOException : " + e.getMessage());
        }
    }
}
