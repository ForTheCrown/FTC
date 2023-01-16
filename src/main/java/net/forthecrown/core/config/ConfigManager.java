package net.forthecrown.core.config;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Set;
import jdk.jfr.Timestamp;
import net.forthecrown.core.FTC;
import net.forthecrown.core.module.OnLoad;
import net.forthecrown.core.module.OnSave;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.PathUtil;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;

public class ConfigManager {

  private static final Logger LOGGER = FTC.getLogger();

  private static final ConfigManager inst = new ConfigManager();

  private final Set<ConfigFileData> configs = new ObjectOpenHashSet<>();

  public static ConfigManager get() {
    return inst;
  }

  public void registerConfig(Class<?> file) {
    Validate.isTrue(file.isAnnotationPresent(ConfigData.class), "Config annotation present");

    ConfigData data = file.getAnnotation(ConfigData.class);

    if (data.filePath().isBlank()) {
      throw new IllegalArgumentException("File path not set");
    }

    String trimmed = data.filePath().trim();
    Path path;

    if (trimmed.startsWith("ForTheCrown/")) {
      path = PathUtil.pluginPath(trimmed.replaceAll("ForTheCrown/", ""));
    } else if (trimmed.startsWith("plugins/")) {
      path = Paths.get(data.filePath());
    } else {
      path = Configs.DIRECTORY.resolve(trimmed);
    }

    configs.add(new ConfigFileData(path, file));
  }

  @OnSave
  public void save() {
    for (var f : configs) {
      saveConfig(f);
    }
  }

  @OnLoad
  public void load() {
    for (var f : configs) {
      var path = f.filePath();

      if (!Files.exists(path)) {
        saveConfig(f);
        continue;
      }

      try {
        JsonObject json = JsonUtils.readFileObject(path);
        forEachField(f, field -> {
          if (!json.has(field.getName())) {
            LOGGER.warn("'{}' config is missing field '{}', not attempting to set it",
                f.filePath().getFileName(),
                field.getName()
            );

            return;
          }

          invokeStaticIfExists(f.configClass(), "onLoad");

          Object value = Configs.GSON.fromJson(json.get(field.getName()), field.getType());
          field.set(null, value);
        });
      } catch (IOException exc) {
        LOGGER.error("Couldn't deserialize '{}'", path, exc);
      }
    }
  }

  private void saveConfig(ConfigFileData f) {
    JsonObject json = new JsonObject();
    var path = f.filePath();

    forEachField(f, field -> {
      if (field.isAnnotationPresent(Timestamp.class)) {
        long value = field.getLong(null);
        json.add(field.getName(), JsonUtils.writeDate(new Date(value)));
      }

      json.add(
          field.getName(),
          Configs.GSON.toJsonTree(field.get(null))
      );
    });

    invokeStaticIfExists(f.configClass(), "onSave");

    try {
      JsonUtils.writeFile(json, path);
    } catch (IOException exc) {
      LOGGER.error("Couldn't serialize file '{}'", path, exc);
    }
  }

  private static void invokeStaticIfExists(Class c, String name) {
    try {
      var onSave = c.getDeclaredMethod(name);
      onSave.setAccessible(true);
      onSave.invoke(null);
    } catch (ReflectiveOperationException exc) {
      if (!(exc instanceof NoSuchMethodException)) {
        if (exc instanceof InvocationTargetException e) {
          exc = e;
        }

        LOGGER.error("Couldn't invoke onSave in {}", c, exc);
      }
    }
  }

  private void forEachField(ConfigFileData file, ReflectiveConsumer consumer) {
    var clazz = file.configClass();

    for (var f : clazz.getDeclaredFields()) {
      f.setAccessible(true);

      int mods = f.getModifiers();

      if (Modifier.isTransient(mods)
          || !Modifier.isStatic(mods)
          || Modifier.isFinal(mods)
      ) {
        continue;
      }

      try {
        consumer.accept(f);
      } catch (ReflectiveOperationException exc) {
        LOGGER.error("Couldn't iterate on field '{}'", f.getName(), exc);
      }
    }
  }

  private interface ReflectiveConsumer {

    void accept(Field field) throws ReflectiveOperationException;
  }

  private record ConfigFileData(Path filePath, Class configClass) {

  }
}