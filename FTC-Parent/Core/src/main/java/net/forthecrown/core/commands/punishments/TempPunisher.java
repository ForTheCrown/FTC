package net.forthecrown.core.commands.punishments;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.grenadier.CommandSource;

public interface TempPunisher extends GenericPunisher{
    int punish(CrownUser user, CommandSource source, long length, String reason) throws CommandSyntaxException;
}
