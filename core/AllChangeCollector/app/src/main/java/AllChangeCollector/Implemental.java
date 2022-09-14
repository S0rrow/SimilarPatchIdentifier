package AllChangeCollector;

import org.apache.commons.cli.*;
import java.io.*;
import java.util.*;
import org.apache.logging.log4j.Logger;
import com.opencsv.*;
import java.nio.file.FileSystems;

public class Implemental {
    // log4j2 logger
    public Logger logger = App.logger;
    // Defects4J bug
    public String name = null; // Defects4J Bug Name
    public int identifier = -1; // Defects4J Bug Identifier
    // directory paths
    public String project_root;
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
    private String new_cid;
    private String old_cid;

    // constructor
    public Implemental() {
        super();
    }

    // set the values for variables to collect change vector of a defects4j bug
    // @param project_root: the root directory of the project
    // @param name: Defects4J bug name
    // @param identifier: Defects4J bug identifier
    // @param result_dir: result directory
    // @param jdk8_dir: jdk8 directory
    // @param hash_id: hash id of the current execution
    public boolean config(String project_root, String name, int identifier, String result_dir, String jdk8_dir,
            String hash_id) {
        this.project_root = project_root;
        this.name = name;
        this.identifier = identifier;
        this.target = String.format("%s/%s", this.project_root, "target");
        this.result_dir = result_dir;
        this.jdk8_dir = jdk8_dir;
        this.hash_id = hash_id;
        ready = true;
        return ready;
    }

    // according to configured variables, set directories and load defects4j bug
    // information
    public boolean preprocess() {
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
        String project_dir = String.format("%s/%s", workspace_dir, name);
        if (ready) {
            try {
                ProcessBuilder pb = new ProcessBuilder("defects4j", "checkout", "-p", name, "-v",
                        String.format("%db", identifier),
                        "-w", project_dir);
                Process p = pb.start();
                exit_code = p.waitFor();
            } catch (Exception e) {
                logger.error(App.ANSI_RED + "[error] > Exception : " + e.getMessage() + App.ANSI_RESET);
                return false;
            }
        }
        return exit_code == 0;
    }

    // resolve the information of given Defects4J bug with given name and identifier
    public boolean parse() {
        boolean result = false;
        String project_dir = String.format("%s/%s", workspace_dir, name);
        String info = String.format("%s/components/commit_collector/Defects4J_bugs_info/%s.csv", project_root,
                name);
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

    private boolean blame() {
        // TODO : this blame method can only be used for internal use. need to refactor
        // this as public method.
        boolean result = false;
        String project_dir = String.format("%s/%s", workspace_dir, name);
        File project = new File(project_dir);
        try {
            ProcessBuilder blame_builder = new ProcessBuilder("git", "-C", project_dir, "blame", "-C", "-C", "-f", "-l",
                    "-L",
                    String.format("%s,%s", faultyLineBlame, faultyLineBlame), faultyPath);
            blame_builder.directory(project);
            Process blame_process = blame_builder.start();
            BufferedReader process_output = new BufferedReader(new InputStreamReader(blame_process.getInputStream()));
            StringBuilder str_builder = new StringBuilder();
            for (String line = process_output.readLine(); line != null; line = process_output.readLine()) {
                str_builder.append(line);
                str_builder.append(System.lineSeparator());
            }
            new_cid = str_builder.toString().split(" ")[0].strip();
            int exit_code = blame_process.waitFor();
            if (exit_code != 0) {
                logger.error(
                        App.ANSI_RED + "[error] > Git blame failed with exit code : " + exit_code + App.ANSI_RESET);
                return false;
            }

            ProcessBuilder parse_builder = new ProcessBuilder("git", "-C", project_dir, "rev-parse",
                    String.format("%s~1", new_cid));
            parse_builder.directory(project);
            Process parse_process = parse_builder.start();
            BufferedReader parse_output = new BufferedReader(new InputStreamReader(parse_process.getInputStream()));
            str_builder = new StringBuilder();
            for (String line = parse_output.readLine(); line != null; line = parse_output.readLine()) {
                str_builder.append(line);
                str_builder.append(System.lineSeparator());
            }
            old_cid = str_builder.toString().split(" ")[0].strip();
            exit_code = parse_process.waitFor();
            if (exit_code != 0) {
                logger.error(
                        App.ANSI_RED + "[error] > Git rev-parse failed with exit code : " + exit_code + App.ANSI_RESET);
                return false;
            }
            result = exit_code == 0;
        } catch (Exception e) {
            logger.error(App.ANSI_RED + "[error] > Exception : " + e.getMessage() + App.ANSI_RESET);
            return false;
        }
        return result;
    }

    public boolean extract() {
        boolean result = false;
        try {
            if (!blame()) {
                logger.error(App.ANSI_RED + "[error] > Failed to blame the faulty line" + App.ANSI_RESET);
                return false;
            }
            CSVWriter writer = new CSVWriter(new FileWriter(String.format("%s/BFIC.csv", result_dir)));
            String[] headers = "Project,D4J ID,Faulty file path,faulty line,FIC_sha,BFIC_sha".split(",");
            String[] entries = { name, String.format("%d", identifier), faultyPath,
                    String.format("%d", faultyLineBlame), old_cid, new_cid };
            writer.writeNext(headers);
            writer.writeNext(entries);
            writer.close();
            result = true;
        } catch (Exception e) {
            logger.error(App.ANSI_RED + "[error] > Exception : " + e.getMessage() + App.ANSI_RESET);
            return false;
        }
        return result;
    }
}
