package net.forthecrown.emperor.useables;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.utils.CrownUtils;
import net.forthecrown.emperor.utils.JsonUtils;
import org.bukkit.Location;
import org.bukkit.block.Sign;

public class CrownUsableSign extends AbstractUsable implements UsableSign {
    private final Location location;

    public CrownUsableSign(Location location, boolean create){
        super(CrownUtils.locationToFilename(location), "signs", !create);
        this.location = location;

        if(!fileExists && !create) throw new IllegalStateException(fileName + " doesn't exist");

        CrownCore.getUsablesManager().addSign(this);
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

        CrownCore.getUsablesManager().removeSign(this);
        Sign sign = getSign();
        sign.getPersistentDataContainer().remove(UsablesManager.USABLE_KEY);
        sign.update();
    }
}
