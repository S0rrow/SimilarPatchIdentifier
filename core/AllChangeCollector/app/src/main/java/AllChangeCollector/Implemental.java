package AllChangeCollector;

import org.apache.logging.log4j.Logger;

public class Implemental {
    // log4j2 logger
    public Logger logger = App.logger;
    // Defects4J bug
    public String name = null; // Defects4J Bug Name
    public int identifier = -1; // Defects4J Bug Identifier
    // directory paths
    public String root;
    public String target;
    public String result_dir;
    public String jdk8_dir;
    public String workspace_dir;
    // hash id of the current execution
    public String hash_id;
    // if configured or not
    public boolean ready = false;
    // Defects4J bug information
    private String faultyPath;
    private Integer faultyLineBlame;
    private Integer faultyLineFix;

    // constructor
    public Implemental() {
        super();
        // path of the root directory
        root = FileSystems.getDefault().getPath(".").toAbsolutePath();
    }

    // set the values for variables to collect change vector of a defects4j bug
    // @param name: Defects4J bug name
    // @param identifier: Defects4J bug identifier
    // @param target: target directory
    // @param result_dir: result directory
    // @param jdk8_dir: jdk8 directory
    // @param hash_id: hash id of the current execution
    public boolean config(String name, int identifier, String target, String result_dir, String jdk8_dir,
            String hash_id) {
        this.name = name;
        this.identifier = identifier;
        this.target = target;
        this.result_dir = result_dir;
        this.jdk8_dir = jdk8_dir;
        this.hash_id = hash_id;
        ready = true;
        return ready;
    }

    // according to configured variables, set directories and load defects4j bug
    // information
    public boolean preprocess() {
        logger.debug(App.ANSI_PURPLE + "[debug] > Generating directories " + App.ANSI_RESET);
        if (ready) {
            try {
                workspace_dir = String.format("%s/%s", target, hash_id);
                File workspace = new File(workspace_dir);
                if (!workspace.exists()) {
                    if (!workspace.mkdirs()) {
                        // failed to create workspace directory
                        logger.error(App.ANSI_RED + "[error] > Failed to create workspace directory " + App.ANSI_RESET);
                        return false;
                    }
                }
                String output_dir = String.format("%s/outputs/ChangeCollector", workspace_dir);
                File output_path = new File(output_dir);
                if (!output_path.exists()) {
                    if (!output_path.mkdirs()) {
                        // failed to create output directory
                        logger.error(App.ANSI_RED + "[error] > Failed to create output directory " + App.ANSI_RESET);
                        return false;
                    }
                }
            } catch (Exception e) {
                logger.error(App.ANSI_RED + "[error] > Exception : " + e.getMessage() + App.ANSI_RESET);
                return false;
            }
            return true;
        }
        logger.error(App.ANSI_RED + "[error] > Not configured yet" + App.ANSI_RESET);
        return false;
    }

    // collect the current source code of the Defects4J bug
    public boolean fetch() {
        int exit_code = -1;
        if (ready) {
            try {
                ProcessBuilder pb = new ProcessBuilder("defects4j", "checkout", "-p", name, "-v",
                        String.valueOf(identifier),
                        "-w", target);
                Map<String, String> env_var = pb.environment();
                env_var.put("PATH", String.format("%s/bin:%s", jdk8_dir, System.getenv("PATH")));
                env_var.put("JAVA_HOME", jdk8_dir);

                Process p = pb.start();
                exit_code = p.waitFor();
                if (exit_code != 0) {
                    logger.error(App.ANSI_RED + "[error] > Failed to fetch defects4j bug " + App.ANSI_RESET);
                    return false;
                }
            } catch (Exception e) {
                logger.error(App.ANSI_RED + "[error] > Exception : " + e.getMessage() + App.ANSI_RESET);
                return false;
            }
        }
        return exit_code == 0;
    }

    // resolve the information of given Defects4J bug with given name and identifier
    public boolean parse(String[] args) {
        boolean result = false;
        String project_dir = String.format("%s/%s", workspace_dir, name);
        String info = String.format("%s/componente/commit_collector/Defects4J_bugs_info/%s.csv", root,
                name + "-" + identifier);
        try {
            CSVReader reader = new CSVReader(new FileReader(info));
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                if (nextLine[0].startsWith("Defects4J"))
                    continue;
                if (Integer.parseInt(nextLine[0]) == identifier) {
                    faultyPath = nextLine[1];
                    faultyLineBlame = Integer.parseInt(nextLine[2]);
                    faultyLineFix = Integer.parseInt(nextLine[3]);
                    result = true;
                    break;
                }
            }
        } catch (Exception e) {
            logger.error(App.ANSI_RED + "[error] > Exception : " + e.getMessage() + App.ANSI_RESET);
            return false;
        }
        return result;
    }
}
