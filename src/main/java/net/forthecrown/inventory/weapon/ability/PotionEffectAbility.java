package net.forthecrown.inventory.weapon.ability;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class PotionEffectAbility extends WeaponAbility {
  public static final String
      TAG_DURATION = "duration",
      TAG_EFFECT = "effectType";

  private final PotionEffectType effectType;
  private int duration;

  public PotionEffectAbility(WeaponAbilityType type,
                             PotionEffectType effectType
  ) {
    super(type);
    this.effectType = effectType;
  }

  @Override
  public Component displayName() {
    return Component.translatable(effectType);
  }

  @Override
  public boolean onRightClick(Player player, @Nullable Entity clicked) {
    int time = getDuration();
    PotionEffect effect = new PotionEffect(
        effectType, time, level - 1, false, true, true
    );

    player.addPotionEffect(effect);
    return true;
  }

  @Override
  public boolean onLeftClick(Player player, @Nullable Entity clicked) {
    return false;
  }

  @Override
  public int getCooldownTicks() {
    return scaledCooldown(getDuration());
  }

  /* --------------------------- SERIALIZATION ---------------------------- */

  @Override
  protected void saveAdditional(CompoundTag tag) {
    tag.putInt(TAG_DURATION, getDuration());
  }

  @Override
  protected void loadAdditional(CompoundTag tag) {
    setDuration(tag.getInt(TAG_DURATION));
  }
}