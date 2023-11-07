package net.forthecrown.afk;

import net.forthecrown.text.TextWriter;
import net.forthecrown.user.User;
import net.forthecrown.user.name.DisplayContext;
import net.forthecrown.user.name.ProfileDisplayElement;

public class AfkProfileField implements ProfileDisplayElement {

  @Override
  public void write(TextWriter writer, User user, DisplayContext context) {
    Afk.getAfkReason(user).ifPresent(message -> {
      writer.field("AFK", message);
    });
  }
}
