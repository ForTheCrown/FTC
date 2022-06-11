package net.forthecrown.vars;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.vars.types.VarType;
import net.kyori.adventure.key.Key;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * A class that serializes all non-transient, used variables.
 *
 * The current implementation of the serializer uses a format
 * in which everything is placed into a JSON object where each
 * entry is a type's key and its value a JSON object. The entries
 * in that object are variables and their serialized values
 */
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

    /**
     * Loads all variables into the given variable map
     * @param comvarMap The map to load into
     */
    static void load(Map<String, Var> comvarMap) {
        JsonWrapper json;

        try {
            json = read();
        } catch (IOException e) {
            LOGGER.error("Couldn't read comvar file", e);
            return;
        }

        // Initial JSON object, keys = types
        for (Map.Entry<String, JsonElement> e: json.entrySet()) {
            JsonObject values = e.getValue().getAsJsonObject();
            Key typeKey = Keys.parse(e.getKey());
            VarType type = Registries.VAR_TYPES.get(typeKey);

            if(type == null) {
                LOGGER.warn("Found unknown var type: '{}', ignoring", typeKey);
                continue;
            }

            // Sub JSON object, entries = variables
            for (Map.Entry<String, JsonElement> val: values.entrySet()) {
                String key = val.getKey();

                if(!VarRegistry.isValidName(key)) {
                    LOGGER.warn("Found illegally named var in JSON, ignoring. name: '{}', section: '{}'", key, typeKey.asString());
                    continue;
                }

                try {
                    Object deserialized = type.deserialize(val.getValue());

                    Var v = comvarMap.computeIfAbsent(val.getKey(), s -> new Var(type, s, deserialized));
                    v.update(deserialized);
                } catch (Exception exc) {
                    LOGGER.error("Couldn't load variable named: " + key + ", type: " + typeKey, exc);
                }
            }
        }
    }

    /**
     * Save all variables in the given variable collection
     * @param values The values to save
     */
    static void save(Collection<Var> values) {
        JsonWrapper json = JsonWrapper.empty();

        for (Var v: values) {
            // If the var is transient or its not used then skip it
            if(v.isTransient()) continue;
            if (!v.used && !VarRegistry.SERIALIZE_UNUSED.get()) continue;

            VarType t = v.getType();
            JsonWrapper section = json.getWrappedNonNull(t.key().asString());

            section.add(v.getName(), v);
            json.add(t.key().asString(), section);
        }

        File f = getOrCreate();
        try {
            JsonUtils.writeFile(json.getSource(), f);
        } catch (IOException e) {
            LOGGER.error("Couldn't write Var file", e);
        }
    }
}