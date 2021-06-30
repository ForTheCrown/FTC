package net.forthecrown.cosmetics.effects;

import net.forthecrown.core.inventory.CrownItems;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.cosmetics.custominvs.options.ClickableOption;
import net.forthecrown.cosmetics.effects.arrow.effects.*;
import net.forthecrown.cosmetics.effects.death.effects.*;
import net.forthecrown.cosmetics.effects.emote.emotes.*;
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
    public static final CosmeticDeathEffect deathNone = new DeathNone();
    public static final CosmeticDeathEffect deathSoul = new DeathSoul();
    public static final CosmeticDeathEffect enderRing = new EnderRing();
    public static final CosmeticDeathEffect explosion = new Explosion();
    public static final CosmeticDeathEffect totem = new Totem();

    private static final Set<CosmeticDeathEffect> deathEffects = Set.of(deathNone, deathSoul, enderRing, explosion, totem);
    public static Collection<CosmeticDeathEffect> getDeathEffects() { return deathEffects; }


    // Arrow Effects
    public static final CosmeticArrowEffect arrowNone = new ArrowNone();
    public static final CosmeticArrowEffect arrowSoul = new ArrowSoul();
    public static final CosmeticArrowEffect campfireCozySmoke = new CampfireCozySmoke();
    public static final CosmeticArrowEffect damageIndicator = new DamageIndicator();
    public static final CosmeticArrowEffect drippingHoney = new DrippingHoney();
    public static final CosmeticArrowEffect fireworkSpark = new FireworksSpark();
    public static final CosmeticArrowEffect flame = new Flame();
    public static final CosmeticArrowEffect heart = new Heart();
    public static final CosmeticArrowEffect sneeze = new Sneeze();
    public static final CosmeticArrowEffect snowball = new Snowball();

    private static final Set<CosmeticArrowEffect> arrowEffects = Set.of(
            arrowNone, arrowSoul, campfireCozySmoke, damageIndicator, drippingHoney,
            fireworkSpark, flame, heart, sneeze, snowball);
    public static Collection<CosmeticArrowEffect> getArrowEffects() { return arrowEffects; }

    // Emotes
    public static final CosmeticEmoteEffect bonk = new Bonk();
    public static final CosmeticEmoteEffect hug = new Hug();
    public static final CosmeticEmoteEffect jingle = new Jingle();
    public static final CosmeticEmoteEffect mwah = new Mwah();
    public static final CosmeticEmoteEffect poke = new Poke();
    public static final CosmeticEmoteEffect scare = new Scare();

    private static final Set<CosmeticEmoteEffect> emotes = Set.of(bonk, hug, jingle, mwah, poke, scare);
    public static Collection<CosmeticEmoteEffect> getEmotes() { return emotes; }
}
