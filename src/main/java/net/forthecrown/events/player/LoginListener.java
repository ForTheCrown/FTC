package net.forthecrown.events.player;

import static net.forthecrown.core.admin.Punishments.INDEFINITE_EXPIRY;

import com.google.common.base.Strings;
import java.util.Objects;
import java.util.UUID;
import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.core.admin.PunishType;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

public class LoginListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
    var msg = testBanned(event.getUniqueId());

    if (msg != null) {
      event.disallow(Result.KICK_BANNED, msg);
    }

    if (!GeneralConfig.enforceAltRule) {
      return;
    }

    var alts = UserManager.get().getAlts();
    var alreadyOnlineOpt = alts.getOtherAccounts(event.getUniqueId())
        .stream()
        .map(Bukkit::getPlayer)
        .filter(Objects::nonNull)
        .findAny();

    if (alreadyOnlineOpt.isEmpty()) {
      return;
    }

    event.disallow(
        Result.KICK_OTHER,
        Text.format(
            "Your other account ({0, user}) is already online",
            alreadyOnlineOpt.get()
        )
    );
  }

  private Component testBanned(UUID userId) {
    var alts = UserManager.get().getAlts();
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

    if (punishment.getExpires() != INDEFINITE_EXPIRY) {
      writer.formattedLine("Expires: {0, date}", punishment.getExpires());
    }

    // writer.formattedLine("Source: {0}", punishment.getSource());
    return writer.asComponent();
  }
}