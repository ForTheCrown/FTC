package net.forthecrown.inventory.weapon.ability;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.inventory.weapon.SwordConfig;
import net.forthecrown.utils.text.writer.TextWriter;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
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

  public abstract Component displayName();

  public void write(TextWriter writer) {
    writer.write(displayName());

    if (level > START_LEVEL) {
      writer.formatted(" {0, number, -roman}", level);
    }
  }

  public abstract int getCooldownTicks();

  protected int scaledCooldown(int baseDuration) {
    float level = getLevel();
    float mod = SwordConfig.swordAbilityCooldownScalar / level;
    return GenericMath.floor(baseDuration * mod);
  }

  /* ----------------------------- CALLBACKS ------------------------------ */

  /**
   * Right-click callback, triggered when the player right-clicks, aka
   * interacts, with a block, entity or air.
   *
   * @param player The player that right-clicked
   * @param clicked The right-clicked entity,
   *
   * @return True, if the item should be placed on cooldown, false otherwise
   */
  public abstract boolean onRightClick(Player player, @Nullable Entity clicked);

  /**
   * Left-click callback, triggered when the player left-clicks a block, entity
   * or air.
   *
   * @param player The player that left-clicked
   * @param clicked The clicked entity, null, if a block or air was clicked
   *
   * @return True, if the item should be placed on cooldown, false otherwise
   */
  public abstract boolean onLeftClick(Player player, @Nullable Entity clicked);

  /* --------------------------- SERIALIZATION ---------------------------- */

  public void load(CompoundTag tag) {
    setLevel(tag.getInt(TAG_LEVEL));
    loadAdditional(tag);
  }

  public void save(CompoundTag tag) {
    tag.putInt(TAG_LEVEL, level);
    saveAdditional(tag);
  }

  protected abstract void saveAdditional(CompoundTag tag);
  protected abstract void loadAdditional(CompoundTag tag);
}