package net.forthecrown.inventory.weapon.ability;

import static net.forthecrown.inventory.weapon.ability.WeaponAbility.START_LEVEL;
import static net.forthecrown.utils.io.FtcJar.ALLOW_OVERWRITE;
import static net.forthecrown.utils.io.FtcJar.OVERWRITE_IF_NEWER;

import com.google.gson.JsonArray;
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
import net.forthecrown.core.registry.Holder;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.core.registry.RegistryListener;
import net.forthecrown.core.script2.Script;
import net.forthecrown.core.script2.ScriptSource;
import net.forthecrown.inventory.weapon.ability.WeaponAbilityType.AbilityTrialArea;
import net.forthecrown.inventory.weapon.ability.WeaponAbilityType.TrialInfoNode;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.FtcJar;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.Results;
import net.forthecrown.utils.io.SerializationHelper;
import net.forthecrown.utils.math.Vectors;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang.ArrayUtils;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.math.vector.Vector3d;

@Getter
public class SwordAbilityManager {

  private static final Logger LOGGER = Loggers.getLogger();

  @Getter
  private static final SwordAbilityManager instance = new SwordAbilityManager();

  private final Registry<WeaponAbilityType> registry = Registries.newRegistry();

  private final Path directory;
  private final Path loaderFile;
  private final Path itemListFile;
  private final Path trialData;

  private boolean enabled;

  private String trialInventoryStore;

  public SwordAbilityManager() {
    this.directory = PathUtil.getPluginDirectory("weapon_abilities");
    this.loaderFile = directory.resolve("loader.toml");
    this.itemListFile = directory.resolve("items.txt");
    this.trialData = directory.resolve("trial_areas.toml");

    registry.setListener(new RegistryListener<>() {
      @Override
      public void onRegister(Holder<WeaponAbilityType> value) {
        var type = value.getValue();
        type.setHolder(value);

        var trialArea = type.getTrialArea();
        if (trialArea != null) {
          trialArea.start();
        }
      }

      @Override
      public void onUnregister(Holder<WeaponAbilityType> value) {
        var type = value.getValue();
        type.setHolder(null);

        var trialArea = type.getTrialArea();
        if (trialArea != null) {
          trialArea.close();
        }
      }
    });
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

    Map<String, AbilityTrialArea> trialData;
    try {
      trialData = readTrialInfo().getOrThrow(false, s -> {});
    } catch (RuntimeException exc) {
      LOGGER.error("Couldn't read trial data", exc);
      return;
    }

    var path = getLoaderFile();
    SerializationHelper.readTomlAsJson(path, wrapper -> {
      enabled = wrapper.getBool("enabled", true);
      wrapper.remove("enabled");

      UpgradeCooldown genericCooldown
          = UpgradeCooldown.read(wrapper.get("genericCooldown"));

      wrapper.remove("genericCooldown");

      UseLimit genericUseLimit;
      if (wrapper.has("genericUseLimit")) {
        genericUseLimit = UseLimit.load(wrapper.get("genericUseLimit"));
        wrapper.remove("genericUseLimit");
      } else {
        genericUseLimit = null;
      }

      AbilityReadContext context = new AbilityReadContext(
          genericCooldown,
          genericUseLimit,
          itemLists,
          trialData
      );

      for (var e: wrapper.entrySet()) {
        var key = e.getKey();

        if (!Registries.isValidKey(key)) {
          LOGGER.warn("{} is an invalid registry key for ability", key);
          continue;
        }

        deserialize(
            e.getKey(),
            e.getValue(),
            context
        )
            .resultOrPartial(s -> {
              LOGGER.error("Couldn't deserialize ability {}: {}", key, s);
            })

            .ifPresent(type -> getRegistry().register(key, type));
      }
    });
  }

  record AbilityReadContext(UpgradeCooldown cooldown,
                            UseLimit useLimit,
                            Map<String, List<ItemStack>> itemMap,
                            Map<String, AbilityTrialArea> trials
  ) {

  }

