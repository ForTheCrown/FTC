package net.forthecrown.commands.arguments;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.selectors.EntitySelector;
import net.forthecrown.royalgrenadier.types.selector.EntityArgumentImpl;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.ListUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserParseResult {
    private final CrownUser user;
    private final EntitySelector selector;
    private final boolean allowOffline;

    UserParseResult(CrownUser user, boolean allowOffline) {
        this.user = user;
        this.selector = null;
        this.allowOffline = allowOffline;
    }

    UserParseResult(EntitySelector selector, boolean allowOffline){
        this.selector = selector;
        this.user = null;
        this.allowOffline = allowOffline;
    }

    public CrownUser getUser(CommandSource source, boolean checkVanished) throws CommandSyntaxException {
        if(selector == null) return user;
        CrownUser user = UserManager.getUser(selector.getPlayer(source));

        if(!allowOffline() && !checkSourceCanSee(user, source) && checkVanished) throw EntityArgumentImpl.PLAYER_NOT_FOUND.create();
        return user;
    }

    public List<CrownUser> getUsers(CommandSource source, boolean checkVanished) throws CommandSyntaxException {
        if(selector == null) return new ArrayList<>(Collections.singletonList(user));
        List<CrownUser> users = ListUtils.convert(selector.getPlayers(source), UserManager::getUser);

        if(checkVanished) users.removeIf(u -> !checkSourceCanSee(u, source));

        if(users.size() < 1) throw EntityArgumentImpl.NO_ENTITIES_FOUND.create();
        return users;
    }

    public boolean checkSourceCanSee(CrownUser user, CommandSource source){
        if(source.isPlayer() && !source.hasPermission(Permissions.VANISH_SEE)){
            try {
                Player player = source.asPlayer();
                if(!user.isOnline()) return true;

                return player.canSee(user.getPlayer());
            } catch (CommandSyntaxException ignored) {}
        }
        return true;
    }

    public boolean allowOffline(){
        return allowOffline;
    }

    public boolean isSelectorUsed(){
        return selector != null;
    }
}