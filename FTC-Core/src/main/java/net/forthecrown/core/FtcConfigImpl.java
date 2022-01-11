package net.forthecrown.core;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.util.List;

class FtcConfigImpl implements FtcConfig {
    private final List<ConfigSection> sections = new ObjectArrayList<>();

    private JsonWrapper json;
    private Component prefix;
    private Location spawn;

    FtcConfigImpl() {}

    @Override
    public void save() {
        json.addComponent("prefix", prefix);
        json.addLocation("server_spawn", spawn);

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

    @Override
    public void reload() {
        read();
        load();
    }

    void load() {
        setServerSpawn(json.getLocation("server_spawn"));
        prefix = json.getComponent("prefix");

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
        return prefix;
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
    public void addSection(ConfigSection attachment) {
        sections.add(attachment);
    }

    @Override
    public void removeSection(ConfigSection attachment) {
        sections.remove(attachment);
    }
}
