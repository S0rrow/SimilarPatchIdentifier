package LCE;

public class GitLoader {
    private String url; // git url
    private String name; // git repo name
    private String cid; // commit id
    private String filepath; // file name
    private String result_dir; // result dir

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
    }

    public void set_result_dir(String path) {
        this.result_dir = path;
    }

    public void run() {
        System.out.println("\n[info] > git clone " + url);
        print_debug_info();
        // TODO
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

    private void print_debug_info() {
        System.out.println("[debug] > url : " + url);
        System.out.println("[debug] > name : " + name);
        System.out.println("[debug] > cid : " + cid);
        System.out.println("[debug] > filepath : " + filepath);
        System.out.println("[debug] > result_dir : " + result_dir);
    }
}
