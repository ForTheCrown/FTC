package net.forthecrown.commands.click;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.user.CrownUser;

public interface TextExecutor {
    void execute(CrownUser user) throws CommandSyntaxException;
}
