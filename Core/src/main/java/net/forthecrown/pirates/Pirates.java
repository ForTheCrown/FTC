package net.forthecrown.pirates;

import net.forthecrown.core.Crown;
import net.forthecrown.crownevents.ObjectiveLeaderboard;
import net.forthecrown.economy.pirates.FtcPirateEconomy;
import net.forthecrown.economy.pirates.PirateEconomy;
import net.forthecrown.squire.Squire;
import net.forthecrown.utils.Worlds;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;

import static net.forthecrown.utils.FtcUtils.safeRunnable;

public final class Pirates {
    private Pirates() {}

    public static final NamespacedKey SHULKER_KEY = Squire.createPiratesKey("treasure_shulker");

    static TreasureShulker shulker;
    static FtcPirateEconomy pirateEconomy;
    static ParrotTracker parrotTracker;

    static ObjectiveLeaderboard leaderboard;

    /**
     * Initiates the pirates. NOT API
     */
    public static void init(){
        safeRunnable(() -> pirateEconomy = new FtcPirateEconomy());
        safeRunnable(() -> shulker = new TreasureShulker());
        safeRunnable(() -> parrotTracker = new ParrotTracker());

        initLeaderboard();

        Crown.logger().info("Pirates loaded");
    }

    private static void initLeaderboard(){
        leaderboard = new ObjectiveLeaderboard(
                Bukkit.getScoreboardManager().getMainScoreboard().getObjective("PiratePoints"),
                new Location(Worlds.OVERWORLD, -639, 71, 3830.5),
                Component.text("Pirate Points Leaderboard")
        );

        leaderboard.setFormat((pos, name, score) ->
                Component.text()
                        .append(Component.text(pos + ". "))
                        .append(Component.text(name).color(NamedTextColor.YELLOW))
                        .append(Component.text(": "))
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
        parrotTracker.save();
        leaderboard.destroy();
    }

    public static PirateEconomy getPirateEconomy() { return pirateEconomy; }
    public static TreasureShulker getTreasure() { return shulker; }
    public static ParrotTracker getParrotTracker() { return parrotTracker; }
    public static ObjectiveLeaderboard getLeaderboard() { return leaderboard; }
}
