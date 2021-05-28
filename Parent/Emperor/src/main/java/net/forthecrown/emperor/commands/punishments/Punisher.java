package net.forthecrown.emperor.commands.punishments;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.grenadier.CommandSource;

public interface Punisher extends GenericPunisher {
    int punish(CrownUser user, CommandSource source, String reason) throws CommandSyntaxException;
}
