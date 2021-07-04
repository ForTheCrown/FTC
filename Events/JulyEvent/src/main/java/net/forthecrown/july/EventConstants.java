package net.forthecrown.july;

import com.google.common.collect.ImmutableList;
import net.forthecrown.core.utils.CrownBoundingBox;
import net.forthecrown.july.items.GemSpawnData;
import net.forthecrown.july.offset.BlockOffset;
import net.forthecrown.july.offset.BoundingBoxOffset;
import org.bukkit.*;
import org.bukkit.scoreboard.Objective;

import java.util.Objects;

//Hard coding things is great :DDDDDDDDDDDDDDD
//Kill me
public class EventConstants {
    public static final World EVENT_WORLD = Objects.requireNonNull(Bukkit.getWorld("world_july_event"));
    public static final Objective CROWN = Objects.requireNonNull(Bukkit.getScoreboardManager().getMainScoreboard().getObjective("crown"));

    public static final CrownBoundingBox WORLD_REGION = new CrownBoundingBox(EVENT_WORLD, 30000000, 256, 30000000, -30000000, 0, -30000000);
    public static final CrownBoundingBox REGION = new CrownBoundingBox(EVENT_WORLD, -225, 70, -238, 0, 256, 130);

    public static final Location MIN_LOC = REGION.getMinLocation();
    public static final Location ELYTRA_FALLBACK = new Location(EVENT_WORLD, -184.5, 222, -87.5, 0, 0);

    public static final Location BARRIERS   = new Location(EVENT_WORLD, -39, 95, 122);
    public static final Location PRACTISE   = new Location(EVENT_WORLD, -34.5, 94, 125.5, 180, 0);
    public static final Location LOBBY      = new Location(EVENT_WORLD, -34.5, 74, 194.5, 180, 0);

    public static final Location FIREWORK_1 = new Location(EVENT_WORLD, -43.5, 74, 184.5);
    public static final Location FIREWORK_2 = new Location(EVENT_WORLD, -25.5, 74, 184.5);

    public static final BlockOffset START_OFFSET    = BlockOffset.of(minLoc(), PRACTISE);
    public static final BlockOffset BARRIER_OFFSET  = BlockOffset.of(minLoc(), BARRIERS);
    public static final BlockOffset ELYTRA_FALLBACK_OFFSET = BlockOffset.of(minLoc(), ELYTRA_FALLBACK);

    public static final BoundingBoxOffset ELYTRA_LOOPS  = new BoundingBoxOffset(-199, 151, -82, -87, 224, -4);
    public static final BoundingBoxOffset ELYTRA_FAIL   = new BoundingBoxOffset(-218, 115, -124, -71, 132, 15);
    public static final BoundingBoxOffset END_REGION    = new BoundingBoxOffset(-135, 148, -17, -127, 159, -5);
    public static final BoundingBoxOffset REL_REGION    = new BoundingBoxOffset(REGION);
    public static final BoundingBoxOffset ELYTRA_ALLOW  = new BoundingBoxOffset(-206, 207, -95, 166, 232, -84);

    public static final BoundingBoxOffset CHECKPOINT_2  = new BoundingBoxOffset(-93, 148, -184, -88, 160, -174);
    public static final BlockOffset CHECK_2_FALLBACK = BlockOffset.of(minLoc(), -88, 151, -178);

    public static NamespacedKey NPC_KEY = new NamespacedKey(JulyMain.inst, "npc");

    public static final int GEM_COMPLETION = 750;
    public static final int DISTANCE_BETWEEN = 750;
    public static final int GEM_SECRET_VALUE = 100;
    public static final int GEM_PATH_VALUE = 10;

    public static final long FAST_TIME = 85000L;                        //One minute and 25 seconds
    public static final long SLOW_TIME = 3 * 60 * 1000;                 //Three minutes

    public static final FireworkEffect COOL_EFFECT = FireworkEffect.builder()
            .flicker(true)
            .trail(true)
            .withColor(Color.RED, Color.AQUA, Color.GREEN)
            .with(FireworkEffect.Type.BURST)
            .withFade(Color.PURPLE)
            .build();

