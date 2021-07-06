package net.forthecrown.cosmetics;

import com.google.gson.JsonElement;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.options.InventoryOption;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.Nameable;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public interface CosmeticEffect extends JsonSerializable, Nameable, Keyed, InventoryOption {
    Component[] getDescription();

    @Override
    int getSlot();

    @Override
    void place(Inventory inventory, CrownUser user);

    @Override
    void onClick(CrownUser user, ClickContext context) throws RoyalCommandException;

    @Override
    JsonElement serialize();

    @Override
    String getName();

    @Override
    @NotNull Key key();
}
