package net.forthecrown.core.admin;

import lombok.RequiredArgsConstructor;

/**
 * Represents the mute status of someone
 */
@RequiredArgsConstructor
public enum MuteStatus {
    /**
     * Thay can speak, but only the sender sees the message
     */
    SOFT ("(Softmuted) ", false, true),

    /**
     * Cannot speak, they do not even see their own messages
     */
    HARD ("(Muted) ", false, false),

    /**
     * They can speak, there's no mute in effect
     */
    NONE ("", true, true);

    public final String edPrefix;
    public final boolean maySpeak;
    public final boolean senderMaySee;
}