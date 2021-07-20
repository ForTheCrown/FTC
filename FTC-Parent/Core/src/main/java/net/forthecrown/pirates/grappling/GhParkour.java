package net.forthecrown.pirates.grappling;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.CrownCore;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.enums.Branch;
import net.forthecrown.utils.Worlds;
import org.bukkit.Location;

import java.util.*;

public class GhParkour extends AbstractJsonSerializer {

    public static final Location EXIT = new Location(Worlds.VOID, 1, 1, 1);
    private final Map<String, GhLevelData> byName = new HashMap<>();
    private final ArrayList<GhLevelData> orderedList = new ArrayList<>();

    public GhParkour() {
        super("parkour_data");

        Registries.NPCS.register(CrownCore.coreKey("gh_jack"), (player, entity) -> {
            CrownUser user = UserManager.getUser(player);

            if(user.getBranch() != Branch.PIRATES) throw FtcExceptionProvider.notPirate();

            GhLevelSelector.SELECTOR_MENU.open(user);
        });

        reload();
    }

    public GhLevelData byName(String s){
        return byName.get(s);
    }

    public GhLevelData byIndex(int index) {
        return orderedList.get(index);
    }

    public void add(GhLevelData data){
        add0(data);
        GhLevelSelector.recreateSelector();
    }

    private void add0(GhLevelData data) {
        byName.put(data.getName(), data);
        orderedList.add(data);
    }

    public void resetProgress(UUID uuid) {
        byName.values().forEach(d -> d.uncomplete(uuid));
    }

    public boolean isFirstUncompleted(UUID uuid, GhLevelData data) {
        if(data.hasCompleted(uuid)) return false;

        for (GhLevelData level : orderedList) {
            if (level.getNextLevel() == null) continue;
            if (!level.getNextLevel().equalsIgnoreCase(data.getName())) continue;

            return data.hasCompleted(uuid);
        }

        return false;
    }

    public Set<String> keySet() {
        return byName.keySet();
    }

    public Collection<GhLevelData> values() {
        return byName.values();
    }

    public Set<Map.Entry<String, GhLevelData>> entrySet() {
        return byName.entrySet();
    }

    public List<GhLevelData> getOrderedList() {
        return orderedList;
    }

    @Override
    protected void save(JsonObject json) {
        for (GhLevelData d: orderedList) {
            json.add(d.getName(), d.serialize());
        }
    }

    @Override
    protected void reload(JsonObject json) {
        byName.clear();
        orderedList.clear();

        for (Map.Entry<String, JsonElement> e: json.entrySet()) {
            add0(new GhLevelData(e.getKey(), e.getValue()));
        }
    }

    @Override
    protected JsonObject createDefaults(JsonObject json) {
        return json;
    }
}
