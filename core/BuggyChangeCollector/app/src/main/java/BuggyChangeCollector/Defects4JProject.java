package BuggyChangeCollector;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

public class Defects4JProject extends Project {
    // Defects4J Bug Name into String project
    private int identifier; // Defects4J Bug Identifier, not its name

    private String jdk8Directory = null;
    private String SPIDirectory = null;

    private static final Logger logger = LogManager.getLogger();

    public Defects4JProject(String projectName, String projectDirectory, String jdk8Directory, String SPIDirectory) {
        this.jdk8Directory = jdk8Directory;
        this.SPIDirectory = SPIDirectory;

        String[] defects4jBug = projectName.split("-");
        this.project = defects4jBug[0];
        this.identifier = Integer.parseInt(defects4jBug[1]);

        this.projectDirectory = String.format("%s/%s", projectDirectory, projectName);

        String file = String.format("%s/components/commit_collector/Defects4J_bugs_info/%s.csv", this.SPIDirectory,
                this.project);
        try {
            CSVReader reader = new CSVReader(new FileReader(file));
            String[] nextLine;

            while ((nextLine = reader.readNext()) != null) {
                if (nextLine[0].startsWith("Defects4J"))
                    continue;

                if (Integer.parseInt(nextLine[0]) == this.identifier) {
                    this.faultyPath = nextLine[1];
                    this.faultyLineBlame = Integer.parseInt(nextLine[2]);
                    this.faultyLineFix = Integer.parseInt(nextLine[3]);
                    break;
                }
            }
        } catch (IOException | CsvValidationException ex) {
            Debug.logError(logger, Debug.getStackTrace(ex));

            this.faultyPath = null;
            this.faultyLineBlame = -1;
            this.faultyLineFix = -1;
        }
    }

    public int getIdentifier() {
        return this.identifier;
    }

    public void fetch() {
        try {
            ProcessBuilder fetcher = new ProcessBuilder("defects4j", "checkout",
                    "-p", this.project, "-v", String.format("%db", this.identifier),
                    "-w", this.projectDirectory);

            Map<String, String> fetcherEnvs = fetcher.environment();
            fetcherEnvs.put("PATH", String.format("%s/bin:%s", this.jdk8Directory, System.getenv("PATH")));
            fetcherEnvs.put("JAVA_HOME", this.jdk8Directory);
            // fetcherEnvs.forEach((key, value) -> System.out.printf("%s : %s\n", key
            // ,value));

            Debug.logDebug(logger, "Performing defects4j checkout...");
            Process p = fetcher.start();
            int ret = p.waitFor();
            Debug.logDebug(logger, String.format("Process defects4j checkout exited with code %d", ret));
        } catch (IOException | InterruptedException ex) {
            Debug.logError(logger, Debug.getStackTrace(ex));
        }
    }

}