package BuggyChangeCollector;

import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;

import com.opencsv.CSVReader;
import com.opencsv.CSVIterator;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;

public class Defects4JProject extends Project {
    // Defects4J Bug Name into String project
    private int identifier; // Defects4J Bug Identifier, not its name

    private String jdk8Directory = null;
    private String SPIPath = null;

    public Defects4JProject(String projectName, String projectDirectory)
    {
        Properties D4JProps = new Properties();

        try
        {
            D4JProps.load(new FileInputStream("BCC.properties"));

            jdk8Directory = D4JProps.getProperty("JAVA_HOME.8");
            SPIPath = D4JProps.getProperty("SPI.path");
        }
        catch(Exception ex)
        {
            ex.printStackTrace();

            jdk8Directory = null;
            SPIPath = null;
        }

        String[] defects4jBug = projectName.split("-");
        this.project = defects4jBug[0];
        this.identifier = Integer.parseInt(defects4jBug[1]);

        this.projectDirectory = projectDirectory;
        
        String file = String.format("%s/components/commit_collector/Defects4J_bugs_info/%s.csv", this.SPIPath, this.project);
        try
        {    
            CSVReader reader = new CSVReader(new FileReader(file));
            String [] nextLine;

            while((nextLine = reader.readNext()) != null)
            {
                if(nextLine[0].startsWith("Defects4J")) continue;

                if(Integer.parseInt(nextLine[0]) == this.identifier)
                {
                    this.faultyPath         = nextLine[1];
                    this.faultyLineBlame    = Integer.parseInt(nextLine[2]);
                    this.faultyLineFix      = Integer.parseInt(nextLine[3]);
                    break;
                }
            }
        }
        catch(IOException | CsvValidationException ex)
        {
            ex.printStackTrace();

            this.faultyPath = null;
            this.faultyLineBlame = -1;
            this.faultyLineFix = -1;
        }
    }

    public void fetch()
    {
        try
        {
            ProcessBuilder fetcher = new ProcessBuilder("defects4j", "checkout",
                "-p", project, "-v", String.format("%db", this.identifier),
                "-w", this.projectDirectory);
            Map<String, String> fetcherEnvs = fetcher.environment();

            fetcherEnvs.put("PATH", String.format("%s%s%s", this.jdk8Directory, File.pathSeparator, System.getenv("PATH")));
            fetcherEnvs.put("JAVA_HOME", this.jdk8Directory);

            fetcherEnvs.forEach((key, value) -> System.out.printf("%s : %s\n", key ,value));

            Process p = fetcher.start();

            p.waitFor();
        }
        catch(IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }
    
}