package net.forthecrown.core.admin;

/**
 * Represents the mute status of someone
 */
public enum MuteStatus {
    SOFT ("(Softmuted) ", false),
    HARD ("(Muted) ", false),
    NONE ("", true);

    public final String edPrefix;
    public final boolean maySpeak;

    MuteStatus(String s, boolean maySpeak) {
        this.edPrefix = s;
        this.maySpeak = maySpeak;
    }
}
