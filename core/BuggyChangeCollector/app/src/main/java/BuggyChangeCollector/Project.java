package BuggyChangeCollector;

import java.io.IOException;
import java.io.File;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public abstract class Project {
    protected String project;
    protected String projectDirectory;

    protected String faultyPath;
    protected int faultyLineBlame;
    protected int faultyLineFix;

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
            Process blameProc = blamePB.start();
            BufferedReader blameProcOutput = new BufferedReader(new InputStreamReader(blameProc.getInputStream()));
            StringBuilder strBuilder = new StringBuilder();

            String line = null;
            while((line = blameProcOutput.readLine()) != null)
            {
                strBuilder.append(line);
                strBuilder.append(System.lineSeparator());
            }
            FIC = strBuilder.toString().split(" ")[0];

            // git rev-parse
            ProcessBuilder parsePB = new ProcessBuilder("git", "-C", this.projectDirectory,
                "rev-parse", String.format("%s~1", FIC));
            Process parseProc = parsePB.start();
            BufferedReader parseProcOutput = new BufferedReader(new InputStreamReader(parseProc.getInputStream()));
            strBuilder = new StringBuilder();
            while((line = parseProcOutput.readLine()) != null)
            {
                strBuilder.append(line);
                strBuilder.append(System.lineSeparator());
            }
            BFIC = strBuilder.toString().split(" ")[0];
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return new String[] {BFIC, FIC};
    }
}