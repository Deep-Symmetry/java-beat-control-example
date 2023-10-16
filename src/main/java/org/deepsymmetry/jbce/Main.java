package org.deepsymmetry.jbce;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static void printUsage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java-beat-control-example", options);
    }

    private static Options buildCommandLineOptions() {
        Options options = new Options();
        options.addOption("r", "real-player", false,
                "Try to pose as a real CDJ (device #1-4)");
        options.addOption("h", "help", false, "Display help information and exit");

        return options;
    }

    public static void main(String[] args) {
        // Start by parsing command-line arguments.
        Options options = buildCommandLineOptions();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {  // User asked for usage information.
                printUsage(options);
                System.exit(0);
            }
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            printUsage(options);
        }

        logger.info("I ran.");  // TODO remove
    }
}