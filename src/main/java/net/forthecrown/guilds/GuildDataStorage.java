package net.forthecrown.guilds;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.FTC;
import net.forthecrown.utils.io.*;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

/**
 * Class for managing guild data storage
 */
@Getter
@RequiredArgsConstructor
public class GuildDataStorage {
    private static final Logger LOGGER = FTC.getLogger();

    /**
     * The guild directory, contains the individual
     * guild files, the archive directory and the chunk file
     */
    private final Path directory;

    /** The directory for archived guilds that have been removed */
    private final Path archiveDirectory;

    /** The file that contains the claimed chunk data for each guild */
    private final Path chunkFile;

    /* ---------------------------- CONSTRUCTOR ----------------------------- */

    GuildDataStorage(Path directory) {
        this.directory = directory;
        this.archiveDirectory = directory.resolve("archive");
        this.chunkFile = directory.resolve("chunks.json");
    }

    /* ----------------------- GUILD SERIALIZATION ------------------------- */

    /** Deletes the file belonging to the given guild */
    public void delete(UUID uuid) {
        PathUtil.safeDelete(getFile(uuid), false, false)
                .resultOrPartial(LOGGER::error);

        saveChunks(uuid, null);
    }

    /**
     * Finds all existing guild UUIDs by iterating
     * through the {@link #getDirectory()}
     *
     * @return All existing guild UUIDs
     */
    public Set<UUID> findExistingGuilds() {
        Set<UUID> result = new ObjectArraySet<>();

        try (var stream = Files.newDirectoryStream(getDirectory())) {
            for (var p: stream) {
                if (p.equals(getChunkFile())
                        || p.equals(getArchiveDirectory())
                ) {
                    continue;
                }

                try {
                    UUID id = UUID.fromString(
                            p.getFileName()
                                    .toString()
                                    .replaceAll(".json", "")
                    );

                    result.add(id);
                } catch (IllegalArgumentException exc) {
                    LOGGER.error("Couldn't parse file {} to UUID", p, exc);
                }
            }
        } catch (IOException exc) {
            LOGGER.error("Couldn't iterate guild directory", exc);
        }

        return result;
    }

    /**
     * Loads a guild by the given UUID.
     * <p>
     * Note: This does not load guild chunks,
     * for that, use {@link #loadChunks(UUID)}
     *
     * @param uuid The UUID of the guild to load
     * @return The loaded guild
     */
    public Guild loadGuild(UUID uuid) {
        Path path = getFile(uuid);

        return SerializationHelper.readJson(path)
                .map(JsonWrapper::wrap)
                .map(wrapper -> Guild.deserialize(uuid, wrapper))
                .resultOrPartial(LOGGER::error)
                .orElseThrow();
    }

    /**
     * Saves the given guild.
     * <p>
     * Note: Guild chunks are saved separately
     * with {@link #saveChunks(UUID, LongSet)}
     *
     * @param guild The guild to save
     */
    public void saveGuild(Guild guild) {
        Path path = getFile(guild.getId());
        SerializationHelper.writeJsonFile(path, guild::serialize);
    }

    /** Gets the given guild's data file */
    public Path getFile(UUID guildId) {
        return directory.resolve(guildId + ".json");
    }

    /* ----------------------------- ARCHIVING ------------------------------ */

    /**
     * Moves the given guild into the archive
     * <p>
     * This does not delete the guild's existing file
     * or remove it from the {@link GuildManager} instance,
     * it simply creates a json file in the archive directory
     * with the given guild's current data.
     *
     * @param guild The guild to archive
     * @param archiveDate The date of the archival
     * @param archiver The source of the archiving.
     *                 eg: The name of the player/staff-member
     *                 that deleted the guild
     *
     * @param reason The reason the guild is being archived, normally
     *               this will be something akin to 'Closed by leader'
     */
    public void archive(Guild guild,
                        long archiveDate,
                        String archiver,
                        String reason,
                        LongSet chunks
    ) {
        Path path = getArchiveFile(guild.getId());

        SerializationHelper.writeJsonFile(path, wrapper -> {
            JsonWrapper archiveData = JsonWrapper.create();
            archiveData.addTimeStamp("date", archiveDate);

            if (archiver != null) {
                archiveData.add("source", archiver);
            }

            if (reason != null) {
                archiveData.add("reason", reason);
            }

            JsonWrapper guildData = JsonWrapper.create();
            guild.serialize(guildData);

            wrapper.add("archiveData", archiveData);
            wrapper.add("guildData", guildData);

            if (!chunks.isEmpty()) {
                wrapper.add("archivedChunks",
                        JsonUtils.ofStream(
                                chunks.longStream()
                                        .mapToObj(JsonPrimitive::new)
                        )
                );
            }
        });
    }

    /** Gets the given guild's archive file */
    public Path getArchiveFile(UUID uuid) {
        return archiveDirectory.resolve(uuid + ".json");
    }

    /* ------------------------------ CHUNKS ------------------------------- */

    /**
     * Saves the given guild's chunks
     * @param guildId The ID of the guild the chunks belong to
     * @param chunks The set of packed chunk positions, null or empty,
     *               to clear the guild from the chunk file
     */
    public void saveChunks(UUID guildId, @Nullable LongSet chunks) {
        JsonObject obj = SerializationHelper.readJson(getChunkFile())
                .result()
                .orElseGet(JsonObject::new);

        if (chunks == null || chunks.isEmpty()) {
            obj.remove(guildId.toString());
        } else {
            obj.add(guildId.toString(),
                    JsonUtils.ofStream(
                            chunks.longStream()
                                    .mapToObj(JsonPrimitive::new)
                    )
            );
        }

        SerializationHelper.writeJson(getChunkFile(), obj);
    }

    /**
     * Loads the claimed chunks of the given Guild.
     * <p>
     * This method doesn't throw errors, but if the chunk file
     * doesn't exist or reading it throws an error, then this
     * method will still return an empty set. This is known as
     * the irresponsible way of handling IO exceptions
     *
     * @param guildId the ID of the guild to load the chunks of
     * @return The guild's loaded chunks, empty set, if no claimed chunks found
     */
    public DataResult<LongSet> loadChunks(UUID guildId) {
        return SerializationHelper.readJson(getChunkFile())
                .flatMap(object -> {
                    var el = object.get(guildId.toString());

                    if (el == null) {
                        return DataResult.success(LongSets.emptySet());
                    }

                    if (!el.isJsonArray()) {
                        return Results.errorResult(
                                "Guild chunk file %s, expected JsonArray, found %s",
                                guildId, el.toString()
                        );
                    }

                    return DataResult.success(
                            LongOpenHashSet.toSet(
                                    JsonUtils.stream(el.getAsJsonArray())
                                            .mapToLong(JsonElement::getAsLong)
                            )
                    );
                });
    }
}