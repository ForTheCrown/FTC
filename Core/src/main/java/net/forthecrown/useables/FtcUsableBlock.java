package net.forthecrown.useables;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.forthecrown.core.Crown;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.math.WorldVec3i;
import org.bukkit.block.TileState;

public class FtcUsableBlock extends AbstractUsable implements UsableBlock {
    private final WorldVec3i location;

    public FtcUsableBlock(WorldVec3i location, boolean create){
        super(FtcUtils.locationToFilename(location), "signs", !create);
        this.location = location;

        if(!fileExists && !create) throw new IllegalStateException(fileName + " doesn't exist");

        Crown.getUsablesManager().addBlock(this);
        reload();
    }

    @Override
    protected void save(JsonWrapper json) {
        json.add("location", location);

        saveInto(json);
    }

    @Override
    protected void reload(JsonWrapper json) {
        reloadFrom(json);
    }

    @Override
    protected void createDefaults(JsonWrapper json) {
        json.add("location", location);
        json.add("preconditions", new JsonObject());
        json.add("actions", new JsonArray());
    }

    @Override
    public WorldVec3i getLocation() {
        return location;
    }

    @Override
    public TileState getBlock() {
        return (TileState) location.getBlock().getState();
    }

    @Override
    public void delete() {
        deleteFile();

        Crown.getUsablesManager().removeBlock(this);
        TileState sign = getBlock();
        sign.getPersistentDataContainer().remove(UsablesManager.USABLE_KEY);
        sign.update();
    }
}