    public static final FireworkEffect COOL_END_EFFECT = FireworkEffect.builder()
            .flicker(true)
            .trail(true)
            .withColor(Color.RED, Color.AQUA, Color.WHITE)
            .with(FireworkEffect.Type.BALL)
            .withFade(Color.PURPLE, Color.ORANGE, Color.YELLOW)
            .build();

    public static final ImmutableList<BoundingBoxOffset> ELYTRA_TRIGGERS = ImmutableList.of(
            new BoundingBoxOffset(-193, 208, -64, -179, 222, -62),
            new BoundingBoxOffset(-176, 210, -80, -165, 196, -69),
            new BoundingBoxOffset(-180, 186, -68, -182, 197, -58),
            new BoundingBoxOffset(-161, 180, -75, -163, 191, -65),
            new BoundingBoxOffset(-145, 175, -76, -135, 186, -74),
            new BoundingBoxOffset(-148, 168, -57, -139, 178, -55),
            new BoundingBoxOffset(-138, 163, -44, -136, 173, -35),
            new BoundingBoxOffset(-116, 159, -37, -114, 167, -30),
            new BoundingBoxOffset(-99, 166, -28, -92, 158, -21),
            new BoundingBoxOffset(-103, 153, -15, -101, 160, -8)
    );

    public static final ImmutableList<GemSpawnData> GEM_SPAWN_DATA = ImmutableList.of(
            new GemSpawnData(GEM_SECRET_VALUE, true, true, -21, 125, 8),
            new GemSpawnData(GEM_SECRET_VALUE, true, true,-52, 130, -186),
            new GemSpawnData(GEM_SECRET_VALUE, true, true, -74, 113, -137),
            new GemSpawnData(GEM_SECRET_VALUE, true, true,-31, 140, -83),
            new GemSpawnData(GEM_SECRET_VALUE, true, true, -41, 90, -121),
            new GemSpawnData(GEM_SECRET_VALUE, true, true,-54, 105, -182),
            new GemSpawnData(GEM_SECRET_VALUE, true, true, -21, 128, -30),
            new GemSpawnData(GEM_SECRET_VALUE, true, true,-191, 213, -154),
            new GemSpawnData(GEM_SECRET_VALUE, true, true, -31, 126, -137),

            new GemSpawnData(GEM_PATH_VALUE,true, -34, 94, 107),
            new GemSpawnData(GEM_PATH_VALUE,false, -34, 93, 91),
            new GemSpawnData(GEM_PATH_VALUE,true, -47, 94, 52),
            new GemSpawnData(GEM_PATH_VALUE, false, -32, 124, 11),
            new GemSpawnData(GEM_PATH_VALUE, true, -39, 127, -26),
            new GemSpawnData(GEM_PATH_VALUE, false, -35, 123, -73),
            new GemSpawnData(GEM_PATH_VALUE, true, -38, 117, -91),
            new GemSpawnData(GEM_PATH_VALUE, false, -41, 134, -121),
            new GemSpawnData(GEM_PATH_VALUE, true, -64, 138, -157),
            new GemSpawnData(GEM_PATH_VALUE, false, -89, 151, -178),
            new GemSpawnData(GEM_PATH_VALUE, true, -113, 152, -179),
            new GemSpawnData(GEM_PATH_VALUE, false, -126, 158, -215),
            new GemSpawnData(GEM_PATH_VALUE, true, -154, 162, -225),
            new GemSpawnData(GEM_PATH_VALUE, false, -183, 140, -226),
            new GemSpawnData(GEM_PATH_VALUE, true, -187, 176, -226),
            new GemSpawnData(GEM_PATH_VALUE, false, -180, 195, -226),
            new GemSpawnData(GEM_PATH_VALUE, true, -185, 223, -141),
            new GemSpawnData(GEM_PATH_VALUE, false, -185, 224, -90),
            new GemSpawnData(GEM_PATH_VALUE, true, -187, 215, -63),
            new GemSpawnData(GEM_PATH_VALUE, false, -144, 173, -56),
            new GemSpawnData(GEM_PATH_VALUE, true, -115, 163, -34)
    );

    public static Location minLoc(){
        return MIN_LOC;
    }
}
