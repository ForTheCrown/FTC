package net.forthecrown.emperor.useables;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
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

        CrownCore.getUsablesManager().addEntity(this);
        reload();
    }

    @Override
    protected void save(JsonObject json) {
        saveInto(json);
    }

    @Override
    protected void reload(JsonObject json) {
        try {
            reloadFrom(json);
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
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

        CrownCore.getUsablesManager().removeEntity(this);
        entity.getPersistentDataContainer().remove(UsablesManager.USABLE_KEY);
    }
}
