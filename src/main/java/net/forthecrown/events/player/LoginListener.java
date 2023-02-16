package net.forthecrown.events.player;

import static net.forthecrown.core.admin.Punishments.INDEFINITE_EXPIRY;

import com.google.common.base.Strings;
import java.util.Objects;
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
    PunishEntry entry = Punishments.get().getEntry(event.getUniqueId());

    var ipBan = banMessage(entry, PunishType.IP_BAN);
    if (ipBan != null) {
      event.disallow(Result.KICK_BANNED, ipBan);
    }

    var ban = banMessage(entry, PunishType.BAN);
    if (ban != null) {
      event.disallow(Result.KICK_BANNED, ban);
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