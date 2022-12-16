package net.forthecrown.core.challenge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.core.registry.Keys;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.utils.io.*;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import static net.forthecrown.utils.io.FtcJar.ALLOW_OVERWRITE;
import static net.forthecrown.utils.io.FtcJar.OVERWRITE_IF_NEWER;

@Getter
public class ChallengeDataStorage {
    private static final Logger LOGGER = FTC.getLogger();

    private static final String
            KEY_HIGHEST_STREAK = "highest::streak";


    private final Path directory;
    private final Path itemDataDirectory;

    private final Path challengesFile;
    private final Path itemChallengesFile;
    private final Path userDataFile;
    private final Path streakScriptFile;

    public ChallengeDataStorage(Path directory) {
        this.directory = directory;

        // Directories
        this.itemDataDirectory = directory.resolve("item_data");

        // Files
        this.challengesFile = directory.resolve("challenges.json");
        this.itemChallengesFile = directory.resolve("item_challenges.json");
        this.userDataFile = directory.resolve("user_data.json");
        this.streakScriptFile = directory.resolve("streak_scripts.json");
    }

    void ensureDefaultsExist() {
        try {
            FtcJar.saveResources(
                    "challenges",
                    ALLOW_OVERWRITE | OVERWRITE_IF_NEWER
            );

            // Default scripts are saved by ScriptManager
        } catch (IOException exc) {
            LOGGER.error("Error trying to save challenge defaults!", exc);
        }
    }

    /**
     * Gets all callback scripts for a streak category, these scripts are
     * intended to be called when a user increments their streak progress
     * to give them rewards.
     */
    public Set<String> getScripts(StreakCategory category) {
        Set<String> result = new ObjectOpenHashSet<>();

        SerializationHelper.readJsonFile(streakScriptFile, json -> {
            var element = json.get(category.name().toLowerCase());

            if (element == null) {
                return;
            }

            // If a single script
            if (element.isJsonPrimitive()) {
                result.add(element.getAsString());
                return;
            }

            // If an array of scripts is declared
            JsonUtils.stream(element.getAsJsonArray())
                    .map(JsonElement::getAsString)
                    .forEach(result::add);
        });

        return result;
    }

    public void loadChallenges(Registry<Challenge> target) {
        _loadChallenges(
                target,
                getChallengesFile(),
                ChallengeParser::parse
        );
    }

    public void loadItemChallenges(Registry<Challenge> registry) {
        _loadChallenges(
                registry,
                getItemChallengesFile(),
                ItemChallengeParser::parse
        );
    }

    private void _loadChallenges(Registry<Challenge> registry,
                                 Path path,
                                 Function<JsonObject, DataResult<? extends Challenge>> parser
    ) {
        SerializationHelper.readJsonFile(path, json -> {
            int loaded = 0;

            for (var e: json.entrySet()) {
                if (!Keys.isValidKey(e.getKey())) {
                    LOGGER.warn("Invalid key found: '{}'", e.getKey());
                    continue;
                }

                if (!e.getValue().isJsonObject()) {
                    LOGGER.warn("Expected JSON Object, found: {} in '{}'",
                            e.getValue().getClass(), e.getKey()
                    );

                    continue;
                }

                ++loaded;
                registry.register(
                        e.getKey(),

                        parser.apply(e.getValue().getAsJsonObject())
                                .mapError(s -> e.getKey() + ": " + s)
                                .getOrThrow(true, s -> {})
                );
            }

            LOGGER.debug("Loaded {} challenges from {}", loaded, path);
        });
    }

    /* ----------------------------- USER DATA ------------------------------ */

    public DataResult<List<ChallengeEntry>> loadEntries(Registry<Challenge> challenges) {
        return SerializationHelper.readJson(getUserDataFile())
                .map(object -> {
                    List<ChallengeEntry> entries = new ObjectArrayList<>();

                    for (var e: object.entrySet()) {
                        if (!e.getValue().isJsonObject()) {
                            LOGGER.warn(
                                    "Found non object in entry json file under {}",
                                    e.getKey()
                            );

                            continue;
                        }

                        UUID uuid = UUID.fromString(e.getKey());
                        ChallengeEntry entry = new ChallengeEntry(uuid);

                        var eObj = JsonWrapper.wrap(e.getValue().getAsJsonObject());

                        if (eObj.has(KEY_HIGHEST_STREAK)) {
                            int highestStreak = eObj.getInt(KEY_HIGHEST_STREAK);
                            entry.setHighestStreak(highestStreak);

                            eObj.remove(KEY_HIGHEST_STREAK);
                        }

                        for (var p: e.getValue().getAsJsonObject().entrySet()) {
                            if (!p.getValue().isJsonPrimitive()
                                    || !((JsonPrimitive) p.getValue()).isNumber()
                            ) {
                                LOGGER.warn("Found non-number in {}'s data", uuid);
                                continue;
                            }

                            float progress = p.getValue()
                                    .getAsNumber()
                                    .floatValue();

                            challenges.get(p.getKey())
                                    .ifPresentOrElse(challenge -> {
                                        entry.getProgress()
                                                .put(challenge, progress);
                                    }, () -> {
                                        LOGGER.warn(
                                                "Unknown challenge {} in {} data",
                                                p.getKey(), uuid
                                        );
                                    });
                        }

                        entries.add(entry);
                    }

                    LOGGER.debug("Loaded {} challenge entries", entries.size());
                    return entries;
                });
    }

    public void saveEntries(Collection<ChallengeEntry> entries,
                            Registry<Challenge> challenges
    ) {
        SerializationHelper.writeJsonFile(getUserDataFile(), wrapper -> {
            for (var e: entries) {
                JsonWrapper json = JsonWrapper.create();

                for (var p: e.getProgress().object2FloatEntrySet()) {
                    if (p.getFloatValue() <= 0) {
                        continue;
                    }

                    if (e.getHighestStreak() > 0) {
                        json.add(KEY_HIGHEST_STREAK, e.getHighestStreak());
                    }

                    challenges.getHolderByValue(p.getKey())
                            .ifPresentOrElse(holder -> {
                                json.add(holder.getKey(), p.getFloatValue());
                            }, () -> {
                                LOGGER.warn(
                                        "Unregistered challenge found in {}",
                                        e.getId()
                                );
                            });
                }

                wrapper.add(e.getId().toString(), json);
            }

            LOGGER.debug("Saved {} challenge entries", wrapper.size());
        });
    }

    /* ----------------------------- ITEM DATA ------------------------------ */

    public Path getItemFile(String holder) {
        return itemDataDirectory.resolve(holder + ".dat");
    }

    public ChallengeItemContainer loadContainer(Holder<Challenge> holder) {
        ChallengeItemContainer
                container = new ChallengeItemContainer(holder.getKey());

        SerializationHelper.readTagFile(
                getItemFile(container.getChallengeKey()),
                container::load
        );

        return container;
    }

    public void saveContainer(ChallengeItemContainer container) {
        if (container.isEmpty()) {
            PathUtil.safeDelete(getItemFile(container.getChallengeKey()));
            return;
        }

        SerializationHelper.writeTagFile(
                getItemFile(container.getChallengeKey()),
                container::save
        );
    }
}