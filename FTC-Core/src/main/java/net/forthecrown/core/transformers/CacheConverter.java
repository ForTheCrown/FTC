package net.forthecrown.core.transformers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.serializer.UserJsonSerializer;
import net.forthecrown.user.UserCacheImpl;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class CacheConverter {
    private static final Logger LOGGER = Crown.logger();

    private static File progressFile() {
        return new File(Crown.dataFolder(), "cache_converted");
    }

    public static boolean shouldRun() {
        return !progressFile().exists();
    }

    public static void run() {
        UserCacheImpl cache = (UserCacheImpl) Crown.getUserManager().getCache();
        File progress = progressFile();

        if(progress.exists()) {
            return;
        }

        LOGGER.info("Starting cache data converter");

        try {
            readUserData(cache);
        } catch (Exception e) {
            LOGGER.error("Could not convert user data to cache, stopping", e);
            return;
        }

        try {
            readServerUserCache(cache);
        } catch (Exception e) {
            LOGGER.error("Could not convert usercache.json to FTC cache, stopping", e);
            return;
        }

        LOGGER.info("Cache data conversion completed, saving cache and creating progress file");
        LOGGER.info("Cache sizes: total: {}, nicked: {}", cache.size(), cache.getNicknamed().size());
        Crown.getUserManager().saveCache();

        try {
            progressFile().createNewFile();
        } catch (IOException e) {
            LOGGER.error("Could not create progress file", e);
        }
    }

    private static void readServerUserCache(UserCacheImpl cache) throws IOException {
        File cacheFile = new File("usercache.json");
        JsonArray array = JsonUtils.readFile(cacheFile).getAsJsonArray();

        for (JsonElement e: array) {
            JsonWrapper json = JsonWrapper.of(e.getAsJsonObject());
            UUID id = UUID.fromString(json.getString("uuid"));
            String name = json.getString("name");

            getOrCreate(cache, id, name);

            LOGGER.info("added '{}' or '{}' to cache", id, name);
        }

        LOGGER.info("Usercache.json reading finished");
    }

    private static void readUserData(UserCacheImpl cache) throws IOException {
        File dir = UserJsonSerializer.USER_DIR;

        for (File f: dir.listFiles()) {
            String fName = f.getName();
            UUID id = UUID.fromString(fName.substring(0, fName.lastIndexOf('.')));

            LOGGER.info("attempting to convert data of {}" , id);

            JsonWrapper json = JsonWrapper.of(JsonUtils.readFileObject(f));

            String name = json.getString("lastOnlineName");
            Component nick = json.getComponent("nickname");

            UserCacheImpl.CacheEntryImpl entry = getOrCreate(cache, id, name);
            if(nick != null) cache.onNickChange(entry, ChatUtils.plainText(nick));

            LOGGER.info("Added user data of '{}' or '{}' to cache", id, name);
        }

        LOGGER.info("All user data read to cache");
    }

    private static UserCacheImpl.CacheEntryImpl getOrCreate(UserCacheImpl cache, UUID uuid, String name) {
        UserCacheImpl.CacheEntryImpl entry = cache.getEntry(uuid);

        if(entry == null) return cache.createEntry(uuid, name);
        return entry;
    }
}
