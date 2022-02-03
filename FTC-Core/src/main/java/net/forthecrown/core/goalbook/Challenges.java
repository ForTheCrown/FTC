package net.forthecrown.core.goalbook;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.forthecrown.core.Crown;
import net.forthecrown.dungeons.bosses.DungeonBoss;
import net.forthecrown.events.custom.*;
import net.forthecrown.registry.Registries;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;
import java.util.function.Function;

public final class Challenges {
    private Challenges() {}

    // -----------------------------------
    //          Daily Challenges
    // -----------------------------------
    public static GoalBookChallenge
            LOG_IN = register(
                    "Log in", GoalBook.Category.DAILY, 1, 100,
                    LogInListener::new,
                    Component.text("Log in to FTC :D")
            ),

            KILL_PASSIVES = register(
                    "Kill passive mobs", GoalBook.Category.DAILY, 5, 150,
                    PassiveKillListener::new,
                    Component.text("Kill 5 passive mobs"),
                    Component.text("Mob must not be from a spawner")
            ),

            MINE_ORES = register(
                    "Mine ores", GoalBook.Category.DAILY, 30, 150,
                    OreMineListener::new,
                    Component.text("Mine 30 ores"),
                    Component.text("Blocks must be natural")
            ),

            TRAVEL_TO_5_REGIONS = register(
                    "Travel to 5 regions", GoalBook.Category.DAILY, 5, 150,
                    RegionTravelListener::new,
                    Component.text("Travel to 5 different regions,"),
                    Component.text("Explore around :D")
            );

    // -----------------------------------
    //         Weekly Challenges
    // -----------------------------------
    public static GoalBookChallenge
            SPEND_100K_RHINES = register(
                    "Spend 100,000 Rhines", GoalBook.Category.WEEKLY, 100000, 1000,
                    SpendListener::new,
                    Component.text("Spend 100,000 Rhines on anything")
            ),

            USE_20_SIGN_SHOPS = register(
                    "Purchase from 20 Sign Shops", GoalBook.Category.WEEKLY, 20, 650,
                    SignShopListener::new,
                    Component.text("Use 20 player-owner Sign Shops"),
                    Component.text("Your own shops don't count")
            ),

            BEAT_4_DUNGEON_BOSSES = register(
                    "Kill all the Dungeon Bosses", GoalBook.Category.WEEKLY, 4, 1000,
                    DungeonBossesListener::new,
                    Component.text("Kill all the dungeon bosses :D")
            ),

            RANK_SWORD_UP = register(
                    "Rank your sword up", GoalBook.Category.WEEKLY, 1, 650,
                    SwordRankUpListener::new,
                    Component.text("Rank your sword up once")
            );

    // -----------------------------------
    //         Monthly Challenges
    // -----------------------------------

    public static void init() {
        Registries.GOAL_BOOK.close();

        Crown.logger().info("GoalBook Challenges initialized");
    }

    private static GoalBookChallenge register(GoalBookChallenge c) {
        return Registries.GOAL_BOOK.register(c.key(), c);
    }

    private static GoalBookChallenge register(String name, GoalBook.Category category, int target, int exp,
                                              Function<GoalBookChallenge, GoalBookListener> listenerFactory,
                                              Component... desc) {
        return register(new GoalBookChallenge(name, category, target, exp, listenerFactory, desc));
    }

    // Daily listeners
    private static class PassiveKillListener extends GoalBookListener {
        public PassiveKillListener(GoalBookChallenge challenge) {
            super(challenge);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onEntityDeath(EntityDeathEvent event) {
            LivingEntity entity = event.getEntity();
            if(entity.fromMobSpawner()) return;
            if(!(entity instanceof Animals)) return;

            Player killer = entity.getKiller();
            if(killer == null) return;

            GoalBook.Progress progress = getProgress(killer.getUniqueId());
            progress.increment(challenge);
        }
    }
    private static class OreMineListener extends GoalBookListener {
        public OreMineListener(GoalBookChallenge challenge) {
            super(challenge);
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onBlockBreak(BlockBreakEvent event) {
            if(!event.getBlock().getType().name().contains("ORE")) return;
            if(!FtcUtils.isNaturallyPlaced(event.getBlock())) return;
            Player player = event.getPlayer();
            GoalBook.Progress progress = getProgress(player.getUniqueId());

            progress.increment(challenge);
        }
    }
    private static class LogInListener extends GoalBookListener {
        public LogInListener(GoalBookChallenge challenge) {
            super(challenge);
        }

