package net.forthecrown.core;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.utils.FtcUtils;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.util.CachedServerIcon;

import java.io.File;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Dynamically/randomly changes the server icon
 */
public class ServerIcons {
    private static final Logger LOGGER = Crown.logger();

    /**
     * A count of how many pride icons there are lmao
     */
    public static int PRIDE_COUNT = 0;

    public static final String
        TAG_NORMAL      = "default",
        TAG_WHITELIST   = "maintenance",
        TAG_DEBUG       = "debug_mode",
        PREFIX_PRIDE    = "pride_";

    private static final Map<String, CachedServerIcon> SERVER_ICONS = new Object2ObjectOpenHashMap<>();

    static void loadIcons() {
        File dir = folder();

        if (!dir.exists()) {
            LOGGER.warn("Server icon directory doesn't exist, cannot load");
            return;
        }

        for (File f: dir.listFiles()) {
            loadIcon(f);
        }
    }

    static File folder() {
        return new File("icons");
    }

    /**
     * Loads an icon from the given file into this
     * @param file The file to load
     * @return The loaded icon
     */
    public static CachedServerIcon loadIcon(File file) {
        try {
            // Format file name to create formatted name
            String tag = file.getName()
                    .toLowerCase()
                    .replaceAll("icon_", "")
                    .replaceAll( ".jpg", "")
                    .replaceAll(".jpeg", "")
                    .replaceAll( ".png", "")
                    .replaceAll("_icon", "")
                    .trim();

            // Formatting name caused name to become blank :(
            if (tag.isBlank()) {
                LOGGER.error("Invalid icon file name: {}", file.getName());
                return null;
            }

            // If this is a pride icon, increment pride counter lol
            if (tag.contains(PREFIX_PRIDE)) {
                PRIDE_COUNT++;
            }

            CachedServerIcon icon = Bukkit.getServer().loadServerIcon(file);
            SERVER_ICONS.put(tag, icon);

            LOGGER.info("Loaded icon: '{}', file: '{}'", tag, file.getPath());
            return icon;
        } catch (Exception e) {
            LOGGER.error("Error loading server icon: " + file.getName(), e);
            return null;
        }
    }

    /**
     * Gets the server icon to display currently
     * @return The current server icon
     */
    public static CachedServerIcon getCurrent() {
        // None to display :( display default
        if (SERVER_ICONS.isEmpty()) {
            return Bukkit.getServerIcon();
        }

        // Debug mode -> debug icon
        if (Crown.inDebugMode()) {
            return get(TAG_DEBUG);
        }

        // Whitelist on -> maintenance icon
        if (Bukkit.hasWhitelist()) {
            return get(TAG_WHITELIST);
        }

        ZonedDateTime time = ZonedDateTime.now();

        // Pride month? Select an icon with a random LGBTQ flag
        if (time.getMonth() == Month.JUNE) {
            return get(PREFIX_PRIDE + (FtcUtils.RANDOM.nextInt(PRIDE_COUNT)));
        }

        return get(TAG_NORMAL);
    }

    public static CachedServerIcon get(String tag) {
        return SERVER_ICONS.get(tag);
    }
}