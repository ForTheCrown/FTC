package net.forthecrown.useables;

import net.forthecrown.core.Crown;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.LocationFileName;
import net.forthecrown.utils.math.WorldVec3i;
import org.bukkit.block.TileState;

public class FtcUsableBlock extends AbstractUsable implements UsableBlock {
    private final WorldVec3i location;

    public FtcUsableBlock(WorldVec3i location, boolean create){
        super(LocationFileName.of(location).toString(), "signs", !create);
        this.location = location;

        if(!fileExists && !create) throw new IllegalStateException(fileName + " doesn't exist");

        Crown.getUsables().addBlock(this);
        reload();
    }

    @Override
    protected void save(JsonWrapper json) {
        json.add("location", getLocation());
        saveInto(json);
    }

    @Override
    protected void reload(JsonWrapper json) {
        reloadFrom(json);
    }

    @Override
    protected void createDefaults(JsonWrapper json) {
        json.add("location", getLocation());
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

        Crown.getUsables().removeBlock(this);
        TileState sign = getBlock();
        sign.getPersistentDataContainer().remove(UsablesManager.USABLE_KEY);
        sign.update();
    }
}