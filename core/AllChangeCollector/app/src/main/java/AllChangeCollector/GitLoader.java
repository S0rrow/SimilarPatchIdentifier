package AllChangeCollector;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.TextProgressMonitor;

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

    public boolean clone(String url, String path) {
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
