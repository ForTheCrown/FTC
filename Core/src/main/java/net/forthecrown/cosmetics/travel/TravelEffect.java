package net.forthecrown.cosmetics.travel;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.cosmetics.CosmeticConstants;
import net.forthecrown.cosmetics.CosmeticEffect;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.user.CosmeticData;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

public abstract class TravelEffect extends CosmeticEffect {
    TravelEffect(String name, InventoryPos cords, Component... description) {
        super(name, cords, description);
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
    public void place(Inventory inventory, CrownUser user) {
        CosmeticData data = user.getCosmeticData();

        inventory.setItem(
                getSlot(),

                CosmeticEffect.makeItem(
                        equals(data.getActiveTravel()),
                        data.hasTravel(this),
                        this,
                        CosmeticConstants.TRAVEL_PRICE
                )
        );
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
        CosmeticData data = user.getCosmeticData();
        boolean owned = data.hasTravel(this);

        if(owned){
            data.setActiveTravel(this);
            user.sendMessage(Component.translatable("user.travelEffect.set", NamedTextColor.YELLOW, name()));
        } else {
            if(user.getGems() < CosmeticConstants.DEATH_PRICE){
                user.sendMessage(Component.translatable("commands.cannotAfford", NamedTextColor.RED, FtcFormatter.queryGems(CosmeticConstants.DEATH_PRICE)));
                return;
            }

            user.setGems(user.getGems() - CosmeticConstants.DEATH_PRICE);
            data.addTravel(this);
            data.setActiveTravel(this);

            user.sendMessage(
                    Component.translatable("user.particle.bought", NamedTextColor.GRAY, name())
            );
        }

        context.setReloadInventory(true);
    }
}