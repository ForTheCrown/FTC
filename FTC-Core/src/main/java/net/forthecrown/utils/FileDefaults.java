package net.forthecrown.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.forthecrown.serializer.JsonWrapper;

import java.io.*;
import java.util.Map;

/**
 * A class for ensuring a file has what it's meant to have
 * by default
 * @param <T> The
 */
public interface FileDefaults<T> {
    FileDefaults<JsonWrapper> JSON = new FileDefaults<>() {
        @Override
        public JsonWrapper ofInputStream(InputStream stream) throws IOException {
            return JsonWrapper.of(JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject());
        }

        @Override
        public void addMissingEntries(JsonWrapper current, JsonWrapper example) {
            checkObject(current, example);
        }

        private void checkObject(JsonWrapper current, JsonWrapper example) {
            for (Map.Entry<String, JsonElement> e: example.entrySet()) {
                if(current.has(e.getKey())) {
                    //JsonElement element = current.get(e.getKey());
                    //check(element, example.get(e.getKey()));

                    continue;
                }

                current.add(e.getKey(), e.getValue().deepCopy());
            }
        }

        private void checkArray(JsonArray array, JsonArray example) {
            for (JsonElement e: example) {
                if(!array.contains(e)) continue;
                array.add(e);
            }
        }

        private void check(JsonElement element, JsonElement example) {
            if(element instanceof JsonObject object) {
                checkObject(JsonWrapper.of(object), JsonWrapper.of(example.getAsJsonObject()));
            } else if(element instanceof JsonArray array) {
                checkArray(array, example.getAsJsonArray());
            }
        }

        @Override
        public void save(JsonWrapper val, File file) throws IOException {
            JsonUtils.writeFile(val.getSource(), file);
        }
    };

    T ofInputStream(InputStream stream) throws IOException;

    default T ofFile(File file) throws IOException {
        return ofInputStream(new FileInputStream(file));
    }

    void addMissingEntries(T current, T example);
    void save(T val, File file) throws IOException;

    default void compareAndSave(File current, InputStream example) throws IOException {
        T val = ofFile(current);
        T exampleVal = ofInputStream(example);

        addMissingEntries(val, exampleVal);
        save(val, current);
    }
}