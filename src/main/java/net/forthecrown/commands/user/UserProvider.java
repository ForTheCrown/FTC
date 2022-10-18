package net.forthecrown.commands.user;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.User;

interface UserProvider {
    User get(CommandContext<CommandSource> c) throws CommandSyntaxException;

    default User getOnline(CommandContext<CommandSource> c) throws CommandSyntaxException {
        var result = get(c);

        if (!result.isOnline()) {
            throw Exceptions.notOnline(result);
        }

        return result;
    }
}