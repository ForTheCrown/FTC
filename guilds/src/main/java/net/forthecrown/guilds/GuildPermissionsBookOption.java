package net.forthecrown.guilds;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;

import com.mojang.datafixers.util.Pair;
import lombok.RequiredArgsConstructor;
import net.forthecrown.command.settings.BookSetting;
import net.kyori.adventure.text.Component;

@RequiredArgsConstructor
public class GuildPermissionsBookOption extends BookSetting<Pair<Guild, GuildRank>> {

  final GuildPermission perm; // GuildPermission this option refers to
  final String cmd; // Command to run when player clicks [✔] or [✖]
  final Component displayName; // setting in book
  final String on; // Hover [✔] in book
  final String off; // Hover [✖] in book
  final String description; // Hover setting in book

  public Component getDescription() {
    return text(description);
  }

  @Override
  public Component displayName() {
    return displayName;
  }

  // Call this to get a component with both the [✔] and [✖] options, only make one clickable.
  @Override
  public Component createButtons(Pair<Guild, GuildRank> pair) {
    var guild = pair.getFirst();
    var rank = pair.getSecond();

    boolean state = rank.hasPermission(perm);

    var cmd = getCmd(rank.getId(), guild);
    var allow = BookSetting.createButton(true, state, cmd, text(on));
    var deny = BookSetting.createButton(false, state, cmd, text(off));

    return text()
        .append(allow, space(), deny)
        .build();
  }

  @Override
  public boolean shouldInclude(Pair<Guild, GuildRank> pair) {
    return true;
  }

  private String getCmd(int rankId, Guild guild) {
    return cmd.replaceAll("%rank", Integer.toString(rankId))
        .replaceAll("%guild", guild.getName())
        .replaceAll("%perm", perm.name());
  }
}