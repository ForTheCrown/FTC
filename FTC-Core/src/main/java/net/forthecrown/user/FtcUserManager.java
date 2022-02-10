package net.forthecrown.user;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Crown;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.serializer.UserJsonSerializer;
import net.forthecrown.serializer.UserSerializer;
import net.forthecrown.user.actions.FtcUserActionHandler;
import net.forthecrown.user.actions.UserActionHandler;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class FtcUserManager extends AbstractJsonSerializer implements UserManager {

    public static final Map<UUID, FtcUser> LOADED_USERS = new Object2ObjectOpenHashMap<>();
    public static final Map<UUID, FtcUserAlt> LOADED_ALTS = new Object2ObjectOpenHashMap<>();

    private final FtcUserActionHandler actionHandler = new FtcUserActionHandler();
    private final UserJsonSerializer serializer = new UserJsonSerializer();

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
            File userDir = UserJsonSerializer.USER_DIR;
            List<CrownUser> users = new ObjectArrayList<>();

            for (File f: userDir.listFiles()) {
                String fName = f.getName();
                UUID id = UUID.fromString(fName.substring(0, fName.indexOf('.')));

                try {
                    users.add(UserManager.getUser(id));
                } catch (Exception e) {
                    // It appears some user files were created wrongly,
                    // Like there's a 'Wou' user file, IDK how that exists,
                    // but it does lol, so gotta check for them
                    Crown.logger().error("Couldn't load data of user " + id + ". Corrupted or invalid data?", e);
                }
            }

            return users;
        });
    }

    @Override
    public UserActionHandler getActionHandler() {
        return actionHandler;
    }

    @Override
    public UserSerializer getSerializer() {
        return serializer;
    }

    @Override
    public void unloadOffline() {
        Iterator<Map.Entry<UUID, FtcUser>> iterator = LOADED_USERS.entrySet().iterator();

        while (iterator.hasNext()) {
            FtcUser u = iterator.next().getValue();
            if(!u.isOnline()) iterator.remove();
        }
    }
}