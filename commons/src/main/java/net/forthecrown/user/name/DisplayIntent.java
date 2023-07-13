package net.forthecrown.user.name;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The display destination of a formatted name
 */
@Getter
@RequiredArgsConstructor
public enum DisplayIntent {

  /**
   * No intent specified
   */
  UNSET(true),

  /**
   * Name is being formatted for a join/leave message
   */
  JOIN_LEAVE_MESSAGE(true),

  /**
   * Name is being formatted for the tab list
   */
  TABLIST(false),

  /**
   * Means a name is being formatted for display inside another user's
   * {@link net.kyori.adventure.text.event.HoverEvent} text
   */
  HOVER_TEXT(false);

  final boolean hoverTextAllowed;
}
