package net.forthecrown.vars;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.vars.types.VarType;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class VarSerializer {
    private static final Logger LOGGER = Crown.logger();

    static File getFile() {
        return new File(Crown.dataFolder(), "com_vars.json");
    }

    static File getOrCreate() {
        File comvarFile = getFile();

        if(comvarFile.isDirectory()) comvarFile.delete();

        if(!comvarFile.exists()) {
            try {
                comvarFile.createNewFile();
            } catch (IOException e) {
                LOGGER.error("Could not create comvar file", e);
            }
        }

        return comvarFile;
    }

    static JsonWrapper read() throws IOException {
        File f = getFile();
        if(!f.exists() || f.length() < 1) return JsonWrapper.empty();

        return JsonWrapper.of(JsonUtils.readFileObject(f));
    }

    static void load(Map<String, Var> comvarMap) {
        JsonWrapper json;
        try {
            json = read();
        } catch (IOException e) {
            LOGGER.error("Couldn't read comvar file", e);
            return;
        }

        for (Map.Entry<String, JsonElement> e: json.entrySet()) {
            JsonObject values = e.getValue().getAsJsonObject();
            Key typeKey = Keys.parse(e.getKey());
            VarType type = Registries.VAR_TYPES.get(typeKey);

            if(type == null) {
                LOGGER.warn("Found unknown var type: '{}', ignoring", typeKey);
                continue;
            }

            for (Map.Entry<String, JsonElement> val: values.entrySet()) {
                String key = val.getKey();

                if(!VarRegistry.isValidName(key)) {
                    LOGGER.warn("Found illegally named comvar in JSON, ignoring. name: '{}', section: '{}'", key, typeKey.asString());
                    continue;
                }

                Object deserialized = type.deserialize(val.getValue());

                Var v = comvarMap.computeIfAbsent(val.getKey(), s -> new Var(type, s, deserialized));
                v.update(deserialized);
            }
        }
    }

    static void save(Collection<Var> values) {
        JsonWrapper json = JsonWrapper.empty();

        for (Var v: values) {
            if(v.isTransient()) continue;
            if (!v.used && !VarRegistry.SERIALIZE_UNSUSED.get()) continue;

            VarType t = v.getType();
            JsonWrapper section = json.getWrappedNonNull(t.key().asString());

            section.add(v.getName(), v);
            json.add(t.key().asString(), section);
        }

        File f = getOrCreate();
        try {
            JsonUtils.writeFile(json.getSource(), f);
        } catch (IOException e) {
            LOGGER.error("Couldn't write Comvar file", e);
        }
    }
}
