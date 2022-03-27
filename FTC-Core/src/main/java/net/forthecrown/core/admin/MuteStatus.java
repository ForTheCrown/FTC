package net.forthecrown.core.admin;

/**
 * Represents the mute status of someone
 */
public enum MuteStatus {
    SOFT ("(Softmuted) ", false, true),
    HARD ("(Muted) ", false, false),
    NONE ("", true, true);

    public final String edPrefix;
    public final boolean maySpeak;
    public final boolean senderMaySee;

    MuteStatus(String s, boolean maySpeak, boolean senderMaySee) {
        this.edPrefix = s;
        this.maySpeak = maySpeak;
        this.senderMaySee = senderMaySee;
    }
}