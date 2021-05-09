package net.forthecrown.core.commands.brigadier.types;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.utils.ListUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.selectors.EntitySelector;

import java.util.Collections;
import java.util.List;

public class UserParseResult {
    private final CrownUser user;
    private final EntitySelector selector;

    UserParseResult(CrownUser user) {
        this.user = user;
        this.selector = null;
    }

    UserParseResult(EntitySelector selector){
        this.selector = selector;
        this.user = null;
    }

    public CrownUser getUser(CommandSource source) throws CommandSyntaxException {
        if(selector == null) return user;
        return UserManager.getUser(selector.getPlayer(source));
    }

    public List<CrownUser> getUsers(CommandSource source) throws CommandSyntaxException {
        if(selector == null) return Collections.singletonList(user);
        return ListUtils.convertToList(selector.getPlayers(source), UserManager::getUser);
    }

    public boolean isSelectorUsed(){
        return selector != null;
    }
}