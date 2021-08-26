package net.forthecrown.useables;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.forthecrown.core.Crown;
import net.forthecrown.serializer.JsonBuf;
import org.bukkit.entity.Entity;

import java.util.UUID;

public class CrownUsableEntity extends AbstractUsable implements UsableEntity {
    private final UUID id;
    private final Entity entity;

    public CrownUsableEntity(Entity entity, boolean create) {
        super(entity.getUniqueId().toString(), "entities", !create);
        if(!create && !fileExists) throw new IllegalStateException(fileName + " doesn't exist");

        this.id = entity.getUniqueId();
        this.entity = entity;

        Crown.getUsablesManager().addEntity(this);
        reload();
    }

    @Override
    protected void save(JsonBuf json) {
        json.add("uuid", new JsonPrimitive(id.toString()));
        saveInto(json);
    }

    @Override
    protected void reload(JsonBuf json) {
        reloadFrom(json);
    }

    public Entity getEntity() {
        return entity;
    }

    public UUID getUniqueId() {
        return id;
    }

    @Override
    protected void createDefaults(JsonBuf json) {
        json.add("uuid", new JsonPrimitive(id.toString()));
        json.add("preconditions", new JsonObject());
        json.add("actions", new JsonArray());
    }

    public void delete(){
        deleteFile();

        Crown.getUsablesManager().removeEntity(this);
        entity.getPersistentDataContainer().remove(UsablesManager.USABLE_KEY);
    }
}
