package net.forthecrown.pirates.grappling;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.forthecrown.pirates.Pirates;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.math.BlockPos;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GhLevelData implements JsonSerializable {

    private final String name;
    private final GhBiome biome;
    private final Material selectorMat;

    private final BlockPos exitDest;
    private final int nextHooks;
    private final int nextDistance;
    private final GhType type;

    private final byte index;
    private final List<UUID> completed = new ArrayList<>();

    public GhLevelData(String name, GhBiome biome, Material selectorMat, BlockPos exitDest, int nextHooks, int nextDistance) {
        this(name, biome, selectorMat, exitDest, nextHooks, nextDistance, GhType.NORMAL);
    }

    public GhLevelData(String name, GhBiome biome, Material selectorMat, BlockPos exitDest) {
        this(name, biome, selectorMat, exitDest, -1, -1, GhType.NORMAL);
    }

    public GhLevelData(String name, GhBiome biome, Material selectorMat, BlockPos exitDest, GhType type) {
        this(name, biome, selectorMat, exitDest, -1, -1, type);
    }

    public GhLevelData(String name, GhBiome biome, Material selectorMat, BlockPos exitDest, int nextHooks) {
        this(name, biome, selectorMat, exitDest, nextHooks, -1, GhType.NORMAL);
    }

    public GhLevelData(String name, GhBiome biome, Material selectorMat, BlockPos exitDest, int nextHooks, GhType type) {
        this(name, biome, selectorMat, exitDest, nextHooks, -1, type);
    }

    public GhLevelData(String name, GhBiome biome, Material selectorMat, BlockPos exitDest, int nextHooks, int nextDistance, GhType type) {
        this.name = name;
        this.biome = biome;
        this.selectorMat = selectorMat;
        this.exitDest = exitDest;
        this.nextHooks = nextHooks;
        this.nextDistance = nextDistance;
        this.type = type;

        this.index = (byte) Pirates.getParkour().getData().entrySet().size();
    }

    public GhLevelData(JsonElement element) {
        JsonObject json = element.getAsJsonObject();

        this.name = json.get("name").getAsString();
        this.exitDest = BlockPos.of(json.get("exitDest"));
        this.biome = JsonUtils.readEnum(GhBiome.class, json.get("biome"));
        this.selectorMat = JsonUtils.readEnum(Material.class, json.get("selectorMat"));
        this.index = json.get("index").getAsByte();

        this.nextHooks = json.has("nextHooks") ? json.get("nextHooks").getAsInt() : -1;
        this.nextDistance = json.has("nextDistance") ? json.get("nextDistance").getAsInt() : -1;

        GhType type = JsonUtils.readEnum(GhType.class, json.get("type"));
        if(type == null) this.type = GhType.NORMAL;
        else this.type = type;

        if(json.has("completed")){
            completed.clear();

            for (JsonElement ele: json.getAsJsonArray("completed")){
                completed.add(UUID.fromString(ele.getAsString()));
            }
        }
    }

    public void giveHook(Player player){
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gh give " + player.getName() +
                (hookLimited() ? "" : " " + nextHooks) +
                (distanceLimited() ? "" : " " + nextDistance)
        );
    }

    public boolean distanceLimited(){
        return nextDistance == -1;
    }

    public boolean hookLimited(){
        return nextHooks == -1;
    }

    public String getName() {
        return name;
    }

    public Component displayName(){
        return Component.text(getName())
                .color(NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false);
    }

    public GhBiome getBiome() {
        return biome;
    }

    public Material getSelectorMat() {
        return selectorMat;
    }

    public Material getCompletedMat(){
        return switch (selectorMat) {
            case GRASS_BLOCK -> Material.GREEN_TERRACOTTA;
            case RED_SANDSTONE -> Material.ORANGE_TERRACOTTA;
            case PURPLE_STAINED_GLASS -> Material.PURPLE_TERRACOTTA;
            case OAK_PLANKS -> Material.TERRACOTTA;
            default -> Material.BLACK_TERRACOTTA;
        };
    }

    public BlockPos getExitDest() {
        return exitDest;
    }

    public Location getExitLoc(World world){
        return exitDest.toLoc(world);
    }

    public int getNextHooks() {
        return nextHooks;
    }

    public int getNextDistance() {
        return nextDistance;
    }

    public GhType getType() {
        return type;
    }

    public boolean hasCompleted(UUID id){
        return completed.contains(id);
    }

    public void complete(UUID id){
        completed.add(id);
    }

    public void removeCompleted(UUID id){
        completed.remove(id);
    }

    public List<UUID> getCompleted() {
        return completed;
    }

    public byte getIndex() {
        return index;
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();

        json.addProperty("name", name);
        json.addProperty("index", index);

        json.add("biome", JsonUtils.writeEnum(biome));
        json.add("selectorMat", JsonUtils.writeEnum(selectorMat));

        json.add("exitDest", exitDest.serialize());

        if(type != GhType.NORMAL) json.add("type", JsonUtils.writeEnum(type));
        if(hookLimited()) json.add("nextHooks", new JsonPrimitive(nextHooks));
        if(distanceLimited()) json.add("nextDistance", new JsonPrimitive(nextDistance));
        if(!completed.isEmpty()) json.add("completed", JsonUtils.writeCollection(completed, id -> new JsonPrimitive(id.toString())));

        return json;
    }
}
