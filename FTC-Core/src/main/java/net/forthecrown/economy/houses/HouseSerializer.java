package net.forthecrown.economy.houses;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.core.Crown;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.JsonUtils;

import java.io.File;
import java.io.IOException;

public final class HouseSerializer {
    private HouseSerializer() {}

    public static void serialize() {
        JsonWrapper json = JsonWrapper.empty();

        for (House h: Registries.HOUSES) {
            json.add(h.toString(), h.serializeFull());
        }

        try {
            JsonUtils.writeFile(json.getSource(), getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deserialize() {
        JsonWrapper json;

        try {
            json = JsonWrapper.of(JsonUtils.readFileObject(getFile()));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        for (House h: Registries.HOUSES) {
            JsonElement element = json.get(h.toString());
            h.deserialize(element == null ? new JsonObject() : element);
        }
    }

    private static File getFile() {
        File file = new File(Crown.dataFolder(), "houses.json");

        if(file.isDirectory()) file.delete();
        if(!file.exists()) Crown.saveResource(true, "houses.json");

        return file;
    }
}
