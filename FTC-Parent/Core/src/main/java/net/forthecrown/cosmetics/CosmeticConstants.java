package net.forthecrown.cosmetics;

import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.cosmetics.arrows.ArrowEffects;
import net.forthecrown.cosmetics.deaths.DeathEffects;
import net.forthecrown.cosmetics.emotes.CosmeticEmotes;
import net.forthecrown.cosmetics.options.*;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.inventory.builder.options.InventoryBorder;
import net.forthecrown.inventory.builder.InventoryBuilder;
import net.forthecrown.inventory.builder.options.SimpleOption;
import net.forthecrown.utils.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

//Constants are upper case
public abstract class CosmeticConstants {
    private CosmeticConstants() {}

    //private static final int MENU_COOLDOWN = 0;

    public static final int ARROW_PRICE = 1000;
    public static final int DEATH_PRICE = 2000;

    public static final CosmeticHeader HEADER = new CosmeticHeader();
    public static final RidingToggleOption RIDING_TOGGLE = new RidingToggleOption();
    public static final EmoteToggleOption EMOTE_TOGGLE = new EmoteToggleOption();

    public static final NoDeathOption NO_DEATH = new NoDeathOption();
    public static final NoArrowOption NO_ARROW = new NoArrowOption();

    public static InventoryBuilder baseInventory(int size, Component title, boolean goBack){
        InventoryBuilder builder = new InventoryBuilder(size, title)
                .add(new InventoryBorder());

        if(goBack) builder.add(GO_BACK);

        return builder;
    }

    public static final BuiltInventory MAIN = baseInventory(54, Component.text("Cosmetics"), false)
            .add(RIDING_TOGGLE)
            .add(HEADER)
            .add(20,
                    new ItemStackBuilder(Material.BOW)
                            .setName(Component.text("Arrow Particle Trails").style(FtcFormatter.nonItalic(NamedTextColor.GOLD)))
                            .addLore(Component.empty())
                            .addLore(Component.text("Upgrade your arrows with fancy particle").style(FtcFormatter.nonItalic(NamedTextColor.GRAY)))
                            .addLore(Component.text("trails as they fly through the air!").style(FtcFormatter.nonItalic(NamedTextColor.GRAY)))
                            .build(),
                    (user, context) -> ArrowEffects.getInventory().open(user)
            )
            .add(22,
                    new ItemStackBuilder(Material.TOTEM_OF_UNDYING)
                            .setName(Component.text("Emotes").style(FtcFormatter.nonItalic(NamedTextColor.GOLD)))
                            .addLore(Component.empty())
                            .addLore(Component.text("Poking, smooching, bonking and more").style(FtcFormatter.nonItalic(NamedTextColor.GRAY)))
                            .addLore(Component.text("to interact with friends!").style(FtcFormatter.nonItalic(NamedTextColor.GRAY)))
                            .build(),
                    (user, context) -> CosmeticEmotes.getInventory().open(user)
            )
            .add(24,
                    new ItemStackBuilder(Material.SKELETON_SKULL)
                            .setName(Component.text("Death Particles").style(FtcFormatter.nonItalic(NamedTextColor.GOLD)))
                            .addLore(Component.empty())
                            .addLore(Component.text("Make your death more spectacular by").style(FtcFormatter.nonItalic(NamedTextColor.GRAY)))
                            .addLore(Component.text("exploding into pretty particles!").style(FtcFormatter.nonItalic(NamedTextColor.GRAY)))
                            .build(),
                    (user, context) -> DeathEffects.getInventory().open(user)
            )
            .build();

    public static final SimpleOption GO_BACK = new SimpleOption(
            4,
            new ItemStackBuilder(Material.PAPER, 1)
                    .setName(Component.text("< Go back").style(FtcFormatter.nonItalic(NamedTextColor.YELLOW)))
                    .addLore(Component.text("Back to the main menu").style(FtcFormatter.nonItalic(NamedTextColor.GRAY)))
                    .build(),
            (user, context) -> MAIN.open(user)
    );


    /* I'm sorry, I'm an asshole

    I regret it, I threw aside all your code because I didn't want to take 20 mins to understand the structure of it
    I'm sorry

    If you see this message and want to take revenge on me, tell me, I'll destroy all the above code and
    implement your version of it.

    I've destroyed bigger things so dw lol, my friendship with you being a great example




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
    public static Collection<CosmeticEmoteEffect> getEmotes() { return EMOTES; }*/
}
