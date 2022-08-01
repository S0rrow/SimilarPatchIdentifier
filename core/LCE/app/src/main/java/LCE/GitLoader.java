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
        System.out.println("\n[info #" + counter + "] > git clone " + url);
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
        System.out.println("[debug] > url : " + url);
        System.out.println("[debug] > repo_name : " + name);
        System.out.println("[debug] > cid : " + cid);
        System.out.println("[debug] > file_name : " + filename);
        System.out.println("[debug] > result_dir : " + result_dir);
        System.out.println("[debug] > candidate_dir : " + candidate_dir);
    }

    public void clone_file() throws InvalidRemoteException, TransportException, GitAPIException, JGitInternalException {
        if (set) {
            System.out.println("[debug] > cloning repository from " + url);
            Git git = Git.cloneRepository().setURI(url).setDirectory(new File(result_dir + "/" + name + "_" + counter))
                    .call();
            git.reset().setMode(ResetType.HARD).setRef("HEAD").call();
            System.out.println("[debug] > cloning done");
            System.out.println("[debug] > checkout repository :" + cid);
            git.checkout().setForce(true).setName(cid).call();
            System.out.println("[debug] > checkout done");
            System.out.println("[debug] > copy file : " + filepath + " as candidate_no_" + counter + ".java" + " to "
                    + candidate_dir);
            copy(result_dir + "/" + name + "_" + counter + "/" + filepath,
                    candidate_dir + "/" + "candidate_no_" + counter + ".java");
            System.out.println("[debug] > copy done");
        } else {
            System.out.println("[debug] > error : result_dir or candidate_dir is not set");
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
