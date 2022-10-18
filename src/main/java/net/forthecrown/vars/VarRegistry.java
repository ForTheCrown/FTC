package net.forthecrown.vars;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollections;
import lombok.Getter;
import net.forthecrown.core.Crown;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializableObject;
import net.forthecrown.vars.types.VarType;
import net.forthecrown.vars.types.VarTypes;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.StackLocatorUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class VarRegistry extends SerializableObject.Json {
    private static final Logger LOGGER = Crown.logger();

    @Getter
    private final VarFactory factory = new VarFactory();

    private final Object2ObjectMap<String, VarData> variables = new Object2ObjectOpenHashMap<>();

    private final Map<String, LoadedVar> loaded = new Object2ObjectOpenHashMap<>();

    public VarRegistry() {
        super(PathUtil.pluginPath("com_vars.json"));

        reload();
    }

    // --- VAR REGISTRATION ---

    public void register() {
        registerVars(StackLocatorUtil.getCallerClass(2));
    }

    public void registerVars(Class c) {
        try {
            Collection<VarData> dataCollection = factory.getVariables(c);
            dataCollection.forEach(this::addVar);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    // --- VAR ADDITION ---

    public <T> void addVar(VarData<T> data) {
        if (variables.containsKey(data.getName())) {
            throw new IllegalArgumentException(
                    String.format("Var named '%s' is already added", data.getName())
            );
        }

        variables.put(data.getName(), data);

        LoadedVar<T> loaded = this.loaded.remove(data.getName());
        if (loaded == null) {
            return;
        }

        loadVar(data, loaded);
    }

    private void loadVar(VarData data, LoadedVar var) {
        if (data.getType() != var.type()) {
            throw new IllegalArgumentException(
                    String.format("Type mismatch on vars '%s', data type='%s', loaded type='%s'",
                            data.getName(),

                            VarTypes.TYPE_REGISTRY.getKey(data.getType()).orElse("UNKNOWN"),
                            VarTypes.TYPE_REGISTRY.getKey(var.type()).orElse("UNKNOWN")
                    )
            );
        }

        data.update(var.value());
    }

    // --- VAR GETTING ---

    public VarData get(String name) {
        return variables.get(name);
    }

    public Collection<VarData> values() {
        return ObjectCollections.unmodifiable(variables.values());
    }

    // --- SERIALIZATION ---

    private <T> void onVarLoad(LoadedVar<T> var) {
        VarData<T> data = get(var.name());

        if (data != null) {
            loadVar(data, var);
            return;
        }

        loaded.put(var.name(), var);
    }

    @Override
    protected void load(JsonWrapper json) {
        for (var e: json.entrySet()) {
            Optional<VarType> typeOptional = VarTypes.TYPE_REGISTRY.get(e.getKey());

            if (typeOptional.isEmpty()) {
                LOGGER.warn("Unknown var type found in var file: '{}'", e.getKey());
                continue;
            }

            VarType type = typeOptional.get();
            JsonObject obj = (JsonObject) e.getValue();

            for (var var: obj.entrySet()) {
                String name = var.getKey();

                JsonElement element = var.getValue();
                Object loaded = type.deserialize(element);

                onVarLoad(new LoadedVar<>(name, loaded, type));
            }
        }
    }

    @Override
    protected void save(JsonWrapper json) {
        for (var d: variables.values()) {
            if (d.isTransient()) {
                continue;
            }

            VarType type = d.getType();
            Optional<String> keyOptional = VarTypes.TYPE_REGISTRY.getKey(type);

            if (keyOptional.isEmpty()) {
                LOGGER.warn("Found unregistered type with var: '{}'", d.getName());
                continue;
            }

            String key = keyOptional.get();
            JsonWrapper typeJson = json.getWrappedNonNull(key);
            typeJson.add(d.getName(), type.serialize(d.getValue()));

            if (!json.has(key)) {
                json.add(key, typeJson);
            }
        }
    }
}