package net.forthecrown.cosmetics.arrows;

import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.cosmetics.CosmeticConstants;
import net.forthecrown.cosmetics.CosmeticEffect;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.user.CosmeticData;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Particle;

public class ArrowEffect extends CosmeticEffect {

    private final Particle particle;

    ArrowEffect(int slot, Particle particle, String name, Component... description) {
        super(name, InventoryPos.fromSlot(slot), description);

        this.particle = particle;
    }

    ArrowEffect(int slot, Particle particle, String name, String desc){
        this(slot, particle, name, ChatUtils.convertString(desc, true));
    }

    public Particle getParticle() {
        return particle;
    }

    @Override
    public void place(FtcInventory inventory, CrownUser user) {
        CosmeticData data = user.getCosmeticData();

        inventory.setItem(
                getSlot(),

                CosmeticEffect.makeItem(
                        equals(data.getActiveArrow()),
                        data.hasArrow(this),
                        this,
                        CosmeticConstants.ARROW_PRICE
                )
        );
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws RoyalCommandException {
        CosmeticData data = user.getCosmeticData();
        boolean owned = data.hasArrow(this);

        if(owned){
            data.setActiveArrow(this);
            user.sendMessage(Component.translatable("cosmetics.set.arrow", NamedTextColor.YELLOW, name()));
        } else {
            if(user.getGems() < CosmeticConstants.ARROW_PRICE){
                user.sendMessage(Component.translatable("commands.cannotAfford", NamedTextColor.RED, FtcFormatter.queryGems(CosmeticConstants.ARROW_PRICE)));
                return;
            }

            user.setGems(user.getGems() - CosmeticConstants.ARROW_PRICE);
            data.addArrow(this);
            data.setActiveArrow(this);

            user.sendMessage(
                    Component.translatable("cosmetics.bought", NamedTextColor.GRAY, name())
            );
        }

        context.setReloadInventory(true);
    }
}
