package net.forthecrown.core.admin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.serializer.CrownSerializer;
import net.kyori.adventure.key.Key;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

/**
 * Manages punishments.
 * <p></p>
 * Implementation: {@link FtcPunishments}
 */
public interface Punishments extends CrownSerializer {
    PunishmentEntry getEntry(UUID id);

    PunishmentEntry getOrNew(UUID id);

    void setEntry(PunishmentEntry entry);

    boolean isSoftmuted(UUID id);
    boolean isMuted(UUID id);

    MuteStatus checkMute(CommandSender sender);
    MuteStatus checkMuteSilent(UUID id);

    boolean checkJailed(CommandSender id);

    Collection<PunishmentEntry> getEntries();

    PunishmentRecord punish(UUID id, PunishmentType type, CommandSource punisher, String reason, long length);

    PunishmentRecord punish(UUID id, PunishmentType type, CommandSource source, String reason,  long length, String extra);

    void jail(Key key, Player player);

    void pardon(UUID id, PunishmentType type) throws CommandSyntaxException;
}
