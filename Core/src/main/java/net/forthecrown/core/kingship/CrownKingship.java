package net.forthecrown.core.kingship;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import net.forthecrown.core.Crown;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CrownKingship extends AbstractJsonSerializer implements Kingship {

    private UUID id;
    private boolean female;

    public CrownKingship(){
        super("king");

        reload();
        Crown.logger().info("Kingship loaded");
    }

    private void attemptSetting(String from){
        try {
            this.id = UUID.fromString(from);
        } catch (IllegalArgumentException e){
            this.id = null;
        }
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
    public boolean isFemale() {
        return female;
    }

    @Override
    public void setFemale(boolean female) {
        this.female = female;
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
    protected void save(JsonBuf json) {
        json.add("uuid", (id == null ? null : id.toString()));
        json.add("female", female);
    }

    @Override
    protected void reload(JsonBuf json) {
        JsonElement uuid = json.get("uuid");
        if(uuid != null && uuid.isJsonPrimitive()) attemptSetting(uuid.getAsString());
        else this.id = null;

        this.female = json.get("female").getAsBoolean();
    }

    @Override
    protected void createDefaults(JsonBuf json) {
        json.add("uuid", JsonNull.INSTANCE);
        json.add("female", new JsonPrimitive(female));
    }
}
