package net.forthecrown.inventory.weapon;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.inventory.FtcItems;
import net.forthecrown.inventory.weapon.abilities.WeaponAbilities;
import net.forthecrown.inventory.weapon.goals.ChargedCreeperGoal;
import net.forthecrown.inventory.weapon.goals.DonatorWeaponGoal;
import net.forthecrown.inventory.weapon.goals.WeaponGoal;
import net.forthecrown.inventory.weapon.upgrades.*;
import net.forthecrown.registry.Registries;
import net.forthecrown.utils.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;
import static net.forthecrown.inventory.weapon.goals.WeaponGoal.*;
import static net.forthecrown.utils.FtcUtils.safeRunnable;

public final class RoyalWeapons {
    public static final Component
            RANK_1_NAME     = makeName("Traveller's",   NamedTextColor.GRAY,    NamedTextColor.DARK_GRAY,   false),
            RANK_2_NAME     = makeName("Squire's",      NamedTextColor.YELLOW,  NamedTextColor.GRAY,        false),
            RANK_3_NAME     = makeName("Knight's",      NamedTextColor.YELLOW,  NamedTextColor.YELLOW,      false),
            RANK_4_NAME     = makeName("Lord's",        NamedTextColor.YELLOW,  NamedTextColor.YELLOW,      true),
            RANK_5_NAME     = makeName("Royal",         NamedTextColor.YELLOW,  NamedTextColor.GOLD,        true),
            RANK_FINAL_NAME = makeName("Dragon's",      NamedTextColor.RED,     NamedTextColor.DARK_RED,    true);

    public static final int MAX_RANK = 30;
    public static int DONATOR_RANK;
    public static final String TAG_KEY = "royal_weapon";

    private RoyalWeapons() {}

    private static final Int2ObjectMap<CachedUpgrades> UPGRADES = new Int2ObjectOpenHashMap<>();

    public static void init() {
        int rank = 1;

        //Traveller
        putUpgrade(rank, ReforgeUpgrade.reforge(
                Material.WOODEN_SWORD,
                RANK_1_NAME,
                Component.text("Why are you even seeing this???"),
                "The sword of an aspiring", "adventurer"
        ));
        putUpgrade(rank, new ModifierUpgrade(0, 1));

        register(anyEntity(100, rank));

        //Squire
        putUpgrade(++rank, ReforgeUpgrade.reforge(
                Material.STONE_SWORD,
                RANK_2_NAME,
                Component.text("Stone"),
                "Forged from grand rock",
                "it carries the hero on"
        ));

        register(simple(EntityType.ZOMBIE, 100, rank));
        register(simple(EntityType.CREEPER, 50, rank));
        register(simple(EntityType.SKELETON, 150, rank));
        register(simple(EntityType.SPIDER, 75, rank));

        //Knight
        putUpgrade(++rank, ReforgeUpgrade.reforge(
                Material.IRON_SWORD,
                RANK_3_NAME,
                Component.text("Iron"),
                "The magnificent, unbreaking sword",
                "of a true hero"
        ));
        putUpgrade(rank, new ModifierUpgrade(0.5, 1));

        register(simple(EntityType.BLAZE, 200, rank));
        register(simple(EntityType.WITHER_SKELETON, 200, rank));
        register(simple(EntityType.MAGMA_CUBE, 200, rank));
        register(simple(EntityType.PIGLIN, 200, rank));
        register(simple(EntityType.ENDERMAN, 200, rank));

        //Lord
        putUpgrade(++rank, ReforgeUpgrade.reforge(
                Material.DIAMOND_SWORD,
                RANK_4_NAME,
                Component.text("Diamond"),
                "The shining beauty of",
                "diamonds blinds all enemies"
        ));
        putUpgrade(rank, new ModifierUpgrade(1, 2));

        register(dungeonBoss(Bosses.zhambie(), 1, rank));
        register(dungeonBoss(Bosses.skalatan(), 1, rank));
        register(dungeonBoss(Bosses.hideySpidey(), 1, rank));
        register(anyEntity(500, rank));

        //Royal
        putUpgrade(++rank, ReforgeUpgrade.reforge(
                Material.GOLDEN_SWORD,
                RANK_5_NAME,
                Component.text("Gold"),
                "The bearer of this weapon has",
                "proven themselves to the Crown..."
        ));
        putUpgrade(rank, new EnchantUpgrade(Enchantment.LOOT_BONUS_MOBS, 4));
        putUpgrade(rank, new ModifierUpgrade(((double) rank) / 10, rank));

        register(simple(EntityType.SNOWMAN, 100, rank));
        register(simple(EntityType.GHAST, 200, ++rank));
        register(new DonatorWeaponGoal(rank));
        DONATOR_RANK = rank;
        register(new ChargedCreeperGoal(25, ++rank));
        register(simple(EntityType.WITHER, 10, ++rank));

        //Dragon

        //Rank it up so sword can't simply
        //be leveled up with kills to dragon
        ++rank;

        putUpgrade(++rank, ReforgeUpgrade.reforge(
                Material.NETHERITE_SWORD,
                RANK_FINAL_NAME,
                Component.text("Netherite"),
                "The bearer of this weapon has",
                "proven themselves to the Crown..."
        ));

        Double2DoubleFunction
                speed   = i -> (i / 10) + 0.8,   // Speed = rank / 10 + 0.8
                attack  = i -> (i / 5) + 2;      // attack = rank / 5 + 2

        putUpgrade(rank, new EnchantUpgrade(Enchantment.LOOT_BONUS_MOBS, 5));
        putUpgrade(rank, new ModifierUpgrade(speed.get(rank), attack.get(rank)));

        register(anyEntity(2500, ++rank));
        putUpgrade(rank, new ModifierUpgrade(speed.get(rank), attack.get(rank)));

        register(simple(EntityType.WANDERING_TRADER, 20, ++rank));
        putUpgrade(rank, new ModifierUpgrade(speed.get(rank), attack.get(rank)));

        ++rank;
        register(simple(EntityType.RAVAGER, 50, rank));
        register(simple(EntityType.EVOKER, 50, rank));
        register(simple(EntityType.WITCH, 50, rank));
        register(simple(EntityType.PILLAGER, 250, rank));
        register(simple(EntityType.VINDICATOR, 250, rank));
        putUpgrade(rank, new ModifierUpgrade(speed.get(rank), attack.get(rank)));

        //Endless dragon stuf
        for (int i = rank; i < MAX_RANK; i++) {
            int endGoal = (int) (((double) i) * 1.25D);

            register(endBoss(EntityType.ENDER_DRAGON, endGoal, i));
            register(endBoss(EntityType.WITHER, endGoal, i));
            register(damage((int) (1000 * i * 1.25D), i));

            if(i != rank) {
                putUpgrade(i, new ModifierUpgrade(speed.get(i), attack.get(i)));
                putUpgrade(i, EndBossUpgrade.endBoss(i));
            }
        }

        safeRunnable(WeaponAbilities::init);
        Registries.WEAPON_GOALS.close();
    }

