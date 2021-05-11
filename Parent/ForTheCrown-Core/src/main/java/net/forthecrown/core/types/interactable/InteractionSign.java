package net.forthecrown.core.types.interactable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.forthecrown.core.api.InteractableSign;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.core.utils.JsonUtils;
import org.bukkit.Location;
import org.bukkit.block.Sign;

public class InteractionSign extends AbstractInteractable implements InteractableSign {

    private final Location location;

    public InteractionSign(Location location, boolean create){
        super(CrownUtils.locationToFilename(location), "signs", !create);
        this.location = location;

        if(!fileExists && !create) throw new IllegalStateException(fileName + " doesn't exist");

        UseablesManager.SIGNS.put(location, this);
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

    @Override
    protected JsonObject createDefaults(JsonObject json) {
        json.add("location", JsonUtils.serializeLocation(location));
        json.add("preconditions", new JsonObject());
        json.add("actions", new JsonArray());

        return json;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public Sign getSign() {
        return (Sign) location.getBlock().getState();
    }

    @Override
    public void delete() {
        deleteFile();

        UseablesManager.SIGNS.remove(location);
        Sign sign = getSign();
        sign.getPersistentDataContainer().remove(UseablesManager.USEABLE_KEY);
        sign.update();
    }
}
