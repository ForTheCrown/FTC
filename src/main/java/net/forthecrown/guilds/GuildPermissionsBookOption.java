package net.forthecrown.guilds;

import com.mojang.datafixers.util.Pair;
import net.forthecrown.utils.book.BookSetting;
import net.kyori.adventure.text.Component;

import static net.kyori.adventure.text.Component.*;

public record GuildPermissionsBookOption(
        GuildPermission perm,          // GuildPermission this option refers to
        String cmd,                    // Command to run when player clicks [✔] or [✖]
        Component displayName,         // setting in book
        String on,                     // Hover [✔] in book
        String off,                    // Hover [✖] in book
        String description             // Hover setting in book
) implements BookSetting<Pair<Guild, GuildRank>> {

    public Component getDescription() {
        return text(description);
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
                .append(
                        allow,
                        space(),
                        deny
                )
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