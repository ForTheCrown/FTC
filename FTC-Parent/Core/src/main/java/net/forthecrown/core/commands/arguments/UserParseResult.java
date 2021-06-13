package net.forthecrown.core.commands.arguments;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.core.user.UserManager;
import net.forthecrown.core.utils.ListUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.selectors.EntitySelector;
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

    public CrownUser getUser(CommandSource source) throws CommandSyntaxException {
        if(selector == null) return user;
        return UserManager.getUser(selector.getPlayer(source));
    }

    public List<CrownUser> getUsers(CommandSource source) throws CommandSyntaxException {
        if(selector == null) return new ArrayList<>(Collections.singletonList(user));
        return ListUtils.convertToList(selector.getPlayers(source), UserManager::getUser);
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