  DataResult<WeaponAbilityType> deserialize(
      String registryKey,
      JsonElement element,
      AbilityReadContext context
  ) {
    if (element == null || !element.isJsonObject()) {
      return Results.error("Element was not a object/table");
    }

    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    // Ensure all required keys are present
    var missing = missingKey(json, "item")
        .or(() -> missingKey(json, "displayName"))
        .or(() -> missingKey(json, "script"));

    if (missing.isPresent()) {
      return Results.error(missing.get());
    }

    if (context.cooldown == null && !json.has("cooldown")) {
      return Results.error(
          "Generic cooldown is unset, and no 'baseCooldown' value is set"
      );
    }

    if (context.useLimit == null && !json.has("useLimit")) {
      return Results.error(
          "Generic useLimit is unset and no 'useLimit' value is set"
      );
    }

    var builder = WeaponAbilityType.builder()
        .displayName(json.getComponent("displayName"))
        .item(json.getItem("item"));

    json.getList("description", JsonUtils::readText)
        .forEach(builder::addDesc);

    // Read recipe
    var list = context.itemMap.get(registryKey);
    if (list == null) {
      return Results.error("No items found");
    }
    list.forEach(builder::addItem);

    if (ItemStacks.isEmpty(builder.item())) {
      return Results.error("Item at 'item' is empty!");
    }

    if (!json.has("levelUses")) {
      return Results.error("No 'levelUses' set");
    }

    JsonUtils.stream(json.getArray("levelUses"))
        .mapToInt(JsonElement::getAsInt)
        .forEach(builder.levelRequirements()::add);

    if (json.has("advancement")) {
      NamespacedKey key = json.getKey("advancement");
      builder.advancementKey(key);
    }

    if (json.has("useLimit")) {
      builder.limit(UseLimit.load(json.get("useLimit")));
    } else {
      builder.limit(context.useLimit);
    }

    if (json.has("cooldown")) {
      builder.cooldown(UpgradeCooldown.read(json.get("cooldown")));
    } else {
      builder.cooldown(context.cooldown);
    }

    var cd = builder.cooldown();
    if (cd.getMin() <= 0 || cd.getMax() <= 0) {
      return Results.error("Cooldown min or max was below 0");
    }

    AbilityTrialArea trialInfo = context.trials.get(registryKey);
    if (trialInfo != null) {
      builder.trialArea(trialInfo);
    }

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

  DataResult<Map<String, AbilityTrialArea>> readTrialInfo() {
    return SerializationHelper.readTomlAsJson(getTrialData()).flatMap(object -> {
      if (!object.has("trial_world")) {
        return Results.error("JSON had no 'trial_world' set");
      }

      String worldName = object.remove("trial_world").getAsString();
      World world = Bukkit.getWorld(worldName);

      if (world == null) {
        return Results.error("Unknown world '%s'", worldName);
      }

      if (!object.has("inventory_store")) {
        trialInventoryStore = null;
      } else {
        trialInventoryStore = object.remove("inventory_store").getAsString();
      }

      Map<String, AbilityTrialArea> trialData
          = new Object2ObjectOpenHashMap<>();

      for (var e: object.entrySet()) {
        String key = e.getKey();
        JsonWrapper json = JsonWrapper.wrap(e.getValue().getAsJsonObject());

        if (!json.has("position")) {
          return Results.error(
              "Trial area entry '%s' has no 'position' set",
              key
          );
        }

        Vector3d pos = Vectors.read3d(json.get("position"));
        float yaw = 90.0F;
        float pitch = 0.0F;

        if (json.has("rotation")) {
          var rotJson = json.getWrapped("rotation");
          assert rotJson != null;

          yaw = rotJson.getFloat("yaw", yaw);
          pitch = rotJson.getFloat("pitch", pitch);
        }

        Location l = new Location(
            world,
            pos.x(), pos.y(), pos.z(),
            yaw, pitch
        );

        TrialInfoNode node = null;

        if (json.has("info")) {
          node = readInfoNode(json.get("info"))
              .mapError(s -> "Couldn't read info: " + s)
              .resultOrPartial(LOGGER::error)
              .orElse(null);
        }

        boolean giveSword = json.getBool("give_sword");
        int level = json.getInt("ability_level", START_LEVEL);
        Script script;

        if (json.has("script")) {
          script = Script.read(json.get("script"), false);
        } else {
          script = null;
        }

        long cooldown;
        if (json.has("cooldown_override")) {
          cooldown = UpgradeCooldown.readTicks(json.get("cooldown_override"));
        } else {
          cooldown = 2 * 20;
        }

        trialData.put(
            key,
            new AbilityTrialArea(l, node, giveSword, script, level, cooldown)
        );
      }

      return DataResult.success(trialData);
    });
  }

  DataResult<TrialInfoNode> readInfoNode(JsonElement element) {
    if (!element.isJsonArray()) {
      return Results.error("Element was not Array");
    }

    JsonArray arr = element.getAsJsonArray();

    if (arr.isEmpty()) {
      return Results.error("Empty array");
    }

    TrialInfoNode root = null;
    TrialInfoNode lastNode = null;

    for (var e: arr) {
      JsonWrapper json = JsonWrapper.wrap(e.getAsJsonObject());

      if (!json.has("info")) {
        return Results.error("Node has no 'info' value: " + e);
      }

      long delay = 0;

      if (json.has("delay")) {
        delay = UpgradeCooldown.readTicks(json.get("delay"));
      }

      Component text = json.getComponent("info");
      var node = new TrialInfoNode(text, delay);

      if (root == null) {
        root = node;
      } else {
        lastNode.setNext(node);
      }

      lastNode = node;
    }

    return DataResult.success(root);
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