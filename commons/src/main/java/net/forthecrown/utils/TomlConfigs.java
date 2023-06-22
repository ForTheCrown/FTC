package net.forthecrown.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.time.Duration;
import me.lucko.configurate.toml.TOMLConfigurationLoader;
import net.forthecrown.grenadier.types.ArgumentTypes;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

public final class TomlConfigs {
  private TomlConfigs() {}

  public static <T> T loadPluginConfig(JavaPlugin plugin, Class<T> type) {
    return loadConfig("config.toml", plugin, type);
  }

  public static <T> T loadConfig(String configName, JavaPlugin plugin, Class<T> type) {
    plugin.saveResource(configName, false);
    Path path = plugin.getDataFolder().toPath().resolve(configName);

    try {
      ConfigurationNode node = load(path);
      return node.get(type);
    } catch (ConfigurateException exc) {
      throw new RuntimeException(exc);
    }
  }

  public static ConfigurationNode load(Path path) throws ConfigurateException {
    var loader = createLoader(path);
    return loader.load();
  }

  public static TOMLConfigurationLoader createLoader(Path path) {
    return TOMLConfigurationLoader.builder()
        .path(path)
        .defaultOptions(configurationOptions -> {
          return configurationOptions.serializers(builder -> {
            builder.registerExact(Duration.class, createDurationSerializer());
          });
        })
        .setTableIndent(2)
        .setKeyIndent(2)
        .headerMode(HeaderMode.PRESERVE)
        .build();
  }

  public static TypeSerializer<Duration> createDurationSerializer() {
    return new TypeSerializer<>() {
      @Override
      public Duration deserialize(Type type, ConfigurationNode node) throws SerializationException {
        var strValue = node.getString();

        if (strValue == null) {
          long longVal = node.getLong();

          if (longVal == ConfigurationNode.NUMBER_DEF) {
            throw new SerializationException("Don't know how to deserialize Duration");
          }

          return Duration.ofMillis(longVal);
        }

        StringReader reader = new StringReader(strValue);
        try {
          return ArgumentTypes.time().parse(reader);
        } catch (CommandSyntaxException exc) {
          throw new SerializationException(exc);
        }
      }

      @Override
      public void serialize(Type type, @Nullable Duration obj, ConfigurationNode node)
          throws SerializationException
      {
        if (obj == null) {
          node.set(null);
        } else {
          node.set(obj.toMillis());
        }
      }
    };
  }
}