/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package LCE;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;

public class App {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    static Logger appLogger = LogManager.getLogger(App.class.getName());

    public static void main(String[] args) {
        appLogger.info(ANSI_YELLOW + "==========================================================" + ANSI_RESET);
        appLogger.info(ANSI_YELLOW + "[status] App Initiated" + ANSI_RESET);
        App main = new App();
        Properties argv = args.length == 0 ? main.loadProperties() : main.loadProperties(args[0]);
        if (argv == null)
            appLogger.error(ANSI_RED + "[error] > Properties file not found" + ANSI_RESET);
        else {
            appLogger.info(ANSI_YELLOW + "[status] Properties file loaded" + ANSI_RESET);
            appLogger.info(ANSI_YELLOW + "[status] running LCE" + ANSI_RESET);
            main.run(argv);
        }
    }

    public void run(Properties properties) {
        String spi_path = properties.getProperty("SPI.dir"); // argv

        File propertiesFile = new File(spi_path + "/log4j.properties");
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        context.setConfigLocation(propertiesFile.toURI());

        Extractor extractor = new Extractor(properties); // argv
        GitLoader gitLoader = new GitLoader();
        appLogger.trace(ANSI_BLUE + "[status] > Extractor running..." + ANSI_RESET);
        extractor.run();
        appLogger.trace(ANSI_GREEN + "[status] > extractor ready" + ANSI_RESET);
        List<String> result = extractor.extract();
        appLogger.trace(ANSI_GREEN + "[status] > extraction done" + ANSI_RESET);
        List<String[]> preprocessed = preprocess(result);
        appLogger.trace(ANSI_GREEN + "[status] > preprocess success" + ANSI_RESET);
        gitLoader.set(properties.getProperty("pool.dir"), properties.getProperty("candidates.dir")); // argv
        boolean doClean = properties.getProperty("doClean").equals("true");

        if (doClean) { // DEBUG : to clean the output directory and generate
                       // gitignore files
            appLogger.trace(ANSI_BLUE + "[status] > cleaning result and candidate directory" + ANSI_RESET);
            gitLoader.purge();
            appLogger.trace(ANSI_GREEN + "[status] > cleaning done" + ANSI_RESET);
            appLogger.trace(ANSI_BLUE + "[status] > copying gitignore file to result directory and candidate directory"
                    + ANSI_RESET);
            gitLoader.copy(spi_path + "/core/LCE/gitignore/.gitignore",
                    properties.getProperty("pool.dir") + ".gitignore"); // argv
            gitLoader.copy(spi_path + "/core/LCE/gitignore/.gitignore",
                    properties.getProperty("candidates.dir") + ".gitignore"); // argv
            appLogger.trace(ANSI_GREEN + "[status] > gitignore file copied" + ANSI_RESET);
        }

        appLogger.trace(ANSI_BLUE + "[status] > Initiating gitLoader" + ANSI_RESET);
        int counter = 0;
        for (String[] line : preprocessed) {
            gitLoader.getCounter(counter);
            gitLoader.config(line[4], line[0], line[1], line[2], line[3], properties.getProperty("d4j_project_name"),
                    Integer.parseInt(properties.getProperty("d4j_project_num"))); // argv
            gitLoader.run();
            try {
                if (gitLoader.load()) {
                    appLogger.trace(ANSI_GREEN + "[status] > gitLoader load success" + ANSI_RESET);
                } else {
                    appLogger.error(ANSI_RED + "[status] > gitLoader load failed" + ANSI_RESET);
                }
            } catch (Exception e) {
                appLogger.error(ANSI_RED + "[error] > Exception :" + e.getMessage() + ANSI_RESET);
            }
            counter++;
        }
        appLogger.trace(ANSI_GREEN + "[status] > gitLoader done" + ANSI_RESET);
        appLogger.info(ANSI_YELLOW + "==========================================================" + ANSI_RESET);
        appLogger.info(ANSI_YELLOW + "[status] > App done" + ANSI_RESET);
    }

    private List<String[]> preprocess(List<String> result) {
        List<String[]> result_split = new ArrayList<>();
        try {
            for (String line : result) {
                // System.out.println("[debug] line : " + line);
                String[] line_split = line.split(",");
                String[] selection = new String[] { line_split[0], line_split[1], line_split[2], line_split[3],
                        line_split[4] };
                result_split.add(selection);
            }
        } catch (Exception e) {
            System.out.println("[error] > " + e.getMessage());
        }
        return result_split;
    }

    public Properties loadProperties() {
        return loadProperties("../lce.properties");
    }

    public Properties loadProperties(String path) {
        try {
            appLogger.trace(ANSI_BLUE + "[status] > loading properties" + ANSI_RESET);
            File file = new File(path);
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));
            appLogger.trace(ANSI_GREEN + "[status] > properties loaded" + ANSI_RESET);
            return properties;
        } catch (Exception e) {
            appLogger.error(ANSI_RED + "[error] > Exception : " + e.getMessage() + ANSI_RESET);
            return null;
        }
    }
}
