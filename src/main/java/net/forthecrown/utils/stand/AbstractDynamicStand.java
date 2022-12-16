package net.forthecrown.utils.stand;

import net.forthecrown.core.FTC;
import net.kyori.adventure.text.Component;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public abstract class AbstractDynamicStand {
    private final Location location;
    private final Chunk chunk;

    public AbstractDynamicStand(Location location) {
        this.location = Objects.requireNonNull(location);

        this.chunk = location.getChunk();
        chunk.addPluginChunkTicket(FTC.getPlugin());

        kill();
    }

    public Location getLocation() {
        return location.clone();
    }

    protected void kill(NamespacedKey key) {
        var entities = chunk.getEntities();

        for (var e: entities) {
            if (!e.getPersistentDataContainer().has(key)) {
                continue;
            }

            e.remove();
        }
    }

    public static ArmorStand spawn(Location l,
                                   NamespacedKey standKey,
                                   Component displayName
    ) {
        return l.getWorld().spawn(l, ArmorStand.class, stand -> {
            stand.setMarker(true);
            stand.setBasePlate(false);
            stand.setInvisible(true);
            stand.setInvulnerable(true);
            stand.setCustomNameVisible(true);
            stand.setCanTick(false);
            stand.getPersistentDataContainer()
                    .set(standKey, PersistentDataType.INTEGER, 1);

            stand.customName(displayName);
        });
    }

    public abstract void kill();
}