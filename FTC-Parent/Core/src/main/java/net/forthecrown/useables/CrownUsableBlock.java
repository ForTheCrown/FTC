package net.forthecrown.useables;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.forthecrown.core.CrownCore;
import net.forthecrown.utils.CrownUtils;
import net.forthecrown.utils.JsonUtils;
import org.bukkit.Location;
import org.bukkit.block.TileState;

public class CrownUsableBlock extends AbstractUsable implements UsableBlock {
    private final Location location;

    public CrownUsableBlock(Location location, boolean create){
        super(CrownUtils.locationToFilename(location), "signs", !create);
        this.location = location;

        if(!fileExists && !create) throw new IllegalStateException(fileName + " doesn't exist");

        CrownCore.getUsablesManager().addBlock(this);
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
        json.add("location", JsonUtils.writeLocation(location));
        json.add("preconditions", new JsonObject());
        json.add("actions", new JsonArray());

        return json;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public TileState getSign() {
        return (TileState) location.getBlock().getState();
    }

    @Override
    public void delete() {
        deleteFile();

        CrownCore.getUsablesManager().removeBlock(this);
        TileState sign = getSign();
        sign.getPersistentDataContainer().remove(UsablesManager.USABLE_KEY);
        sign.update();
    }
}
