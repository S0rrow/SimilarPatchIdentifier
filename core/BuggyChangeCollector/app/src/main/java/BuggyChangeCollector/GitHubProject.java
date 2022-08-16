package BuggyChangeCollector;

import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class GitHubProject extends Project {
    private String projectLink; // URL of the GitHub Repository

    private static final Logger logger = LogManager.getLogger();

    public GitHubProject(String projectName, String projectLink, String projectDirectory, String faultyPath, int faultyLineBlame, int faultyLineFix)
    {
        this.project = projectName;
        this.projectLink = projectLink;
        
        this.projectDirectory = String.format("%s/%s", projectDirectory, projectName);

        this.faultyPath = faultyPath;
        this.faultyLineBlame = faultyLineBlame;
        this.faultyLineFix = faultyLineFix;
    }

    public String getProjectName() { return this.project; }
    public int getIdentifier() { return -1; }

    public void fetch()
    {
        try
        {
            ProcessBuilder fetcher = new ProcessBuilder("git", "clone", this.projectLink, this.projectDirectory);
            Process p = fetcher.start();

            p.waitFor();
        }
        catch(IOException | InterruptedException ex)
        {
            logger.error(Debug.getStackTrace(ex));
        }
    }
}
