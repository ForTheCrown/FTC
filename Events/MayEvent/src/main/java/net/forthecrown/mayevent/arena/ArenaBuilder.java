package net.forthecrown.mayevent.arena;

import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import net.forthecrown.core.CrownBoundingBox;
import net.forthecrown.core.utils.ListUtils;
import net.forthecrown.core.utils.MapUtils;
import net.forthecrown.mayevent.ArenaEntry;
import net.forthecrown.mayevent.DoomEvent;
import net.forthecrown.mayevent.MayUtils;
import net.forthecrown.mayevent.RelativeLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.EntityTypes;
import org.apache.logging.log4j.core.util.Builder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArenaBuilder implements Builder<EventArena> {

    public static final CrownBoundingBox copyFrom = new CrownBoundingBox(DoomEvent.EVENT_WORLD, -35, 88, -457, 10, 125, -387);
    public static final short DISTANCE_BETWEEN = 250;

    private final ArenaEntry entry;
    private final Location minLoc;

    private List<RelativeLocation> mobSpawns;
    private List<RelativeLocation> itemDrops;
    private Location[] wallSpawns;
    private List<TriggerableEvent> events;
    private List<RelativeLocation> gems;
    private Map<RelativeLocation, EntityTypes<? extends EntityLiving>> specialSpawns;

    private final RelativeLocation firstGun;
    private final RelativeLocation shotGunPos;
    private final RelativeLocation startLocation;
    private final RelativeLocation rocketLocation;

    private final CrownBoundingBox areaBox;

    public ArenaBuilder(ArenaEntry entry) {
        this.entry = entry;

        int zCord = 0;
        while (DoomEvent.IN_USE_ARENAS.get(zCord)){
            zCord++;
        }

        this.minLoc = new Location(DoomEvent.EVENT_WORLD, 0, 25, DISTANCE_BETWEEN * zCord);

        DoomEvent.IN_USE_ARENAS.set(zCord, true);

        firstGun = new RelativeLocation(27.5, 6, 41.5);
        shotGunPos = new RelativeLocation(12.5, 15, 61.5);
        startLocation = new RelativeLocation(21.5, 5, 41.5);
        rocketLocation = new RelativeLocation(24.5, 1, 3.5);

        areaBox = CrownBoundingBox.of(minLoc.clone(), minLoc.clone().add(45, 37, 70));
    }

    private void makeLists(){
        //mob spawns
        mobSpawns = new ArrayList<>();
        mobSpawns.add(new RelativeLocation(15, 11, 21));
        mobSpawns.add(new RelativeLocation(14, 11, 10));
        mobSpawns.add(new RelativeLocation(28, 10, 6));
        mobSpawns.add(new RelativeLocation(16, 5, 21));
        mobSpawns.add(new RelativeLocation(17, 5, 18));
        mobSpawns.add(new RelativeLocation(10, 5, 5));
        mobSpawns.add(new RelativeLocation(10, 5, 13));
        mobSpawns.add(new RelativeLocation(6, 5, 29));
        mobSpawns.add(new RelativeLocation(6, 5, 39));
        mobSpawns.add(new RelativeLocation(6, 6, 43));
        mobSpawns.add(new RelativeLocation(7, 5, 48));
        mobSpawns.add(new RelativeLocation(6, 5, 54));
        mobSpawns.add(new RelativeLocation(7, 5, 61));
        mobSpawns.add(new RelativeLocation(15, 5, 58));
        mobSpawns.add(new RelativeLocation(25, 5, 16));
        mobSpawns.add(new RelativeLocation(21, 5, 9));
        mobSpawns.add(new RelativeLocation(29, 11, 7));
        mobSpawns.add(new RelativeLocation(33, 11, 6));
        mobSpawns.add(new RelativeLocation(34, 11, 7));
        mobSpawns.add(new RelativeLocation(33, 11, 16));
        mobSpawns.add(new RelativeLocation(36, 6, 54));
        mobSpawns.add(new RelativeLocation(34, 6, 55));
        mobSpawns.add(new RelativeLocation(29, 12, 62));
        mobSpawns.add(new RelativeLocation(37, 6, 22));
        mobSpawns.add(new RelativeLocation(32, 6, 16));
        mobSpawns.add(new RelativeLocation(34, 6, 10));
        mobSpawns.add(new RelativeLocation(33, 6, 8));
        mobSpawns.add(new RelativeLocation(31, 6, 7));
        mobSpawns.add(new RelativeLocation(10, 13, 63));
        mobSpawns.add(new RelativeLocation(16, 13, 62));
        mobSpawns.add(new RelativeLocation(23, 11, 62));

        //Special spawns
        specialSpawns = new HashMap<>();
        specialSpawns.put(new RelativeLocation(23, 10, 15), EntityTypes.SKELETON);
        specialSpawns.put(new RelativeLocation(27, 10, 15), EntityTypes.SKELETON);
        specialSpawns.put(new RelativeLocation(36, 10, 21), EntityTypes.SKELETON);
        specialSpawns.put(new RelativeLocation(36, 10, 29), EntityTypes.SKELETON);
        specialSpawns.put(new RelativeLocation(36, 10, 35), EntityTypes.SKELETON);
        specialSpawns.put(new RelativeLocation(27, 5, 59), EntityTypes.ZOMBIE_VILLAGER);

        //Item pickup locations
        itemDrops = new ArrayList<>();
        itemDrops.add(new RelativeLocation(13, 5, 9));
        itemDrops.add(new RelativeLocation(12, 11, 8));
        itemDrops.add(new RelativeLocation(33, 10, 9));
        itemDrops.add(new RelativeLocation(36, 9, 55));
        itemDrops.add(new RelativeLocation(37, 9, 41));
        itemDrops.add(new RelativeLocation(10, 5, 59));
        itemDrops.add(new RelativeLocation(15, 23, 18));
        itemDrops.add(new RelativeLocation(40, 22, 41));

        //gems list
        gems = new ArrayList<>();
        gems.add(new RelativeLocation(7, 12, 52));
        gems.add(new RelativeLocation(40, 10, 34));
        gems.add(new RelativeLocation(4, 19, 41));

        wallSpawns = new Location[] {
                new RelativeLocation(minLoc.clone(), 20, 22, 5).getLocation(),
                new RelativeLocation(minLoc.clone(), 16, 19, 4).getLocation(),
                new RelativeLocation(minLoc.clone(), 6, 19, 9).getLocation(),
                new RelativeLocation(minLoc.clone(), 24, 22, -84).getLocation(),
                new RelativeLocation(minLoc.clone(), 33, 19, 5).getLocation(),
                new RelativeLocation(minLoc.clone(), 38, 19, 12).getLocation(),
                new RelativeLocation(minLoc.clone(), 38, 22, 19).getLocation(),
                new RelativeLocation(minLoc.clone(), 37, 22, 34).getLocation(),
                new RelativeLocation(minLoc.clone(), 41, 22, 35).getLocation(),
                new RelativeLocation(minLoc.clone(), 38, 22, 47).getLocation(),
                new RelativeLocation(minLoc.clone(), 40, 19, 54).getLocation(),
                new RelativeLocation(minLoc.clone(), 41, 22, 65).getLocation(),
                new RelativeLocation(minLoc.clone(), 30, 19, 66).getLocation(),
                new RelativeLocation(minLoc.clone(), 21, 19, 64).getLocation(),
                new RelativeLocation(minLoc.clone(), 11, 19, 66).getLocation(),
                new RelativeLocation(minLoc.clone(), 4, 22, 64).getLocation(),
                new RelativeLocation(minLoc.clone(), 2, 19, 57).getLocation(),
                new RelativeLocation(minLoc.clone(), 4, 19, 47).getLocation(),
                new RelativeLocation(minLoc.clone(), 2, 22, 42).getLocation(),
                new RelativeLocation(minLoc.clone(), 4, 19, 27).getLocation(),
                new RelativeLocation(minLoc.clone(), 13, 22, 14).getLocation()
        };
    }

    private void createEvents(){
        events = new ArrayList<>();

        //Spawns the "Rhino fucker"
        events.add(new TriggerableEvent(false, true, a -> a.wave() % 25 == 0, arena -> {
            //Remove glass panes
            CrownBoundingBox box = new CrownBoundingBox(
                    new RelativeLocation(arena.minLoc(), 35, 8, 43).getLocation(),
                    new RelativeLocation(arena.minLoc(), 33, 4, 37).getLocation()
            );
            box.getBlocks(block -> block.getType() == Material.GLASS_PANE).forEach(b -> b.breakNaturally(MayUtils.noDropper, true));

            //Spawn ravager and add to mob list
            DoomEvent.EVENT_WORLD.spawn(new RelativeLocation(arena.minLoc(), 37, 6, 40).getLocation(),
                    Ravager.class,
                    rav -> rav.getAttribute(Attribute.GENERIC_MAX_HEALTH).addModifier(arena.getWaveModifier())
            );

            arena.initialMobAmount++;
            arena.currentMobAmount++;

            //Inform player
            arena.entry.player().sendMessage(Component.text("The Ravager has spawned!").color(NamedTextColor.RED));
        }));

        //Makes some cool destruction
        events.add(new TriggerableEvent(true, false ,arena -> arena.wave() == 10, arena ->
                MayUtils.attemptDestruction(new RelativeLocation(arena.minLoc(), 38, 20, 24).getLocation(), 3)
        ));

        //Drops shotgun
        events.add(new TriggerableEvent(true, false, arena -> arena.wave() == 3,
                arena -> {
                    DoomEvent.EVENT_WORLD.dropItem(arena.secondGun, arena.playerShotgun.item());
                    arena.entry.player().sendMessage(Component.text("Shotgun has spawned in the office"));
                }
        ));

        //Ghast Spawns
        events.add(new TriggerableEvent(false, true, arena -> arena.wave() % 10 == 0, arena -> {
            Location[] ghasts = new Location[]{ //Spawnable locations list
                    new RelativeLocation(arena.minLoc(), 8, 28, 40).getLocation(),
                    new RelativeLocation(arena.minLoc(), 31, 26, 53).getLocation(),
                    new RelativeLocation(arena.minLoc(), 18, 16, 32).getLocation(),
                    new RelativeLocation(arena.minLoc(), 30, 22, 21).getLocation()
            };

            World world = DoomEvent.EVENT_WORLD;
            for (Location l: ghasts){
                world.spawn(l, Ghast.class, ghast -> {
                    ghast.getAttribute(Attribute.GENERIC_MAX_HEALTH).addModifier(arena.getWaveModifier());
                    ghast.setHealth(ghast.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

                    arena.initialMobAmount++;
                    arena.currentMobAmount++;
                });
            }
        }));

        //Spawns pillagers
        events.add(new TriggerableEvent(false, true, arena -> arena.wave() % 5 == 0, arena -> {
            Location[] spawns = new Location[] {
                    new RelativeLocation(arena.minLoc(), 4, 24, 17).getLocation(),
                    new RelativeLocation(arena.minLoc(), 4, 24, 41).getLocation(),
                    new RelativeLocation(arena.minLoc(), 4, 24, 65).getLocation(),
                    new RelativeLocation(arena.minLoc(), 7, 15, 47).getLocation(),
                    new RelativeLocation(arena.minLoc(), 5, 16, 31).getLocation(),
                    new RelativeLocation(arena.minLoc(), 38, 22, 36).getLocation(),
                    new RelativeLocation(arena.minLoc(), 39, 19, 27).getLocation(),
                    new RelativeLocation(arena.minLoc(), 11, 13, 56).getLocation(),
                    new RelativeLocation(arena.minLoc(), 26, 17, 9).getLocation(),
                    new RelativeLocation(arena.minLoc(), 33, 24, 59).getLocation()
            };

            for (Location l: spawns){
                MayUtils.spawnAndEffect(l, Pillager.class, pillager -> {
                    pillager.getAttribute(Attribute.GENERIC_MAX_HEALTH).addModifier(arena.getWaveModifier());
                    pillager.setTarget(arena.entry.player());
                });

                arena.initialMobAmount++;
                arena.currentMobAmount++;
            }
        }));

        events.add(new TriggerableEvent(false, false,
                arena -> arena.wave() % 100 == 0,
                arena ->  MayUtils.spawnAndEffect(arena.startLocation, Giant.class, arena::onMobSpawn)
        ));
    }

    private void buildArena(){
        BlockVector3 origMin = BukkitAdapter.asBlockVector(copyFrom.getMinLocation());
        BlockVector3 origMax = BukkitAdapter.asBlockVector(copyFrom.getMaxLocation());
        CuboidRegion region = new CuboidRegion(origMin, origMax);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        try (EditSession editSession = new EditSession(new EditSessionBuilder(BukkitAdapter.adapt(DoomEvent.EVENT_WORLD)))) {
            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                    editSession, region, clipboard, region.getMinimumPoint()
            );
            Operations.complete(forwardExtentCopy);
        }

        try (EditSession editSession = new EditSession(new EditSessionBuilder(BukkitAdapter.adapt(DoomEvent.EVENT_WORLD)))) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BukkitAdapter.asBlockVector(minLoc))
                    .build();
            Operations.complete(operation);
        }
    }

    @Override
    public EventArena build() {
        buildArena();
        makeLists();
        createEvents();

        return new EventArena(entry,
                ListUtils.convertToList(mobSpawns, rel -> MayUtils.validateIsAir(rel.setRelativeTo(minLoc).getLocation())),
                ListUtils.convertToList(itemDrops, rel -> MayUtils.validateIsAir(rel.setRelativeTo(minLoc).getLocation())),
                ListUtils.convertToList(gems, rel -> MayUtils.validateIsAir(rel.setRelativeTo(minLoc).getLocation())),
                wallSpawns,
                MapUtils.convertKeys(specialSpawns, rel -> MayUtils.validateIsAir(rel.setRelativeTo(minLoc).getLocation())),
                events,
                MayUtils.validateIsAir(firstGun.setRelativeTo(minLoc).getLocation()),
                MayUtils.validateIsAir(shotGunPos.setRelativeTo(minLoc).getLocation()),
                MayUtils.validateIsAir(rocketLocation.setRelativeTo(minLoc).getLocation()),
                minLoc,
                MayUtils.validateIsAir(startLocation.setRelativeTo(minLoc).getLocation()),
                areaBox
        );
    }
}