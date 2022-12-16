package net.forthecrown.commands.click;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.user.User;

@FunctionalInterface
public interface TextExecutor {
    void execute(User user) throws CommandSyntaxException;
}