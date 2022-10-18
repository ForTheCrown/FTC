package net.forthecrown.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.forthecrown.utils.JsonSerializable;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializableObject;
import net.forthecrown.utils.io.SerializationHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class FtcConfig implements SerializableObject {
    @Getter
    private final Path path;

    private final List<Section> sections = new ObjectArrayList<>();

    @Getter
    private final Set<String> illegalWorlds = new ObjectOpenHashSet<>();

    @Getter
    private JsonWrapper json;
    private Component prefix;

    @Setter
    private Location serverSpawn;

    FtcConfig() {
        this.path = PathUtil.pluginPath("configuration.json");
    }

    @Override
    public void save() {
        // Save basic info
        json.addComponent("prefix", prefix);
        json.addLocation("server_spawn", serverSpawn);

        // Write all illegal worlds
        json.addList("illegal_worlds", illegalWorlds, JsonPrimitive::new);

        // Save sections
        for (Section a: sections) {
            JsonElement e = a.serialize();

            // Don't serialize nulls
            if(e == null) {
                json.remove(a.getSerializationKey());
                continue;
            }

            json.add(a.getSerializationKey(), e);
        }

        write();
    }

    @Override
    public void reload() {
        read();
        load();

        AutoSave.get().start();
    }

    void load() {
        // Basic info loading
        setServerSpawn(json.getLocation("server_spawn"));
        prefix = json.getComponent("prefix");

        // Illegal world loading
        illegalWorlds.clear();
        JsonArray array = json.getArray("illegal_worlds");
        if(array != null) {
            for (JsonElement e: array) {
                String name = e.getAsString();
                illegalWorlds.add(name);
            }
        }

        // Section loading
        for (Section a: sections) {
            a.deserialize(json.get(a.getSerializationKey()));
        }
    }

    public void write() {
        SerializationHelper.writeJsonFile(path, json -> json.addAll(this.json));
    }

    public void read() {
        SerializationHelper.readJsonFile(path, jsonWrapper -> this.json = jsonWrapper);
    }

    /**
     * Gets the prefix FTC should be using
     * @return The FTC prefix, '[FTC] '
     */
    public Component prefix() {
        return prefix.hoverEvent(Component.text("For The Crown :D"));
    }

    /**
     * Gets the server spawn location.
     * This is the location everyone will spawn at
     * when they join the server for the first time
     *
     * @return The server spawn
     */
    public Location getServerSpawn() {
        return serverSpawn.clone();
    }

    /**
     * Adds a section to the config
     * @param attachment The section to add
     */
    public void addSection(Section attachment) {
        sections.add(attachment);
    }

    /**
     * Removes a section from the config
     * @param attachment The section to remove
     */
    public void removeSection(Section attachment) {
        sections.remove(attachment);
    }

    /**
     * Marks a world as illegal
     * @param world The world to mark
     */
    public void addIllegalWorld(World world) {
        getIllegalWorlds().add(world.getName());
    }

    /**
     * Unmarks a world as illegal
     * @param world The world to unmark
     */
    public void removeIllegalWorld(World world) {
        getIllegalWorlds().remove(world.getName());
    }

    /**
     * Checks if the given world is an illegal world
     * @param world The world to check
     * @return Whether the world is illegal
     */
    public boolean isIllegalWorld(World world) {
        return getIllegalWorlds().contains(world.getName());
    }

    /**
     * A config section is just an object which is serialized and deserialized to and from the
     * FtcConfig. It must provide a serializationKey, aka just a key for the json object so
     * the config can find it.
     */
    @RequiredArgsConstructor
    public static abstract class Section implements JsonSerializable {
        @Getter
        private final String serializationKey;

        public abstract void deserialize(JsonElement element);
    }
}