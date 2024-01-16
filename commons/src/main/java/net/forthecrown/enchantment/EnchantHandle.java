package net.forthecrown.enchantment;

import io.papermc.paper.adventure.PaperAdventure;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.bukkit.craftbukkit.v1_20_R3.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class EnchantHandle extends Enchantment {

  private final FtcEnchant wrapper;

  protected EnchantHandle(
      FtcEnchant wrapper,
      EnchantmentTarget type,
      EquipmentSlot... slotTypes
  ) {
    super(Rarity.VERY_RARE, fromTarget(type), fromBukkitSlots(slotTypes));
    this.wrapper = wrapper;
  }

  static net.minecraft.world.entity.EquipmentSlot[] fromBukkitSlots(EquipmentSlot[] slots) {
    net.minecraft.world.entity.EquipmentSlot[] result
        = new net.minecraft.world.entity.EquipmentSlot[slots.length];

    for (int i = 0; i < slots.length; i++) {
      result[i] = CraftEquipmentSlot.getNMS(slots[i]);
    }

    return result;
  }

  static EnchantmentCategory fromTarget(EnchantmentTarget target) {
    return switch (target) {
      case BOW -> EnchantmentCategory.BOW;
      case TOOL -> EnchantmentCategory.DIGGER;
      case ARMOR -> EnchantmentCategory.ARMOR;
      case WEAPON -> EnchantmentCategory.WEAPON;
      case TRIDENT -> EnchantmentCategory.TRIDENT;
      case CROSSBOW -> EnchantmentCategory.CROSSBOW;
      case WEARABLE -> EnchantmentCategory.WEARABLE;
      case BREAKABLE -> EnchantmentCategory.BREAKABLE;
      case ARMOR_FEET -> EnchantmentCategory.ARMOR_FEET;
      case ARMOR_HEAD -> EnchantmentCategory.ARMOR_HEAD;
      case ARMOR_LEGS -> EnchantmentCategory.ARMOR_LEGS;
      case ARMOR_TORSO -> EnchantmentCategory.ARMOR_CHEST;
      case VANISHABLE -> EnchantmentCategory.VANISHABLE;
      case FISHING_ROD -> EnchantmentCategory.FISHING_ROD;
      default -> EnchantmentCategory.DIGGER;
    };
  }

  @Override
  public Component getFullname(int level) {
    return PaperAdventure.asVanilla(wrapper.displayName(level));
  }

  @Override
  public int getMinLevel() {
    return wrapper.getStartLevel();
  }

  @Override
  public int getMaxLevel() {
    return wrapper.getMaxLevel();
  }

  @Override
  public boolean isTreasureOnly() {
    return wrapper.isTreasure();
  }

  @Override
  public boolean isTradeable() {
    return wrapper.isTradeable();
  }

  @Override
  public boolean isDiscoverable() {
    return wrapper.isDiscoverable();
  }

  @Override
  public boolean isCurse() {
    return wrapper.isCursed();
  }

  @Override
  public boolean canEnchant(@NotNull ItemStack itemstack) {
    return wrapper.canEnchantItem(CraftItemStack.asBukkitCopy(itemstack));
  }

  @Override
  public void doPostAttack(LivingEntity user, Entity target, int level) {
    wrapper.onAttack(
        user.getBukkitLivingEntity(),
        target.getBukkitEntity(),
        level
    );
  }

  @Override
  public void doPostHurt(LivingEntity user, @Nullable Entity attacker, int level) {
    wrapper.onHurt(
        user.getBukkitLivingEntity(),
        attacker == null ? null : attacker.getBukkitEntity(),
        level
    );
  }
}