    private static void putUpgrade(int rank, WeaponUpgrade upgrade) {
        CachedUpgrades upgrades = UPGRADES.computeIfAbsent(rank, integer -> new CachedUpgrades());
        upgrades.add(upgrade);
    }

    private static void register(WeaponGoal goal) {
        Registries.WEAPON_GOALS.register(goal.key(), goal);
    }

    /**
     * Gets all the goals the given rank needs to beat to rank up
     * @param rank The rank
     * @return All goals that rank has to beat
     */
    public static List<WeaponGoal> getGoalsAtRank(int rank) {
        List<WeaponGoal> result = new ObjectArrayList<>();

        for (WeaponGoal g: Registries.WEAPON_GOALS) {
            if(g.getRank() == rank) result.add(g);
        }

        return result;
    }

    /**
     * Gets the upgrade the given level will recieve
     * @param rank The rank
     * @return The upgrade recieved when getting to that level, null, if no reward for the given level.
     */
    public static CachedUpgrades getUpgrades(int rank) {
        return UPGRADES.get(rank);
    }

    /**
     * Makes a royal sword
     * @param owner The owner of the sword
     * @return The Sword itself
     */
    public static ItemStack make(UUID owner) {
        ItemStackBuilder builder = new ItemStackBuilder(Material.GOLDEN_SWORD, 1)
                .setName(RANK_1_NAME)
                .setFlags(ItemFlag.HIDE_ATTRIBUTES)
                .setUnbreakable(true);

        ItemStack result = builder.build();
        RoyalSword sword = new RoyalSword(owner, result);

        sword.setRank(0);
        sword.setNextUpgrades(getUpgrades(1));
        sword.incrementGoal();

        sword.update();
        return result;
    }

    /**
     * Checks if the given item is a royal sword
     * @param item The item to check
     * @return True if the item is not null or empty and has the {@link RoyalWeapons#TAG_KEY} tag
     */
    public static boolean isRoyalSword(ItemStack item) {
        if(FtcItems.isEmpty(item)) return false;
        return FtcItems.hasTagElement(item.getItemMeta(), TAG_KEY) && item.getType().name().contains("SWORD");
    }

    private static Component makeName(String name, TextColor nameColor, TextColor borderColor, boolean bold) {
        return Component.text()
                .style(nonItalic(nameColor).decoration(TextDecoration.BOLD, bold))
                .append(Component.text("-").color(borderColor))
                .append(Component.text(name + " Sword"))
                .append(Component.text("-").color(borderColor))
                .build();
    }
}
