package net.forthecrown.utils.io;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.utils.JsonSerializable;
import net.forthecrown.utils.math.Vectors;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.math.vector.Vectord;
import org.spongepowered.math.vector.Vectorf;
import org.spongepowered.math.vector.Vectori;
import org.spongepowered.math.vector.Vectorl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;

import static net.forthecrown.utils.io.JsonUtils.*;

/**
 * A JSON wrapper because this shit is too verbose on its own
 */
public final class JsonWrapper {
    //The source and handle of the wrapper
    private final JsonObject json;

    private JsonWrapper(JsonObject json) {
        this.json = json;
    }

    /**
     * Creates a wrapper for the given JSON
     *
     * @param json the json to create the wrapper for
     * @return The wrapper for the given JSON object
     */
    public static JsonWrapper wrap(JsonObject json) {
        return new JsonWrapper(json);
    }

    /**
     * Creates an empty json buf
     *
     * @return An empty json buf
     */
    public static JsonWrapper create() {
        return new JsonWrapper(new JsonObject());
    }

    public void add(String name, JsonSerializable serializable) {
        json.add(name, serializable.serialize());
    }
    
    public <E extends Enum<E>> void addEnum(String name, E anum) {
        json.add(name, writeEnum(anum));
    }

    public <E extends Enum<E>> E getEnum(String name, Class<E> clazz) {
        return getEnum(name, clazz, null);
    }
    
    public <E extends Enum<E>> E getEnum(String name, Class<E> clazz, E def) {
        if (missingOrNull(name)) return def;
        return readEnum(clazz, json.get(name));
    }

    public void addLocation(String name, Location location) {
        json.add(name, writeLocation(location));
    }

    public Location getLocation(String name) {
        return get(name, e -> readLocation(e.getAsJsonObject()));
    }

    public void addUUID(String name, UUID id) {
        add(name, writeUUID(id));
    }

    public UUID getUUID(String name) {
        if (missingOrNull(name)) return null;
        return readUUID(get(name));
    }

    public void addKey(String name, Key key) {
        json.add(name, writeKey(key));
    }
    
    public NamespacedKey getKey(String name) {
        return get(name, JsonUtils::readKey);
    }
    
    public void addItem(String name, ItemStack item) {
        json.add(name, writeItem(item));
    }

    public ItemStack getItem(String name) {
        return getItem(name, null);
    }

    public ItemStack getItem(String name, ItemStack def) {
        if (missingOrNull(name)) {
            return def;
        }

        return readItem(get(name));
    }

    public <T> T get(String name, Function<JsonElement, T> function) {
        return get(name, function, null);
    }

    public <T> T get(String name, Function<JsonElement, T> function, T def) {
        if (missingOrNull(name)) {
            return def;
        }

        T parsed = function.apply(get(name));

        return parsed == null ? def : parsed;
    }

    public <T> Collection<T> getList(String name, Function<JsonElement, T> func) {
        return getList(name, func, Collections.emptyList());
    }

    public <T> Collection<T> getList(String name, Function<JsonElement, T> func, Collection<T> def) {
        if (missingOrNull(name)) {
            return def;
        }

        return JsonUtils.stream(getArray(name))
                .map(func)
                .collect(ObjectArrayList.toList());
    }

    public void addList(String name, Iterable<? extends JsonSerializable> list) {
        addList(name, list, JsonSerializable::serialize);
    }

    public <T> void addList(String name, Iterable<T> iterable, Function<T, JsonElement> function) {
        JsonArray array = new JsonArray();
        iterable.forEach(e -> array.add(function.apply(e)));
        json.add(name, array);
    }

    public String getString(String name) {
        return getString(name, null);
    }
    
    public String getString(String name, String def) {
        if (missingOrNull(name)) return def;
        return json.get(name).getAsString();
    }
    
    public boolean getBool(String name) {
        return getBool(name, false);
    }
    
    public boolean getBool(String name, boolean def) {
        if (missingOrNull(name)) return def;
        return get(name).getAsBoolean();
    }
    
    public long getLong(String name) {
        return getLong(name, 0L);
    }

    public long getLong(String name, long def) {
        if (missingOrNull(name)) return def;
        return get(name).getAsLong();
    }
    
    public double getDouble(String name) {
        return getDouble(name, 0D);
    }
    
