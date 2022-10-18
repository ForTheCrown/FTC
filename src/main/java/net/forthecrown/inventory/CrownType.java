package net.forthecrown.inventory;

import net.forthecrown.utils.inventory.BaseItemBuilder;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static net.forthecrown.text.Text.nonItalic;
import static net.kyori.adventure.text.Component.text;

public class CrownType implements ExtendedItemType<RoyalCrown> {
    public static final int MODEL_DATA = 1478819153; // hashCode() result for "ForTheCrown"

    /** The Crown's title, the -Crown- */
    public static final Component CROWN_TITLE = text()
            .style(nonItalic(NamedTextColor.GOLD))
            .append(text("-"))
            .append(text("Crown").style(nonItalic(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD)))
            .append(text("-"))
            .build();

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
    public @NotNull BaseItemBuilder createBaseItem() {
        return ItemStacks.builder(Material.GOLDEN_HELMET)
                .setName(CROWN_TITLE)
                .setModelData(MODEL_DATA)
                .setUnbreakable(true);
    }
}