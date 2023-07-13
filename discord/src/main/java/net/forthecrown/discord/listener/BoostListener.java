package net.forthecrown.discord.listener;

import github.scarsz.discordsrv.dependencies.jda.api.events.GenericEvent;
import github.scarsz.discordsrv.dependencies.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.EventListener;
import java.util.UUID;
import net.forthecrown.Loggers;
import net.forthecrown.discord.FtcDiscord;
import net.forthecrown.titles.UserRanks;
import net.forthecrown.titles.UserTitles;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.PluginUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class BoostListener implements EventListener {

  public static final Logger LOGGER = Loggers.getLogger();

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

    if (PluginUtil.isEnabled("FTC-UserTitles")) {
      UserTitles titles = user.getComponent(UserTitles.class);

      UserRanks.REGISTRY.get("booster").ifPresent(rank -> {
        if (boosting) {
          titles.addTitle(rank);
        } else {
          titles.removeTitle(rank);
        }
      });
    }

    if (boosting) {
      user.setPermission("ftc.emotes.pog");

      LOGGER.info(
          "{} began boosting the server, gave pog command and booster title",
          user
      );
    } else {
      user.unsetPermission("ftc.emotes.pog");

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
