package AllChangeCollector;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

public class GitFunctions {
    public String name;

    public boolean clone(String url, String path) {
        String repo_name = get_repo_name_from_url(url);
        App.logger.info(App.ANSI_BLUE + "[status] > cloning " + App.ANSI_YELLOW + url + App.ANSI_RESET + " to "
                + App.ANSI_YELLOW + path
                + App.ANSI_RESET + " as " + App.ANSI_YELLOW + repo_name + App.ANSI_RESET);
        try {
            if (new File(path + "/" + repo_name).exists()) {
                App.logger.info(App.ANSI_YELLOW + "[status] > " + repo_name + App.ANSI_RESET + " already exists");
                return true;
            }
            if (!new File(path).exists()) {
                new File(path).mkdir();
            }
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

    private ArrayList<String> log(String repo_path) {
        ArrayList<String> hashes = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.directory(new File(repo_path));
            pb.command("git", "log", "--pretty=format:%H");
            Process p = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                hashes.add(line);
            }
            p.waitFor();
            p.destroy();
        } catch (Exception e) {
            App.logger.error(App.ANSI_RED + "[error] > Exception : " + e.getMessage() + App.ANSI_RESET);
            return null;
        }
        return hashes;
    }

    private ArrayList<String> log(String repo_path, String file_name) {
        App.logger.trace(App.ANSI_BLUE + "[status] > getting log of " + App.ANSI_YELLOW + repo_path + App.ANSI_RESET
                + " with " + App.ANSI_YELLOW + file_name + App.ANSI_RESET);
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
            process.destroy();
        } catch (Exception e) {
            App.logger.error(App.ANSI_RED + "[error] > Exception : " + e.getMessage() + App.ANSI_RESET);
            return null;
        }
        return hashes;
    }

    private ArrayList<String> list_tree(String repo_git, String hash) {
        ArrayList<String> files = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.directory(new File(repo_git));
            pb.command("git", "ls-tree", "-r", "--name-only", hash);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                if (line.contains(".java")) {
                    // App.logger.debug(App.ANSI_PURPLE + "[debug] > line : " + line +
                    // App.ANSI_RESET);
                    files.add(line);
                }
            }
            process.waitFor();
        } catch (Exception e) {
            App.logger.error(App.ANSI_RED + "[error] > Exception : " + e.getMessage() + App.ANSI_RESET);
            return null;
        }
        return files;
    }

    public ArrayList<String[]> extract_diff(String repo_git) {
        ArrayList<String[]> results = new ArrayList<>();
        ArrayList<String> hashes = log(repo_git);
        for (String hash : hashes) {
            ArrayList<String> files = list_tree(repo_git, hash);
            for (String file_name : files) {
                String[] diff = extract_diff(repo_git, file_name, hash);
                if (diff != null) {
                    results.add(diff);
                }
            }
        }
        return results;
    }

    public String[] extract_diff(String repo_git, String file_name, String new_cid) {
        String repo_name = get_repo_name_from_url(repo_git);
        App.logger.trace(App.ANSI_BLUE + "[status] > extracting diff from " + repo_name + App.ANSI_RESET + " to "
                + App.ANSI_BLUE + file_name + App.ANSI_RESET + " with " + App.ANSI_BLUE + new_cid + App.ANSI_RESET);
        String old_cid = "";
        boolean found = false;
        try {
            ArrayList<String> commit_hashes = log(repo_git, file_name);
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
                if (commit_hashes.indexOf(cid) == commit_hashes.size() - 1 && !found) {
                    App.logger.debug(App.ANSI_PURPLE + "[debug] > " + App.ANSI_YELLOW + repo_name + App.ANSI_PURPLE
                            + " does not have "
                            + App.ANSI_YELLOW + new_cid + App.ANSI_RESET);
                    return null;
                }
            }
            if (!found || old_cid.equals("")) {
                App.logger
                        .debug(App.ANSI_PURPLE + "[debug] > no commit ids found before : " + App.ANSI_YELLOW + new_cid
                                + App.ANSI_RESET);
                return null;
            }
        } catch (Exception e) {
            App.logger.error(App.ANSI_RED + e.getMessage() + App.ANSI_RESET);
            return null;
        }
        return extract_diff(repo_git, file_name, new_cid, old_cid);
    }

    public String[] extract_diff(String repo_git, String file_name, String new_cid, String old_cid) {
        String repo_name = get_repo_name_from_url(repo_git);
        App.logger.trace(App.ANSI_BLUE + "[status] > extracting diff from " + App.ANSI_BLUE + repo_name + App.ANSI_RESET
                + " between "
                + App.ANSI_BLUE + old_cid + App.ANSI_RESET + " and " + App.ANSI_BLUE + new_cid + App.ANSI_RESET);
        String[] result = new String[4];
        try {
            Git git = Git.open(new File(repo_git));
            Repository repository = git.getRepository();
            App.logger.trace(App.ANSI_BLUE + "[status] > repo " + App.ANSI_YELLOW + repo_name + App.ANSI_RESET + " is "
                    + App.ANSI_YELLOW + repository.getDirectory().getAbsolutePath() + App.ANSI_RESET);
            App.logger.trace(App.ANSI_BLUE + "[status] > old cid : " + App.ANSI_YELLOW + old_cid + App.ANSI_RESET);
            App.logger.trace(App.ANSI_BLUE + "[status] > new cid : " + App.ANSI_YELLOW + new_cid + App.ANSI_RESET);

            ObjectReader reader = repository.newObjectReader();

            ObjectId oldHead = repository.resolve(old_cid + "^{tree}");
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(reader, oldHead);
            if (oldHead == null) {
                App.logger.error(App.ANSI_RED + "[error] > oldHead is null" + App.ANSI_RESET);
                return null;
            }

            ObjectId newHead = repository.resolve(new_cid + "^{tree}");
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, newHead);
            if (newHead == null) {
                App.logger.error(App.ANSI_RED + "[error] > newHead is null" + App.ANSI_RESET);
                return null;
            }
            DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
            diffFormatter.setRepository(repository);
            List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);
            for (DiffEntry entry : entries) {
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
            diffFormatter.close();
            repository.close();
            git.close();
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
