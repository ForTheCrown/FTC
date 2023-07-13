package net.forthecrown.core.user;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.core.user.UserLookupImpl.UserLookupEntry;
import net.forthecrown.user.TimeField;
import net.forthecrown.user.UserComponent;
import net.forthecrown.utils.ScoreIntMap;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import org.slf4j.Logger;

@Getter
public class UserDataStorage {

  private static final Logger LOGGER = Loggers.getLogger();

  public static final String KEY_LAST_NAME = "lastOnlineName";
  public static final String KEY_NAME = "name";
  public static final String KEY_PREVIOUS_NAMES = "previousNames";
  public static final String KEY_IP = "ip";
  public static final String KEY_LAST_LOC = "lastLocation";
  public static final String KEY_LOCATION = "location";
  public static final String KEY_TIMESTAMPS = "timeStamps";

  private final Path directory;
  private final Path userDirectory;

  private final Path alts;

  private final Path balances;
  private final Path gems;
  private final Path playtime;
  private final Path votes;

  private final Path userLookup;

  public UserDataStorage(Path dir) {
    this.directory = dir;
    this.userDirectory = directory.resolve("userdata");

    this.alts     = directory.resolve("alts.json");

    this.balances = directory.resolve("balances.json");
    this.gems     = directory.resolve("gems.json");
    this.playtime = directory.resolve("playtime.json");
    this.votes    = directory.resolve("votes.json");

    this.userLookup = directory.resolve("profiles.json");
  }

  public Path file(String first, String... others) {
    return this.directory.resolve(directory.getFileSystem().getPath(first, others));
  }

  public void saveMap(ScoreIntMap<UUID> map, Path file) {
    SerializationHelper.writeJsonFile(file, json -> {
      map.forEach(entry -> {
        json.add(entry.key().toString(), entry.value());
      });
    });
  }

  public void loadMap(ScoreIntMap<UUID> map, Path file) {
    map.clear();

    SerializationHelper.readJsonFile(file, json -> {
      for (var e: json.entrySet()) {
        UUID uuid = UUID.fromString(e.getKey());
        int value = e.getValue().getAsInt();

        map.set(uuid, value);
      }
    });
  }

  public void loadProfiles(UserLookupImpl lookup) {
    SerializationHelper.readFile(
        userLookup,
        file -> JsonUtils.readFile(file).getAsJsonArray(),

        array -> {
          for (var e: array) {
            UserLookupEntry entry = loadEntry(e);
            lookup.addEntry(entry);
          }

          lookup.setUnsaved(false);
        }
    );
  }

  public void saveProfiles(UserLookupImpl lookup) {
    if (!lookup.isUnsaved()) {
      return;
    }

    JsonArray arr = new JsonArray();
    lookup.stream().forEach(entry -> {
      JsonElement element = saveEntry(entry);
      arr.add(element);
    });

    if (SerializationHelper.writeJson(userLookup, arr)) {
      lookup.setUnsaved(false);
    }
  }

  private JsonElement saveEntry(UserLookupEntry entry) {
    JsonWrapper json = JsonWrapper.create();
    json.addUUID("uuid", entry.getUniqueId());
    json.add("name", entry.getName());

    if (!Strings.isNullOrEmpty(entry.getNickname())) {
      json.add("nick", entry.getNickname());
    }

    if (!Strings.isNullOrEmpty(entry.getLastName())) {
      json.add("lastName", entry.getLastName());
      json.add("lastNameChange", entry.getLastNameChange());
    }

    return json.getSource();
  }

  private UserLookupEntry loadEntry(JsonElement element) {
    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());
    UUID uuid = json.getUUID("uuid");
    String name = json.getString("name");

    UserLookupEntry entry = new UserLookupEntry(uuid);
    entry.setName(name);

    if (json.has("nick")) {
      entry.setNickname(json.getString("nick"));
    }

    if (json.has("lastName")) {
      entry.setLastName(json.getString("lastName"));
      entry.setLastNameChange(json.getLong("lastNameChange"));
    }

