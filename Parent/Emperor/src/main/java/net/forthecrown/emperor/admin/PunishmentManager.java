package net.forthecrown.emperor.admin;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.admin.jails.JailManager;
import net.forthecrown.emperor.admin.record.PunishmentRecord;
import net.forthecrown.emperor.admin.record.PunishmentType;
import net.forthecrown.emperor.serializer.CrownSerializer;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.key.Key;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public interface PunishmentManager extends CrownSerializer<CrownCore> {
    MuteStatus checkMute(CommandSender sender);

    PunishmentEntry getEntry(UUID id);

    PunishmentEntry getOrNew(UUID id);

    void setEntry(PunishmentEntry entry);

    boolean isSoftmuted(UUID id);

    boolean isMuted(UUID id);

    MuteStatus checkMuteSilent(UUID id);

    boolean checkJailed(CommandSender id);

    boolean checkBanned(UUID id);

    Collection<PunishmentEntry> getEntries();

    PunishmentRecord punish(UUID id, PunishmentType type, CommandSource punisher, String reason, long length);

    PunishmentRecord punish(UUID id, PunishmentType type, CommandSource source, String reason,  long length, String extra);

    void jail(Key key, Player player);

    void pardon(UUID id, PunishmentType type) throws CommandSyntaxException;

    default JailManager getJailManager(){
        return CrownCore.getJailManager();
    }
}
