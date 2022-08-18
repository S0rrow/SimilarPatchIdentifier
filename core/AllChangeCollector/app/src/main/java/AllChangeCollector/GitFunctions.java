package AllChangeCollector;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

public class GitFunctions {
    public String name;

    public boolean clone(String url, String path) {
        String repo_name = get_repo_name_from_url(url);
        App.logger.info("> cloning " + App.ANSI_BLUE + url + App.ANSI_RESET + " to " + App.ANSI_BLUE + path
                + App.ANSI_RESET + " as " + App.ANSI_YELLOW + repo_name + App.ANSI_RESET);
        try {
            if (new File(path + "/" + repo_name).exists())
                return true;
            Git.cloneRepository()
                    .setURI(url)
                    .setDirectory(new java.io.File(path + "/" + repo_name))
                    .setProgressMonitor(new TextProgressMonitor())
                    .call();
            return true;
        } catch (Exception e) {
            App.logger.error(App.ANSI_RED + "[error] > Exception : " + e.getMessage());
            return false;
        }
    }

    private ArrayList<String> get_commit_hash(String repo_path, String file_name) {
        ArrayList<String> hashes = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.directory(new File(repo_path));
            pb.command("git", "log", "--pretty=format:%H", file_name);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                hashes.add(line);
            }
            process.waitFor();
            return hashes;
        } catch (Exception e) {
            App.logger.error(App.ANSI_RED + "[error] > Exception : " + e.getMessage());
            return null;
        }
    }

    public String[] extract_diff(String repo_git, String repo_name, String file_name, String new_cid) {
        App.logger.trace(App.ANSI_BLUE + "[status] > extracting diff from " + repo_name + App.ANSI_RESET + " to "
                + App.ANSI_BLUE + file_name + App.ANSI_RESET + " with " + App.ANSI_BLUE + new_cid + App.ANSI_RESET);
        String old_cid = "";
        boolean found = false;
        try {
            ArrayList<String> commit_hashes = get_commit_hash(repo_git, file_name);
            if (commit_hashes == null) {
                App.logger.error(App.ANSI_RED + "[error] > Failed to get commit hashes" + App.ANSI_RESET);
                return null;
            }
            for (String cid : commit_hashes) {
                if (found) {
                    old_cid = cid;
                    break;
                }
                if (cid.equals(new_cid)) {
                    found = true;
                }
            }
            if (!found) {
                App.logger.error(App.ANSI_RED + "[error] > Failed to find commit " + new_cid + App.ANSI_RESET);
                return null;
            }
        } catch (Exception e) {
            App.logger.error(App.ANSI_RED + e.getMessage() + App.ANSI_RESET);
            return null;
        }
        return extract_diff(repo_git, repo_name, file_name, new_cid, old_cid);
    }

    public String[] extract_diff(String repo_git, String repo_name, String file_name, String new_cid, String old_cid) {
        App.logger.trace(App.ANSI_BLUE + "[status] > extracting diff from " + App.ANSI_BLUE + repo_name + App.ANSI_RESET
                + " between "
                + App.ANSI_BLUE + old_cid + App.ANSI_RESET + " and " + App.ANSI_BLUE + new_cid + App.ANSI_RESET);
        String[] result = new String[4];
        try {
            Repository repo = new FileRepository(repo_git);
            App.logger.info(App.ANSI_YELLOW + "[status] > repo " + repo_name + App.ANSI_RESET + " is "
                    + App.ANSI_YELLOW + repo.getDirectory().getAbsolutePath() + App.ANSI_RESET);
            ObjectId oldHead = repo.resolve(old_cid + "^{tree}");
            ObjectId newHead = repo.resolve(new_cid + "^{tree}");
            if (oldHead == null || newHead == null) {
                App.logger.error(App.ANSI_RED + "[error] > oldHead or newHead is null" + App.ANSI_RESET);
                repo.close();
                return null;
            }
            ObjectReader reader = repo.newObjectReader();
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(reader, oldHead);
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, newHead);
            Git git = new Git(repo);
            List<DiffEntry> diffs = git.diff().setNewTree(newTreeIter).setOldTree(oldTreeIter).call();
            for (DiffEntry entry : diffs) {
                String str_new = entry.getNewPath();
                String str_old = entry.getOldPath();
                if (str_new.endsWith(".java") && str_old.endsWith(".java")) {
                    if (file_name.equals("")) {
                        result[0] = new_cid;
                        result[1] = old_cid;
                        result[2] = str_new;
                        result[3] = str_old;
                    } else {
                        if (str_new.contains(file_name)) {
                            result[0] = new_cid;
                            result[1] = old_cid;
                            result[2] = str_new;
                            result[3] = str_old;
                        }
                    }
                }
            }
            git.close();
            repo.close();
        } catch (Exception e) {
            App.logger.error(App.ANSI_RED + "[error] > Exception : " + e.getMessage() + App.ANSI_RESET);
            return null;
        }
        return result;
    }

    // use globally for extracting repo name from url
    public static String get_repo_name_from_url(String url) {
        String[] url_split = url.split("/");
        for (String split : url_split) {
            if (split.contains(".git")) {
                return split.replace(".git", "");
            }
        }
        return url_split[url_split.length - 1];
    }
}
