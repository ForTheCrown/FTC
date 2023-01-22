package net.forthecrown.inventory.weapon.ability;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.inventory.weapon.RoyalSword;
import net.forthecrown.inventory.weapon.SwordConfig;
import net.forthecrown.utils.text.writer.TextWriter;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.math.GenericMath;

@Getter @Setter
public abstract class WeaponAbility {
  public static final String TAG_LEVEL = "level";
  public static final int START_LEVEL = 1;

  protected int level = START_LEVEL;
  private final WeaponAbilityType type;

  public WeaponAbility(WeaponAbilityType type) {
    this.type = type;
  }

  public Component displayName() {
    return getType().fullDisplayName();
  }

  public void write(TextWriter writer) {
    writer.write(displayName());

    if (level > START_LEVEL) {
      writer.formatted(" {0, number, -roman}", level);
    }
  }

  public abstract long getCooldownTicks();

  protected long scaledCooldown(long baseDuration) {
    float level = getLevel();
    float mod = SwordConfig.swordAbilityCooldownScalar / level;
    return GenericMath.floor(baseDuration * mod);
  }

  /* ----------------------------- CALLBACKS ------------------------------ */

  /**
   * Right-click callback, triggered when the player right-clicks, aka
   * interacts, with a block, entity or air.
   *
   * @param player       The player that right-clicked
   * @param clicked      The right-clicked entity,
   * @param clickedBlock
   * @return True, if the item should be placed on cooldown, false otherwise
   */
  public abstract boolean onRightClick(Player player,
                                       @Nullable Entity clicked,
                                       @Nullable Block clickedBlock
  );

  /**
   * Left-click callback, triggered when the player left-clicks a block, entity
   * or air.
   *
   * @param player       The player that left-clicked
   * @param clicked      The clicked entity, null, if a block or air was
   *                     clicked
   * @param clickedBlock
   * @return True, if the item should be placed on cooldown, false otherwise
   */
  public abstract boolean onLeftClick(Player player,
                                      @Nullable Entity clicked,
                                      @Nullable Block clickedBlock
  );

  /* --------------------------- SERIALIZATION ---------------------------- */

  public void load(CompoundTag tag) {
    setLevel(tag.getInt(TAG_LEVEL));
    loadAdditional(tag);
  }

  public void save(CompoundTag tag) {
    tag.putInt(TAG_LEVEL, getLevel());
    saveAdditional(tag);
  }

  protected abstract void saveAdditional(CompoundTag tag);
  protected abstract void loadAdditional(CompoundTag tag);

  /* -------------------------- UPDATE CALLBACK --------------------------- */

  public void onUpdate(ItemStack item, ItemMeta meta, RoyalSword royalSword) {
  }
}