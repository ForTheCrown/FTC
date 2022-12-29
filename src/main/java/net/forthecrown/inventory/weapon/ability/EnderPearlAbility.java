package net.forthecrown.inventory.weapon.ability;

import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class EnderPearlAbility extends WeaponAbility {
  public static final int BASE_COOLDOWN = 3 * 20; // 3 seconds

  public EnderPearlAbility(WeaponAbilityType type) {
    super(type);
  }

  @Override
  public Component displayName() {
    return Component.text("Ender pearl");
  }

  @Override
  public void onRightClick(Player player, @Nullable Entity clicked) {
    if (clicked != null) {
      return;
    }

    player.getWorld().spawn(player.getEyeLocation(), EnderPearl.class, pearl -> {
      pearl.setShooter(player);
      pearl.setHasLeftShooter(false);

      pearl.setVelocity(
          player.getEyeLocation().getDirection()
      );
    });
  }

  @Override
  public int getCooldownTicks() {
    return scaledCooldown(BASE_COOLDOWN);
  }

  @Override
  public void onLeftClick(Player player, @Nullable Entity clicked) {
    // Do nothing
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {}

  @Override
  protected void loadAdditional(CompoundTag tag) {}
}