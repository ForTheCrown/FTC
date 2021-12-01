package net.forthecrown.cosmetics;

import com.google.gson.JsonPrimitive;
import net.forthecrown.core.Keys;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.ItemStackBuilder;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.Nameable;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class CosmeticEffect implements JsonSerializable, Keyed, CordedInventoryOption, Nameable {
    protected final String name;
    protected final Component[] description;
    protected final InventoryPos pos;
    protected final Key key;

    public CosmeticEffect(String name, InventoryPos pos, Component... description) {
        this.name = name;
        this.description = description;
        this.pos = pos;

        key = Keys.ftccore(name
                .toLowerCase()
                .replaceAll(" ", "_")
                .replaceAll("'", "")
        );
    }

    public Component[] getDescription() {
        return description;
    }

    public Component displayName() {
        return name().style(FtcFormatter.nonItalic(NamedTextColor.YELLOW));
    }

    @Override
    public InventoryPos getPos() {
        return pos;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    @Override
    public JsonPrimitive serialize() {
        return JsonUtils.writeKey(key());
    }

    @Override
    public String toString() {
        return key().asString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CosmeticEffect effect = (CosmeticEffect) o;

        return key.equals(effect.key);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getName())
                .append(getDescription())
                .append(getPos())
                .append(key())
                .toHashCode();
    }

    public static ItemStack makeItem(boolean active, boolean owned, CosmeticEffect effect, int price) {
        ItemStackBuilder builder = new ItemStackBuilder(owned ? Material.ORANGE_DYE : Material.GRAY_DYE)
                .setName(effect.displayName());

        for (Component c: effect.getDescription()){
            builder.addLore(c.style(FtcFormatter.nonItalic(NamedTextColor.GRAY)));
        }

        builder.addLore(Component.empty());

        if(!owned){
            builder.addLore(
                    Component.text("Click to purchase for ")
                            .style(FtcFormatter.nonItalic(NamedTextColor.GRAY))
                            .append(FtcFormatter.gems(price).style(FtcFormatter.nonItalic(NamedTextColor.GOLD)))
            );
        }

        if(active){
            builder
                    .addEnchant(Enchantment.CHANNELING, 1)
                    .setFlags(ItemFlag.HIDE_ENCHANTS);
        }

        return builder.build();
    }
}
