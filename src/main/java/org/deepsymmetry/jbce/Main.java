package org.deepsymmetry.jbce;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static void printUsage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        String jarName = "jbce.jar";  // Start with a reasonable default
        try {
            String jarPath = Main.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();
            jarName = new File(jarPath).getName();
        } catch (URISyntaxException e) {
            logger.warn("Problem determining our jar path", e);
        }
        formatter.printHelp("java -jar " + jarName, options);
    }

    private static Options buildCommandLineOptions() {
        Options options = new Options();
        options.addOption("r", "real-player", false,
                "Try to pose as a real CDJ (device #1-4)");
        options.addOption("d", "device-number", true, "Use fixed device # (overrides -r)");
        options.addOption("B", "bridge", false, "Use Carabiner to bridge to Ableton Link");
        options.addOption("a", "ableton-master", false,
                "When bridging, Ableton Link tempo wins");
        options.addOption("b", "beat-align", false,
                "When bridging, sync to beats only, not bars");
        options.addOption("c", "carabiner-port", true,
                "When bridging, port # of Carabiner daemon (default 17000)");
        options.addOption("l", "latency", true,
                "How many milliseconds are we behind the CDJs (default 20)");
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
            System.exit(1);
        }
    }
}