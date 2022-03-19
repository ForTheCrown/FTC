package net.forthecrown.core.battlepass.challenges;

import net.forthecrown.core.Crown;
import net.forthecrown.core.battlepass.BattlePass;
import net.forthecrown.registry.Registries;
import net.kyori.adventure.text.Component;

public final class Challenges {
    private Challenges() {}

    // -----------------------------------
    //          Daily Challenges
    // -----------------------------------
    public static SimpleChallenge
            LOG_IN = register(
                    "Log in", BattlePass.Category.DAILY, 1, 100,
                    Component.text("Log in to FTC :D")
            ),

            KILL_PASSIVES = register(
                    "Kill passive mobs", BattlePass.Category.DAILY, 5, 150,
                    Component.text("Kill 5 passive mobs"),
                    Component.text("Mob must not be from a spawner")
            ),

            MINE_ORES = register(
                    "Mine ores", BattlePass.Category.DAILY, 30, 150,
                    Component.text("Mine 30 ores"),
                    Component.text("Blocks must be natural")
            ),

            TRAVEL_TO_5_REGIONS = register(
                    "Travel to 5 regions", BattlePass.Category.DAILY, 5, 150,
                    Component.text("Travel to 5 different regions,"),
                    Component.text("Explore around :D")
            );

    // -----------------------------------
    //         Weekly Challenges
    // -----------------------------------
    public static final DungeonBossChallenge BEAT_4_DUNGEON_BOSSES = register(
            new DungeonBossChallenge(
                    "Kill all the Dungeon Bosses", BattlePass.Category.WEEKLY, 4, 1000,
                    Component.text("Kill 4 dungeon bosses :D")
            )
    );

    public static SimpleChallenge
            SPEND_100K_RHINES = register(
                    "Spend 100,000 Rhines", BattlePass.Category.WEEKLY, 100000, 1000,
                    Component.text("Spend 100,000 Rhines on anything")
            ),

            USE_20_SIGN_SHOPS = register(
                    "Purchase from 20 Sign Shops", BattlePass.Category.WEEKLY, 20, 650,
                    Component.text("Use 20 player-owner Sign Shops"),
                    Component.text("Your own shops don't count")
            ),

            RANK_SWORD_UP = register(
                    "Rank your sword up", BattlePass.Category.WEEKLY, 1, 650,
                    Component.text("Rank your sword up once")
            );

    // -----------------------------------
    //         Monthly Challenges
    // -----------------------------------

    public static void init() {
        Registries.GOAL_BOOK.close();

        Crown.logger().info("GoalBook Challenges initialized");
    }

    private static <T extends BattlePassChallenge> T register(T c) {
        return (T) Registries.GOAL_BOOK.register(c.key(), c);
    }

    private static SimpleChallenge register(String name, BattlePass.Category category, int target, int exp, Component... desc) {
        return register(
                new SimpleChallenge(name, category, target, exp, desc) {
                    @Override
                    protected void onTrigger(BattlePass.Progress progress, int amount) {
                        progress.increment(this, amount);
                    }
                }
        );
    }
}