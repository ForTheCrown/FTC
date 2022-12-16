package net.forthecrown.user;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.forthecrown.core.FTC;
import net.forthecrown.user.data.TimeField;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class UserJsonSerializer implements UserSerializer {
    private static final Logger LOGGER = FTC.getLogger();

    public static final String
            KEY_LAST_NAME = "lastOnlineName",
            KEY_PREVIOUS_NAMES = "previousNames",
            KEY_IP = "ip",
            KEY_LAST_LOC = "lastLocation",
            KEY_LOCATION = "location",
            KEY_GUILD = "guild";

    private final Path userDirectory;

    public UserJsonSerializer(Path userDir) {
        this.userDirectory = PathUtil.ensureDirectoryExists(userDir)
                .orThrow();
    }

    @Override
    public void serialize(User user) {
        user.setTimeToNow(TimeField.LAST_LOADED);

        try {
            SerializationHelper.writeJsonFile(getUserFile(user.getUniqueId()), json -> _serialize(user, json));
        } catch (Throwable t) {
            LOGGER.error("Error serializing user: " + user.getUniqueId() + " or " + user.getName(), t);
        }
    }

    @Override
    public void deserialize(User user) {
        user.setTimeToNow(TimeField.LAST_LOADED);

        try {
            SerializationHelper.readJsonFile(getUserFile(user.getUniqueId()), json -> _deserialize(user, json));
        } catch (Throwable t) {
            LOGGER.error("Error deserializing user: " + user.getUniqueId() + " or " + user.getName(), t);
        }
    }

    private void _deserialize(User user, JsonWrapper json) {
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

        if (json.has(KEY_GUILD)) {
            user.setGuildId(json.getUUID(KEY_GUILD));

            if (user.getGuild() == null) {
                LOGGER.warn("Found unknown guild in {} (or {})'s file: {}",
                        user.getUniqueId(), user.getName(),
                        user.getGuildId()
                );
            }
        }

        loadComponents(json, user);
    }

    private void _serialize(User user, JsonWrapper json) {
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

        if (user.getGuildId() != null) {
            json.addUUID(KEY_GUILD, user.getGuildId());
        }

        saveComponents(json, user);
    }

    public void saveComponents(JsonWrapper json, User user) {
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

        json.add(component.getType().getSerialId(), element);
    }

    public void loadComponents(JsonWrapper json, User user) {
        var it = Components.typeIterator();

        while (it.hasNext()) {
            var type = it.next();

            if (user.hasComponent(type) && !json.has(type.getSerialId())) {
                continue;
            }

            var component = user.getComponent(type);
            var componentJson = json.get(type.getSerialId());
            component.deserialize(componentJson);
        }
    }

    @Override
    public void delete(UUID id) {
        UserManager.get().remove(id);
        UserManager.get().getOnline()
                .remove(id);

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