    public double getDouble(String name, double def) {
        if (missingOrNull(name)) return def;
        return get(name).getAsDouble();
    }
    
    public float getFloat(String name) {
        return getFloat(name, 0f);
    }

    public float getFloat(String name, float def) {
        if (missingOrNull(name)) return def;
        return get(name).getAsFloat();
    }

    public BigDecimal getBigDecimal(String name) {
        return getBigDecimal(name, null);
    }

    public BigDecimal getBigDecimal(String name, BigDecimal def) {
        if (missingOrNull(name)) return def;
        return get(name).getAsBigDecimal();
    }

    public BigInteger getBigInt(String name) {
        return getBigInt(name, null);
    }

    public BigInteger getBigInt(String name, BigInteger def) {
        if (missingOrNull(name)) return def;
        return get(name).getAsBigInteger();
    }
    
    public int getInt(String name) {
        return getInt(name, 0);
    }

    public int getInt(String name, int def) {
        if (missingOrNull(name)) return def;
        return json.get(name).getAsInt();
    }

    public short getShort(String name) {
        return getShort(name, (short) 0);
    }

    public short getShort(String name, short def) {
        if (missingOrNull(name)) return def;
        return json.get(name).getAsShort();
    }

    public byte getByte(String name) {
        return getByte(name, (byte) 0);
    }

    public byte getByte(String name, byte def) {
        if (missingOrNull(name)) return def;
        return get(name).getAsByte();
    }

    public JsonWrapper getWrapped(String name) {
        if (missingOrNull(name)) return null;
        return wrap(getObject(name));
    }

    public JsonWrapper createWrapped(String name) {
        JsonWrapper empty = JsonWrapper.create();

        add(name, empty);
        return empty;
    }

    public JsonWrapper getWrappedNonNull(String name) {
        if (missingOrNull(name)) {
            return create();
        }

        return wrap(getObject(name));
    }

    public void add(String name, JsonWrapper buf) {
        json.add(name, buf.json);
    }

    public void addAll(JsonWrapper json) {
        addAll(json.getSource());
    }

    public void addAll(JsonObject json) {
        for (var e: json.entrySet()) {
            add(e.getKey(), e.getValue());
        }
    }

    public boolean isEmpty() {
        return size() < 1;
    }

    public boolean missingOrNull(String name) {
        return !has(name) || get(name).isJsonNull();
    }

    public <T> void addMap(String name, Map<String, T> map, Function<T, JsonElement> func) {
        addMap(name, map, Function.identity(), func);
    }

    public <K, V> void addMap(String name, Map<K, V> map, Function<K, String> keyFunc, Function<V, JsonElement> valueFunc) {
        JsonObject jsonMap = new JsonObject();

        for (Map.Entry<K, V> e : map.entrySet()) {
            jsonMap.add(keyFunc.apply(e.getKey()), valueFunc.apply(e.getValue()));
        }

        json.add(name, jsonMap);
    }

    public <K, V> Map<K, V> getMap(String name, Function<String, K> keyFunc, Function<JsonElement, V> valueFunc) {
        return getMap(name, keyFunc, valueFunc, false);
    }

    public <K, V> Map<K, V> getMap(String name, Function<String, K> keyFunc, Function<JsonElement, V> valueFunc, boolean returnEmptyIfMissing) {
        if (missingOrNull(name)) return returnEmptyIfMissing ? new HashMap<>() : null;

        JsonObject json = getObject(name);
        Map<K, V> result = new HashMap<>();

        for (Map.Entry<String, JsonElement> e : json.entrySet()) {
            result.put(keyFunc.apply(e.getKey()), valueFunc.apply(e.getValue()));
        }

        return result;
    }

    public void writeMap(Map<String, ? extends JsonSerializable> map) {
        writeMap(map, JsonSerializable::serialize);
    }

    public <V> void writeMap(Map<String, V> map, Function<V, JsonElement> function) {
        writeMap(map, Function.identity(), function);
    }

    public <K, V> void writeMap(Map<K, V> map, Function<K, String> keyFunc, Function<V, JsonElement> valueFunc) {
        for (Map.Entry<K, V> e : map.entrySet()) {
            add(keyFunc.apply(e.getKey()), valueFunc.apply(e.getValue()));
        }
    }

