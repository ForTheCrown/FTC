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
  public boolean onRightClick(Player player, @Nullable Entity clicked) {
    if (clicked != null) {
      return false;
    }

    var projectile = player.launchProjectile(
        EnderPearl.class,
        player.getLocation()
            .getDirection()
            .multiply(getLevel())
    );

    player.setInvisible(true);

    // If something like WorldGuard cancels the projectile launch event that's
    // triggered by the above code, the projectile will be removed, if it is
    // removed, then don't put the player on cooldown
    return !projectile.isDead();
  }

  @Override
  public int getCooldownTicks() {
    return scaledCooldown(BASE_COOLDOWN);
  }

  @Override
  public boolean onLeftClick(Player player, @Nullable Entity clicked) {
    // Do nothing
    return false;
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {}

  @Override
  protected void loadAdditional(CompoundTag tag) {}
}