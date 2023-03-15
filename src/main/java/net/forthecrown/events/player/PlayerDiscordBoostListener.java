package net.forthecrown.events.player;

import github.scarsz.discordsrv.dependencies.jda.api.events.GenericEvent;
import github.scarsz.discordsrv.dependencies.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.EventListener;
import java.util.UUID;
import net.forthecrown.core.FtcDiscord;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.data.UserRanks;
import net.forthecrown.utils.Util;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class PlayerDiscordBoostListener implements EventListener {

  private static final Logger LOGGER = Loggers.getLogger();

  @Override
  public void onEvent(@NotNull GenericEvent genericEvent) {
    if (!(genericEvent instanceof GuildMemberUpdateBoostTimeEvent event)) {
      return;
    }

    onBoostEvent(event);
  }

  private void onBoostEvent(GuildMemberUpdateBoostTimeEvent event) {
    boolean boosting = isBoosting(event);
    UUID uuid = FtcDiscord.getPlayerId(event.getMember());

    if (uuid == null) {
      LOGGER.warn(
          "Cannot give/take ingame booster perks from member {}",
          event.getMember().getEffectiveName()
      );

      return;
    }

    User user = Users.get(uuid);

    UserRanks.REGISTRY.get("booster").ifPresent(rank -> {
      if (boosting) {
        user.getTitles().addTitle(rank);
      } else {
        user.getTitles().removeTitle(rank);
      }
    });

    if (boosting) {
      Util.consoleCommand("lp user %s permission set ftc.emotes.pog",
          user.getName()
      );

      LOGGER.info(
          "{} began boosting the server, gave pog command and booster title",
          user
      );
    } else {
      Util.consoleCommand("lp user %s permission unset ftc.emotes.pog",
          user.getName()
      );

      LOGGER.info(
          "{} stopped boosting the server, "
              + "removed pog command and booster title",
          user
      );
    }
  }

  private boolean isBoosting(GuildMemberUpdateBoostTimeEvent event) {
    if (event.getOldTimeBoosted() == null) {
      return true;
    }

    if (event.getNewTimeBoosted() == null) {
      return false;
    }

    return event.getOldTimeBoosted().isBefore(event.getNewTimeBoosted());
  }
}