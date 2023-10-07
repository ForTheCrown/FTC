package net.forthecrown.guilds;

import net.forthecrown.text.TextWriter;
import net.forthecrown.user.User;
import net.forthecrown.user.name.DisplayContext;
import net.forthecrown.user.name.FieldPlacement;
import net.forthecrown.user.name.ProfileDisplayElement;

class GuildProfileElement implements ProfileDisplayElement {

  @Override
  public void write(TextWriter writer, User user, DisplayContext context) {
    var guild = Guilds.getGuild(user);

    if (guild == null) {
      return;
    }

    writer.field("Guild", guild.displayName());
  }

  @Override
  public FieldPlacement placement() {
    return FieldPlacement.ALL;
  }
}
