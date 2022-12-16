package net.forthecrown.guilds;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.guilds.menu.GuildMenus;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import java.util.UUID;

import static net.forthecrown.guilds.menu.GuildMenus.GUILD;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

public class GuildMessage {

    private static final String
            SIGN_TYPE_KEY = "signType",
            CREATOR_KEY = "creator",
            TIMESTAMP_KEY = "createdOn",
            LINES_KEY = "msgLines";

    // List to preserve nice gradient of colors
    public static final ObjectList<Material> SIGN_TYPES = ObjectList.of(
            Material.DARK_OAK_SIGN, Material.SPRUCE_SIGN, Material.OAK_SIGN,
            Material.BIRCH_SIGN, Material.JUNGLE_SIGN, Material.ACACIA_SIGN,
            Material.MANGROVE_SIGN, Material.CRIMSON_SIGN, Material.WARPED_SIGN
    );

    private final Material signType;
    private final UUID creatorName;
    private final long createTimeStamp;
    private final Component[] msgLines;

    public GuildMessage(Material signType, UUID creatorName, long createTimeStamp, Component[] msgLines) {
        this.signType = signType;
        this.creatorName = creatorName;
        this.createTimeStamp = createTimeStamp;
        this.msgLines = msgLines;
    }

    public MenuNode toMenuNode() {
        String signBorder = "---------------";

        return MenuNode.builder()
                .setItem(
                        ItemStacks.builder(this.signType)
                                .setName(
                                        Text.format("Message by: {0, user}",
                                                NamedTextColor.YELLOW,
                                                creatorName
                                        )
                                )

                                .addLore(text("Created on: ", NamedTextColor.WHITE).append(Text.formatDate(this.createTimeStamp)))

                                .addLore(text(signBorder, NamedTextColor.GRAY))
                                .addLore(wrapSignLine(msgLines[0]))
                                .addLore(wrapSignLine(msgLines[1]))
                                .addLore(wrapSignLine(msgLines[2]))
                                .addLore(wrapSignLine(msgLines[3]))
                                .addLore(text(signBorder, NamedTextColor.GRAY))

                                .addLore(text("Shift-click to delete this message.", NamedTextColor.DARK_GRAY))

                                .build()
                )

                .setRunnable((user, context, c) -> {
                    if (!c.getClickType().isShiftClick()) {
                        return;
                    }

                    context.getOrThrow(GUILD).removeMsgBoardPost(this);
                    user.sendMessage(text("Message removed.", NamedTextColor.GRAY));

                    GuildMenus.MAIN_MENU.getMessageBoard()
                            .getMenu()
                            .open(user, context);
                })
                .build();
    }

    private static Component wrapSignLine(Component txt) {
        return text()
                .append(txt)
                .color(NamedTextColor.GRAY)
                .build();
    }

    public static GuildMessage deserialize(JsonObject json) {
        // Type
        Material type = Material.getMaterial(json.get(SIGN_TYPE_KEY).getAsString());
        if (!SIGN_TYPES.contains(type)) {
            type = Material.OAK_SIGN;
        }

        // Creation data
        UUID creator = JsonUtils.readUUID(json.get(CREATOR_KEY));
        long ts = json.get(TIMESTAMP_KEY).getAsLong();

        // Lines
        Component[] lines = new Component[4];
        JsonArray lineJson = json.get(LINES_KEY).getAsJsonArray();
        for (int i = 0; i < 4; i++) {
            JsonElement e = lineJson.get(i);
            if (e == null) {
                lines[i] = empty();
            } else {
                lines[i] = JsonUtils.readText(e);
            }
        }

        return new GuildMessage(type, creator, ts, lines);
    }

    public JsonObject serialize() {
        JsonObject result = new JsonObject();

        // Type
        result.addProperty(SIGN_TYPE_KEY, signType.name());

        // Creation data
        result.add(CREATOR_KEY, JsonUtils.writeUUID(creatorName));
        result.addProperty(TIMESTAMP_KEY, createTimeStamp);

        // Lines
        JsonArray lineJson = new JsonArray();
        for (Component line : msgLines) {
            lineJson.add(JsonUtils.writeText(line));
        }
        result.add(LINES_KEY, lineJson);

        return result;
    }
}