package net.forthecrown.antigrief.listeners;

import com.google.common.base.Strings;
import java.util.UUID;
import net.forthecrown.antigrief.PunishEntry;
import net.forthecrown.antigrief.PunishType;
import net.forthecrown.antigrief.Punishments;
import net.forthecrown.text.TextWriters;
import net.forthecrown.user.Users;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

class LoginListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
    var msg = testBanned(event.getUniqueId());

    if (msg != null) {
      event.disallow(Result.KICK_BANNED, msg);
    }
  }

  private Component testBanned(UUID userId) {
    var alts = Users.getService();
    var accounts = alts.getOtherAccounts(userId);
    accounts.add(userId);

    var punishments = Punishments.get();

    for (var accountId: accounts) {
      var entry = punishments.getEntry(accountId);

      var msg = banMessage(entry, PunishType.BAN);
      if (msg != null) {
        return msg;
      }

      var ipMsg = banMessage(entry, PunishType.IP_BAN);
      if (ipMsg != null) {
        return ipMsg;
      }
    }

    return null;
  }

  private Component banMessage(PunishEntry entry, PunishType type) {
    var punishment = entry.getCurrent(type);
    if (punishment == null) {
      return null;
    }

    var writer = TextWriters.newWriter();
    writer.line("You are banned!");

    if (!Strings.isNullOrEmpty(punishment.getReason())) {
      writer.line(punishment.getReason());
      writer.newLine();
      writer.newLine();
    }

    if (punishment.getExpires() != null) {
      writer.formattedField("Expires",
          "{0, date} (in {0, time, -timestamp})",
          punishment.getExpires()
      );
    }

    // writer.formattedLine("Source: {0}", punishment.getSource());
    return writer.asComponent();
  }
}