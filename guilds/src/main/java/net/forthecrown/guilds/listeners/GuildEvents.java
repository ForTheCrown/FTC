package net.forthecrown.guilds.listeners;

import static net.forthecrown.events.Events.register;

import github.scarsz.discordsrv.DiscordSRV;
import net.forthecrown.guilds.GuildPlugin;

public class GuildEvents {

  public static void registerAll(GuildPlugin plugin) {
    var manager = plugin.getManager();

    register(new DayChangeListener(manager));
    register(new GuildChatListener());
    register(new GuildDeathListener());
    register(new GuildDurabilityListener());
    register(new GuildEntityDeathListener());
    register(new GuildFallListener());
    register(new GuildFoodChangeListener());
    register(new GuildMoveListener());
    register(new GuildPearlThrowListener());
    register(new GuildSlimeSpawnListener());
    register(new PlayerLeashVillagerListener());
    register(new PotionEffectListener());
    register(new ServerLoadListener(manager));
    register(new SignListener(manager));
    register(new WhitelistListener(manager));

    var api = DiscordSRV.api;
    api.subscribe(new GuildDiscordListener());
  }
}