package net.forthecrown.inventory.weapon;

import net.forthecrown.dungeons.Bosses;
import net.forthecrown.inventory.weapon.goals.*;
import net.forthecrown.inventory.weapon.upgrades.EnchantUpgrade;
import net.forthecrown.inventory.weapon.upgrades.EndBossUpgrade;
import net.forthecrown.inventory.weapon.upgrades.ModifierUpgrade;
import net.forthecrown.inventory.weapon.upgrades.ReforgeUpgrade;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;

public class SwordRanks {
    public static final int DONATOR_RANK = 6;
    public static final int MAX_RANK = 30;

    static final SwordRank[] RANKS = createRanks();

    static void init() {}

    private static SwordRank[] createRanks() {
        SwordRank.Builder[] ranks = new SwordRank.Builder[MAX_RANK];

        // --- RANK  1 ---
        SwordRank.builder(1)
                .addUpgrade(
                        ReforgeUpgrade.reforge(
                                Material.WOODEN_SWORD,
                                RoyalSwordType.RANK_1_NAME,
                                Component.empty(),
                                "The sword of an aspiring",
                                "adventurer"
                        )
                )
                .addUpgrade(new ModifierUpgrade(0, 1))

                .addGoal(new EntityGoal(100, null))

                .register(ranks);

        // --- RANK  2 ---
        SwordRank.builder(2)
                .addUpgrade(
                        ReforgeUpgrade.reforge(
                                Material.STONE_SWORD,
                                RoyalSwordType.RANK_2_NAME,
                                Component.text("Stone upgrade"),
                                "Forged from grand rock",
                                "it carries the hero onwards"
                        )
                )
                // Removed bal upgrade: 10_000 rhines
                // Removed gem upgrade: 500 gems
                .addGoal(new EntityGoal(100, EntityType.ZOMBIE))
                .addGoal(new EntityGoal(50, EntityType.CREEPER))
                .addGoal(new EntityGoal(150, EntityType.SKELETON))
                .addGoal(new EntityGoal(75, EntityType.SPIDER))

                .register(ranks);

        // --- RANK  3 ---
        SwordRank.builder(3)
                .addUpgrade(
                        ReforgeUpgrade.reforge(
                                Material.IRON_SWORD,
                                RoyalSwordType.RANK_3_NAME,
                                Component.text("Iron upgrade"),
                                "The magnificent, unbreaking sword",
                                "of a true hero"
                        )
                )
                .addUpgrade(new ModifierUpgrade(0.5, 1))
                // Removed bal upgrade: 15_000 rhines

                .addGoal(new EntityGoal(200, EntityType.BLAZE))
                .addGoal(new EntityGoal(200, EntityType.WITHER_SKELETON))
                .addGoal(new EntityGoal(200, EntityType.MAGMA_CUBE))
                .addGoal(new EntityGoal(200, EntityType.PIGLIN))
                .addGoal(new EntityGoal(200, EntityType.ENDERMAN))

                .register(ranks);

        // --- RANK  4 ---
        SwordRank.builder(4)
                .addUpgrade(
                        ReforgeUpgrade.reforge(
                                Material.DIAMOND_SWORD,
                                RoyalSwordType.RANK_4_NAME,
                                Component.text("Diamond upgrade"),
                                "The shining beauty of",
                                "diamonds blinds all enemies"
                        )
                )
                .addUpgrade(new ModifierUpgrade(1, 2))
                // Removed bal upgrade: 12_500 rhines
                // Removed gem upgrade: 1000 gems

                .addGoal(new DungeonBossGoal(Bosses.ZHAMBIE))
                .addGoal(new DungeonBossGoal(Bosses.SKALATAN))
                .addGoal(new DungeonBossGoal(Bosses.HIDEY_SPIDEY))
                .addGoal(new EntityGoal(500, null))

                .register(ranks);

        // --- RANK  5 ---
        SwordRank.builder(5)
                .addUpgrade(
                        ReforgeUpgrade.reforge(
                                Material.GOLDEN_SWORD,
                                RoyalSwordType.RANK_5_NAME,
                                Component.text("Gold upgrade"),
                                "The bearer of this weapon has",
                                "proven themselves to the Crown..."
                        )
                )
                .addUpgrade(new EnchantUpgrade(Enchantment.LOOT_BONUS_MOBS, 4))
                .addUpgrade(new ModifierUpgrade(0.5D, 5))
                // Removed bal upgrade: 10_000 rhines
                // Removed gem upgrade: 1000 gems

                .addGoal(new EntityGoal(100, EntityType.SNOWMAN))

                .register(ranks);

        // --- RANK  6 ---
        SwordRank.builder(DONATOR_RANK)
                .addGoal(new EntityGoal(200, EntityType.GHAST))
                .addGoal(new DonatorWeaponGoal())
                .register(ranks);

        // --- RANK  7 ---
        SwordRank.builder(7)
                .addGoal(new ChargedCreeperGoal(25))
                .register(ranks);

        // --- RANK  8 ---
        SwordRank.builder(8)
                .addGoal(new EntityGoal(10, EntityType.WITHER))
                .register(ranks);

        // --- RANK  9 ---
        SwordRank.builder(9)
                .addGoal(new EntityGoal(10, EntityType.ENDER_DRAGON))
                .register(ranks);

        ModifierFunction
                speed   = i -> (i / 10) + 0.8,   // Speed = (rank / 10) + 0.8
                attack  = i -> (i / 5) + 2;      // attack = (rank / 5) + 2

        // --- RANK 10 ---
        SwordRank.builder(10)
                .addUpgrade(
                        ReforgeUpgrade.reforge(
                                Material.NETHERITE_SWORD,
                                RoyalSwordType.RANK_FINAL_NAME,
                                Component.text("Netherite upgrade"),
                                "The bearer of this weapon has",
                                "proven themselves to the Crown..."
                        )
                )
                .addGoal(new EntityGoal(10, EntityType.WARDEN))

                .addUpgrade(new EnchantUpgrade(Enchantment.LOOT_BONUS_MOBS, 5))
                .addUpgrade(new ModifierUpgrade(speed.apply(10), attack.apply(10)))

                // Removed bal upgrade: 15_000 rhines
                // Removed gem upgrade: 1000 gems

                .register(ranks);

        // --- RANK 11 ---
        SwordRank.builder(11)
                .addUpgrade(new ModifierUpgrade(speed.apply(11), attack.apply(11)))
                // Removed bal upgrade: 25_000 rhines
                // Removed gem upgrade: 1000 gems

                .addGoal(new EntityGoal(25_000, null))
                .register(ranks);

        // --- RANK 12 ---
        SwordRank.builder(12)
                .addUpgrade(new ModifierUpgrade(speed.apply(12), attack.apply(12)))
                // Removed bal upgrade: 35_000 rhines
                // Removed gem upgrade: 2000 gems

                .addGoal(new EntityGoal(20, EntityType.WANDERING_TRADER))
                .register(ranks);

        // --- RANK 13 ---
        SwordRank.builder(13)
                .addUpgrade(new ModifierUpgrade(speed.apply(13), attack.apply(13)))
                // Removed bal upgrade: 100_000 rhines
                // Removed gem upgrade: 2000 gems

                .addGoal(new EntityGoal( 50, EntityType.RAVAGER))
                .addGoal(new EntityGoal( 50, EntityType.EVOKER))
                .addGoal(new EntityGoal( 50, EntityType.WITHER))
                .addGoal(new EntityGoal(250, EntityType.PILLAGER))
                .addGoal(new EntityGoal(250, EntityType.VINDICATOR))

                .register(ranks);

        // --- RANK 14 AND ONWARDS ---

        for (int i = 14; i <= MAX_RANK; i++) {
            int endGoal = (int) (((double) i) * 1.25D);

            SwordRank.builder(i)
                    .addUpgrade(new ModifierUpgrade(speed.apply(i), attack.apply(i)))
                    .addUpgrade(EndBossUpgrade.endBoss(i))
                    // Removed bal upgrade: i * 5000 rhines
                    // Removed gem upgrade: i * 100 gems

                    .addGoal(new EntityGoal(endGoal, EntityType.ENDER_DRAGON))
                    .addGoal(new EntityGoal(endGoal, EntityType.WITHER))
                    .addGoal(new DamageGoal((int) (1000D * i * 1.25D)))

                    .register(ranks);
        }

        return build(ranks);
    }

    private static SwordRank[] build(SwordRank.Builder[] ranks) {
        SwordRank[] result = new SwordRank[ranks.length];

        for (int i = 0; i < ranks.length; i++) {
            SwordRank rank = new SwordRank(ranks[i]);

            if (i != 0) {
                SwordRank previous = result[i - 1];
                previous.next = rank;
                rank.previous = previous;
            }

            result[i] = rank;
        }

        return result;
    }

    interface ModifierFunction {
        double apply(double rank);
    }
}