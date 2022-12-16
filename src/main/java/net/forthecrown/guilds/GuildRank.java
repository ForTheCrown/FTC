package net.forthecrown.guilds;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.user.User;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class GuildRank {

    public static final int
            ID_MEMBER = 0,
            ID_LEADER = 6,

            RANK_COUNT = ID_LEADER + 1;

    private static final String
            FORMATTED_NAME_KEY = "formattedName",
            DESCRIPTION_KEY = "description",
            PERMS_KEY = "permissions",
            ALL_PERMS = "all";

    @Getter
    private final int id;

    @Getter
    private String name;
    private final String description;

    // Jules: Use EnumSet
    private final EnumSet<GuildPermission> permissions = EnumSet.noneOf(GuildPermission.class);

    /* ----------------------------- STATIC METHODS ------------------------------ */

    public static GuildRank createLeader() {
        var rank = new GuildRank(ID_LEADER, "&fLeader", "Guild leader");
        rank.permissions.addAll(List.of(GuildPermission.values()));
        return rank;
    }

    public static GuildRank createDefault() {
        return new GuildRank(ID_MEMBER, "&8Member", "Default Rank");
    }

    /* ----------------------------- METHODS ------------------------------ */

    public String getDescription() {
        return this.description == null ? "Guild Rank" : this.description ;
    }

    public boolean hasFormattedName() {
        return !Strings.isNullOrEmpty(name);
    }

    public Component getFormattedName() {
        return Text.renderString(name);
    }

    public void setName(User setter, String newName) {
        String oldName = getName();
        this.name = newName;

        if (oldName.equals(getName())) {
            setter.sendMessage(Component.text("Nothing changed", NamedTextColor.GRAY));
        } else {
            setter.getGuild().sendMessage(
                    Text.format("{0, user} has renamed rank '{1}' to '{2}'",
                            setter, Text.renderString(oldName), getName()
                    )
            );
        }
    }

    public boolean hasPermission(GuildPermission perm) {
        if (perm == null) {
            return true;
        }

        return this.permissions.contains(perm);
    }

    public boolean togglePermission(GuildPermission perm) {
        if (hasPermission(perm)) {
            this.permissions.remove(perm);
            return false;
        }

        this.permissions.add(perm);
        return true;
    }

    // Get GuildRank from Json
    public static GuildRank deserialize(JsonObject json, int id) {
        // Formatted name
        String formattedName = json.has(FORMATTED_NAME_KEY) ?
                json.get(FORMATTED_NAME_KEY).getAsString() : null;

        String description = json.has(DESCRIPTION_KEY) ?
                json.get(DESCRIPTION_KEY).getAsString() : null;

        var rank = new GuildRank(id, formattedName, description);

        var perms = json.get(PERMS_KEY);

        if (perms != null) {
            // If 'all' string: Give all permissions
            if (perms.isJsonPrimitive()
                    && perms.getAsJsonPrimitive().isString()
                    && perms.getAsString().equalsIgnoreCase(ALL_PERMS)
            ) {
                rank.permissions.addAll(Arrays.asList(GuildPermission.values()));
            }
            // If array: read permissions
            else if (perms.isJsonArray()) {
                JsonUtils.stream(perms.getAsJsonArray())
                        .map(element -> {
                            try {
                                return JsonUtils.readEnum(GuildPermission.class, element);
                            } catch (IllegalArgumentException exc) {
                                FTC.getLogger().warn("Unknown permission: {}", element);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .forEach(rank.permissions::add);
            }
            // Invalid element, warn the console
            else {
                FTC.getLogger().warn("Invalid JSON element found for guild rank permissions: {}",
                        perms
                );
            }
        }

        return rank;
    }

    // Get Json from GuildRank
    public JsonObject serialize() {
        JsonObject result = new JsonObject();

        // Formatted name
        if (hasFormattedName()) {
            result.addProperty(FORMATTED_NAME_KEY, this.name);
        }

        if (description != null) {
            result.addProperty(DESCRIPTION_KEY, this.description);
        }

        // Permissions
        if (id == ID_LEADER
                || permissions.size() >= GuildPermission.values().length
        ) {
            result.addProperty(PERMS_KEY, ALL_PERMS);
        } else if (!permissions.isEmpty()) {
            result.add(PERMS_KEY,
                    JsonUtils.ofStream(permissions.stream().map(JsonUtils::writeEnum))
            );
        }

        return result;
    }

    @Override
    public String toString() {
        return "GuildRank{" +
                ", formattedName='" + name + '\'' +
                ", permissions=" + permissions +
                '}';
    }
}