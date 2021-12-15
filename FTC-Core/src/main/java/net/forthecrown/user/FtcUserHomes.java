package net.forthecrown.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.core.Permissions;
import net.forthecrown.regions.RegionPos;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.JsonUtils;
import org.bukkit.Location;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FtcUserHomes extends AbstractUserAttachment implements UserHomes {

    // They shouldn't be able to input this in a command
    // Hacky solution, but it'll work, and it also won't require me to transform the way
    // homes are stored
    public static final String HOME_REGION_JSON_NAME = "user:home:region";

    public Map<String, Location> homes = new HashMap<>();
    public RegionPos homeRegion;

    public FtcUserHomes(FtcUser user){
        super(user);

        checkOverLimit();
    }

    public void checkOverLimit(){ //Remove homes until under limit
        if(user.isOp()) return;

        homes.entrySet().removeIf(e -> check());
    }

    private boolean check(){
        if(getUser().hasPermission(Permissions.ADMIN)) return false;

        int max = user.getRankTier().maxHomes;
        int current = size();

        return max < current;
    }

    @Override
    public void clear() {
        homes.clear();
    }

    @Override
    public int size(){
        return homes.size();
    }

    @Override
    public boolean contains(String name){
        return homes.containsKey(name);
    }

    @Override
    public boolean canMakeMore(){
        if(user.isOp()) return true;

        int currentHomes = size();
        int maxHomes = user.getRankTier().maxHomes;

        return currentHomes <= maxHomes;
    }

    @Override
    public boolean isEmpty() {
        checkOverLimit();
        return homes.isEmpty();
    }

    @Override
    public Map<String, Location> getHomes() {
        checkOverLimit();
        return homes;
    }

    @Override
    public Set<String> getHomeNames(){
        checkOverLimit();
        return homes.keySet();
    }

    @Override
    public Collection<Location> getHomeLocations(){
        checkOverLimit();
        return homes.values();
    }

    @Override
    public void set(String name, Location location){
        homes.put(name, location);
    }

    @Override
    public void remove(String name){
        homes.remove(name);
    }

    @Override
    public Location get(String name){
        return homes.get(name);
    }

    @Override
    public RegionPos getHomeRegion() {
        return homeRegion;
    }

    @Override
    public void setHomeRegion(RegionPos cords) {
        this.homeRegion = cords;
    }

    @Override
    public JsonObject serialize() {
        if(homes.isEmpty()) return null;

        JsonWrapper json = JsonWrapper.empty();
        if(hasHomeRegion()) json.add(HOME_REGION_JSON_NAME, homeRegion.toString());

        for (Map.Entry<String, Location> e: homes.entrySet()){
            json.addLocation(e.getKey(), e.getValue());
        }

        return json.nullIfEmpty();
    }

    @Override
    public void deserialize(JsonElement element) {
        homes.clear();
        homeRegion = null;

        if(element == null) return;
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        if(json.has(HOME_REGION_JSON_NAME)) {
            this.homeRegion = RegionPos.fromString(json.getString(HOME_REGION_JSON_NAME));
            json.remove(HOME_REGION_JSON_NAME);
        }

        for (Map.Entry<String, JsonElement> e: json.entrySet()) {
            homes.put(e.getKey(), JsonUtils.readLocation(e.getValue().getAsJsonObject()));
        }
    }
}
