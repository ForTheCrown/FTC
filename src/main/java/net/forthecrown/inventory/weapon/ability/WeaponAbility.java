package net.forthecrown.inventory.weapon.ability;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.core.config.GeneralConfig;
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

  protected int level;
  private final WeaponAbilityType type;

  public WeaponAbility(WeaponAbilityType type) {
    this.type = type;
  }

  public abstract Component displayName();

  public void write(TextWriter writer) {
    writer.write(displayName());

    if (level > 1) {
      writer.formatted(" {0, number, -roman}", level);
    }
  }

  public abstract int getCooldownTicks();

  protected int scaledCooldown(int baseDuration) {
    float level = getLevel();
    float mod = GeneralConfig.swordAbilityCooldownScalar / level;
    return GenericMath.floor(baseDuration * mod);
  }

  /* ----------------------------- CALLBACKS ------------------------------ */

  public abstract void onRightClick(Player player, @Nullable Entity clicked);

  public abstract void onLeftClick(Player player, @Nullable Entity clicked);

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