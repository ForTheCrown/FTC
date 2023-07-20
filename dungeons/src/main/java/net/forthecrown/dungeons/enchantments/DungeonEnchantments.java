package net.forthecrown.dungeons.enchantments;

import static net.forthecrown.enchantment.FtcEnchants.register;

public final class DungeonEnchantments {
  private DungeonEnchantments() {}

  public static final DolphinSwimmer DOLPHIN_SWIMMER = new DolphinSwimmer();
  public static final HealingBlock HEALING_BLOCK = new HealingBlock();
  public static final PoisonCrit POISON_CRIT = new PoisonCrit();
  public static final SoulBound SOUL_BOUND = new SoulBound();
  public static final StrongAim STRONG_AIM = new StrongAim();

  public static void init() {
    register(DOLPHIN_SWIMMER);
    register(HEALING_BLOCK);
    register(POISON_CRIT);
    register(SOUL_BOUND);
    register(STRONG_AIM);
  }
}
