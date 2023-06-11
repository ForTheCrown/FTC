package net.forthecrown.dungeons;

import net.forthecrown.dungeons.boss.DrawnedBoss;
import net.forthecrown.dungeons.boss.HideySpideyBoss;
import net.forthecrown.dungeons.boss.KeyedBoss;
import net.forthecrown.dungeons.boss.SimpleBoss;
import net.forthecrown.dungeons.boss.SkalatanBoss;
import net.forthecrown.dungeons.boss.ZhambieBoss;
import net.forthecrown.dungeons.boss.evoker.EvokerBoss;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import org.bukkit.NamespacedKey;

public class Bosses {
  private Bosses() {}

  /**
   * Registry of currently existing dungeon bosses
   *
   * @see net.forthecrown.dungeons.boss.DungeonBoss
   * @see Bosses
   */
  public static final Registry<KeyedBoss> REGISTRY = Registries.newFreezable();

  public static final NamespacedKey KEY = DungeonUtils.royalsKey("bossitem");
  public static final NamespacedKey BOSS_TAG = DungeonUtils.royalsKey("boss_tag");

  public static final EvokerBoss EVOKER = register(new EvokerBoss());

  public static final SimpleBoss
      ZHAMBIE = register(new ZhambieBoss()),
      SKALATAN = register(new SkalatanBoss()),
      HIDEY_SPIDEY = register(new HideySpideyBoss()),
      DRAWNED = register(new DrawnedBoss());

  static void init() {
    REGISTRY.freeze();

    //ConfigManager.get().registerConfig(EvokerConfig.class);
  }

  private static <T extends KeyedBoss> T register(T boss) {
    return (T) REGISTRY.register(boss.getKey(), boss).getValue();
  }

  public static void shutdown() {
    REGISTRY.forEach(boss -> boss.kill(true));
  }
}