    public <V> Map<String, V> asMap(Function<JsonElement, V> valueFunc) {
        return asMap(Function.identity(), valueFunc);
    }

    public <K, V> Map<K, V> asMap(Function<String, K> keyFunc, Function<JsonElement, V> valueFunc) {
        Map<K, V> map = new Object2ObjectOpenHashMap<>();

        for (Map.Entry<String, JsonElement> e : entrySet()) {
            map.put(keyFunc.apply(e.getKey()), valueFunc.apply(e.getValue()));
        }

        return map;
    }

    public <T> void addArray(String name, T[] arr, Function<T, JsonElement> converter) {
        JsonArray array = new JsonArray();

        for (T t : arr) {
            array.add(converter.apply(t));
        }

        json.add(name, array);
    }

    public <T> T[] getArray(String name, Function<JsonElement, T> parser, IntFunction<T[]> arrayCreator) {
        JsonArray array = getArray(name);
        T[] arr = arrayCreator.apply(array.size());

        for (int i = 0; i < array.size(); i++) {
            arr[i] = parser.apply(array.get(i));
        }

        return arr;
    }

    public int[] getIntArray(String name) {
        JsonArray array = getArray(name);
        return JsonUtils.readIntArray(array);
    }

    public void addTimeStamp(String name, long time) {
        addDate(name, new Date(time));
    }

    public long getTimeStamp(String name) {
        return getTimeStamp(name, -1L);
    }

    public long getTimeStamp(String name, long def) {
        if (missingOrNull(name)) {
            return def;
        }

        var get = getPrimitive(name);

        if (get.isNumber()) {
            return get.getAsLong();
        }

        return JsonUtils.readDate(get)
                .getTime();
    }

    public void addDate(String name, Date date) {
        add(name, JsonUtils.writeDate(date));
    }

    public Date getDate(String name) {
        return getDate(name, null);
    }

    public Date getDate(String name, Date def) {
        return get(name, JsonUtils::readDate, def);
    }

    public Component getComponent(String name) {
        return missingOrNull(name) ? null : JsonUtils.readText(get(name));
    }

    public void addComponent(String name, Component component) {
        add(name, JsonUtils.writeText(component));
    }

    public void add(String name, Vectori v) {
        add(name, Vectors.writeJson(v));
    }

    public void add(String name, Vectorl v) {
        add(name, Vectors.writeJson(v));
    }

    public void add(String name, Vectorf v) {
        add(name, Vectors.writeJson(v));
    }

    public void add(String name, Vectord v) {
        add(name, Vectors.writeJson(v));
    }

    //------------------ Delegate Methods -----------------//

    public void add(String property, JsonElement value) {
        json.add(property, value);
    }
    
    public void remove(String property) {
        json.remove(property);
    }

    public void add(String property, String value) {
        json.addProperty(property, value);
    }
    
    public void add(String property, byte value) {
        json.addProperty(property, value);
    }

    public void add(String property, short value) {
        json.addProperty(property, value);
    }
    
    public void add(String property, int value) {
        json.addProperty(property, value);
    }
    
    public void add(String property, float value) {
        json.addProperty(property, value);
    }

    public void add(String property, double value) {
        json.addProperty(property, value);
    }

    public void add(String property, long value) {
        json.addProperty(property, value);
    }
    
    public void add(String property, boolean value) {
        json.addProperty(property, value);
    }
    
    public void add(String property, char value) {
        json.addProperty(property, value);
    }

    public Set<Map.Entry<String, JsonElement>> entrySet() {
        return json.entrySet();
    }

    public int size() {
        return json.size();
    }
    
    public boolean has(String memberName) {
        return json.has(memberName);
    }

    public JsonElement get(String memberName) {
        return json.get(memberName);
    }

    public JsonPrimitive getPrimitive(String memberName) {
        return json.getAsJsonPrimitive(memberName);
    }

    public JsonArray getArray(String memberName) {
        return json.getAsJsonArray(memberName);
    }

    public JsonObject getObject(String memberName) {
        return json.getAsJsonObject(memberName);
    }

    public JsonObject getSource() {
        return json;
    }

    public JsonObject nullIfEmpty() {
        return isEmpty() ? null : json;
    }

    public String toString() {
        return json.toString();
    }

    public boolean equals(Object o) {
        return json.equals(o);
    }

    public int hashCode() {
        return json.hashCode();
    }
}