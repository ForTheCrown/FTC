package net.forthecrown.valhalla;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.math.Vector3i;
import net.forthecrown.valhalla.data.*;
import net.kyori.adventure.key.Key;
import net.minecraft.core.Position;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.NamespacedKey;
import org.bukkit.util.BoundingBox;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class VikingRaidSerializer implements RaidSerializer {

    private final File directory;

    VikingRaidSerializer() {
        directory = new File(ForTheCrown.dataFolder().getPath() + File.separator + "raids");
        checkDirectory();
    }

    private void checkDirectory() {
        if(!directory.exists()) directory.mkdir();
        else if(!directory.isDirectory()) {
            directory.delete();
            directory.mkdir();
        }
    }

    @Override
    public VikingRaid deserialize(String keyValue) throws IOException {
        JsonBuf json = JsonBuf.of(JsonUtils.readFile(getFile(keyValue)));

        Position pos = JsonUtils.readPosition(json.getObject("start"));
        BoundingBox region = JsonUtils.readBoundingBox(json.getObject("region"));
        Key key = Key.key(Valhalla.getInstance(), keyValue);

        VikingRaid result = new VikingRaid(key, region, pos);

        if(json.has("lootData")) result.setLootData(readLoot(json.getBuf("lootData")));
        if(json.has("mobData")) result.setMobData(readMobData(json.getBuf("mobData")));

        return result;
    }

    @Override
    public void serialize(VikingRaid raid) {
        JsonBuf json = JsonBuf.empty();

        json.add("start", JsonUtils.writePosition(raid.getStartingPos()));
        json.add("region", JsonUtils.writeBoundingBox(raid.getRegion()));

        if(raid.hasLootData()) json.add("lootData", writeLoot(raid.getLootData()));
        if(raid.hasMobData()) {
            JsonObject mobData = writeMobData(raid.getMobData());
            if(mobData != null) json.add("mobData", mobData);
        }

        try {
            JsonUtils.writeFile(json.getSource(), getFile(raid.key()));
        } catch (IOException e) {
            ForTheCrown.logger().severe("Error while serializing viking raid");
            e.printStackTrace();
        }
    }

    @Override
    public File getFile(String keyValue) throws IOException {
        checkDirectory();
        File result = new File(directory, keyValue + (keyValue.endsWith(".json") ? "" : ".json"));

        if(!result.exists()) {
            result.createNewFile();
            JsonUtils.writeFile(new JsonObject(), result);
        }

        return result;
    }

    @Override
    public File getRaidDirectory() {
        return directory;
    }

    private JsonBuf writeLoot(LootData data) {
        JsonBuf json = JsonBuf.empty();

        if(data.hasDefiniteSpawns()) {
            JsonArray defPos = new JsonArray();

            for (Map.Entry<Vector3i, Key> e: data.getDefiniteSpawns().entrySet()) {
                JsonBuf spawn = JsonBuf.empty();

                spawn.add("pos", e.getKey());
                spawn.addKey("lootTable", e.getValue());

                defPos.add(spawn.getSource());
            }

            json.add("definiteSpawns", defPos);
        }

        if(data.hasChestGroups()) {
            JsonBuf groups = JsonBuf.empty();

            for (ChestGroup g: data.getChestGroups().values()) {
                JsonBuf groupJson = JsonBuf.empty();

                groupJson.addKey("lootTable", g.getLootTableKey());
                groupJson.add("maxChests", g.getMaxChests());
                groupJson.addList("possibleSpawns", g.getPossibleLocations(), Vector3i::serialize);

                groups.add(g.key().asString(), groupJson);
            }

            json.add("chestGroups", groups);
        }

        return json;
    }

    private LootData readLoot(JsonBuf json) {
        Map<Vector3i, Key> defSpawns = new HashMap<>();
        Map<Key, ChestGroup> groups = new HashMap<>();

        if(!json.missingOrNull("definiteSpawns")) {
            JsonArray array = json.getArray("definiteSpawns");

            for (JsonElement e: array) {
                JsonBuf spawn = JsonBuf.of(e.getAsJsonObject());

                Vector3i pos = spawn.getPos("pos");
                Key key = spawn.getKey("lootTable");

                defSpawns.put(pos, key);
            }
        }

        if(!json.missingOrNull("chestGroups")) {
            for (Map.Entry<String, JsonElement> e: json.getObject("chestGroups").entrySet()) {
                Key key = FtcUtils.parseKey(e.getKey());
                JsonBuf group = JsonBuf.of(e.getValue().getAsJsonObject());

                Key lootTable = group.getKey("lootTable");
                byte maxChests = group.getByte("maxChests", (byte) 5);
                ObjectList<Vector3i> pos = new ObjectArrayList<>(group.getList("possibleSpawns", Vector3i::of));

                ChestGroup chestGroup = new ChestGroup(key, lootTable, maxChests, pos);
                groups.put(chestGroup.key(), chestGroup);
            }
        }

        return new LootData(defSpawns, groups);
    }

    private JsonObject writeMobData(MobData data) {
        JsonBuf json = JsonBuf.empty();

        if(data.hasHostile()) json.add("hostile", writeMobList(data.getHostile()));
        if(data.hasPassive()) json.add("passive", writeMobList(data.getPassive()));
        if(data.hasSpecial()) json.add("special", writeMobList(data.getSpecial()));

        return json.nullIfEmpty();
    }

    private JsonArray writeMobList(ObjectList<EntitySpawnData> data) {
        JsonArray array = new JsonArray();

        for (EntitySpawnData d: data) {
            JsonBuf buf = JsonBuf.empty();

            buf.addKey("key", d.getEntityKey());
            buf.add("pos", JsonUtils.writePosition(d.getPosition()));
            if(d.hasTag()) buf.addNBT("tag", d.getTag());

            array.add(buf.getSource());
        }

        return array;
    }

    private MobData readMobData(JsonBuf json) {
        ObjectList<EntitySpawnData> hostile = readMobList(json.get("hostile"));
        ObjectList<EntitySpawnData> special = readMobList(json.get("special"));
        ObjectList<EntitySpawnData> passive = readMobList(json.get("passive"));

        return new MobData(passive, hostile, special);
    }

    private ObjectList<EntitySpawnData> readMobList(JsonElement element) {
        if(element == null || element.isJsonNull()) return null;
        ObjectList<EntitySpawnData> result = new ObjectArrayList<>();

        for (JsonElement e: element.getAsJsonArray()) {
            JsonBuf buf = JsonBuf.of(e.getAsJsonObject());

            Position pos = JsonUtils.readPosition(buf.getObject("pos"));
            NamespacedKey key = FtcUtils.keyToBukkit(buf.getKey("key"));
            CompoundTag tag = buf.getNBT("tag");

            result.add(new EntitySpawnData(key, tag, pos));
        }

        return result.isEmpty() ? null : result;
    }
}
