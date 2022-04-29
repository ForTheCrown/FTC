package net.forthecrown.core;

import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

class FtcKingship extends FtcConfig.ConfigSection implements Kingship {

    private UUID id;

    @Getter @Setter
    private boolean female;

    FtcKingship(){
        super("king");

        Crown.logger().info("Kingship loaded");
    }

    @Override
    public boolean hasKing() {
        return id != null;
    }

    @Override
    public UUID getUniqueId() {
        return id;
    }

    @Override
    public void set(@Nullable UUID uuid) {
        this.id = uuid;
    }

    @Override
    public String getName() {
        if(!hasKing()) return "none";
        return getUser().getName();
    }

    @Override
    public CrownUser getUser() {
        if(!hasKing()) return null;
        return UserManager.getUser(id);
    }

    @Override
    public void deserialize(JsonElement element) {
        if(element == null) {
            female = false;
            id = null;

            return;
        }

        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        this.female = json.getBool("female");
        this.id = json.getUUID("uuid");
    }

    @Override
    public JsonElement serialize() {
        if(id == null) return null;

        JsonWrapper json = JsonWrapper.empty();

        json.addUUID("uuid", id);
        json.add("female", female);

        return json.getSource();
    }
}