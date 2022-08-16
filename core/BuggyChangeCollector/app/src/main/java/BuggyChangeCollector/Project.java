package BuggyChangeCollector;

import java.io.IOException;
import java.io.File;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public abstract class Project {
    protected String project;
    protected String projectDirectory;

    protected String faultyPath;
    protected int faultyLineBlame;
    protected int faultyLineFix;

    public abstract int getIdentifier(); // Will return -1 for GItHubProject

    public String getProjectName() { return this.project; }
    public String getFaultyPath() { return this.faultyPath; }
    public int getFaultyLineBlame() { return this.faultyLineBlame; }
    public int getFaultyLineFix() { return this.faultyLineFix; }

    private static final Logger logger = LogManager.getLogger();

    public abstract void fetch();
    public String[] getFICs()
    {
        String FIC = new String();
        String BFIC = new String();

        try
        {
            // git blame
            logger.info("Performing Git Blame...");
            ProcessBuilder blamePB = new ProcessBuilder("git", "-C", this.projectDirectory,
                "blame", "-C", "-C",
                "-f", "-l", "-L", String.format("%s,%s", this.faultyLineBlame, this.faultyLineBlame),
                this.faultyPath);
            Process blameProc = blamePB.start();
            BufferedReader blameProcOutput = new BufferedReader(new InputStreamReader(blameProc.getInputStream()));
            StringBuilder strBuilder = new StringBuilder();

            String line = null;
            while((line = blameProcOutput.readLine()) != null)
            {
                strBuilder.append(line);
                strBuilder.append(System.lineSeparator());
            }
            FIC = strBuilder.toString().split(" ")[0].strip();
            logger.info(String.format("FIC ID extracted as %s", FIC));
            blameProc.waitFor();

            // git rev-parse
            logger.info("Performing Git Rev-Parse...");
            ProcessBuilder parsePB = new ProcessBuilder("git",
                "-C", this.projectDirectory,
                "rev-parse", String.format("%s~1", FIC));
            Process parseProc = parsePB.start();
            BufferedReader parseProcOutput = new BufferedReader(new InputStreamReader(parseProc.getInputStream()));
            strBuilder = new StringBuilder();
            while((line = parseProcOutput.readLine()) != null)
            {
                strBuilder.append(line);
                strBuilder.append(System.lineSeparator());
            }
            BFIC = strBuilder.toString().split(" ")[0].strip();
            logger.info(String.format("BFIC ID extracted as %s", BFIC));
            parseProc.waitFor();
        }
        catch(IOException | InterruptedException e)
        {
            logger.error(Debug.getStackTrace(e));
        }

        return new String[] {BFIC, FIC};
    }
}