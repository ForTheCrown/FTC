package net.forthecrown.user.name;

import net.forthecrown.text.TextWriter;
import net.forthecrown.user.User;

/**
 * Element within a user's hover text or fully formatted profile display
 */
public interface ProfileDisplayElement {

  /**
   * Writes this element's data to the specified writer
   *
   * @param writer  Output writer
   * @param user    User whose name is being formatted
   * @param context Display context
   */
  void write(TextWriter writer, User user, DisplayContext context);

  /**
   * This field's placement. Determines where the field is displayed, either in both the hover text
   * and regular profile view, or in 1 of those exclusively
   *
   * @return Field placement
   */
  default FieldPlacement placement() {
    return FieldPlacement.IN_PROFILE;
  }
}
