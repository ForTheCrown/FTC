package net.forthecrown.core.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents a user's mute status
 */
@RequiredArgsConstructor
@Getter
public enum Mute {
    // --- ENUM CONSTANTS ---
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

    // --- INSTANCE FIELDS ---

    /**
     * The prefix to use in the {@link EavesDropper}
     * message
     */
    private final String prefix;

    /**
     * True, if this status allows users to send
     * messages that other players can see, false
     * otherwise
     */
    private final boolean visibleToOthers;

    /**
     * Determines if the user that sent a message
     * is allowed to see the message they sent if
     * they have this status
     */
    private final boolean visibleToSender;
}