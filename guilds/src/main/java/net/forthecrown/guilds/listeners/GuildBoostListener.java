package net.forthecrown.guilds.listeners;

import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild.BoostTier;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Icon;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import github.scarsz.discordsrv.dependencies.jda.api.events.GenericEvent;
import github.scarsz.discordsrv.dependencies.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.EventListener;
import java.io.IOException;
import net.forthecrown.Loggers;
import net.forthecrown.discord.listener.BoostListener;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.guilds.unlockables.DiscordUnlocks;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class GuildBoostListener implements EventListener {

  private static final Logger LOGGER = Loggers.getLogger();

  private final GuildManager manager;

  public GuildBoostListener(GuildManager manager) {
    this.manager = manager;
  }

  @Override
  public void onEvent(@NotNull GenericEvent genericEvent) {
    if (!(genericEvent instanceof GuildMemberUpdateBoostTimeEvent event)) {
      return;
    }

    onBoostEvent(event);
  }

  private void onBoostEvent(GuildMemberUpdateBoostTimeEvent event) {
    if (!BoostListener.isBoosting(event)) {
      return;
    }

    var dcGuild = event.getGuild();
    var boost = dcGuild.getBoostTier();

    if (boost == BoostTier.NONE || boost == BoostTier.TIER_1) {
      return;
    }

    for (Guild guild : manager.getGuilds()) {
      // Check if donator
      if (!DiscordUnlocks.COLOR.isUnlocked(guild)) {
        continue;
      }

      var opt = guild.getDiscord().getRole();

      if (opt.isEmpty()) {
        continue;
      }

      Role role = opt.get();
      Icon icon;

      try {
        icon = guild.getDiscord().getIcon();
      } catch (IOException exc) {
        LOGGER.error("Couldn't load icon for guild {}", guild, exc);
        continue;
      }

      var action = role.getManager();
      action.setIcon(icon);
      action.submit();
    }

  }
}
