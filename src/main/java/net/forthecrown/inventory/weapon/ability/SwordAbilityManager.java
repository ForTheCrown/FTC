package net.forthecrown.inventory.weapon.ability;

import static net.forthecrown.utils.io.FtcJar.ALLOW_OVERWRITE;
import static net.forthecrown.utils.io.FtcJar.OVERWRITE_IF_NEWER;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.module.OnEnable;
import net.forthecrown.core.module.OnLoad;
import net.forthecrown.core.registry.Keys;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.core.script2.ScriptSource;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.FtcJar;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import org.apache.commons.lang.ArrayUtils;
import org.apache.logging.log4j.Logger;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

@Getter
public class SwordAbilityManager {

  private static final Logger LOGGER = Loggers.getLogger();

  @Getter
  private static final SwordAbilityManager instance = new SwordAbilityManager();

  private final Registry<WeaponAbilityType> registry = Registries.newRegistry();

  private final Path directory;
  private final Path loaderFile;
  private final Path itemListFile;

  private boolean enabled;

  public SwordAbilityManager() {
    this.directory = PathUtil.getPluginDirectory("weapon_abilities");
    this.loaderFile = directory.resolve("loader.toml");
    this.itemListFile = directory.resolve("items.txt");
  }

  @OnEnable
  void init() throws IOException {
    FtcJar.saveResources(
        "weapon_abilities",
        getDirectory(),
        ALLOW_OVERWRITE | OVERWRITE_IF_NEWER
    );
  }

  @OnLoad
  public void loadAbilities() {
    registry.clear();

    Map<String, List<ItemStack>> itemLists;
    try {
      itemLists = readItemListFile();
    } catch (IOException exc) {
      LOGGER.error("Couldn't read item list file", exc);
      return;
    }

    var path = getLoaderFile();
    SerializationHelper.readTomlAsJson(path, wrapper -> {
      enabled = wrapper.getBool("enabled", true);
      wrapper.remove("enabled");

      UpgradeCooldown genericCooldown
          = UpgradeCooldown.read(wrapper.get("genericCooldown"));

      wrapper.remove("genericCooldown");

      int genericMaxLevel = wrapper.getInt("genericMaxLevel", -1);
      wrapper.remove("genericMaxLevel");

      UseLimit genericUseLimit;
      if (wrapper.has("genericUseLimit")) {
        genericUseLimit = UseLimit.load(wrapper.get("genericUseLimit"));
        wrapper.remove("genericUseLimit");
      } else {
        genericUseLimit = null;
      }

      for (var e: wrapper.entrySet()) {
        var key = e.getKey();

        if (!Keys.isValidKey(key)) {
          LOGGER.warn("{} is an invalid registry key for ability", key);
          continue;
        }

        deserialize(
            e.getKey(),
            e.getValue(),
            genericCooldown,
            genericMaxLevel,
            genericUseLimit,
            itemLists
        )
            .resultOrPartial(s -> {
              LOGGER.error("Couldn't deserialize ability {}: {}", key, s);
            })

            .ifPresent(type -> getRegistry().register(key, type));
      }
    });
  }

  DataResult<WeaponAbilityType> deserialize(
      String registryKey,
      JsonElement element,
      UpgradeCooldown genericCooldown,
      int genericMaxLevel,
      UseLimit genericUseLimit,
      Map<String, List<ItemStack>> itemMap
  ) {
    if (element == null || !element.isJsonObject()) {
      return DataResult.error("Element was not a object/table");
    }

    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    // Ensure all required keys are present
    var missing = missingKey(json, "item")
        .or(() -> missingKey(json, "displayName"))
        .or(() -> missingKey(json, "script"));

    if (missing.isPresent()) {
      return DataResult.error(missing.get());
    }

    if (genericCooldown == null && !json.has("cooldown")) {
      return DataResult.error(
          "Generic cooldown is unset, and no 'baseCooldown' value is set"
      );
    }

    if (genericMaxLevel == -1 && !json.has("maxLevel")) {
      return DataResult.error(
          "Generic maxLevel is unset, and no 'maxLevel' value is set"
      );
    }

    if (genericUseLimit == null && !json.has("useLimit")) {
      return DataResult.error(
          "Generic useLimit is unset and no 'useLimit' value is set"
      );
    }

    var builder = WeaponAbilityType.builder()
        .maxLevel(json.getInt("maxLevel", genericMaxLevel))
        .displayName(json.getComponent("displayName"))
        .item(json.getItem("item"));

    json.getList("description", JsonUtils::readText)
        .forEach(builder::addDesc);

    if (ItemStacks.isEmpty(builder.item())) {
      return DataResult.error("Item at 'item' is empty!");
    }

    if (json.has("advancement")) {
      NamespacedKey key = json.getKey("advancement");
      builder.advancementKey(key);
    }

    if (json.has("useLimit")) {
      builder.limit(UseLimit.load(json.get("useLimit")));
    } else {
      builder.limit(genericUseLimit);
    }

    if (json.has("cooldown")) {
      builder.cooldown(UpgradeCooldown.read(json.get("cooldown")));
    } else {
      builder.cooldown(genericCooldown);
    }

    var list = itemMap.get(registryKey);

    if (list == null) {
      return DataResult.error("No items found");
    }

    list.forEach(builder::addItem);

    builder
        .source(ScriptSource.readSource(json.get("script"), false))
        .args(getArgs(json.get("inputArgs")));

    return DataResult.success(builder.build());
  }

  Map<String, List<ItemStack>> readItemListFile() throws IOException {
    Map<String, List<ItemStack>> map = new Object2ObjectOpenHashMap<>();

    var reader = Files.newBufferedReader(getItemListFile());
    String section = null;

    String line;
    while ((line = reader.readLine()) != null) {
      line = line.trim();

      if (line.startsWith("#") || line.isBlank()) {
        continue;
      }

      if (line.endsWith(":")) {
        section = line.substring(0, line.length() - 1);
        map.computeIfAbsent(section, s -> new ArrayList<>());
        continue;
      }

      if (section == null) {
        throw new IOException("Item found outside of section");
      }

      ItemStack item = ItemStacks.fromNbtString(line);

      if (ItemStacks.isEmpty(item)) {
        throw new IOException(
            String.format("Item in section '%s' is empty", section)
        );
      }

      map.computeIfAbsent(section, s -> new ArrayList<>()).add(item);
    }

    reader.close();
    return map;
  }

  String[] getArgs(JsonElement element) {
    if (element == null || element.isJsonNull()) {
      return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    if (element.isJsonArray()) {
      var arr = element.getAsJsonArray();
      String[] result = new String[arr.size()];

      for (int i = 0; i < arr.size(); i++) {
        result[i] = arr.get(i).getAsString();
      }

      return result;
    }

    String s = element.getAsString();
    return s.split(" ");
  }

  Optional<String> missingKey(JsonWrapper json, String k) {
    if (json.has(k)) {
      return Optional.empty();
    }

    return Optional.of(String.format("Missing value for: '%s'", k));
  }
}