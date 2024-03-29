package net.forthecrown.inventory;

import static net.forthecrown.text.Text.nonItalic;
import static net.kyori.adventure.text.Component.text;

import java.util.UUID;
import net.forthecrown.dungeons.enchantments.DungeonEnchantments;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.registry.Registries;
import net.forthecrown.utils.inventory.ItemBuilder;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrownType implements ExtendedItemType<RoyalCrown> {

  public static final int MODEL_DATA = 1478819153; // hashCode() result for "ForTheCrown"

  /**
   * The Crown's title, the -Crown-
   */
  public static final Component CROWN_TITLE = text()
      .style(nonItalic(NamedTextColor.GOLD))
      .append(text("-"))
      .append(text("Crown").style(nonItalic(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD)))
      .append(text("-"))
      .build();

  @Pattern(Registries.VALID_KEY_REGEX)
  @Override
  public String getKey() {
    return "royal_crown";
  }

  @Override
  public @NotNull RoyalCrown create(@Nullable UUID owner) {
    return new RoyalCrown(this, owner);
  }

  @Override
  public @NotNull RoyalCrown load(@NotNull CompoundTag item) {
    return new RoyalCrown(this, item);
  }

  @Override
  public void rankUp(ItemStack itemStack, RoyalCrown value) {
    value.upgrade(itemStack);
  }

  @Override
  public @NotNull ItemBuilder<?> createBaseItem() {
    return ItemStacks.builder(Material.GOLDEN_HELMET)
        .setNameRaw(CROWN_TITLE)
        .setModelData(MODEL_DATA)
        .setUnbreakable(true)
        .addEnchant(DungeonEnchantments.SOUL_BOUND, 1);
  }
}