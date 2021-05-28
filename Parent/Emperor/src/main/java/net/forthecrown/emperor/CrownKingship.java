package net.forthecrown.emperor;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.forthecrown.emperor.serialization.AbstractJsonSerializer;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.UserManager;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CrownKingship extends AbstractJsonSerializer<CrownCore> implements Kingship {

    private UUID id;
    private boolean female;

    CrownKingship(Main main){
        super("king", main);

        reload();
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
    public UUID get() {
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
    protected void save(JsonObject json) {
        json.addProperty("uuid", (id == null ? null : id.toString()));
        json.addProperty("female", female);
    }

    @Override
    protected void reload(JsonObject json) {
        JsonElement uuid = json.get("uuid");
        if(uuid != null && uuid.isJsonPrimitive()) attemptSetting(uuid.getAsString());
        else this.id = null;

        this.female = json.get("female").getAsBoolean();
    }

    @Override
    protected JsonObject createDefaults(JsonObject json) {
        json.add("uuid", JsonNull.INSTANCE);
        json.add("female", new JsonPrimitive(female));

        return json;
    }
}
