package BuggyChangeCollector;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

            Debug.logDebug(logger, "Performing Git Clone...");
            Process p = fetcher.start();

            int ret = p.waitFor();
            Debug.logDebug(logger, String.format("Process Git Clone exited with code %d", ret));
        }
        catch(IOException | InterruptedException ex)
        {
            Debug.logError(logger, Debug.getStackTrace(ex));
        }
    }
}
