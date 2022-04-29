package net.forthecrown.user;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcVars;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.UserJsonSerializer;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.actions.FtcUserActionHandler;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.TimeUtil;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static net.forthecrown.user.UserCache.NO_NAME_CHANGE;

public final class FtcUserManager extends AbstractJsonSerializer implements UserManager {
    public static final Map<UUID, FtcUser> LOADED_USERS = new Object2ObjectOpenHashMap<>();
    public static final Map<UUID, FtcUserAlt> LOADED_ALTS = new Object2ObjectOpenHashMap<>();

    @Getter private final UserCacheImpl cache = new UserCacheImpl();
    @Getter private final FtcUserActionHandler actionHandler = new FtcUserActionHandler();
    @Getter private final UserJsonSerializer serializer = new UserJsonSerializer();

    // Alt 2 Main
    private final Map<UUID, UUID> alts = new Object2ObjectOpenHashMap<>();

    public FtcUserManager(){
        super("alt_accounts");

        ensureYamlDead();

        reload();
        Crown.logger().info("User manager loaded");
    }

    private void ensureYamlDead() {
        File old = new File(Crown.dataFolder(), "usermanager.yml");
        if(old.exists() || old.isDirectory()) old.delete();
    }

    @Override
    protected void save(JsonWrapper json) {
        for (Map.Entry<UUID, UUID> e: alts.entrySet()) {
            json.add(e.getKey().toString(), e.getValue().toString());
        }
    }

    @Override
    protected void reload(JsonWrapper json) {
        alts.clear();

        for (Map.Entry<String, JsonElement> e: json.entrySet()) {
            UUID first = UUID.fromString(e.getKey());
            UUID second = UUID.fromString(e.getValue().getAsString());

            alts.put(first, second);
        }
    }

    @Override
    public void saveUsers(){
        LOADED_USERS.values().forEach(FtcUser::save);
        LOADED_USERS.entrySet().removeIf(entry -> !entry.getValue().isOnline());
    }

    @Override
    public void reloadUsers(){
        LOADED_USERS.values().forEach(FtcUser::reload);
    }

    @Override
    public UUID getMain(UUID id){
        return alts.get(id);
    }

    @Override
    public boolean isAlt(UUID id){
        return alts.containsKey(id);
    }

    @Override
    public boolean isAltForAny(UUID id, Collection<Player> players){
        UUID main = getMain(id);
        if(main == null) return false;

        for (Player p: players){
            if(main.equals(p.getUniqueId())) return true;
        }
        return false;
    }

    @Override
    public boolean isMainForAny(UUID id, Collection<Player> players){
        List<UUID> id_alts = getAlts(id);

        for (Player p: players){
            if(id_alts.contains(p.getUniqueId())) return true;
        }

        return false;
    }

    @Override
    public List<UUID> getAlts(UUID main){
        List<UUID> list = new ObjectArrayList<>();

        for (Map.Entry<UUID, UUID> entry: alts.entrySet()){
            if(!entry.getValue().equals(main)) continue;
            list.add(entry.getKey());
        }
        return list;
    }

    @Override
    public void addEntry(UUID alt, UUID main){
        alts.put(alt, main);

        CrownUser user = LOADED_USERS.get(alt);

        // If user is offline or not loaded... for some reason
        // Don't do stuff
        if(user == null || !user.isOnline()) return;

        // Unload user and replace entry with alt
        // (getUser automatically returns an alt user
        //  if the user is an alt)
        user.unload();
        UserManager.getUser(alt);
    }

    @Override
    public void removeEntry(UUID alt){
        alts.remove(alt);

        // Same logic as addEntry(UUID, UUID),
        // except with LOADED_ALTS this time
        CrownUser user = LOADED_ALTS.get(alt);
        if(user == null || !user.isOnline()) return;

        user.unload();
        UserManager.getUser(alt);
    }

    @Override
    public CompletableFuture<List<CrownUser>> getAllUsers() {
        return CompletableFuture.supplyAsync(() -> {
            List<CrownUser> users = new ObjectArrayList<>();

            getCache().readerStream()
                    .forEach(entry -> {
                        users.add(UserManager.getUser(entry));
                    });

            return users;
        });
    }

    @Override
    public File getCacheFile() {
        File result = new File(Crown.dataFolder(), "usercache.json");

        if(result.isDirectory()) {
            result.delete();
        }

        if(!result.exists()) {
            try {
                result.createNewFile();
                JsonUtils.writeFile(new JsonArray(), result);
                LOGGER.info("Cache file did not exist, created new one");
            } catch (IOException e) {
                LOGGER.error("Could not create usercache file", e);
            }
        }

        return result;
    }

    JsonArray readCacheFile() {
        File f = getCacheFile();

        try {
            return (JsonArray) JsonUtils.readFile(f);
        } catch (IOException e) {
            LOGGER.error("Could not read usercache file", e);
            return null;
        }
    }

    @Override
    public void saveCache() {
        JsonArray array = new JsonArray();

        cache.readerStream()
                .forEach(reader -> {
                    JsonWrapper json = JsonWrapper.empty();
                    json.addUUID("uuid", reader.getUniqueId());
                    json.add("name", reader.getName());

                    if(reader.getNickname() != null) {
                        json.add("nick", reader.getNickname());
                    }

                    if(reader.getLastName() != null
                            && TimeUtil.hasCooldownEnded(FtcVars.dataRetentionTime.get(), reader.getLastNameChange())
                    ) {
                        json.add("lastName", reader.getLastName());
                        json.add("lastNameChange", reader.getLastNameChange());
                    }

                    array.add(json.getSource());
                });

        try {
            JsonUtils.writeFile(array, getCacheFile());
            LOGGER.info("Saved user cache, savedSize: {}", array.size());
        } catch (IOException e) {
            LOGGER.error("Could not save user cache", e);
        }
    }

    @Override
    public void loadCache() {
        JsonArray array = readCacheFile();
        if(array == null) return;

        List<UserCacheImpl.CacheEntryImpl> entries = new ObjectArrayList<>();

        for (JsonElement e: array) {
            JsonWrapper json = JsonWrapper.of(e.getAsJsonObject());
            UserCacheImpl.CacheEntryImpl entry = new UserCacheImpl.CacheEntryImpl(json.getUUID("uuid"));

            entry.name = json.getString("name");
            entry.nickname = json.getString("nick", null);
            entry.lastName = json.getString("lastName", null);
            entry.lastNameChange = json.getLong("lastNameChange", NO_NAME_CHANGE);

            if(entry.lastNameChange != NO_NAME_CHANGE
                    && TimeUtil.hasCooldownEnded(FtcVars.dataRetentionTime.get(), entry.lastNameChange)
            ) {
                entry.lastName = null;
                entry.lastNameChange = NO_NAME_CHANGE;
            }

            entries.add(entry);
        }

        cache.clear();
        cache.addAll(entries);
    }

    @Override
    public void unloadOffline() {
        Iterator<Map.Entry<UUID, FtcUser>> iterator = LOADED_USERS.entrySet().iterator();

        while (iterator.hasNext()) {
            FtcUser u = iterator.next().getValue();
            u.save();

            if(!u.isOnline()) iterator.remove();
        }
    }
}