package net.forthecrown.core;

public interface FtcFeatures {
    boolean
            AFK_SCANNER = shouldEnable(false);

    private static boolean shouldEnable(boolean enabled) {
        return enabled || Crown.inDebugMode();
    }
}
