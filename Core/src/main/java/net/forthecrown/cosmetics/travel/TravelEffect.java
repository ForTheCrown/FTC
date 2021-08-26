package net.forthecrown.cosmetics.travel;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.cosmetics.CosmeticEffect;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public abstract class TravelEffect implements CosmeticEffect {
    protected final InventoryPos cords;
    protected final Key key;
    protected final String name;
    protected final Component[] description;

    TravelEffect(String name, InventoryPos cords, Component... description) {
        this.cords = cords;
        this.name = name;
        this.description = description;

        key = Crown.coreKey(name.toLowerCase().replaceAll(" ", "_"));
    }

    public abstract void onPoleTeleport(CrownUser user, Location from, Location pole);

    public abstract void onHulkStart(Location loc);
    public abstract void onHulkTick(Location loc);
    public abstract void onHulkLand(Location landing);

    @Override
    public Component[] getDescription() {
        return description;
    }

    @Override
    public InventoryPos getPos() {
        return cords;
    }

    @Override
    public void place(Inventory inventory, CrownUser user) {

    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {

    }

    public Component displayName() {
        return name().style(FtcFormatter.nonItalic(NamedTextColor.YELLOW));
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
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        TravelEffect effect = (TravelEffect) o;

        return new EqualsBuilder()
                .append(cords, effect.cords)
                .append(key, effect.key)
                .append(getName(), effect.getName())
                .append(getDescription(), effect.getDescription())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(cords)
                .append(key)
                .append(getName())
                .append(getDescription())
                .toHashCode();
    }
}
