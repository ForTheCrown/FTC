package net.forthecrown.julyevent;

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
import net.forthecrown.core.crownevents.EventTimer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class EventStarter {

    public static final int DISTANCE_BETWEEN = 750;

    public static final int X_OFFSET = 191;
    public static final int Y_OFFSET = -162;
    public static final int Z_OFFSET = 113;

    private final Player player;
    private final JulyEntry entry;
    private final Location minLoc;

    public EventStarter(Player player) {
        this.player = player;

        EventTimer timer = new EventTimer(player, p -> {
            p.sendMessage(Component.text("You took too long lol").color(NamedTextColor.GRAY));
            JulyMain.event.end(CoolParkourEvent.ENTRIES.get(p));
        });

        int xCord = 0;
        while(CoolParkourEvent.IN_USE_TRACKS.get(xCord)){
            xCord++;
        }

        this.minLoc = new Location(CoolParkourEvent.EVENT_WORLD, xCord * DISTANCE_BETWEEN, 70, 1500);
        this.entry = new JulyEntry(player, timer, xCord);
    }

    public void start(){
        CoolParkourEvent.ENTRIES.put(player, entry);
        entry.startTime = System.currentTimeMillis();

        player.teleport(minLoc.clone().add(X_OFFSET, Y_OFFSET, Z_OFFSET));
    }

    public EventStarter buildTrack(){
        BlockVector3 origMin = BukkitAdapter.asBlockVector(CoolParkourEvent.REGION.getMinLocation());
        BlockVector3 origMax = BukkitAdapter.asBlockVector(CoolParkourEvent.REGION.getMaxLocation());

        CuboidRegion region = new CuboidRegion(origMin, origMax);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        try (EditSession editSession = new EditSessionBuilder(BukkitAdapter.adapt(CoolParkourEvent.EVENT_WORLD)).build()) {
            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                    editSession, region, clipboard, region.getMinimumPoint()
            );
            Operations.complete(forwardExtentCopy);
        }

        try (EditSession editSession = new EditSessionBuilder(BukkitAdapter.adapt(CoolParkourEvent.EVENT_WORLD)).build()) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BukkitAdapter.asBlockVector(minLoc))
                    .build();
            Operations.complete(operation);
        }

        return this;
    }
}
