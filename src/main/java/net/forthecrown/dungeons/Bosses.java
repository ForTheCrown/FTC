package net.forthecrown.dungeons;

import net.forthecrown.core.config.ConfigManager;
import net.forthecrown.core.module.OnDisable;
import net.forthecrown.core.module.OnEnable;
import net.forthecrown.core.npc.Npcs;
import net.forthecrown.core.Keys;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.dungeons.boss.DrawnedBoss;
import net.forthecrown.dungeons.boss.HideySpideyBoss;
import net.forthecrown.dungeons.boss.KeyedBoss;
import net.forthecrown.dungeons.boss.SimpleBoss;
import net.forthecrown.dungeons.boss.SkalatanBoss;
import net.forthecrown.dungeons.boss.ZhambieBoss;
import net.forthecrown.dungeons.boss.evoker.EvokerBoss;
import net.forthecrown.dungeons.boss.evoker.EvokerConfig;
import net.forthecrown.dungeons.usables.ActionBossInfo;
import net.forthecrown.dungeons.usables.ActionEntranceInfo;
import net.forthecrown.dungeons.usables.ActionGiveArtifact;
import net.forthecrown.dungeons.usables.ActionSpawnBoss;
import net.forthecrown.dungeons.usables.DiegoNPC;
import net.forthecrown.useables.actions.UsageActions;
import org.bukkit.NamespacedKey;

public class Bosses {
  private Bosses() {}

  /**
   * Registry of currently existing dungeon bosses
   *
   * @see net.forthecrown.dungeons.boss.DungeonBoss
   * @see Bosses
   */
  public static final Registry<KeyedBoss> REGISTRY
      = Registries.newFreezable();

  public static final NamespacedKey KEY
      = Keys.royals("bossitem");

  public static final NamespacedKey BOSS_TAG = Keys.royals("boss_tag");

  public static final EvokerBoss EVOKER = register(new EvokerBoss());

  public static final SimpleBoss
      ZHAMBIE = register(new ZhambieBoss()),
      SKALATAN = register(new SkalatanBoss()),
      HIDEY_SPIDEY = register(new HideySpideyBoss()),
      DRAWNED = register(new DrawnedBoss());

  @OnEnable
  static void init() {
    REGISTRY.freeze();

    UsageActions.REGISTRY.register("entrance_info", ActionEntranceInfo.TYPE);
    UsageActions.REGISTRY.register("give_artifact", ActionGiveArtifact.TYPE);
    UsageActions.REGISTRY.register("boss_info", ActionBossInfo.TYPE);
    UsageActions.REGISTRY.register("spawn_boss", ActionSpawnBoss.TYPE);

    Npcs.REGISTRY.register("diego", new DiegoNPC());

    ConfigManager.get()
        .registerConfig(EvokerConfig.class);
  }

  private static <T extends KeyedBoss> T register(T boss) {
    return (T) REGISTRY.register(boss.getKey(), boss).getValue();
  }

  @OnDisable
  public static void shutdown() {
    REGISTRY.forEach(boss -> boss.kill(true));
  }
}