package net.forthecrown.guilds.listeners;

import static net.forthecrown.events.Events.register;

import net.forthecrown.guilds.GuildPlugin;

public class GuildEvents {

  public static void registerAll(GuildPlugin plugin) {
    var manager = plugin.getManager();

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
    register(new GuildChatListener());

    register(new WhitelistListener(manager));
    register(new DayChangeListener(manager));
    register(new SignListener(manager));
  }
}