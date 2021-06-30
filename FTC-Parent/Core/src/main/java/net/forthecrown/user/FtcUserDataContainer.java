package net.forthecrown.user;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
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
    public void set(String key, ConfigurationSection section){
        data.put(key, section);
    }

    @Nonnull
    @Override
    public ConfigurationSection get(String key){
        return data.getOrDefault(key, createSection(key));
    }

    @Nonnull
    @Override
    public ConfigurationSection createSection(String key){
        return configuration.getConfigurationSection("DataContainer").createSection(key);
    }

    @Override
    public boolean isEmpty(){
        return data.isEmpty();
    }

    @Override
    public void remove(String key){
        data.remove(key);
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
