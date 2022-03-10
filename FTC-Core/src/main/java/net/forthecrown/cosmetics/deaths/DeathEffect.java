package net.forthecrown.cosmetics.deaths;

import net.forthecrown.core.FtcVars;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.cosmetics.CosmeticEffect;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.user.CosmeticData;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;

public abstract class DeathEffect extends CosmeticEffect {

    DeathEffect(int slot, String name, Component... description) {
        super(name, InventoryPos.fromSlot(slot), description);
    }

    DeathEffect(int slot, String name, String desc){
        this(slot, name, ChatUtils.convertString(desc, true));
    }

    public abstract void activate(Location loc);

    @Override
    public void place(FtcInventory inventory, CrownUser user) {
        CosmeticData data = user.getCosmeticData();

        inventory.setItem(
                getPos(),

                CosmeticEffect.makeItem(
                        equals(data.getActiveDeath()),
                        data.hasDeath(this),
                        this,
                        FtcVars.effectCost_death.get()
                )
        );
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws RoyalCommandException {
        CosmeticData data = user.getCosmeticData();
        boolean owned = data.hasDeath(this);

        if(owned){
            data.setActiveDeath(this);
            user.sendMessage(Component.translatable("cosmetics.set.death", NamedTextColor.YELLOW, name()));
        } else {
            if(user.getGems() < FtcVars.effectCost_death.get()){
                user.sendMessage(Component.translatable("commands.cannotAfford", NamedTextColor.RED, FtcFormatter.gems(FtcVars.effectCost_death.get())));
                return;
            }

            user.setGems(user.getGems() - FtcVars.effectCost_death.get());
            data.addDeath(this);
            data.setActiveDeath(this);

            user.sendMessage(
                    Component.translatable("cosmetics.bought", NamedTextColor.GRAY, name())
            );
        }

        context.setReloadInventory(true);
    }
}
