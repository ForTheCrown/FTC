package net.forthecrown.user.actions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.FtcUtils;

public record DivorceAction(CrownUser user, boolean informUsers) implements UserAction {
    @Override
    public void handle(UserActionHandler handler) {
        try {
            handler.handleDivorce(this);
        } catch (CommandSyntaxException e) {
            FtcUtils.handleSyntaxException(user, e);
        }
    }
}
