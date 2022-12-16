package net.forthecrown.guilds;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.user.User;
import net.forthecrown.utils.book.BookBuilder;

import static net.kyori.adventure.text.Component.text;

public class GuildPermissionsBook {
    private GuildPermissionsBook() {}

    private static final ObjectList<GuildPermissionsBookOption> PERMISSIONS = new ObjectArrayList<>();

    static void addPermission(GuildPermissionsBookOption perm) {
        PERMISSIONS.add(perm);
    }

    public static void open(User user, Guild guild, GuildRank rank) {
        Pair<Guild, GuildRank> pair = Pair.of(guild, rank);

        user.openBook(
                BookBuilder.createSettings(pair, text("Rank Settings"), PERMISSIONS)
        );
    }
}