package org.deepsymmetry.jbce;

import org.deepsymmetry.beatlink.VirtualCdj;

/**
 * Provides static utility methods.
 */
public class Util {

    /**
     * Private constructor prevents instantiation.
     */
    private Util() {

    }

    /**
     * Makes sure the Virtual CDJ is online (connected to a Pro DJ Link network and running).
     *
     * @throws IllegalStateException if it is not
     */
    public static void ensureOnline() {
        if (!VirtualCdj.getInstance().isRunning()) {
            throw new IllegalStateException("Virtual CDJ must be online to perform this operation.");
        }
    }

    /**
     * Checks whether the Virtual CDJ was able to use a standard player number in the range 1 to 4, which makes
     * certain features available to us.
     *
     * @return {@code true} if we are successfully posing as a traditional standard player
     */
    public static boolean isRealPlayer() {
        ensureOnline();
        final byte player = VirtualCdj.getInstance().getDeviceNumber();
        return player >= 1 && player <= 4;
    }
}
