package net.forthecrown.pirates.grappling;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.inventory.CrownItems;
import net.forthecrown.pirates.Pirates;
import net.forthecrown.serializer.JsonBuf;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.utils.ItemStackBuilder;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.math.Vector3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class GhLevelData implements JsonSerializable {

    private final String name;

    private final Vector3i startPos;
    private final String nextLevel;

    private final int hooks;
    private final int distance;

    private final GhBiome biome;
    private final GhType type;

    private final Set<UUID> completed = new HashSet<>();

    public GhLevelData(String name, Vector3i startPos, String nextLevel, int hooks, int distance, GhBiome biome, GhType type) {
        this.name = name;

        this.startPos = startPos;
        this.nextLevel = nextLevel;

        this.hooks = hooks;
        this.distance = distance;

        this.biome = biome;
        this.type = type;
    }

    public GhLevelData(String name, JsonElement e) {
        this.name = name;

        JsonBuf json = JsonBuf.of(e.getAsJsonObject());

        this.startPos = json.getPos("start");
        this.nextLevel = json.getString("next", null);

        this.hooks = json.getInt("hooks", -1);
        this.distance = json.getInt("distance", -1);

        this.biome = json.getEnum("biome", GhBiome.class, GhBiome.FLOATING_ISLANDS);
        this.type = json.getEnum("type", GhType.class, GhType.NORMAL);

        completed.addAll(json.getList("completed", JsonUtils::readUUID, Collections.emptySet()));
    }

    public void enter(Player player, World world) {
        player.getInventory().clear();
        player.teleport(startPos.toLoc(world).toCenterLocation());

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gh give " + player.getName() +
                (isHookLimited() ? " " + hooks + (isDistanceLimited() ? " " + distance : "") : "")
        );
    }

    public void exit(Player player, World world) {
        CrownUser user = UserManager.getUser(player);

        if(!hasCompleted(player.getUniqueId())) {
            switch (getType()) {
                default:
                case NORMAL: break;

                case END:
                    Crown.getBalances().add(user.getUniqueId(), ComVars.getGhFinalReward(), false);
                    user.sendMessage(
                            Component.translatable("gh.reward.final", FtcFormatter.rhines(ComVars.getGhFinalReward()).color(NamedTextColor.YELLOW))
                                    .color(NamedTextColor.GRAY)
                    );

                    player.getInventory().addItem(CrownItems.cutlass());
                    break;

                case SPECIAL:
                    Crown.getBalances().add(user.getUniqueId(), ComVars.getGhSpecialReward(), false);
                    user.sendMessage(
                            Component.translatable("gh.reward.special", FtcFormatter.rhines(ComVars.getGhSpecialReward()).color(NamedTextColor.YELLOW))
                                    .color(NamedTextColor.GRAY)
                    );
                    break;
            }

            complete(player.getUniqueId());
        }

        GhLevelData data = Pirates.getParkour().byName(getNextLevel());
        if(data == null) {
            if(type != GhType.END) Crown.logger().warning(name + " has null nextLevel pointer");
            else  player.teleport(GhParkour.EXIT);

            return;
        }

        data.enter(player, world);
        player.sendMessage(Component.translatable("gh.advanced", NamedTextColor.GRAY));
    }

    public boolean hasCompleted(UUID uuid) {
        return completed.contains(uuid);
    }

    public void complete(UUID id) {
        completed.add(id);
    }

    public void uncomplete(UUID id) {
        completed.remove(id);
    }

    public String getName() {
        return name;
    }

    public Vector3i getStartPos() {
        return startPos;
    }

    public String getNextLevel() {
        return nextLevel;
    }

    public int getHooks() {
        return hooks;
    }

    public boolean isHookLimited() {
        return hooks != -1;
    }

    public int getDistance() {
        return distance;
    }

    public boolean isDistanceLimited() {
        return distance != -1;
    }

    public GhBiome getBiome() {
        return biome;
    }

    public GhType getType() {
        return type;
    }

    public Set<UUID> getCompleted() {
        return completed;
    }

    public ItemStack makeItem(UUID uuid) {
        ItemStackBuilder builder = new ItemStackBuilder(hasCompleted(uuid) ? biome.completedMat() : biome.selectorMat())
                .setName(formattedName().style(FtcFormatter.nonItalic(biome.color)));

        if(Pirates.getParkour().isFirstUncompleted(uuid, this)) builder.addEnchant(Enchantment.CHANNELING, 1);

        return builder.build();
    }

    public Component formattedName() {
        return Component.text(name
                .replaceAll("-", " ")
                .replaceAll("_", " ")
        );
    }

    @Override
    public JsonObject serialize() {
        JsonBuf json = JsonBuf.empty();

        json.add("start", startPos);
        json.add("next", nextLevel);

        json.addEnum("biome", biome);
        if(type != GhType.NORMAL) json.addEnum("type", type);

        if(isHookLimited()) json.add("hooks", hooks);
        if(isDistanceLimited()) json.add("distance", distance);

        if(!completed.isEmpty()) json.addList("completed", completed, JsonUtils::writeUUID);

        return json.getSource();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' +
                "name='" + name + '\'' +
                ", nextLevel='" + nextLevel + '\'' +
                ", biome=" + biome +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GhLevelData data = (GhLevelData) o;
        return getHooks() == data.getHooks() &&
                getDistance() == data.getDistance() &&
                Objects.equals(getName(), data.getName()) &&
                Objects.equals(getStartPos(), data.getStartPos()) &&
                Objects.equals(getNextLevel(), data.getNextLevel()) &&
                getBiome() == data.getBiome() &&
                getType() == data.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getStartPos(), getNextLevel(), getHooks(), getDistance(), getBiome(), getType());
    }
}