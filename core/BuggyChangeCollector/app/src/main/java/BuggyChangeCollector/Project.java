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
            ProcessBuilder blamePB = new ProcessBuilder("git", "-C", this.projectDirectory,
                "blame", "-C", "-C",
                "-f", "-l", "-L", String.format("%s,%s", this.faultyLineBlame, this.faultyLineBlame),
                this.faultyPath);

            Debug.logDebug(logger, "Performing Git Blame...");
            Process blameProc = blamePB.start();
            BufferedReader blameProcOutput = new BufferedReader(new InputStreamReader(blameProc.getInputStream()));

            StringBuilder strBuilder = new StringBuilder();
            for(String line = blameProcOutput.readLine(); line != null; line = blameProcOutput.readLine())
            {
                strBuilder.append(line);
                strBuilder.append(System.lineSeparator());
            }
            FIC = strBuilder.toString().split(" ")[0].strip();

            int ret = blameProc.waitFor();
            Debug.logDebug(logger, String.format("Process git blame exited with code %d", ret));
            Debug.logInfo(logger, String.format(" FIC ID extracted as %s", FIC));


            // git rev-parse
            ProcessBuilder parsePB = new ProcessBuilder("git", "-C", this.projectDirectory,
                "rev-parse", String.format("%s~1", FIC));

            Debug.logDebug(logger, "Performing Git Rev-Parse...");
            Process parseProc = parsePB.start();
            BufferedReader parseProcOutput = new BufferedReader(new InputStreamReader(parseProc.getInputStream()));

            strBuilder = new StringBuilder();
            for(String line = parseProcOutput.readLine(); line != null; line = parseProcOutput.readLine())
            {
                strBuilder.append(line);
                strBuilder.append(System.lineSeparator());
            }
            BFIC = strBuilder.toString().split(" ")[0].strip();

            ret = parseProc.waitFor();
            Debug.logDebug(logger, String.format("Process git rev-parse exited with code %d", ret));
            Debug.logInfo(logger, String.format("BFIC ID extracted as %s", BFIC));
        }
        catch(IOException | InterruptedException e)
        {
            Debug.logError(logger, Debug.getStackTrace(e));
        }

        return new String[] {BFIC, FIC};
    }
}