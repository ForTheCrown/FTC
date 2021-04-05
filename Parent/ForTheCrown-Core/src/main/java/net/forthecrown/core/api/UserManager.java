package net.forthecrown.core.api;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.files.CrownUserManager;
import net.forthecrown.core.files.FtcUser;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.core.utils.ListUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public interface UserManager extends CrownSerializer<FtcCore> {

    static @NotNull UserManager inst(){
        return FtcCore.getUserManager();
    }

    static CrownUser getUser(Player base){
        return getUser(base.getUniqueId());
    }

    static CrownUser getUser(OfflinePlayer base){
        return getUser(base.getUniqueId());
    }

    static CrownUser getUser(@NotNull UUID base) {
        Validate.notNull(base, "UUID cannot be null");
        if(CrownUserManager.LOADED_USERS.containsKey(base)) return CrownUserManager.LOADED_USERS.get(base);
        return /*inst().isAlt(base) ? new FtcUserAlt(base, inst().getMain(base)) :*/ new FtcUser(base);
    }

    static CrownUser getUser(String name){
        return getUser(CrownUtils.uuidFromName(name));
    }

    static Collection<CrownUser> getLoadedUsers(){
        return new HashSet<>(CrownUserManager.LOADED_USERS.values());
    }

    static Set<CrownUser> getOnlineUsers(){
        return ListUtils.convertToSet(Bukkit.getOnlinePlayers(), UserManager::getUser);
    }

    void saveUsers();

    void reloadUsers();

    UUID getMain(UUID id);

    boolean isAlt(UUID id);

    List<UUID> getAlts(UUID main);

    void addAltEntry(UUID alt, UUID main);

    void removeAltEntry(UUID alt);
}
