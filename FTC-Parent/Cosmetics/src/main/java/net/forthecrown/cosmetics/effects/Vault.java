package net.forthecrown.cosmetics.effects;

import net.forthecrown.core.inventory.CrownItems;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.cosmetics.custominvs.options.ClickableOption;
import net.forthecrown.cosmetics.effects.death.effects.*;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;

import java.util.Collection;
import java.util.Set;

public abstract class Vault {

    private static final int menuCooldown = 0;

    // Menu options
    public static ClickableOption getArrowMenu(CrownUser user) {
        ClickableOption option = new ClickableOption();
        option.setCooldown(menuCooldown);
        option.setActionOnClick(() -> {
            // TODO: navigate to arrowMenu
        });
        option.setItem(CrownItems.makeItem(Material.BOW, 1, true,
                ChatColor.YELLOW + "Arrow Particle Trails",
                "",
                ChatColor.GRAY + "Upgrade your arrows with fancy particle",
                ChatColor.GRAY + "trails as they fly through the air!"));
        return option;
    }

    public static ClickableOption getDeathMenu(CrownUser user) {
        ClickableOption option = new ClickableOption();
        option.setCooldown(menuCooldown);
        option.setActionOnClick(() -> {
            // TODO: navigate to deathMenu
        });
        option.setItem(CrownItems.makeItem(Material.SKELETON_SKULL, 1, true,
                ChatColor.YELLOW + "Death Particles",
                "",
                ChatColor.GRAY + "Make your deaths more spectacular by",
                ChatColor.GRAY + "exploding into pretty particles!"));
        return option;
    }

    public static ClickableOption getEmoteMenu(CrownUser user) {
        ClickableOption option = new ClickableOption();
        option.setCooldown(menuCooldown);
        option.setActionOnClick(() -> {
            // TODO: navigate to emoteMenu
        });
        option.setItem(CrownItems.makeItem(Material.TOTEM_OF_UNDYING, 1, true,
                ChatColor.YELLOW + "Emotes",
                "",
                ChatColor.GRAY + "Poking, smooching, bonking and more",
                ChatColor.GRAY + "to interact with your friends."));
        return option;
    }


    // Death Effects
    public static final CosmeticDeathEffect soul = new Soul();
    public static final CosmeticDeathEffect totem = new Totem();
    public static final CosmeticDeathEffect explosion = new Explosion();
    public static final CosmeticDeathEffect enderRing = new EnderRing();
    public static final CosmeticDeathEffect none = new None();

    private static final Set<CosmeticDeathEffect> deathEffects = Set.of(soul, totem, explosion, enderRing, none);
    public static Collection<CosmeticDeathEffect> getDeathEffects() { return deathEffects; }

    // Arrow Effects

    // Emotes
}
