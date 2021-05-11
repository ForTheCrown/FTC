package net.forthecrown.core.types.interactable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.forthecrown.core.api.InteractableEntity;
import org.bukkit.entity.Entity;

import java.util.UUID;

public class InteractionEntity extends AbstractInteractable implements InteractableEntity {
    private final UUID id;
    private final Entity entity;

    public InteractionEntity(Entity entity, boolean create) {
        super(entity.getUniqueId().toString(), "entities", !create);
        if(!create && !fileExists) throw new IllegalStateException(fileName + " doesn't exist");

        this.id = entity.getUniqueId();
        this.entity = entity;

        UseablesManager.ENTITIES.put(entity.getUniqueId(), this);
        reload();
    }

    @Override
    protected void save(JsonObject json) {
        saveInto(json);
    }

    @Override
    protected void reload(JsonObject json) {
        reloadFrom(json);
    }

    public Entity getEntity() {
        return entity;
    }

    public UUID getUniqueId() {
        return id;
    }

    @Override
    protected JsonObject createDefaults(JsonObject json) {
        json.add("uuid", new JsonPrimitive(id.toString()));
        json.add("preconditions", new JsonObject());
        json.add("actions", new JsonArray());

        return json;
    }

    public void delete(){
        deleteFile();

        UseablesManager.ENTITIES.remove(entity.getUniqueId());
        entity.getPersistentDataContainer().remove(UseablesManager.USEABLE_KEY);
    }
}
