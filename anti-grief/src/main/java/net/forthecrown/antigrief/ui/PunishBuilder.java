package net.forthecrown.antigrief.ui;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.antigrief.PunishEntry;
import net.forthecrown.antigrief.PunishType;
import net.forthecrown.antigrief.Punishments;
import net.forthecrown.grenadier.CommandSource;

@Setter
@Accessors(chain = true)
@RequiredArgsConstructor
public class PunishBuilder {

  final PunishEntry entry;
  final PunishType type;

  String reason;
  String extra;
  Duration length;

  public void punish(CommandSource source) {
    Punishments.handlePunish(entry.getUser(), source, reason, length, type, extra);
  }
}