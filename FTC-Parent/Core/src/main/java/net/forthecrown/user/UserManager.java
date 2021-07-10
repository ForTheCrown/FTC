package net.forthecrown.user;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.serializer.CrownSerializer;
import net.forthecrown.utils.CrownUtils;
import net.forthecrown.utils.ListUtils;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Represents the class manages users and their ALT accounts
 */
public interface UserManager extends CrownSerializer {

    /**
     * Gets the current instance of the UserManager
     * @return the current UserManager instance, same as FtcCore.getUserManager(); lol
     */
    static @NotNull UserManager inst(){
        return CrownCore.getUserManager();
    }

    /**
     * Gets a user for a player
     * @param base The player to get the user of
     * @return The user :|
     */
    static CrownUser getUser(Player base){
        return getUser(base.getUniqueId());
    }

    /**
     * Gets a user for a player
     * @param base The player to get the user of
     * @return obvious innit
     */
    static CrownUser getUser(OfflinePlayer base){
        return getUser(base.getUniqueId());
    }

    /**
     * Gets a user for the corresponding UUID
     * <p>Be careful, as this doesn't check if the UUID belongs to a player or not lol</p>
     * @param base The UUID to get the player of, will create a new user if it doesn't already exist
     * @return A user :I
     */
    static CrownUser getUser(@NotNull UUID base) {
        Validate.notNull(base, "UUID cannot be null");
        if(CrownUserManager.LOADED_USERS.containsKey(base)) return CrownUserManager.LOADED_USERS.get(base);
        return inst().isAlt(base) ? new FtcUserAlt(base, inst().getMain(base)) : new FtcUser(base);
    }

    /**
     * Same as the above 3 methods except it takes in a player's name
     * @param name
     * @return
     */
    static CrownUser getUser(String name){
        return getUser(CrownUtils.uuidFromName(name));
    }

    /**
     * Gets all currently loaded users
     * @return The currently loaded users
     */
    static Collection<CrownUser> getLoadedUsers(){
        return new ArrayList<>(CrownUserManager.LOADED_USERS.values());
    }

    /**
     * Gets all currently online players as users
     * @return All online users
     */
    static Set<CrownUser> getOnlineUsers(){
        return ListUtils.convertToSet(Bukkit.getOnlinePlayers(), UserManager::getUser);
    }

    /**
     * Gets all vanished players
     * @return All vanished players
     */
    static Set<CrownUser> getVanishedUsers(){
        Set<CrownUser> set = new HashSet<>();

        for (CrownUser u: getOnlineUsers()){
            if(u.isVanished()) set.add(u);
        }

        return set;
    }

    static void updateVanishedFromPerspective(CrownUser user){
        if(!user.isOnline()) return;
        if(user.hasPermission(Permissions.VANISH_SEE)) return;

        getVanishedUsers().forEach(u -> user.getPlayer().hidePlayer(CrownCore.inst(), u.getPlayer()));
    }

    /**
     * Saves every user object to file
     */
    void saveUsers();

    /**
     * Reloads all user objects from file
     */
    void reloadUsers();

    /**
     * Gets the main account for the provided ALT UUID
     * @param id AltAccount UUID
     * @return the alt's main, null if no main exists or if it, itself is a main
     */
    UUID getMain(UUID id);

    /**
     * Checks if a UUID belongs to an alt account
     * @param id
     * @return True if it is, false if it isn't lol
     */
    boolean isAlt(UUID id);

    boolean isAltForAny(UUID id, Collection<Player> players);

    boolean isMainForAny(UUID id, Collection<Player> players);

    /**
     * Gets all of the provided UUID's alt accounts
     * @param main the main account's UUID
     * @return All of the known alts that belong to it
     */
    List<UUID> getAlts(UUID main);

    /**
     * Registers the first UUID as an alt for the second
     * @param alt
     * @param main
     */
    void addEntry(UUID alt, UUID main);

    /**
     * Unregisters the UUID as an alt
     * @param alt
     */
    void removeEntry(UUID alt);

    private static List<ServerPlayer> getSpectators() {
        List<ServerPlayer> list = new ArrayList<>();

        for (Player p: Bukkit.getOnlinePlayers()){
            if(p.getGameMode() != GameMode.SPECTATOR) continue;
            list.add(((CraftPlayer) p).getHandle());
        }

        return list;
    }

    static void updateSpectatorTab(){
        ClientboundPlayerInfoPacket packet = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.UPDATE_GAME_MODE, getSpectators());
        ListIterator<ClientboundPlayerInfoPacket.PlayerUpdate> iterator = packet.getEntries().listIterator();

        while(iterator.hasNext()){
            ClientboundPlayerInfoPacket.PlayerUpdate u = iterator.next();
            iterator.set(new ClientboundPlayerInfoPacket.PlayerUpdate(u.getProfile(), u.getLatency(), GameType.SURVIVAL, u.getDisplayName()));
        }

        for (Player p: Bukkit.getOnlinePlayers()) {
            if(p.getGameMode() == GameMode.SPECTATOR) continue;
            ((CraftPlayer) p).getHandle().connection.connection.send(packet);
        }
    }
}
