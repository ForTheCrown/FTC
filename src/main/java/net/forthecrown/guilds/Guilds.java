package net.forthecrown.guilds;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.LongSet;
import lombok.experimental.UtilityClass;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Worlds;
import net.forthecrown.core.admin.BannedWords;
import net.forthecrown.user.Users;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.waypoint.Waypoint;
import net.forthecrown.waypoint.WaypointManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.world.level.ChunkPos;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;

import static net.forthecrown.guilds.GuildNameFormat.GUILD_NAME;
import static net.kyori.adventure.text.Component.text;

public @UtilityClass class Guilds {
    /* ---------------------------- SERIAL KEYS ----------------------------- */

    public final String
            KEY_SLOT = "slot",
            KEY_ITEM = "item";

    /* ----------------------------- CONSTANTS ------------------------------ */

    public static final int NO_EXP = 0;

    /* ------------------------------ UTILITY ------------------------------- */

    public World getWorld() {
        return Worlds.overworld();
    }

    public void removeAndArchive(Guild guild, String source, String reason) {
        // Clear members
        guild.getMembers()
                .keySet()
                .stream()
                .map(Users::get)

                .forEach(u -> {
                    if (u.getGuildId() == null
                            || !u.getGuildId().equals(guild.getId())
                    ) {
                        return;
                    }

                    u.setGuild(null);
                });

        GuildManager manager = GuildManager.get();
        LongSet chunks = manager.getGuildChunks(guild);

        manager.removeGuild(guild.getId());
        manager.getStorage()
                .archive(
                        guild,
                        System.currentTimeMillis(),
                        source,
                        reason,
                        chunks
                );
    }

    public void yeetWaypoint(Guild guild) {
        Waypoint waypoint = guild.getSettings().getWaypoint();
        guild.getSettings().setWaypoint(null);

        if (waypoint == null) {
            return;
        }

        WaypointManager.getInstance()
                .removeWaypoint(waypoint);
    }

    /* --------------------------- CHUNK UTILITY ---------------------------- */

    public ChunkPos getChunk(Location location) {
        return new ChunkPos(
                Vectors.toChunk(location.getBlockX()),
                Vectors.toChunk(location.getBlockZ())
        );
    }

    public ChunkPos chunkFromPacked(long l) {
        return new ChunkPos(l);
    }

    /* ---------------------- INVENTORY SERIALIZATION ----------------------- */

    public JsonArray writeInventory(Inventory inventory) {
        var it = ItemStacks.nonEmptyIterator(inventory);
        JsonArray arr = new JsonArray();

        while (it.hasNext()) {
            var index = it.nextIndex();
            var item = it.next();

            var obj = new JsonObject();
            obj.addProperty(KEY_SLOT, index);
            obj.add(KEY_ITEM, JsonUtils.writeItem(item));

            arr.add(obj);
        }

        return arr;
    }

    public void readInventory(Inventory into, JsonElement element) {
        into.clear();

        if (element == null) {
            return;
        }

        var arr = element.getAsJsonArray();

        if (arr.isEmpty()) {
            return;
        }

        for (var e: arr) {
            var obj = e.getAsJsonObject();

            int slot = obj.get(KEY_SLOT).getAsInt();
            var item = JsonUtils.readItem(obj.get(KEY_ITEM));

            into.setItem(slot, item);
        }
    }

    public static void testWorld(World world) throws CommandSyntaxException {
        if (world.equals(getWorld())) {
            return;
        }

        throw Exceptions.GUILDS_WRONG_WORLD;
    }

    public static void validateName(String name) throws CommandSyntaxException {
        if (name.length() < Guild.MIN_NAME_SIZE) {
            throw Exceptions.guildNameSmall(name);
        }

        if (name.length() > Guild.MAX_NAME_SIZE) {
            throw Exceptions.guildNameLarge(name);
        }

        if (BannedWords.contains(name)) {
            throw Exceptions.format("'{0}' is an invalid name", name);
        }

        if (GuildManager.get().getGuild(name) != null) {
            throw Exceptions.format("Name '{0}' is already taken", name);
        }
    }

    /* ----------------------------- GRADIENT ------------------------------- */

    public Component createNameGradient(int sections,
                                        String[] fullName,
                                        TextColor primary,
                                        TextColor secondary,
                                        Style bracket,
                                        Style text
    ) {
        String complete = String.join("", fullName);
        int length = complete.length();
        var builder = text();

        Pair<TextColor, TextColor> startEndColors = Pair.of(primary, secondary);
        int sectionSize = length / sections;
        int sectionIndex = 0;

        // Iterate through each part of the full name
        for (int i = 0; i < fullName.length; i++) {
            Style style = i == GUILD_NAME ? text : bracket;
            char[] chars = fullName[i].toCharArray();

            // Iterate through each char in the current part of the name
            for (char c : chars) {
                // Increment section index and find color based off of
                // that index
                float progress = (float) sectionIndex / sectionSize;

                ++sectionIndex;
                sectionIndex %= sectionSize;

                TextColor color = TextColor.lerp(
                        progress,
                        startEndColors.getFirst(),
                        startEndColors.getSecond()
                );

                builder.append(text(c, style.color(color)));

                // Section's ended, flip colors so gradient smoothly
                // transitions from the last appended color to new one
                if (sectionIndex == 0) {
                    startEndColors = startEndColors.swap();
                }
            }
        }

        return builder.build();
    }
}