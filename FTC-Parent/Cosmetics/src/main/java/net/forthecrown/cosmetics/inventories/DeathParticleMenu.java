package net.forthecrown.cosmetics.inventories;

import net.forthecrown.core.inventory.CrownItems;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.cosmetics.custominvs.CustomInvBuilder;
import net.forthecrown.cosmetics.inventories.effects.death.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Map;

public class DeathParticleMenu extends CustomMenu {

    private static final Map<Integer, CosmeticDeathEffect> deathEffectSlots = Map.of(
            10, new Soul(),
            11, new Totem(),
            12, new Explosion(),
            13, new EnderRing(),
            31, new None()
    );

    public static Collection<CosmeticDeathEffect> getDeathEffects() { return deathEffectSlots.values(); }

    public DeathParticleMenu(CrownUser user) {
        setUser(user);
        setInv(makeInventory());
    }

    @Override
    TextComponent getInventoryTitle() { return Component.text("Death Effects"); }

    @Override
    int getSize() { return 36; }

    @Override
    public ItemStack getReturnItem() {
        return CrownItems.makeItem(Material.PAPER, 1, true, ChatColor.YELLOW + "< Go Back");
    }

    @Override
    Inventory makeInventory() {
        CustomInvBuilder invBuilder = new CustomInvBuilder();

        /*Inventory result = getBaseInventory();

        for(Map.Entry<Integer, CosmeticDeathEffect> e : deathEffectSlots.entrySet()) {
            CosmeticDeathEffect effect = e.getValue();
            ItemStack effectItem = effect.getEffectItem();

            if (effect.isOwnedBy(getUser())) effect.setItemOwned(effectItem);
            if (effect.isCurrentActiveEffect(getUser())) effect.addGlow(effectItem);
            result.setItem(e.getKey(), effectItem);
        }

        return result;*/
    }




}
