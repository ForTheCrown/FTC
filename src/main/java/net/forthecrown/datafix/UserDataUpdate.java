package net.forthecrown.datafix;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.user.Components;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.data.CosmeticData;
import net.forthecrown.user.data.TimeField;
import net.forthecrown.user.data.UserShopData;
import net.forthecrown.user.property.BoolProperty;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static net.forthecrown.user.data.UserTimeTracker.UNSET;
import static net.forthecrown.utils.io.PathUtil.safeDelete;

public class UserDataUpdate extends DataUpdater {
    public boolean update() throws IOException {
        Path userPath = PathUtil.pluginPath("user");
        Path dataPath = userPath.resolve("data");

        Files.createDirectories(dataPath);

        // Move user cache file to new directory
        moveSafe(
                PathUtil.pluginPath("usercache.json"),
                userPath.resolve("profiles.json")
        );

        // Move alt accounts over
        moveSafe(
                PathUtil.pluginPath("alt_accounts.json"),
                userPath.resolve("alts.json")
        );

        moveSafe(
                PathUtil.pluginPath("balances.json"),
                userPath.resolve("balances.json")
        );

        var users = UserManager.get();
        users.getBalances().reload();
        users.getUserLookup().reload();

        if (!convertUserData(dataPath, PathUtil.pluginPath("users"))) {
            return false;
        }

        users.save();

        return true;
    }

    boolean convertUserData(Path newDir, Path oldDir) {
        LOGGER.info("Starting user data conversion");

        if (!iterateDirectory(oldDir, false, true, p -> fixUserFile(p, newDir))) {
            return false;
        }

        // Directory deletion doesn't really matter
        // just matters that the above code runs
        if (safeDelete(oldDir, true).result().isEmpty()) {
            return false;
        }

        LOGGER.info("Finished user data transformation, deleted old directory");
        return true;
    }

    void fixUserFile(Path p, Path newDir) throws IOException {
        var name = p.getFileName().toString();
        UUID id = UUID.fromString(name.substring(0, name.lastIndexOf('.')));

        var offlinePlayer = Bukkit.getOfflinePlayer(id);
        if (!offlinePlayer.hasPlayedBefore()) {
            LOGGER.info("Found unknown player: '{}'", id);
            return;
        }

        var ticksPlayed = offlinePlayer.getStatistic(Statistic.PLAY_ONE_MINUTE);
        var secondsPlayed = ticksPlayed / 20;

        UserManager.get()
                .getPlayTime()
                .set(id, secondsPlayed);

        JsonObject obj = JsonUtils.readFileObject(p);
        JsonWrapper updated = fixUserData(id, JsonWrapper.wrap(obj));

        JsonUtils.writeFile(
                updated.getSource(),
                newDir.resolve(id + ".json")
        );

        LOGGER.info("Converted user data file of: {}", id);
    }

