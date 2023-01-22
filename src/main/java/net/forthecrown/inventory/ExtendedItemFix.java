package net.forthecrown.inventory;

import static net.forthecrown.inventory.ExtendedItem.TAG_OWNER;
import static net.forthecrown.inventory.ExtendedItems.CROWN;
import static net.forthecrown.inventory.ExtendedItems.TAG_CONTAINER;
import static net.forthecrown.inventory.ExtendedItems.TAG_DATA;
import static net.forthecrown.inventory.ExtendedItems.TAG_TYPE;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.inventory.weapon.RoyalSword;
import net.forthecrown.utils.inventory.ItemStacks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import org.bukkit.inventory.meta.ItemMeta;

class ExtendedItemFix {

  private static final String SWORD_TAG = ExtendedItems.ROYAL_SWORD.getKey();

  private static final String
      OLD_TAG_GOALS = "goals",
      OLD_TAG_RANK = "rank",
      OLD_TAG_FLAVOR = "lastFluffChange";

  private static final ImmutableMap<String, String> GOAL_RENAMES = ImmutableMap.<String, String>builder()
      .put("forthecrown:goal_r1_any", "entity/any")

      .put("forthecrown:goal_r2_skeleton", "entity/skeleton")
      .put("forthecrown:goal_r2_zombie", "entity/zombie")
      .put("forthecrown:goal_r2_creeper", "entity/creeper")
      .put("forthecrown:goal_r2_spider", "entity/spider")

      .put("forthecrown:goal_r3_blaze", "entity/blaze")
      .put("forthecrown:goal_r3_magma_cube", "entity/magma_cube")
      .put("forthecrown:goal_r3_wither_skeleton", "entity/wither_skeleton")
      .put("forthecrown:goal_r3_piglin", "entity/piglin")
      .put("forthecrown:goal_r3_enderman", "entity/enderman")

      .put("forthecrown:goal_r4_dboss_skalatan", "boss/skalatan")
      .put("forthecrown:goal_r4_dboss_zhambie", "boss/zhambie")
      .put("forthecrown:goal_r4_dboss_hidey_spidey", "boss/hidey_spidey")
      .put("forthecrown:goal_r4_any", "entity/any")

      .put("forthecrown:goal_r5_snowman", "entity/snowman")

      .put("forthecrown:goal_r6_donator", "entity/donator")
      .put("forthecrown:goal_r6_ghast", "entity/ghast")

      .put("forthecrown:goal_r7_charged_creeper", "charged_creeper")

      .put("forthecrown:goal_r8_wither", "entity/wither")

      .put("forthecrown:goal_r9_house_reforge", "entity/ender_dragon")

      .put("forthecrown:goal_r11_any", "entity/any")

      .put("forthecrown:goal_r12_wandering_trader", "entity/wandering_trader")

      .put("forthecrown:goal_r13_damage", "dealt_damage")
      .put("forthecrown:goal_r13_boss_ender_dragon", "entity/ender_dragon")
      .put("forthecrown:goal_r13_boss_wither", "entity/wither")
      .put("forthecrown:goal_r13_vindicator", "entity/vindicator")
      .put("forthecrown:goal_r13_ravager", "entity/ravager")
      .put("forthecrown:goal_r13_evoker", "entity/evoker")
      .put("forthecrown:goal_r13_pillager", "entity/pillager")
      .put("forthecrown:goal_r13_witch", "entity/witch")

      .put("forthecrown:goal_r14_boss_ender_dragon", "entity/ender_dragon")
      .put("forthecrown:goal_r14_boss_wither", "entity/wither")
      .put("forthecrown:goal_r14_damage", "dealt_damage")

      .build();

  public static void fixSword(ItemMeta meta) {
    CompoundTag oldTag = ItemStacks.getTagElement(meta, SWORD_TAG).copy();
    ItemStacks.removeTagElement(meta, SWORD_TAG);

    CompoundTag resultTag = new CompoundTag();

    if (oldTag.contains(OLD_TAG_GOALS)) {
      CompoundTag newGoals = new CompoundTag();

      for (var e : oldTag.getCompound(OLD_TAG_GOALS).tags.entrySet()) {
        String key = e.getKey().replaceAll("ftccore:", "forthecrown:");
        int value = ((IntTag) e.getValue()).getAsInt();

        if (key.contains("r9")) {
          value = 10;
        }

        if (key.contains("_boss_ender_dragon")) {
          key = "entity/ender_dragon";
        } else if (key.contains("_boss_wither")) {
          key = "entity/wither";
        } else if (key.endsWith("_damage")) {
          key = "dealt_damage";
        } else {
          key = GOAL_RENAMES.get(key);

          if (Strings.isNullOrEmpty(key)) {
            Loggers.getLogger().error("Unknown goal: '{}', cannot update", e.getKey());
            continue;
          }
        }

        newGoals.putInt(key, value);
      }

      resultTag.put(RoyalSword.TAG_GOALS, newGoals);
    }

    resultTag.putInt(RoyalSword.TAG_LAST_FLAVOR, oldTag.getInt(OLD_TAG_FLAVOR) - 1);
    resultTag.putInt(RoyalSword.TAG_RANK, oldTag.getInt(OLD_TAG_RANK) - 1);
    resultTag.putUUID(TAG_OWNER, oldTag.getUUID(TAG_OWNER));

    moveToContainer(meta, SWORD_TAG, resultTag);
  }

  public static void fixCrown(ItemMeta meta) {
    moveToContainer(meta, CROWN.getKey(), ItemStacks.getTagElement(meta, CROWN.getKey()));
  }

  private static void moveToContainer(ItemMeta meta, String key, CompoundTag tag) {
    CompoundTag topTag = new CompoundTag();
    topTag.putString(TAG_TYPE, key);
    topTag.put(TAG_DATA, tag);

    CompoundTag unhandled = ItemStacks.getUnhandledTags(meta);
    unhandled.put(TAG_CONTAINER, topTag);
    unhandled.remove(key);

    ItemStacks.setUnhandledTags(meta, unhandled);
  }
}