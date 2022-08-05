package BuggyChangeCollector;

import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class GitHubProject extends Project {
    private String projectLink; // URL of the GitHub Repository

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
        catch(IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
