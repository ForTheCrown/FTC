package net.forthecrown.pirates;

import com.google.gson.JsonPrimitive;
import net.forthecrown.core.Crown;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.JsonUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class ParrotTracker extends AbstractJsonSerializer implements Iterable<UUID> {

    private final Set<UUID> withParrots = new HashSet<>();

    protected ParrotTracker() {
        super("parrot_tracker");

        reload();
        Crown.logger().info("Parrot Tracker loaded");
    }

    @Override
    protected void save(JsonWrapper json) {
        json.add("withParrots", JsonUtils.writeCollection(withParrots, id -> new JsonPrimitive(id.toString())));
    }

    @Override
    protected void reload(JsonWrapper json) {
        withParrots.clear();
        if(!json.has("withParrots")) return;

        json.getArray("withParrots").forEach(e -> withParrots.add(UUID.fromString(e.getAsString())));
    }

    public void check(Player player){
        if(!contains(player.getUniqueId())) return;

        Entity ent = player.getShoulderEntityLeft();
        player.setShoulderEntityLeft(null);

        if(ent != null) ent.remove();

        remove(player.getUniqueId());
    }

    public int size() {
        return withParrots.size();
    }

    public boolean isEmpty() {
        return withParrots.isEmpty();
    }

    public boolean contains(UUID o) {
        return withParrots.contains(o);
    }

    public @NotNull Iterator<UUID> iterator() {
        return withParrots.iterator();
    }

    public boolean add(UUID uuid) {
        return withParrots.add(uuid);
    }

    public boolean remove(UUID o) {
        return withParrots.remove(o);
    }
}