    return entry;
  }

  public void loadAlts(AltUsers users) {
    SerializationHelper.readJsonFile(alts, users::load);
  }

  public void saveAlts(AltUsers users) {
    SerializationHelper.writeJsonFile(alts, users::save);
  }

  public void saveUser(UserImpl user) {
    user.setTimeToNow(TimeField.LAST_LOADED);

    try {
      SerializationHelper.writeJsonFile(
          getUserFile(user.getUniqueId()),
          json -> saveUserInternal(user, json)
      );
    } catch (Throwable t) {
      LOGGER.error("Error serializing user: " + user.getUniqueId() + " or " + user.getName(), t);
    }
  }

  public void loadUser(UserImpl user) {
    user.setTimeToNow(TimeField.LAST_LOADED);

    try {
      SerializationHelper.readJsonFile(
          getUserFile(user.getUniqueId()),
          json -> loadUserInternal(user, json)
      );
    } catch (Throwable t) {
      LOGGER.error("Error deserializing user: " + user.getUniqueId() + " or " + user.getName(), t);
    }
  }

  private void loadUserInternal(UserImpl user, JsonWrapper json) {
    user.setLastOnlineName(json.getString(KEY_LAST_NAME));

    user.getPreviousNames().addAll(json.getList(KEY_PREVIOUS_NAMES, JsonElement::getAsString));

    if (json.has(KEY_IP)) {
      user.setIp(json.getString(KEY_IP));
    }

    if (json.has(KEY_LAST_LOC)) {
      user.setReturnLocation(json.getLocation(KEY_LAST_LOC));
    }

    if (json.has(KEY_LOCATION)) {
      user.setEntityLocation(json.getLocation(KEY_LOCATION));
    }

    loadTimeFields(json, user);
    loadComponents(json, user);
  }

  private void saveUserInternal(UserImpl user, JsonWrapper json) {
    json.add(KEY_NAME, user.getName());
    json.add(KEY_LAST_NAME, user.getLastOnlineName());

    if (!user.getPreviousNames().isEmpty()) {
      json.addList(KEY_PREVIOUS_NAMES, user.getPreviousNames(), JsonPrimitive::new);
    }

    if (!Strings.isNullOrEmpty(user.getIp())) {
      json.add(KEY_IP, user.getIp());
    }

    if (user.getReturnLocation() != null) {
      json.addLocation(KEY_LAST_LOC, user.getReturnLocation());
    }

    if (user.getLocation() != null) {
      json.addLocation(KEY_LOCATION, user.getLocation());
    }

    if (user.getTimeFields() != null) {
      saveTimeFields(json, user.getTimeFields());
    }

    saveComponents(json, user);
  }

  private void saveTimeFields(JsonWrapper json, long[] fields) {
    if (fields == null || fields.length < 1) {
      return;
    }

    JsonWrapper timeStamps = JsonWrapper.create();

    for (int i = 0; i < fields.length; i++) {
      long timestamp = fields[i];
      TimeField field = TimeField.REGISTRY.orThrow(i);

      if (timestamp == -1 || !field.isSerialized()) {
        continue;
      }

      timeStamps.addTimeStamp(field.getKey(), timestamp);
    }

    if (!timeStamps.isEmpty()) {
      json.add(KEY_TIMESTAMPS, timeStamps);
    }
  }

  private void loadTimeFields(JsonWrapper json, UserImpl user) {
    if (json.missingOrNull(KEY_TIMESTAMPS)) {
      return;
    }

    JsonWrapper timeStamps = json.getWrapped(KEY_TIMESTAMPS);
    long[] fields = new long[TimeField.REGISTRY.size()];
    Arrays.fill(fields, -1);

    for (var e: timeStamps.entrySet()) {
      String key = e.getKey();
      long timestamp = JsonUtils.readTimestamp(e.getValue());

      TimeField.REGISTRY.get(key).ifPresentOrElse(field -> {
        fields[field.getId()] = timestamp;
      }, () -> {
        LOGGER.error("Unknown time stamp '{}'", key);
      });
    }

    user.setTimeFields(fields);
  }

  private void saveComponents(JsonWrapper json, UserImpl user) {
    var it = user.componentIterator();

    while (it.hasNext()) {
      var attachment = it.next();
      saveComponent(json, attachment);
    }
  }

  private void saveComponent(JsonWrapper json, UserComponent component) {
    var element = component.serialize();

    if (component instanceof UnknownComponent unknown) {
      json.add(unknown.getKey(), element);
    }

    if (element == null
        || (element instanceof JsonArray arr && arr.isEmpty())
        || (element instanceof JsonObject obj && obj.size() <= 0)
    ) {
      return;
    }

    ComponentFactory<UserComponent> factory
        = Components.getFactory((Class<UserComponent>) component.getClass());

    Components.REGISTRY.getKey(factory).ifPresent(s -> {
      json.add(s, element);
    });
  }

  private void loadComponents(JsonWrapper json, UserImpl user) {
    json.remove(KEY_IP);
    json.remove(KEY_LAST_NAME);
    json.remove(KEY_PREVIOUS_NAMES);
    json.remove(KEY_LOCATION);
    json.remove(KEY_LAST_LOC);
    json.remove(KEY_TIMESTAMPS);
    json.remove(KEY_NAME);

    JsonWrapper componentsJson = json;

    if (componentsJson.isEmpty()) {
      return;
    }

    for (var e: json.entrySet()) {
      String id = e.getKey();
      JsonElement element = e.getValue();

      ComponentFactory<?> factory = Components.REGISTRY.orNull(id);

      if (factory == null) {
        factory = Components.createUnknown(id);
        LOGGER.error("Unknown component '{}' defaulting to simple component object", id);
      }

      UserComponent component = user.getComponent(factory, false);
      component.deserialize(element);

      if (component instanceof UnknownComponent unknown) {
        unknown.setKey(id);
      }
    }
  }

  public void delete(UUID id) {
    PathUtil.safeDelete(getUserFile(id));
  }

  private Path getUserFile(UUID uuid) {
    return userDirectory.resolve(uuid + ".json");
  }
}