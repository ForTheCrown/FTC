package net.forthecrown.commands.arguments;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Permissions;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;

import java.util.UUID;

public class HomeParseResult {

    private final ImmutableStringReader reader;
    private final UUID user;
    private final String home;

    public HomeParseResult(ImmutableStringReader reader, UUID user, String home) {
        this.reader = reader;
        this.user = user;
        this.home = home;
    }

    public HomeParseResult(ImmutableStringReader reader, String home) {
        this.reader = reader;
        this.home = home;
        this.user = null;
    }

    public Location getHome(CommandSource source, boolean ignorePerms) throws CommandSyntaxException {
        if(user != null){
            if(!ignorePerms && !source.hasPermission(Permissions.HOME_OTHERS)) throw exception();

            CrownUser u = UserManager.getUser(user);
            Location l = u.getHomes().get(home);

            if(l == null) throw exception();
            return l;
        }

        CrownUser sUser = UserManager.getUser(source.asPlayer());

        Location l = sUser.getHomes().get(home);
        if(l == null) throw exception();

        return l;
    }

    private RoyalCommandException exception(){
        return HomeType.UNKNOWN_HOME.createWithContext(reader, Component.text(reader.getRemaining()));
    }

    public String getName() {
        return home;
    }

    public ImmutableStringReader getReader() {
        return reader;
    }

    public UUID getUser() {
        return user;
    }
}
