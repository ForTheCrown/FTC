package net.forthecrown.user;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.forthecrown.core.Crown;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.SerializationHelper;
import net.forthecrown.utils.Util;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class UserJsonSerializer implements UserSerializer {
    private static final Logger LOGGER = Crown.logger();

    private final Path userDirectory;

    public UserJsonSerializer(Path userDir) {
        this.userDirectory = userDir;

        try {
            if (Files.exists(userDirectory)) {
                Files.createDirectories(userDirectory);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serialize(User user) {
        try {
            SerializationHelper.writeJsonFile(getUserFile(user.getUniqueId()), json -> _serialize(user, json));
        } catch (Throwable t) {
            LOGGER.error("Error serializing user: " + user.getUniqueId() + " or " + user.getName(), t);
        }
    }

    @Override
    public void deserialize(User user) {
        try {
            SerializationHelper.readJsonFile(getUserFile(user.getUniqueId()), json -> _deserialize(user, json));
        } catch (Throwable t) {
            LOGGER.error("Error deserializing user: " + user.getUniqueId() + " or " + user.getName(), t);
        }
    }

    private void _deserialize(User user, JsonWrapper json) {
        user.setLastOnlineName(json.getString("lastOnlineName"));

        user.getPreviousNames().addAll(json.getList("previousNames", JsonElement::getAsString));

        if (json.has("nickname")) {
            user.setNickname(json.getComponent("nickname"));
        }

        if (json.has("ip")) {
            user.setIp(json.getString("ip"));
        }

        if (json.has("lastLocation")) {
            user.setReturnLocation(json.getLocation("lastLocation"));
        }

        if (json.has("location")) {
            user.setEntityLocation(json.getLocation("location"));
        }

        loadComponents(json, user);
    }

    private void _serialize(User user, JsonWrapper json) {
        json.add("name", user.getName());
        json.add("lastOnlineName", user.getLastOnlineName());

        if (!user.getPreviousNames().isEmpty()) {
            json.addList("previousNames", user.getPreviousNames(), JsonPrimitive::new);
        }

        if (user.hasNickname()) {
            json.addComponent("nickname", user.getNickname());
        }

        if (!Util.isNullOrBlank(user.getIp())) {
            json.add("ip", user.getIp());
        }

        if (user.getReturnLocation() != null) {
            json.addLocation("lastLocation", user.getReturnLocation());
        }

        if (user.getLocation() != null) {
            json.addLocation("location", user.getLocation());
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