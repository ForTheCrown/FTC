package net.forthecrown.antigrief;

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
  SOFT("(Softmuted) "),

  /**
   * Cannot speak, they do not even see their own messages
   */
  HARD("(Muted) "),

  /**
   * They can speak, there's no mute in effect
   */
  NONE("");

  // --- INSTANCE FIELDS ---

  /**
   * The prefix to use in the EavesDropper message
   */
  private final String prefix;
}