package net.forthecrown.commands.arguments;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.User;
import net.forthecrown.user.UserLookupEntry;
import net.forthecrown.user.Users;
import net.forthecrown.user.data.UserHomes;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;

import static net.forthecrown.commands.manager.Commands.EMPTY_READER;

@Getter
public class HomeParseResult {
    public static final HomeParseResult DEFAULT = new HomeParseResult(EMPTY_READER, UserHomes.DEFAULT);

    private final ImmutableStringReader reader;
    private final UserLookupEntry user;
    private final String name;
    private final boolean defaultHome;

    public HomeParseResult(ImmutableStringReader reader, UserLookupEntry user, String name) {
        this.reader = reader;
        this.user = user;
        this.name = name;
        this.defaultHome = UserHomes.DEFAULT.equals(name);
    }

    public HomeParseResult(ImmutableStringReader reader, String name) {
        this(reader, null, name);
    }

    public Pair<String, Location> getHome(CommandSource source, boolean ignorePerms) throws CommandSyntaxException {
        if (user != null) {
            if (!ignorePerms && !source.hasPermission(Permissions.HOME_OTHERS)) {
                throw exception();
            }

            User u = Users.get(user);
            Location l = u.getHomes().get(name);

            if (l == null) {
                throw exception();
            }

            return Pair.of(name, l);
        }

        User sUser = Users.get(source.asPlayer());
        Location l = sUser.getHomes().get(name);

        if (l == null) {
            if (isDefaultHome()) {
                throw Exceptions.NO_DEF_HOME;
            } else {
                throw exception();
            }
        }

        return Pair.of(name, l);
    }

    private CommandSyntaxException exception() {
        return Exceptions.unknownHome(reader, reader.getRemaining());
    }
}