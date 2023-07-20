package net.forthecrown.enchantment;

import io.papermc.paper.enchantments.EnchantmentRarity;
import java.util.Set;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class FtcEnchant extends Enchantment {

  @Getter
  private final EnchantHandle handle;
  @Getter
  private final String name;

  private final EnchantmentTarget target;
  private final Set<EquipmentSlot> slots;

  public FtcEnchant(@NotNull NamespacedKey key,
                    String name,
                    EnchantmentTarget type,
                    EquipmentSlot... slots
  ) {
    super(key);

    this.name = name;
    this.target = type;
    this.slots = Set.of(slots);

    handle = new EnchantHandle(this, type, slots);
  }

  public FtcEnchant(@NotNull NamespacedKey key, String name, Enchantment base) {
    this(key, name, base.getItemTarget(), base.getActiveSlots().toArray(new EquipmentSlot[0]));
  }

  @Override
  public int getMaxLevel() {
    return 1;
  }

  @Override
  public int getStartLevel() {
    return 1;
  }

  @Override
  public boolean isTreasure() {
    return false;
  }

  @Override
  public boolean isCursed() {
    return false;
  }

  @Override
  public boolean conflictsWith(@NotNull Enchantment enchantment) {
    return false;
  }

  @Override
  public boolean canEnchantItem(@NotNull ItemStack stack) {
    return true;
  }

  @Override
  public @NotNull Component displayName(int i) {
    return Component.text(getName());
  }

  @Override
  public boolean isDiscoverable() {
    return false;
  }

  @Override
  public boolean isTradeable() {
    return false;
  }

  @Override
  public float getDamageIncrease(int i, @NotNull EntityCategory category) {
    return 0f;
  }

  @Override
  public @NotNull EnchantmentRarity getRarity() {
    return EnchantmentRarity.VERY_RARE;
  }

  @Override
  public @NotNull EnchantmentTarget getItemTarget() {
    return target;
  }

  @Override
  public @NotNull Set<EquipmentSlot> getActiveSlots() {
    return slots;
  }

  @Override
  public @NotNull String translationKey() {
    return null;
  }

  public void onHurt(LivingEntity user, Entity attacker, int level) {
  }

  public void onAttack(LivingEntity user, Entity target, int level) {
  }
}