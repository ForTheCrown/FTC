package net.forthecrown.core.admin;

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
