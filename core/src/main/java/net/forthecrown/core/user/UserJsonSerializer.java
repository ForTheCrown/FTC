package net.forthecrown.core.user;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import net.forthecrown.Loggers;
import net.forthecrown.user.TimeField;
import net.forthecrown.user.UserComponent;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import org.slf4j.Logger;

public class UserJsonSerializer {

  private static final Logger LOGGER = Loggers.getLogger();

  public static final String
      KEY_LAST_NAME = "lastOnlineName",
      KEY_PREVIOUS_NAMES = "previousNames",
      KEY_IP = "ip",
      KEY_LAST_LOC = "lastLocation",
      KEY_LOCATION = "location",
      KEY_GUILD = "guild";

  private final Path userDirectory;

  public UserJsonSerializer(Path userDir) {
    this.userDirectory = PathUtil.ensureDirectoryExists(userDir);
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

    saveComponents(json, user);
  }

  public void saveComponents(JsonWrapper json, UserImpl user) {
    var it = user.componentIterator();

    while (it.hasNext()) {
      var attachment = it.next();
      saveComponent(json, attachment);
    }
  }

  public void saveComponent(JsonWrapper json, UserComponent component) {
    var element = component.serialize();

    if (element == null
        || (element instanceof JsonArray arr && arr.isEmpty())
        || (element instanceof JsonObject obj && obj.size() <= 0)
    ) {
      return;
    }

    ComponentFactory<UserComponent> factory
        = Components.getFactory((Class<UserComponent>) component.getClass());

    json.add(factory.getHolderKey(), element);
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