    JsonWrapper fixUserData(UUID id, JsonWrapper old) {
        // Declare wrappers
        var result = JsonWrapper.create();
        var properties = JsonWrapper.create();
        var timeStamps = JsonWrapper.create();
        var rankData = JsonWrapper.create();

        // Move basic details
        result.add("name", old.getString("name"));
        result.add("lastOnlineName", old.getString("lastOnlineName"));
        result.add("ip", old.getString("ipAddress", ""));

        // Gems have been moved to user map,
        // so update value there
        if (old.has("gems")) {
            UserManager.get().getGems().set(id, old.getInt("gems"));
        }

        // Rank data
        rankData.add("title", old.get("title"));
        rankData.add("tier", old.get("tier"));
        rankData.add("titles", old.get("titles"));
        result.add(Components.TITLES.getSerialId(), rankData);

        // Sell amount
        properties.add(Properties.SELL_AMOUNT.getKey(), old.get("sellAmount"));

        // Locations
        if (old.has("lastLocation")) {
            result.add("lastLocation", old.get("lastLocation").deepCopy());
        }

        if (old.has("location")) {
            result.add("location", old.get("location").deepCopy());
        }

        // MarketData no longer serializes any data as all the data
        // it held has been moved to UserTimeTracker, so we've gotta
        // exctract those values if they're present
        if (old.has("marketOwnership")) {
            var market = old.getWrapped("marketOwnership");
            var began = market.getLong("ownershipBegan", UNSET);
            var lastAction = market.getLong("lastAction", UNSET);

            if (began != UNSET) {
                timeStamps.addTimeStamp(TimeField.MARKET_OWNERSHIP_STARTED.getKey(), began);
            }

            if (lastAction != UNSET) {
                timeStamps.addTimeStamp(TimeField.MARKET_LAST_ACTION.getKey(), lastAction);
            }
        }

        // Interactions is another thing which now serializes less data so
        // the things that don't have to be serialized anymore get moved
        // away from it
        if (old.has("interactions")) {
            var inter = old.getWrapped("interactions");
            long lastMarriage = inter.getLong("lastMarriage", UNSET);

            if (lastMarriage != UNSET) {
                timeStamps.addTimeStamp(TimeField.MARRIAGE_CHANGE.getKey(), lastMarriage);
            }

            if (inter.has("acceptingProposals")) {
                properties.add("acceptingProposals", false);
            }

            if (inter.has("marriedTo")) {
                inter.addUUID("spouse", inter.getUUID("marriedTo"));
                inter.remove("marriedTo");
            }

            // Remove the data that's no longer serialized
            // here and then test if it's empty, if it is
            // stop, else move the remaining data over to
            // the new JSON
            inter.remove("lastMarriage");
            inter.remove("acceptingProposals");
            inter.remove("marriageChat");

            if (!inter.isEmpty()) {
                result.add(
                        Components.INTERACTIONS.getSerialId(),
                        inter.getSource().deepCopy()
                );
            }
        }

        if (old.has("timeStamps")) {
            var oldTime = old.getWrapped("timeStamps");
            var lastLoad = oldTime.getLong("lastLoad", UNSET);

            if (lastLoad != UNSET) {
                timeStamps.addTimeStamp(TimeField.LAST_LOADED.getKey(), lastLoad);
                timeStamps.addTimeStamp(TimeField.LAST_LOGIN.getKey(), lastLoad);
            }
        }

        if (!timeStamps.isEmpty()) {
            result.add(Components.TIME_TRACKER.getSerialId(), timeStamps);
        }

        if (old.has("properties")) {
            for (var e: old.getArray("properties")) {
                var legacyProperty = JsonUtils.readEnum(LegacyProperty.class, e);

                // Since eavesdropper has been split into several properties
                // We need a special test for eavesdropper here to test for it
                // and to then carry all eavesdrop properties across
                if (legacyProperty == LegacyProperty.LISTENING_TO_EAVESDROPPER) {
                    properties.add(Properties.EAVES_DROP_DM.getKey(), true);
                    properties.add(Properties.EAVES_DROP_MCHAT.getKey(), true);
                    properties.add(Properties.EAVES_DROP_MUTED.getKey(), true);
                    properties.add(Properties.EAVES_DROP_SIGN.getKey(), true);

                    continue;
                }

                if (legacyProperty.valueIfPresent == legacyProperty.updated.getDefaultValue()) {
                    continue;
                }

                properties.add(legacyProperty.updated.getKey(), legacyProperty.valueIfPresent);
            }
        }

        if (!properties.isEmpty()) {
            result.add(Components.PROPERTIES.getSerialId(), properties);
        }

        // Cosmetic data was serialized in a pretty dumb way, this transforms
        // the old system to new... great documentation there genius
        if (old.has("cosmeticData")) {
            result.add(Components.COSMETICS.getSerialId(), transformCosmeticData(old.get("cosmeticData")));
        }

        if (old.has("homes")) {
            result.add(Components.HOMES.getSerialId(), old.getObject("homes").deepCopy());
        }

        if (old.has("mail")) {
            result.add(Components.MAIL.getSerialId(), old.getArray("mail").deepCopy());
        }

        if (old.has("soldData")) {
            var earnings = JsonWrapper.create();
            earnings.add(UserShopData.KEY_EARNED, old.get("soldData").deepCopy());
            result.add(Components.EARNINGS.getSerialId(), earnings);
        }

        return result;
    }

    JsonElement transformCosmeticData(JsonElement element) {
        var source = JsonWrapper.wrap(element.getAsJsonObject());

        var travel = JsonWrapper.create();
        var death = JsonWrapper.create();
        var arrow = JsonWrapper.create();

        var result = JsonWrapper.create();

        if (move("death", death, source)) {
            result.add(Cosmetics.DEATH.getName(), death);
        }

        if (move("arrow", arrow, source)) {
            result.add(Cosmetics.ARROWS.getName(), arrow);
        }

        if (move("travel", travel, source)) {
            result.add(Cosmetics.TRAVEL.getName(), travel);
        }

        return result.getSource();
    }

    // True if any data was moved, false if there was no data to move
    boolean move(String singleKey, JsonWrapper dest, JsonWrapper source) {
        var listKey = singleKey + "Effects";
        boolean result = false;

        if (source.has(listKey)) {
            dest.add(CosmeticData.KEY_AVAILABLE, removeKeyPrefix(source.get(listKey)));
            result = true;
        }

        if (source.has(singleKey)) {
            dest.add(CosmeticData.KEY_ACTIVE, removeKeyPrefix(source.get(singleKey)));
            result = true;
        }

        return result;
    }

    JsonElement removeKeyPrefix(JsonElement element) {
        if (element.isJsonPrimitive()) {
            return new JsonPrimitive(element.getAsString().replaceAll("forthecrown:", ""));
        }

        if (element.isJsonArray()) {
            var array = new JsonArray();

            for (var e: element.getAsJsonArray()) {
                array.add(removeKeyPrefix(e));
            }

            return array;
        }

        var json = new JsonObject();

        for (var e: element.getAsJsonObject().entrySet()) {
            json.add(e.getKey(), removeKeyPrefix(e.getValue()));
        }

        return json;
    }

    @RequiredArgsConstructor
    enum LegacyProperty {
        FORBIDS_EMOTES (Properties.EMOTES, false),
        FORBIDS_RIDING (Properties.PLAYER_RIDING, false),
        FORBIDS_TPA (Properties.TPA, false),
        FORBIDS_PAY (Properties.PAY, false),
        FORBIDS_REGION_INVITES (Properties.REGION_INVITING, false),

        NON_HULK_SMASHER (Properties.HULK_SMASHING, false),

        IGNORING_BROADCASTS (Properties.IGNORING_ANNOUNCEMENTS, true),
        PROFILE_PRIVATE (Properties.PROFILE_PRIVATE, true),
        LISTENING_TO_EAVESDROPPER (Properties.EAVES_DROP_DM, true),
        VANISHED (Properties.VANISHED, true),
        GOD_MODE (Properties.GOD, true),
        FLYING (Properties.FLYING, true);

        private final BoolProperty updated;
        private final boolean valueIfPresent;
    }
}