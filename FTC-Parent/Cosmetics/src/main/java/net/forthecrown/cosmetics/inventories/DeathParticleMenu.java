package net.forthecrown.cosmetics.inventories;

import net.forthecrown.core.inventory.CrownItems;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.cosmetics.custominvs.*;
import net.forthecrown.cosmetics.inventories.effects.death.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class DeathParticleMenu extends CustomMenu {

    private static final CosmeticDeathEffect soul = new Soul();
    private static final CosmeticDeathEffect totem = new Totem();
    private static final CosmeticDeathEffect explosion = new Explosion();
    private static final CosmeticDeathEffect enderRing = new EnderRing();
    private static final CosmeticDeathEffect none = new None();

    private static final Set<CosmeticDeathEffect> deathEffects = Set.of(soul, totem, explosion, enderRing, none);
    public static Collection<CosmeticDeathEffect> getDeathEffects() { return deathEffects; }


    private final Map<Integer, Option> deathEffectSlots;

    public DeathParticleMenu(CrownUser user) {
        setUser(user);
        deathEffectSlots = Map.of(
                10, soul.getClickableOption(getUser()),
                11, totem.getClickableOption(getUser()),
                12, explosion.getClickableOption(getUser()),
                13, enderRing.getClickableOption(getUser()),
                31, none.getClickableOption(getUser())
        );

        setInv(buildInventory());
    }


    @Override
    public TextComponent getInventoryTitle() { return Component.text("Death Effects"); }

    @Override
    public int getSize() { return 36; }

    public Option getReturnItem() {
        ClickableOption returnOption = new ClickableOption();
        returnOption.setCooldown(0);
        returnOption.setActionOnClick(() -> {
            // TODO: go back to main cosmetic menu
        });
        returnOption.setItem(CrownItems.makeItem(Material.PAPER, 1, true, ChatColor.YELLOW + "< Go Back"));
        return returnOption;
    }

    @Override
    Inventory buildInventory() {
        CustomInvBuilder invBuilder = new CustomInvBuilder();

        CustomInv result = invBuilder
                .setSize(this.getSize())
                .setTitle(getInventoryTitle())
                .setInvBorder(new GenericBorder())
                .addOptions(deathEffectSlots)
                .addOption(4, getReturnItem())
                .build();

        return result.getInventory();
    }
}
