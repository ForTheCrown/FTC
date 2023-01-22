package net.forthecrown.inventory.weapon.ability;

import static net.forthecrown.utils.io.FtcJar.ALLOW_OVERWRITE;
import static net.forthecrown.utils.io.FtcJar.OVERWRITE_IF_NEWER;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import lombok.Getter;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.module.OnEnable;
import net.forthecrown.core.module.OnLoad;
import net.forthecrown.core.registry.Keys;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.core.script2.Script;
import net.forthecrown.core.script2.ScriptSource;
import net.forthecrown.grenadier.types.ArrayArgument;
import net.forthecrown.grenadier.types.TimeArgument;
import net.forthecrown.inventory.weapon.ability.WeaponScriptAbility.ScriptAbilityFactory;
import net.forthecrown.user.User;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.FtcJar;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.Results;
import net.forthecrown.utils.io.SerializationHelper;
import org.apache.commons.lang.ArrayUtils;
import org.apache.logging.log4j.Logger;

public class SwordAbilityManager {

  private static final Logger LOGGER = Loggers.getLogger();

  @Getter
  private static final SwordAbilityManager instance = new SwordAbilityManager();

  @Getter
  private final Registry<WeaponAbilityType> registry = Registries.newRegistry();

  @Getter
  private final Path loaderFile;

  private final ArrayArgument<Long> timeParser;

  @Getter
  private boolean enabled;

  public SwordAbilityManager() {
    this.loaderFile = PathUtil.pluginPath("weapon_abilities.toml");
    timeParser = ArrayArgument.of(TimeArgument.time());
  }

  @OnEnable
  void init() throws IOException {
    FtcJar.saveResources(
        "weapon_abilities.toml",
        getLoaderFile(),
        ALLOW_OVERWRITE | OVERWRITE_IF_NEWER
    );
  }

  @OnLoad
  public void loadAbilities() {
    registry.clear();

    var path = getLoaderFile();
    SerializationHelper.readTomlAsJson(path, wrapper -> {
      enabled = wrapper.getBool("enabled", true);
      wrapper.remove("enabled");

      long genericCooldown = readTicks(wrapper.get("genericBaseCooldown"));
      wrapper.remove("genericBaseCooldown");

      int genericMaxLevel = wrapper.getInt("genericMaxLevel", -1);
      wrapper.remove("genericMaxLevel");

      for (var e: wrapper.entrySet()) {
        var key = e.getKey();

        if (!Keys.isValidKey(key)) {
          LOGGER.warn("{} is an invalid registry key for ability", key);
          continue;
        }

        deserialize(e.getValue(), genericCooldown, genericMaxLevel)
            .resultOrPartial(s -> {
              LOGGER.error("Couldn't deserialize ability {}: {}", key, s);
            })

            .ifPresent(type -> getRegistry().register(key, type));
      }
    });
  }

  DataResult<WeaponAbilityType> deserialize(
      JsonElement element,
      long genericBaseCooldown,
      int genericMaxLevel
  ) {
    if (element == null || !element.isJsonObject()) {
      return DataResult.error("Element was not a object/table");
    }

    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    // Ensure all required keys are present
    var missing = missingKey(json, "item")
        .or(() -> missingKey(json, "recipe"))
        .or(() -> missingKey(json, "displayName"))
        .or(() -> missingKey(json, "script"));

    if (missing.isPresent()) {
      return DataResult.error(missing.get());
    }

    if (genericBaseCooldown == -1 && !json.has("baseCooldown")) {
      return DataResult.error(
          "Generic cooldown is unset, and no 'baseCooldown' value is set"
      );
    }

    if (genericMaxLevel == -1 && !json.has("maxLevel")) {
      return DataResult.error(
          "Generic maxLevel is unset, and no 'maxLevel' value is set"
      );
    }

    var builder = WeaponAbilityType.builder()
        .maxLevel(json.getInt("maxLevel", genericMaxLevel))
        .displayName(json.getComponent("displayName"))
        .item(json.getItem("item"));

    json.getList("description", JsonUtils::readText)
        .forEach(builder::addDesc);

    var items = json.getList("recipe", JsonUtils::readItem);
    var it = items.listIterator();

    while (it.hasNext()) {
      int i = it.nextIndex();
      var n = it.next();

      if (ItemStacks.isEmpty(n)) {
        return Results.errorResult("Item at index %s in recipe is empty", i);
      }

      builder.addItem(n);
    }

    if (ItemStacks.isEmpty(builder.item())) {
      return DataResult.error("Item at 'item' is empty!");
    }

    if (json.has("condition")) {
      Script script = Script.read(json.get("condition"), true);
      script.compile();

      Predicate<User> predicate = user -> {
        script.put("user", user);
        var result = script.eval();
        script.getMirror().remove("user");

        return result.asBoolean()
            .orElse(false);
      };

      builder.condition(predicate);
    }

    long baseCooldown;

    if (json.has("baseCooldown")) {
      baseCooldown = readTicks(json.get("baseCooldown"));
    } else {
      baseCooldown = genericBaseCooldown;
    }

    ScriptSource source = ScriptSource.readSource(json.get("script"), false);
    String[] inputArgs = getArgs(json.get("inputArgs"));

    builder.factory(new ScriptAbilityFactory(baseCooldown, source, inputArgs));

    return DataResult.success(builder.build());
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

  private long readTicks(JsonElement element) {
    if (element == null || !element.isJsonPrimitive()) {
      return -1;
    }

    var prim = element.getAsJsonPrimitive();
    if (prim.isString()) {
      return parseTicks(prim.getAsString());
    }

    return prim.getAsLong();
  }

  public long parseTicks(String input) {
    if (input.contains(":")) {
      LocalTime localTime = LocalTime.parse(input);
      long nano = localTime.toNanoOfDay();
      return Time.millisToTicks(TimeUnit.NANOSECONDS.toMillis(nano));
    }

    try {
      long time = timeParser.parse(new StringReader(input))
          .stream()
          .mapToLong(value -> value)
          .sum();

      return Time.millisToTicks(time);
    } catch (CommandSyntaxException exc) {
      throw new IllegalStateException(exc);
    }
  }
}