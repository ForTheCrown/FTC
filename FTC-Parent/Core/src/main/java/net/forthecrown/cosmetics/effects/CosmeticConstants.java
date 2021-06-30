package net.forthecrown.cosmetics.effects;

import net.forthecrown.cosmetics.effects.arrow.ArrowParticleMenu;
import net.forthecrown.cosmetics.effects.arrow.effects.*;
import net.forthecrown.cosmetics.effects.death.DeathParticleMenu;
import net.forthecrown.cosmetics.effects.death.effects.*;
import net.forthecrown.cosmetics.effects.emote.EmoteMenu;
import net.forthecrown.cosmetics.effects.emote.emotes.*;
import net.forthecrown.inventory.CrownItems;
import net.forthecrown.inventory.custom.options.ClickableOption;
import net.forthecrown.user.CrownUser;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;

import java.util.Collection;
import java.util.Set;

//Constants are upper case
public abstract class CosmeticConstants {
    private CosmeticConstants() {}

    private static final int MENU_COOLDOWN = 0;

    // Menu options
    public static ClickableOption getArrowMenu(CrownUser user) {
        ClickableOption option = new ClickableOption();
        option.setCooldown(MENU_COOLDOWN);
        option.setItem(CrownItems.makeItem(Material.BOW, 1, true,
                ChatColor.YELLOW + "Arrow Particle Trails",
                "",
                ChatColor.GRAY + "Upgrade your arrows with fancy particle",
                ChatColor.GRAY + "trails as they fly through the air!"));

        option.setActionOnClick(() -> CosmeticMenu.open(new ArrowParticleMenu(user), user));
        return option;
    }

    public static ClickableOption getDeathMenu(CrownUser user) {
        ClickableOption option = new ClickableOption();
        option.setCooldown(MENU_COOLDOWN);
        option.setItem(CrownItems.makeItem(Material.SKELETON_SKULL, 1, true,
                ChatColor.YELLOW + "Death Particles",
                "",
                ChatColor.GRAY + "Make your deaths more spectacular by",
                ChatColor.GRAY + "exploding into pretty particles!"));

        option.setActionOnClick(() -> CosmeticMenu.open(new DeathParticleMenu(user), user));
        return option;
    }

    public static ClickableOption getEmoteMenu(CrownUser user) {
        ClickableOption option = new ClickableOption();
        option.setCooldown(MENU_COOLDOWN);
        option.setItem(CrownItems.makeItem(Material.TOTEM_OF_UNDYING, 1, true,
                ChatColor.YELLOW + "Emotes",
                "",
                ChatColor.GRAY + "Poking, smooching, bonking and more",
                ChatColor.GRAY + "to interact with your friends."));

        option.setActionOnClick(() -> CosmeticMenu.open(new EmoteMenu(user), user));
        return option;
    }

    // Death Effects
    public static final CosmeticDeathEffect NONE_DEATH = new DeathNone();
    public static final CosmeticDeathEffect DEATH_SOUL = new DeathSoul();
    public static final CosmeticDeathEffect ENDER_RING = new EnderRing();
    public static final CosmeticDeathEffect EXPLOSION = new Explosion();
    public static final CosmeticDeathEffect TOTEM = new Totem();

    private static final Set<CosmeticDeathEffect> DEATH_EFFECTS = Set.of(NONE_DEATH, DEATH_SOUL, ENDER_RING, EXPLOSION, TOTEM);
    public static Collection<CosmeticDeathEffect> getDeathEffects() { return DEATH_EFFECTS; }


    // Arrow Effects
    public static final CosmeticArrowEffect ARROW_NONE = new ArrowNone();
    public static final CosmeticArrowEffect ARROW_SOUL = new ArrowSoul();
    public static final CosmeticArrowEffect CAMPFIRE_COZY_SMOKE = new CampfireCozySmoke();
    public static final CosmeticArrowEffect DAMAGE_INDICATOR = new DamageIndicator();
    public static final CosmeticArrowEffect DRIPPING_HONEY = new DrippingHoney();
    public static final CosmeticArrowEffect FIREWORKS_SPARK = new FireworksSpark();
    public static final CosmeticArrowEffect FLAME = new Flame();
    public static final CosmeticArrowEffect HEART = new Heart();
    public static final CosmeticArrowEffect SNEEZE = new Sneeze();
    public static final CosmeticArrowEffect SNOWBALL = new Snowball();

    private static final Set<CosmeticArrowEffect> ARROW_EFFECTS = Set.of(
            ARROW_NONE, ARROW_SOUL, CAMPFIRE_COZY_SMOKE, DAMAGE_INDICATOR, DRIPPING_HONEY,
            FIREWORKS_SPARK, FLAME, HEART, SNEEZE, SNOWBALL
    );
    public static Collection<CosmeticArrowEffect> getArrowEffects() { return ARROW_EFFECTS; }

    // Emotes
    public static final CosmeticEmoteEffect BONK = new Bonk();
    public static final CosmeticEmoteEffect HUG = new Hug();
    public static final CosmeticEmoteEffect JUG = new Jingle();
    public static final CosmeticEmoteEffect MWAH = new Mwah();
    public static final CosmeticEmoteEffect POKE = new Poke();
    public static final CosmeticEmoteEffect SCARE = new Scare();

    private static final Set<CosmeticEmoteEffect> EMOTES = Set.of(BONK, HUG, JUG, MWAH, POKE, SCARE);
    public static Collection<CosmeticEmoteEffect> getEmotes() { return EMOTES; }
}
