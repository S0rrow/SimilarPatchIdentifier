package BuggyChangeCollector;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.logging.log4j.Logger;

public class Debug
{
    public static final String ANSIFgReset      = "\u001B[0m";
    public static final String ANSIFgDefault    = "\u001B[39m";
    public static final String ANSIFgBlack      = "\u001B[30m";
    public static final String ANSIFgRed        = "\u001B[31m";
    public static final String ANSIFgGreen      = "\u001B[32m";
    public static final String ANSIFgYellow     = "\u001B[33m";
    public static final String ANSIFgBlue       = "\u001B[34m";
    public static final String ANSIFgMagenta    = "\u001B[35m";
    public static final String ANSIFgCyan       = "\u001B[36m";
    public static final String ANSIFgWhite      = "\u001B[37m";

    public static String getStackTrace(Exception e)
    {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));

        return errors.toString();
    }

    public static void logFatal(Logger logger, String message){ logger.fatal(Debug.ANSIFgRed + message + Debug.ANSIFgReset); }
    public static void logError(Logger logger, String message){ logger.error(Debug.ANSIFgRed + message + Debug.ANSIFgReset); }
    public static void logWarn (Logger logger, String message){ logger.warn(Debug.ANSIFgYellow + message + Debug.ANSIFgReset); }
    public static void logInfo (Logger logger, String message){ logger.info(Debug.ANSIFgGreen + message + Debug.ANSIFgReset); }
    public static void logDebug(Logger logger, String message){ logger.debug(Debug.ANSIFgCyan + message + Debug.ANSIFgReset); }
}