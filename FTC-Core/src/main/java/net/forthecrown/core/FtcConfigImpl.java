package net.forthecrown.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.FileDefaults;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

class FtcConfigImpl implements FtcConfig {
    private final List<ConfigSection> sections = new ObjectArrayList<>();
    private final Set<World> illegalWorlds = new ObjectOpenHashSet<>();

    private JsonWrapper json;
    private Component prefix;
    private Location spawn;

    FtcConfigImpl() {}

    @Override
    public void save() {
        // Save basic info
        json.addComponent("prefix", prefix);
        json.addLocation("server_spawn", spawn);

        // Write all illegal worlds
        json.addList("illegal_worlds", illegalWorlds, world -> new JsonPrimitive(world.getName()));

        // Save sections
        for (ConfigSection a: sections) {
            JsonElement e = a.serialize();

            // Don't serialize nulls
            if(e == null) {
                json.remove(a.serializationKey);
                continue;
            }

            json.add(a.serializationKey, e);
        }

        write();
    }

    void ensureDefaultsExist() {
        try {
            FileDefaults.JSON.compareAndSave(fileOrCreate(), Crown.resource("configuration.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reload() {
        read();
        load();

        Main.inst.saverLogic();
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
                World w = Bukkit.getWorld(name);
                if(w == null) {
                    Crown.logger().warn("Found unknown world in illegal_worlds in config: " + name + ", ignoring");
                    continue;
                }

                illegalWorlds.add(w);
            }
        }

        // Section loading
        for (ConfigSection a: sections) {
            a.deserialize(json.get(a.serializationKey));
        }
    }

    public void write() {
        File f = fileOrCreate();

        try {
            JsonUtils.writeFile(json.getSource(), f);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void read() {
        File f = fileOrCreate();

        try {
            this.json =  JsonWrapper.of(JsonUtils.readFileObject(f));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public File fileOrCreate() {
        File f = new File(Crown.dataFolder(), "configuration.json");

        if(f.isDirectory()) f.delete();
        if(!f.exists()) {
            Crown.saveResource(true, "configuration.json");
        }

        return f;
    }

    @Override
    public Component prefix() {
        return prefix.hoverEvent(Component.text("For The Crown :D"));
    }

    @Override
    public JsonWrapper getJson() {
        return json;
    }

    @Override
    public Location getServerSpawn() {
        return spawn;
    }

    @Override
    public void setServerSpawn(Location location) {
        spawn = location;
    }

    @Override
    public Set<World> getIllegalWorlds() {
        return illegalWorlds;
    }

    @Override
    public void addSection(ConfigSection attachment) {
        sections.add(attachment);
    }

    @Override
    public void removeSection(ConfigSection attachment) {
        sections.remove(attachment);
    }
}
