package net.forthecrown.core.admin.ui;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.core.admin.PunishType;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.grenadier.CommandSource;

@Setter
@Accessors(chain = true)
@RequiredArgsConstructor
public class PunishBuilder {
    final PunishEntry entry;
    final PunishType type;

    String reason;
    String extra;
    long length;

    public void punish(CommandSource source) {
        Punishments.handlePunish(entry.getUser(), source, reason, length, type, extra);
    }
}