        @EventHandler(ignoreCancelled = true)
        public void onPlayerJoin(PlayerJoinEvent event) {
            GoalBook.Progress progress = getProgress(event.getPlayer().getUniqueId());
            progress.increment(challenge);
        }
    }
    private static class RegionTravelListener extends GoalBookListener {
        public RegionTravelListener(GoalBookChallenge challenge) {
            super(challenge);
        }

        @EventHandler(ignoreCancelled = true)
        public void onRegionVisit(RegionVisitEvent event) {
            if(event.originIsDestination()) return;
            GoalBook.Progress progress = getProgress(event.getUser().getUniqueId());
            progress.increment(challenge);
        }
    }

    // Weekly listener
    private static class SpendListener extends GoalBookListener {
        public SpendListener(GoalBookChallenge challenge) {
            super(challenge);
        }

        @EventHandler(ignoreCancelled = true)
        public void onBalanceChange(BalanceChangeEvent event) {
            if(event.getAction() != BalanceChangeEvent.Action.REMOVE) return;
            GoalBook.Progress progress = getProgress(event.getBalanceHolder());
            progress.increment(challenge, event.getChangeAmount());
        }
    }
    private static class SignShopListener extends GoalBookListener {
        public SignShopListener(GoalBookChallenge challenge) {
            super(challenge);
        }

        @EventHandler(ignoreCancelled = true)
        public void onSignShopUse(SignShopUseEvent event) {
            if(!event.customerIsPlayer()) return;
            if(event.getSession().customerIsOwner()) return;
            if(event.getShop().getType().isAdmin()) return;

            UUID customer = event.getCustomer().getUniqueId();
            UUID owner = event.getShop().getOwnership().getOwner();

            UserManager manager = Crown.getUserManager();

            // If the customer is an alt for the owner OR
            // if the owner is an olt for the customer,
            // Don't allow
            if(manager.getAlts(customer).contains(owner)
                    || manager.getAlts(owner).contains(customer)
            ) return;

            GoalBook.Progress progress = getProgress(customer);
            progress.increment(challenge);
        }
    }
    private static class DungeonBossesListener extends GoalBookListener {
        public DungeonBossesListener(GoalBookChallenge challenge) {
            super(challenge);
        }

        @EventHandler(ignoreCancelled = true)
        public void onDungeonBossDeath(DungeonBossDeathEvent event) {
            for (Player p: event.getContext().getPlayers()) {
                if(!event.getBoss().getBossRoom().contains(p)) continue;

                run(p, event.getBoss());
            }
        }

        private void run(Player player, DungeonBoss boss) {
            GoalBook.Progress progress = getProgress(player.getUniqueId());
            JsonArray arr = progress.extraData().getArray("beaten_bosses");
            JsonElement bossElement = boss.serialize();

            if(arr.contains(bossElement)) return;
            arr.add(bossElement);
            progress.increment(challenge);
        }
    }
    private static class SwordRankUpListener extends GoalBookListener {
        public SwordRankUpListener(GoalBookChallenge challenge) {
            super(challenge);
        }

        @EventHandler(ignoreCancelled = true)
        public void onSwordRankUp(SwordRankUpEvent event) {
            if(!event.getSword().hasPlayerOwner()) return;

            GoalBook.Progress progress = getProgress(event.getSword().getOwner());
            progress.increment(challenge);
        }
    }

    // Monthly listeners
}