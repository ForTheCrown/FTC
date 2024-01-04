package net.forthecrown.discord;

import net.forthecrown.text.TextWriter;
import net.forthecrown.user.User;
import net.forthecrown.user.name.DisplayContext;
import net.forthecrown.user.name.FieldPlacement;
import net.forthecrown.user.name.ProfileDisplayElement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

class DiscordProfileField implements ProfileDisplayElement {

  @Override
  public void write(TextWriter writer, User user, DisplayContext context) {
    if (!context.profileViewable()) {
      return;
    }

    var opt = FtcDiscord.getUserMember(user);

    if (opt.isEmpty()) {
      return;
    }

    var member = opt.get();
    var dcUser = member.getUser();

    if (!context.intent().isHoverTextAllowed()) {
      writer.field("Discord", dcUser.getName());
      return;
    }

    Component text = Component.text("[" + dcUser.getName() + "]", NamedTextColor.AQUA)
        .hoverEvent(Component.text("Click to copy user ID"))
        .clickEvent(ClickEvent.copyToClipboard(dcUser.getName()));

    writer.field("Discord", text);
  }

  @Override
  public FieldPlacement placement() {
    return FieldPlacement.ALL;
  }
}
