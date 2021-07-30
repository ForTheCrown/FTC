package net.forthecrown.pirates;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.serializer.AbstractJsonSerializer;
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
        ForTheCrown.logger().info("Parrot Tracker loaded");
    }

    @Override
    protected void save(JsonObject json) {
        json.add("withParrots", JsonUtils.writeCollection(withParrots, id -> new JsonPrimitive(id.toString())));
    }

    @Override
    protected void reload(JsonObject json) {
        withParrots.clear();
        if(!json.has("withParrots")) return;

        json.getAsJsonArray("withParrots").forEach(e -> withParrots.add(UUID.fromString(e.getAsString())));
    }

    @Override
    protected JsonObject createDefaults(JsonObject json) {
        return json;
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
