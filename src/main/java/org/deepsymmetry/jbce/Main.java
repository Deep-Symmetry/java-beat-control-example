package org.deepsymmetry.jbce;

import org.apache.commons.cli.*;
import org.deepsymmetry.bcj.Carabiner;
import org.deepsymmetry.bcj.State;
import org.deepsymmetry.bcj.SyncMode;
import org.deepsymmetry.beatlink.DeviceAnnouncement;
import org.deepsymmetry.beatlink.DeviceAnnouncementListener;
import org.deepsymmetry.beatlink.DeviceFinder;
import org.deepsymmetry.beatlink.VirtualCdj;
import org.deepsymmetry.beatlink.data.CrateDigger;
import org.deepsymmetry.beatlink.data.MetadataFinder;
import org.deepsymmetry.beatlink.data.SignatureFinder;
import org.deepsymmetry.beatlink.data.TimeFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * The main class of the application. When the jar file is executed using {@code java -jar}, the
 * {@link #main(String[])} method will be called with any command-line arguments supplied.
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * Called when the Virtual CDJ has started successfully, to start up the full complement of
     * metadata-related finders that we use.
     *
     * @throws Exception if there is a problem starting anything
     */
    private static void startOtherFinders() throws Exception {
        logger.info("Virtual CDJ running as Player " + VirtualCdj.getInstance().getDeviceNumber());
        MetadataFinder.getInstance().start();
        CrateDigger.getInstance().start();  // TODO: Add CLI option to control this, to save memory
        SignatureFinder.getInstance().start();
        TimeFinder.getInstance().start();
    }

    /**
     * Once we have both a Carabiner connection to Ableton Link and the Virtual CDJ is online with a Pro DJ Link
     * network, it is time to set the sync mode the user requested via command-line options, if possible.
     *
     * @param abletonMaster indicates whether the Ableton Link session is supposed to be the tempo master
     */
    private static void establishBridgeMode(boolean abletonMaster) {
        if (abletonMaster && VirtualCdj.getInstance().isSendingStatus()) {  // Have Pro DJ Link follow Ableton Link
            try {
                VirtualCdj.getInstance().becomeTempoMaster();
                final Double currentLinkTempo = Carabiner.getInstance().getState().linkTempo;
                if (currentLinkTempo != null) {
                    VirtualCdj.getInstance().setTempo(currentLinkTempo);
                }
                Carabiner.getInstance().setSyncMode(SyncMode.FULL);
            } catch (IOException e) {
                logger.error("Problem telling Virtual CDJ to become tempo master to bridge timelines:", e);
            }
        } else {  // Have Ableton Link follow Pro DJ Link
            VirtualCdj.getInstance().setSynced(true);
            try {
                Carabiner.getInstance().setSyncMode(SyncMode.PASSIVE);
            } catch (IOException e) {
                logger.error("Problem telling Carabiner to become tempo master to bridge timelines:", e);
            }
        }
    }

    /**
     * Called when we are supposed to bridge to an Ableton Link session, and so need a Carabiner daemon connection.
     *
     * @param abletonMaster indicates whether the Ableton Link session is supposed to be the tempo master
     */
    private static void connectCarabiner(boolean abletonMaster) {
        boolean connected = false;
        while (!connected) {
            State carabinerState = Carabiner.getInstance().getState();
            logger.info("Trying to connect to Carabiner daemon on port {} with latency {}", carabinerState.port,
                    carabinerState.latency);
            try {
                Carabiner.getInstance().connect();
                connected = true;
            } catch (Exception e) {
                logger.error("Problem trying to connect to Carabiner. Waiting ten seconds to try again.", e);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e2) {
                    logger.warn("Unexplained interruption while waiting to reattempt Carabiner connection:",e2);
                }
            }
        }

        // Now that we are connected to Ableton Link, we can set up timeline bridging if we are supposed to,
        // and if we are also online with a Pro DJ Link network.
        if (VirtualCdj.getInstance().isRunning()) {
            establishBridgeMode(abletonMaster);
        }
    }

    /**
     * Print a message describing the usage of this program, including all the available command-line options.
     *
     * @param options the command-line options that can be supplied
     */
    private static void printUsage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        @SuppressWarnings("SpellCheckingInspection") String jarName = "jbce.jar";  // Start with a reasonable default
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

    /**
     * Sets up the definitions of all command-line arguments that we can accept.
     *
     * @return a specification of the options that can be passed to the parser
     */
    private static Options buildCommandLineOptions() {
        Options options = new Options();
        options.addOption("r", "real-player", false,
                "Try to pose as a real CDJ (device #1-4)");
        options.addOption(Option.builder("d").longOpt("device-number").hasArg().argName("num")
                .desc("Use fixed device # (1-6, overrides -r)").build());
        options.addOption("B", "bridge", false, "Use Carabiner to bridge to Ableton Link");
        options.addOption("a", "ableton-master", false,
                "When bridging, Ableton Link tempo wins");
        options.addOption("b", "beat-align", false,
                "When bridging, sync to beats only, not bars");
        options.addOption(Option.builder("c").longOpt("carabiner-port").hasArg().argName("port")
                .desc("When bridging, port # of Carabiner daemon (default 17000)").build());
        options.addOption(Option.builder("l").longOpt("latency").hasArg().argName("ms")
                .desc("How many milliseconds are we behind the CDJs (default 20)").build());
        options.addOption("h", "help", false, "Display help information and exit");
        return options;
    }

    /**
     * Parses and validates a numeric option value.
     *
     * @param cmd the parsed command line
     * @param option the option whose value is to be parsed
     * @param defaultValue the value to return if the option was not present in the command line
     * @param min the minimum legal value for the option
     * @param max the maximum legal value for the option
     *
     * @return the parsed, valid numeric value given for the option, or {@code defaultValue} if the option was omitted
     * @throws ParseException if there is a problem parsing the option as an integer
     */
    private static int parseNumberOption(CommandLine cmd, Option option, int defaultValue, int min, int max)
            throws ParseException {
        int result = defaultValue;
        if (cmd.hasOption(option)) {
            try {
                result = Integer.parseInt(cmd.getOptionValue(option));
            } catch (NumberFormatException e) {
                throw new ParseException(option.getLongOpt() + " must be an integer, received: " +
                        cmd.getOptionValue(option));
            }

            if (result < min || result > max) {
                throw new ParseException(option.getLongOpt() + " must be between " + min + " and " + max +
                        ", received: " + result);
            }
        }
        return result;
    }

    /**
     * Checks for and reports any inconsistencies between supplied command-line options once they have been
     * individually parsed and validated.
     *
     * @param cmd the collected options
     * @param deviceNumber the parsed value specified for the device-number option
     *
     * @throws ParseException if the options are not mutually consistent
     */
    private static void validateOptionCombinations(CommandLine cmd, int deviceNumber) throws ParseException {
        if (cmd.hasOption("ableton-master") &&
                (cmd.hasOption("device-number")? (deviceNumber > 4) : !cmd.hasOption("real-player"))) {
            throw new ParseException("Inconsistent options: ableton-master requires a real player number (1-4).");
        }
    }

    /**
     * Invoked when the executable jar is run using {@code java -jar}, along with any command-line arguments that
     * were present. Sets up the proper configuration as modified by those arguments, and starts looking for
     * Pro DJ Link devices.
     *
     * @param args the command-line arguments, if any, with which the program was run
     */
    public static void main(String[] args) {
        // Start by parsing command-line arguments.
        final Options options = buildCommandLineOptions();
        final CommandLineParser parser = new DefaultParser();
        try {
            final CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("help")) {  // User asked for usage information, display it and exit.
                printUsage(options);
                System.exit(0);
            }

            // If there are any extraneous arguments, complain about them and exit.
            if (!cmd.getArgList().isEmpty()) {
                for (String arg : cmd.getArgList()) {
                    System.err.println("Unsupported argument: " + arg);
                }
                printUsage(options);
                System.exit(1);
            }

            final int chosenDevice = parseNumberOption(cmd, options.getOption("device-number"), 0, 1, 127);
            final int carabinerPort = parseNumberOption(cmd, options.getOption("carabiner-port"), 17000, 1, 65535);
            final int latency = parseNumberOption(cmd, options.getOption("latency"), 20, -1000, 1000);
            validateOptionCombinations(cmd, chosenDevice);

            // See if the user wants to use a specific or real device number.
            if (chosenDevice > 0) {
                logger.info("Virtual CDJ will attempt to use device #{}", chosenDevice);
            } else if (cmd.hasOption("real-player")) {
                VirtualCdj.getInstance().setUseStandardPlayerNumber(true);
                logger.info("Virtual CDJ will attempt to pose as a standard player, device #1 through #4");
            }

            // Set our device name, then start the daemons that do everything.
            VirtualCdj.getInstance().setDeviceName("java-beat-control");
            logger.info("Waiting for Pro DJ Link devices...");
            DeviceFinder.getInstance().addDeviceAnnouncementListener(new DeviceAnnouncementListener() {
                @Override
                public void deviceFound(DeviceAnnouncement announcement) {
                    logger.info("Pro DJ Link Device Found: {}", announcement);
                    new Thread(() -> {  // We have seen a device, so we can start up the VirtualCDJ if it's not running.
                        try {
                            if  (!VirtualCdj.getInstance().isRunning()) {
                                if (VirtualCdj.getInstance().start((byte) chosenDevice)) {
                                    startOtherFinders();
                                    MetadataFinder.getInstance().setPassive(true);  // Start out conservatively.
                                    if (Util.isRealPlayer()) {  // But no, we can use all the bells and whistles!
                                        VirtualCdj.getInstance().setSendingStatus(true);
                                        MetadataFinder.getInstance().setPassive(false);
                                    }
                                    establishBridgeMode(cmd.hasOption("ableton-master"));
                                } else {
                                    logger.warn("Virtual CDJ failed to start.");
                                }
                            }
                        } catch (Throwable t) {
                            logger.error("Problem trying to start Virtual CDJ", t);
                        }
                    }).start();
                }

                @Override
                public void deviceLost(DeviceAnnouncement announcement) {
                    logger.info("Pro DJ Link Device Lost: {}", announcement);
                    if (DeviceFinder.getInstance().getCurrentDevices().isEmpty()) {
                        logger.info("Shutting down Virtual CDJ");  // We lost the last device, shut down for now.
                        VirtualCdj.getInstance().stop();
                    }
                }
            });

            try {
                DeviceFinder.getInstance().start();
            } catch (Exception e) {
                logger.error("Unable to start DeviceFinder:", e);
                System.exit(1);
            }

            // Configure Carabiner options.
            Carabiner.getInstance().setCarabinerPort(carabinerPort);
            Carabiner.getInstance().setLatency(latency);
            Carabiner.getInstance().setSyncToBars(!cmd.hasOption("beat-align"));

            // If we are supposed to bridge to an Ableton Link network, set that up.
            if (cmd.hasOption("bridge")) {
                Carabiner.getInstance().addDisconnectionListener(unexpected -> connectCarabiner(cmd.hasOption("ableton-master")));
                connectCarabiner(cmd.hasOption("ableton-master"));
            }
        } catch (ParseException e) {  // We failed to parse the command-line options to match our specifications.
            System.err.println(e.getMessage());
            printUsage(options);
            System.exit(1);
        }

        // This is where you could add code to register listeners to respond to other Pro DJ Link events, like
        // the Clojure equivalents in Open Beat Control.
        // See https://github.com/Deep-Symmetry/open-beat-control/blob/9a043131028df63f05194632989e2d2f1fd46045/src/open_beat_control/core.clj#L240-L337
        //
        // You could also add a class that manages your bidirectional universe along the lines of the OSC Server
        // namespace in Open Beat Control.
        // See https://github.com/Deep-Symmetry/open-beat-control/blob/main/src/open_beat_control/osc_server.clj

        // Keep the program running until killed.
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                //noinspection BusyWait
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                logger.info("Strange, interrupted in main sleep loop:", e);
            }
        }
    }
}