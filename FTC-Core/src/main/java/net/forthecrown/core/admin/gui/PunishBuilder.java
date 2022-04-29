package net.forthecrown.core.admin.gui;

import lombok.RequiredArgsConstructor;
import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.core.admin.PunishType;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.grenadier.CommandSource;

@RequiredArgsConstructor
class PunishBuilder {
    final PunishEntry entry;
    final PunishType type;

    String reason;
    String extra;
    long length;

    public PunishBuilder setReason(String reason) {
        this.reason = reason;
        return this;
    }

    public PunishBuilder setExtra(String extra) {
        this.extra = extra;
        return this;
    }

    public PunishBuilder setLength(long length) {
        this.length = length;
        return this;
    }

    public void handle(CommandSource source) {
        Punishments.handlePunish(entry.entryUser(), source, reason, length, type, extra);
    }
}