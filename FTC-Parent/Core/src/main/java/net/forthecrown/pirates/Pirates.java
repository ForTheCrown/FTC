package net.forthecrown.pirates;

import net.forthecrown.core.CrownCore;
import net.forthecrown.crownevents.ObjectiveLeaderboard;
import net.forthecrown.economy.pirates.CrownPirateEconomy;
import net.forthecrown.economy.pirates.PirateEconomy;
import net.forthecrown.pirates.grappling.GrapplingHookParkour;
import net.forthecrown.squire.Squire;
import net.forthecrown.utils.Worlds;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;

public class Pirates {
    public static final NamespacedKey SHULKER_KEY = Squire.createPiratesKey("treasure_shulker");
    public static final NamespacedKey GH_STAND_KEY = Squire.createPiratesKey("level_end_stand");
    public static final NamespacedKey BM_MERCHANT = Squire.createPiratesKey("black_market_merchant");

    static TreasureShulker shulker;
    static GrapplingHookParkour ghParkour;
    static CrownPirateEconomy pirateEconomy;
    static ParrotTracker parrotTracker;
    static AuctionManager auctionManager;

    static ObjectiveLeaderboard leaderboard;

    public static void init(){
        pirateEconomy = new CrownPirateEconomy();
        shulker = new TreasureShulker();
        ghParkour = new GrapplingHookParkour();
        auctionManager = new AuctionManager();
        parrotTracker = new ParrotTracker();

        auctionManager.loadAuctions();

        initLeaderboard();

        CrownCore.logger().info("Pirates loaded");
    }

    private static void initLeaderboard(){
        leaderboard = new ObjectiveLeaderboard(
                Bukkit.getScoreboardManager().getMainScoreboard().getObjective("PiratePoints"),
                new Location(Worlds.NORMAL, -639, 71, 3830.5),
                Component.text("Pirate Points Leaderboard")
        );

        leaderboard.setFormat((pos, name, score) ->
                Component.text()
                        .append(Component.text(pos + ". "))
                        .append(Component.text(name).color(NamedTextColor.YELLOW))
                        .append(score)
                        .build()
        );

        leaderboard.setSize((byte) 5);
        leaderboard.setBorder(Component.text("---------=o=O=o=---------").color(NamedTextColor.GOLD));
        leaderboard.create();
    }

    public static void shutDown(){
        pirateEconomy.save();
        shulker.save();
        ghParkour.getData().save();
        parrotTracker.save();
        auctionManager.saveAuctions();
    }

    public static PirateEconomy getPirateEconomy() { return pirateEconomy; }
    public static GrapplingHookParkour getParkour() { return ghParkour; }
    public static TreasureShulker getTreasure() { return shulker; }
    public static ParrotTracker getParrotTracker() { return parrotTracker; }
    public static AuctionManager getAuctions() { return auctionManager; }
    public static ObjectiveLeaderboard getLeaderboard() { return leaderboard; }
}
