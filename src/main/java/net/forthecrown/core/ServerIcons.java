package net.forthecrown.core;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.module.OnLoad;
import net.forthecrown.core.registry.Keys;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.utils.MonthDayPeriod;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.io.FtcJar;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.SerializationHelper;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.util.CachedServerIcon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Dynamically/randomly changes the server icon
 */
@Getter
public class ServerIcons {
    private static final Logger LOGGER = FTC.getLogger();

    @Getter
    private static final ServerIcons instance = new ServerIcons();

    public static final String
            KEY_DEFAULT = "default",
            KEY_DEBUG = "debug_mode",
            KEY_WHITELIST = "maintenance";

    private final Registry<ServerIcon> icons = Registries.newRegistry();

    private final Path directory;
    private final Path loaderFile;

    ServerIcons() {
        this.directory = Path.of("icons");
        this.loaderFile = directory.resolve("icons.json");

        saveDefaults();
    }

    void saveDefaults() {
        try {
            FtcJar.saveResources("icons", directory);
            LOGGER.debug("Saved default server icon directory");
        } catch (IOException exc) {
            LOGGER.error("Couldn't save default icons!", exc);
        }
    }

    public CachedServerIcon getIcon(String key, Random random) {
        Optional<CachedServerIcon> cachedIcon = icons.get(key)
                .map(icon -> icon.get(random));

        return cachedIcon.orElseGet(() -> {
            if (key.equalsIgnoreCase(KEY_DEFAULT)) {
                return Bukkit.getServerIcon();
            } else {
                return getIcon(KEY_DEFAULT, random);
            }
        });
    }

    public CachedServerIcon getCurrent() {
        if (icons.isEmpty()) {
            return Bukkit.getServerIcon();
        }

        var rand = Util.RANDOM;

        if (FTC.inDebugMode()) {
            return getIcon(KEY_DEBUG, rand);
        }

        if (Bukkit.hasWhitelist()) {
            return getIcon(KEY_WHITELIST, rand);
        }

        var date = LocalDate.now();

        for (var i: icons) {
            if (!i.shouldUse(date)) {
                continue;
            }

            return i.get(rand);
        }

        return getIcon(KEY_DEFAULT, rand);
    }

    @OnLoad
    public void load() {
        SerializationHelper.readJsonFile(loaderFile, wrapper -> {
            for (var e: wrapper.entrySet()) {
                if (!Keys.isValidKey(e.getKey())) {
                    LOGGER.warn("Invalid icon key found! '{}'", e.getKey());
                    continue;
                }

                ServerIcon icon;
                JsonElement element = e.getValue();

                if (element.isJsonPrimitive() || element.isJsonArray()) {
                    icon = new ServerIcon(null, readIconList(element));
                } else {
                    var json = JsonWrapper.wrap(element.getAsJsonObject());

                    List<CachedServerIcon> icons11 = readIconList(
                            json.get("icons")
                    );

                    MonthDayPeriod period = null;

                    if (json.has("period")) {
                        period = MonthDayPeriod.load(json.get("period"));
                    }

                    icon = new ServerIcon(period, icons11);
                }

                if (icon.icons.isEmpty()) {
                    continue;
                }

                icons.register(e.getKey(), icon);
            }
        });
    }

    private List<CachedServerIcon> readIconList(JsonElement element) {
        if (element.isJsonPrimitive()) {
            var stringPath = element.getAsString();
            Path path = directory.resolve(stringPath);
            var icon = loadImage(path);

            if (icon == null) {
                return List.of();
            }

            return List.of(icon);
        }

        var arr = element.getAsJsonArray();
        List<CachedServerIcon> icons = new ObjectArrayList<>(arr.size());

        for (int i = 0; i < arr.size(); i++) {
            var stringPath = arr.get(i).getAsString();
            Path path = directory.resolve(stringPath);
            var icon = loadImage(path);

            if (icon == null) {
                continue;
            }

            icons.add(icon);
        }

        return icons;
    }

    private CachedServerIcon loadImage(Path path) {
        if (Files.notExists(path)) {
            LOGGER.warn("Icon '{}' doesn't exist!", path);
            return null;
        }

        try {
            return Bukkit.loadServerIcon(path.toFile());
        } catch (Exception exc) {
            LOGGER.error("Couldn't load server icon '{}'", path, exc);
            return null;
        }
    }

    @Getter
    @RequiredArgsConstructor
    static class ServerIcon {
        private final MonthDayPeriod period;
        private final List<CachedServerIcon> icons;

        public CachedServerIcon get(Random random) {
            if (icons.isEmpty()) {
                return null;
            }

            if (icons.size() == 1) {
                return icons.get(0);
            }

            return icons.get(random.nextInt(icons.size()));
        }

        public boolean shouldUse(LocalDate date) {
            if (icons.isEmpty() || period == null) {
                return false;
            }

            return period.contains(date);
        }
    }
}