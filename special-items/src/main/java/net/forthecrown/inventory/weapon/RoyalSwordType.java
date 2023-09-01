package net.forthecrown.inventory.weapon;

import static net.forthecrown.text.Text.nonItalic;

import java.util.UUID;
import net.forthecrown.dungeons.enchantments.DungeonEnchantments;
import net.forthecrown.inventory.ExtendedItemType;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.utils.inventory.ItemBuilder;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.registry.Registries;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RoyalSwordType implements ExtendedItemType<RoyalSword> {

  public static final Component
      RANK_1_NAME = makeName("Traveller's", NamedTextColor.GRAY, NamedTextColor.DARK_GRAY, false),
      RANK_2_NAME = makeName("Squire's", NamedTextColor.YELLOW, NamedTextColor.GRAY, false),
      RANK_3_NAME = makeName("Knight's", NamedTextColor.YELLOW, NamedTextColor.YELLOW, false),
      RANK_4_NAME = makeName("Lord's", NamedTextColor.YELLOW, NamedTextColor.YELLOW, true),
      RANK_5_NAME = makeName("Royal", NamedTextColor.YELLOW, NamedTextColor.GOLD, true),
      RANK_FINAL_NAME = makeName("Dragon's", NamedTextColor.RED, NamedTextColor.DARK_RED, true);

  private static Component makeName(String name,
                                    TextColor nameColor,
                                    TextColor borderColor,
                                    boolean bold
  ) {
    return Component.text()
        .style(nonItalic(nameColor).decoration(TextDecoration.BOLD, bold))
        .append(Component.text("-").color(borderColor))
        .append(Component.text(name + " Sword"))
        .append(Component.text("-").color(borderColor))
        .build();
  }

  public RoyalSwordType() {
    //ConfigManager.get().registerConfig(SwordConfig.class);
  }

  @Pattern(Registries.VALID_KEY_REGEX)
  @Override
  public String getKey() {
    return "royal_weapon";
  }

  @Override
  public @NotNull RoyalSword create(@Nullable UUID owner) {
    return new RoyalSword(this, owner);
  }

  @Override
  public @NotNull RoyalSword load(@NotNull CompoundTag item) {
    return new RoyalSword(this, item);
  }

  @Override
  public void rankUp(ItemStack itemStack, RoyalSword value) {
    value.incrementRank(itemStack);
    value.update(itemStack);
  }

  @Override
  public @NotNull ItemBuilder<?> createBaseItem() {
    return ItemStacks.builder(Material.WOODEN_SWORD)
        .setNameRaw(RANK_1_NAME)
        .setUnbreakable(true)
        .setFlags(ItemFlag.HIDE_ATTRIBUTES)
        .addEnchant(DungeonEnchantments.SOUL_BOUND, 1);
  }
}