package net.forthecrown.guilds;

import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.core.DynmapUtil;
import net.forthecrown.utils.ArrayIterator;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.waypoint.Waypoint;
import net.forthecrown.waypoint.WaypointManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.Objects;
import java.util.UUID;

import static net.forthecrown.guilds.GuildRank.RANK_COUNT;

public class GuildSettings {

    public static final String
            NAME_KEY = "name",
            WAYPOINT_KEY = "waypoint",
            RANKS_KEY = "ranks",
            PRIMARY_COLOR_KEY = "primaryColor",
            SECONDARY_COLOR_KEY = "secondaryColor",
            NAME_FORMAT_KEY = "nameFormat",
            BANNER_KEY = "banner",
            IS_PUBLIC_KEY = "isPublic",
            ALLOWS_VISIT_KEY = "allowsVisit";

    @Getter
    private final Guild guild;

    @Getter @Setter(AccessLevel.PACKAGE)
    String name;

    final GuildRank[] ranks = new GuildRank[RANK_COUNT];

    @Getter
    private GuildColor primaryColor = GuildColor.WHITE;
    @Getter
    private GuildColor secondaryColor = GuildColor.LIGHT_GRAY;
    @Getter
    private final GuildNameFormat nameFormat = GuildNameFormat.createDefault();

    @Getter
    private ItemStack banner = new ItemStack(Material.WHITE_BANNER);

    @Setter
    private UUID waypoint;

    @Getter @Setter
    private boolean isPublic;

    @Getter @Setter
    @Accessors(fluent = true)
    private boolean allowsVisit;

    public GuildSettings(Guild guild) {
        this.guild = guild;
    }

    public Waypoint getWaypoint() {
        if (waypoint == null) {
            return null;
        }

        return WaypointManager.getInstance()
                .get(waypoint);
    }

    public GuildRank getRank(int id) {
        if (!hasRank(id)) {
            return null;
        }

        return ranks[id];
    }

    public void addRank(int id) {
        if (hasRank(id)) {
            return;
        }

        ranks[id] = new GuildRank(id, "NewRank", "Guild Rank");
    }

    public void addRank(GuildRank rank) {
        if (hasRank(rank.getId())) {
            return;
        }

        setRank(rank.getId(), rank);
    }

    public void setRank(int id, GuildRank rank) {
        Objects.checkIndex(id, ranks.length);
        ranks[id] = rank;
    }

    public boolean hasRank(int id) {
        return id >= 0
                && id < ranks.length
                && ranks[id] != null;
    }

    public void setPrimaryColor(GuildColor color) {
        if (color == this.primaryColor) {
            return;
        }

        this.primaryColor = color;

        // Update dynmap
        if (DynmapUtil.isInstalled()) {
            GuildDynmap.updateGuildChunks(getGuild());
        }
    }

    public void setSecondaryColor(GuildColor color) {
        if (color == this.secondaryColor) {
            return;
        }

        this.secondaryColor = color;

        // Update dynmap
        if (DynmapUtil.isInstalled()) {
            GuildDynmap.updateGuildChunks(getGuild());
        }
    }

    private static void updateDynmap() {

    }

    public void setBanner(ItemStack item) {
        if (!(item.getItemMeta() instanceof BannerMeta meta)) {
            throw new IllegalArgumentException("Given item is not a banner");
        }

        banner.setType(item.getType());
        banner.editMeta(BannerMeta.class, banner -> {
            banner.setPatterns(meta.getPatterns());
        });
    }

    // Get GuildSettings from Json
    public void deserialize(JsonObject jsonObject) {
        var json = JsonWrapper.wrap(jsonObject);

        name = json.getString(NAME_KEY);

        // Guild ranks
        JsonObject ranksJson = json.get(RANKS_KEY).getAsJsonObject();
        for (var e: ranksJson.entrySet()) {
            int id = Integer.parseInt(e.getKey());
            var rank = GuildRank.deserialize(e.getValue().getAsJsonObject(), id);

            setRank(id, rank);
        }

        setPublic(json.get(IS_PUBLIC_KEY).getAsBoolean());
        allowsVisit(json.get(ALLOWS_VISIT_KEY).getAsBoolean());

        primaryColor = json.getEnum(PRIMARY_COLOR_KEY, GuildColor.class);
        secondaryColor = json.getEnum(SECONDARY_COLOR_KEY, GuildColor.class);
        banner = json.getItem(BANNER_KEY);
        setWaypoint(json.getUUID(WAYPOINT_KEY));

        if (json.has(NAME_FORMAT_KEY)) {
            nameFormat.deserialize(json.get(NAME_FORMAT_KEY));
        }
    }

    // Get Json from GuildSettings
    public JsonObject serialize() {
        JsonObject result = new JsonObject();

        result.addProperty(NAME_KEY, this.name);

        if (waypoint != null) {
            result.add(WAYPOINT_KEY, JsonUtils.writeUUID(waypoint));
        }

        // Guild ranks
        JsonObject ranksJson = new JsonObject();

        var it = ArrayIterator.unmodifiable(ranks);
        while (it.hasNext()) {
            var id = it.nextIndex();
            ranksJson.add(String.valueOf(id), it.next().serialize());
        }
        result.add(RANKS_KEY, ranksJson);

        result.addProperty(PRIMARY_COLOR_KEY, this.primaryColor.name());
        result.addProperty(SECONDARY_COLOR_KEY, this.secondaryColor.name());

        result.add(BANNER_KEY, JsonUtils.writeItem(this.banner));
        result.addProperty(IS_PUBLIC_KEY, this.isPublic);
        result.addProperty(ALLOWS_VISIT_KEY, this.allowsVisit);

        if (!nameFormat.isDefault()) {
            result.add(NAME_FORMAT_KEY, nameFormat.serialize());
        }

        return result;
    }
}