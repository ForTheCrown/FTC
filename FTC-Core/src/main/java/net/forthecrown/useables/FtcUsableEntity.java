package net.forthecrown.useables;

import com.google.gson.JsonPrimitive;
import net.forthecrown.core.Crown;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.EntityIdentifier;
import org.bukkit.entity.Entity;

import java.util.UUID;

public class FtcUsableEntity extends AbstractUsable implements UsableEntity {
    private final UUID id;
    private final Entity entity;

    public FtcUsableEntity(Entity entity, boolean create) {
        super(entity.getUniqueId().toString(), "entities", !create);
        if(!create && !fileExists) throw new IllegalStateException(fileName + " doesn't exist");

        this.id = entity.getUniqueId();
        this.entity = entity;

        Crown.getUsables().addEntity(this);
        reload();
    }

    @Override
    protected void save(JsonWrapper json) {
        json.add("uuid", new JsonPrimitive(id.toString()));
        json.add("entityIdentifier", EntityIdentifier.of(entity).toString());

        saveInto(json);
    }

    @Override
    protected void reload(JsonWrapper json) {
        reloadFrom(json);
    }

    public Entity getEntity() {
        return entity;
    }

    public UUID getUniqueId() {
        return id;
    }

    @Override
    protected void createDefaults(JsonWrapper json) {
        json.add("uuid", new JsonPrimitive(id.toString()));
    }

    public void delete(){
        deleteFile();

        Crown.getUsables().removeEntity(this);
        entity.getPersistentDataContainer().remove(UsablesManager.USABLE_KEY);
    }
}