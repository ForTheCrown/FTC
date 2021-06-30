package net.forthecrown.pirates;

import net.forthecrown.economy.pirates.CrownPirateEconomy;
import net.forthecrown.economy.pirates.PirateEconomy;
import net.forthecrown.pirates.grappling.GrapplingHookParkour;
import net.forthecrown.squire.Squire;
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

    public static void init(){
        pirateEconomy = new CrownPirateEconomy();
        shulker = new TreasureShulker();
        ghParkour = new GrapplingHookParkour();
        parrotTracker = new ParrotTracker();
        auctionManager = new AuctionManager();
    }

    public static PirateEconomy getPirateEconomy() { return pirateEconomy; }
    public static GrapplingHookParkour getParkour() { return ghParkour; }
    public static TreasureShulker getTreasure() { return shulker; }
    public static ParrotTracker getParrotTracker() { return parrotTracker; }
    public static AuctionManager getAuctions() { return auctionManager; }
}
