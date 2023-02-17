package net.forthecrown.events.guilds;

import static net.forthecrown.events.Events.register;

public class GuildEvents {
  public static void registerAll() {
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
    register(new WhitelistListener());
  }
}