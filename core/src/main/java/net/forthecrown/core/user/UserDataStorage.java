package net.forthecrown.core.user;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.user.TimeField;
import net.forthecrown.user.UserComponent;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.SerializationHelper;
import org.slf4j.Logger;

@Getter
public class UserDataStorage {

  private static final Logger LOGGER = Loggers.getLogger();

  public static final String
      KEY_LAST_NAME = "lastOnlineName",
      KEY_PREVIOUS_NAMES = "previousNames",
      KEY_IP = "ip",
      KEY_LAST_LOC = "lastLocation",
      KEY_LOCATION = "location",
      KEY_TIMESTAMPS = "timeStamps";

  private final Path directory;
  private final Path userDirectory;

  public UserDataStorage(Path dir) {
    this.directory = dir;
    this.userDirectory = directory.resolve("data");
  }

  public void serialize(UserImpl user) {
    user.setTimeToNow(TimeField.LAST_LOADED);

    try {
      SerializationHelper.writeJsonFile(
          getUserFile(user.getUniqueId()),
          json -> _serialize(user, json)
      );
    } catch (Throwable t) {
      LOGGER.error("Error serializing user: " + user.getUniqueId() + " or " + user.getName(), t);
    }
  }

  public void deserialize(UserImpl user) {
    user.setTimeToNow(TimeField.LAST_LOADED);

    try {
      SerializationHelper.readJsonFile(
          getUserFile(user.getUniqueId()),
          json -> _deserialize(user, json)
      );
    } catch (Throwable t) {
      LOGGER.error("Error deserializing user: " + user.getUniqueId() + " or " + user.getName(), t);
    }
  }

  private void _deserialize(UserImpl user, JsonWrapper json) {
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

  private void _serialize(UserImpl user, JsonWrapper json) {
    json.add("name", user.getName());
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

    json.add(KEY_TIMESTAMPS, timeStamps);
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

  public void loadComponents(JsonWrapper json, UserImpl user) {
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
    }
  }

  public void delete(UUID id) {
    try {
      Files.delete(getUserFile(id));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Path getUserFile(UUID uuid) {
    return userDirectory.resolve(uuid + ".json");
  }
}