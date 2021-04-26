package net.forthecrown.core.types.user;

import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.UserDataContainer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Allows other plugins to store data in a user's file.
 */
public class FtcUserDataContainer implements ConfigurationSerializable, UserDataContainer {

    private final YamlConfiguration configuration;
    private Map<String, ConfigurationSection> data = new HashMap<>();
    private final FtcUser user;

    FtcUserDataContainer(FtcUser user, YamlConfiguration configuration){
        this.configuration = configuration;
        this.user = user;
    }

    @Override
    public void set(Plugin key, ConfigurationSection section){
        data.put(key.getDescription().getName(), section);
    }

    @Nonnull
    @Override
    public ConfigurationSection get(Plugin key){
        return data.getOrDefault(key.getDescription().getName(), createSection(key));
    }

    @Nonnull
    @Override
    public ConfigurationSection createSection(Plugin key){
        return configuration.getConfigurationSection("DataContainer").createSection(key.getDescription().getName());
    }

    @Override
    public boolean isEmpty(){
        return data.isEmpty();
    }

    @Override
    public void remove(Plugin key){
        data.remove(key.getDescription().getName());
    }

    @Nonnull
    @Override
    public CrownUser getUser(){
        return user;
    }

    public void deserialize(ConfigurationSection section){
        Map<String, ConfigurationSection> tempMap = new HashMap<>();
        for (String s: section.getKeys(false)){
            try {
                tempMap.put(s, section.getConfigurationSection(s));
            } catch (Exception ignored) {}
        }

        data = tempMap;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return new HashMap<>(data);
    }
}
