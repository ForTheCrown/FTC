package net.forthecrown.core.transformers;

import net.forthecrown.core.Crown;
import net.forthecrown.user.UserCache;
import net.forthecrown.user.UserManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.util.UUID;

public class InvalidUserDataFilter {
    private static final Logger LOGGER = Crown.logger();

    public static void run(File userDir, UserManager manager) {
        UserCache cache = manager.getCache();
        int total = 0;

        for (File f: userDir.listFiles()) {
            String fName = f.getName();
            UUID id = UUID.fromString(fName.substring(0, fName.lastIndexOf('.')));

            if(shouldRemove(id)) {
                f.delete();
                cache.remove(id);
                total++;
            }
        }

        LOGGER.info("Removed a total of {} invalid user data files", total);
        manager.saveCache();
    }

    private static boolean shouldRemove(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

        if(!player.hasPlayedBefore()) {
            LOGGER.info("{} or '{}' has not played before, removing", uuid, player.getName());
            return true;
        }

        if(player == null || player.getName() == null) {
            LOGGER.info("{} had no name, removing", uuid);
            return true;
        }

        return false;
    }
}
