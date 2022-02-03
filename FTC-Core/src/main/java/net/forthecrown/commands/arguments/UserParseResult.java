package net.forthecrown.commands.arguments;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.selectors.EntitySelector;
import net.forthecrown.royalgrenadier.types.selector.EntityArgumentImpl;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.ListUtils;

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
        CrownUser result = isSelectorUsed() ? UserManager.getUser(selector.getPlayer(source)) : user;
        assert result != null : "UserParseResult: How the fuck is the user null";

        if(checkVanished && result.isVanished()) {
            if(!allowOffline() && !source.hasPermission(Permissions.VANISH_SEE)) {
                throw UserArgument.USER_NOT_ONLINE.create(result.nickDisplayName());
            }
        }

        return result;
    }

    public List<CrownUser> getUsers(CommandSource source, boolean checkVanished) throws CommandSyntaxException {
        if(selector == null) return new ArrayList<>(Collections.singletonList(user));
        List<CrownUser> users = ListUtils.convert(selector.getPlayers(source), UserManager::getUser);

        if(checkVanished && !source.hasPermission(Permissions.VANISH_SEE)) users.removeIf(CrownUser::isVanished);

        if(users.isEmpty()) throw EntityArgumentImpl.NO_ENTITIES_FOUND.create();
        return users;
    }

    public boolean allowOffline(){
        return allowOffline;
    }

    public boolean isSelectorUsed(){
        return selector != null;
    }
}