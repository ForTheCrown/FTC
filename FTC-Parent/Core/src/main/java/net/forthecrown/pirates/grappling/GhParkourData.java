package net.forthecrown.pirates.grappling;

import com.google.gson.JsonElement;
import net.forthecrown.serializer.JsonSerializable;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class GhParkour implements JsonSerializable {

    private Map<String, GhLevelEndData> levelEnds = new HashMap<>();
    private static final String cooldownCategory = "Pirates_GH_end";


    public void use(Player player, String id){
        GhLevelEndData data = levelEnds.get(id);
        Validate.notNull(data, "Invalid ID");


    }

    @Override
    public JsonElement serialize() {
        return null;
    }
}
