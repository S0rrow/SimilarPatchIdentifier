package BuggyChangeCollector;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Debug
{
    public static String getStackTrace(Exception e)
    {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));

        return errors.toString();
    }
}