package BuggyChangeCollector;

import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.opencsv.CSVReader;
import com.opencsv.CSVIterator;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;

public class Defects4JProject extends Project {
    // Defects4J Bug Name into String project
    private int identifier; // Defects4J Bug Identifier, not its name

    public Defects4JProject(String projectName, String projectDirectory)
    {
        String[] defects4jBug = projectName.split("-");
        this.project = defects4jBug[0];
        this.identifier = Integer.parseInt(defects4jBug[1]);

        this.projectDirectory = projectDirectory;
        
        String root = System.getProperty("user.dir");
        // String file = String.format("%s/components/commit_collector/Defects4J_bugs_info/%s.csv", root, this.project);
        String file = String.format("/home/codemodel/turbstructor/SimilarPatchIdentifier/components/commit_collector/Defects4J_bugs_info/%s.csv", this.project);
        
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
            Process p = fetcher.start();

            p.waitFor();
        }
        catch(IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }
    
}