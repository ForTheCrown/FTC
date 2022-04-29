package net.forthecrown.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.forthecrown.core.FtcVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.user.*;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.ListUtils;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.Material;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UserJsonSerializer implements UserSerializer {
    private static final Logger LOGGER = Crown.logger();

    private final Map<UUID, File> userFiles = new HashMap<>();
    private final Set<UUID> deletedFiles = new HashSet<>();

    public static final File USER_DIR;

    static {
        USER_DIR = new File(Crown.dataFolder().getPath() + File.separator + "users");

        if(!USER_DIR.exists()) USER_DIR.mkdir();
        else if(!USER_DIR.isDirectory()) {
            USER_DIR.delete();
            USER_DIR.mkdir();
        }
    }

    @Override
    public void serialize(FtcUser user) {
        if(deletedFiles.contains(user.getUniqueId())) return;

        JsonWrapper json = JsonWrapper.empty();

        //Basic stuff
        json.add("name", user.getName());
        json.add("lastOnlineName", user.lastOnlineName);
        json.addEnum("title", user.currentTitle);
        json.addEnum("tier", user.tier);
        json.addEnum("sellAmount", user.sellAmount);

        if(!user.titles.isEmpty()){
            Set<RankTitle> ranks = new HashSet<>(user.titles);
            ranks.removeIf(RankTitle::isDefaultTitle);

            if(!ranks.isEmpty()) json.addList("titles", ranks);
        }

        if (user.hasNickname()) json.add("nickname", ChatUtils.toJson(user.nickname));
        if (!FtcUtils.isNullOrBlank(user.ip)) json.add("ipAddress", user.ip);
        if (user.totalEarnings > 0) json.add("totalEarnings", user.totalEarnings);
        if (user.getGems() != 0) json.add("gems", user.getGems());
        if (user.currentPrefix != null) json.add("currentPrefix", ChatUtils.toJson(user.currentPrefix));
        if (user.hulkSmashing) json.add("hulkSmashing", true);

        //Properties
        if(!user.prefs.isEmpty()) json.addList("properties", user.prefs);

        //Time stamps
        JsonWrapper timeStamps = JsonWrapper.empty();
        timeStamps.add("nextReset", user.nextResetTime);
        timeStamps.add("lastLoad", user.lastLoad);

        if(user.lastGuildPassDonation != 0) {
            timeStamps.add("lastGuildPassDonation", user.lastGuildPassDonation);
        }

        json.add("timeStamps", timeStamps);

        //PrevNames
        if(!user.previousNames.isEmpty()) json.addList("previousNames", user.previousNames, JsonPrimitive::new);

        //Last location, /back
        Location lastLoc = user.lastLocation;
        if(lastLoc != null) json.addLocation("lastLocation", lastLoc);

        //Last known location
        Location entLoc = user.getLocation();
        if(entLoc != null) json.addLocation("location", entLoc);

        //Sold materials
        if(!FtcUtils.isNullOrEmpty(user.matData)){
            JsonObject sold = new JsonObject();

            for (SoldMaterialData d: user.matData.values()){
                if(d.getEarned() < 1) continue;

                sold.addProperty(d.getMaterial().name().toLowerCase(), d.getEarned());
            }

            json.add("soldData", sold);
        }

        JsonElement filter = user.filter.serialize();
        if(filter != null) json.add("sellShopFilter", filter);

        saveAttach(json, user.cosmeticData);
        saveAttach(json, user.homes);
        saveAttach(json, user.interactions);
        saveAttach(json, user.dataContainer);
        saveAttach(json, user.marketData);
        saveAttach(json, user.mail);

        writeJson(json, user.getUniqueId());
    }

    private void saveAttach(JsonWrapper json, AbstractUserAttachment a) {
        JsonElement element = a.serialize();
        if(element != null) json.add(a.serialKey, element);
    }

    @Override
    public void deserialize(FtcUser user) {
        if(deletedFiles.contains(user.getUniqueId())) return;

        JsonWrapper json = readJson(user);
        if(json == null) return;

        user.lastOnlineName = json.getString("lastOnlineName");
        user.currentTitle = json.getEnum("title", RankTitle.class, RankTitle.DEFAULT);
        user.sellAmount = json.getEnum("sellAmount", SellAmount.class, SellAmount.PER_1);
        user.tier = json.getEnum("tier", RankTier.class, RankTier.NONE);
        user.ip = json.getString("ipAddress");
        user.totalEarnings = json.getLong("totalEarnings");
        user.setGems(json.getInt("gems"));
        user.hulkSmashing = json.getBool("hulkSmashing", false);

        //ranks
        user.titles.clear();
        user.titles.addAll(user.tier.getApplicableDefaults());
        if(json.has("titles")) {
            List<RankTitle> ranks = ListUtils.fromIterable(json.getArray("titles"), e -> JsonUtils.readEnum(RankTitle.class, e));
            user.titles.addAll(ranks);
        }

        //Nickname
        user.nickname = null;
        if(json.has("nickname")) {
            user.nickname = ChatUtils.fromJson(json.get("nickname"));
        }

        //Current Prefix
        user.currentPrefix = null;
        if(!json.missingOrNull("currentPrefix")) {
            user.currentPrefix = ChatUtils.fromJson(json.get("currentPrefix"));
        }

        //Previous names
        user.previousNames.clear();
        if(!json.missingOrNull("previousNames")) {
            user.previousNames.addAll(json.getList("previousNames", JsonElement::getAsString));
        }

        //Properties
        user.prefs.clear();
        if(json.has("properties")) {
            user.prefs.addAll(json.getList("properties", e -> JsonUtils.readEnum(UserPref.class, e)));
        }

        //Time stamps
        JsonWrapper timeStamps = json.getWrapped("timeStamps");
        user.lastLoad = timeStamps.getLong("lastLoad");
        user.nextResetTime = timeStamps.getLong("nextReset");
        user.lastGuildPassDonation = timeStamps.getLong("lastGuildPassDonation", 0L);

        //Last location
        if(json.has("lastLocation")) {
            user.lastLocation = json.getLocation("lastLocation");
        }

        //location
        if(json.has("lastKnowLoc") || json.has("location")) {
            Location l = json.getLocation("location");
            if(l == null) l = json.getLocation("lastKnowLoc");

            user.entityLocation = l;
        }

        loadAttach(json, user.cosmeticData);
        loadAttach(json, user.homes);
        loadAttach(json, user.interactions);
        loadAttach(json, user.dataContainer);
        loadAttach(json, user.marketData);
        loadAttach(json, user.mail);

        //Material data
        user.matData.clear();
        if(json.has("soldData")) {
            JsonObject soldData = json.getObject("soldData");

            for (Map.Entry<String, JsonElement> e: soldData.entrySet()) {
                Material material = Material.valueOf(e.getKey().toUpperCase());
                int earned = e.getValue().getAsInt();

                SoldMaterialData data = new SoldMaterialData(material);
                data.setEarned(earned);
                data.recalculate();

                user.matData.put(material, data);
            }
        }

        user.filter.deserialize(json.get("sellShopFilter"));
    }

    private void loadAttach(JsonWrapper json, AbstractUserAttachment a) {
        a.deserialize(json.get(a.serialKey));
    }

    @Override
    public void onUnload(UUID user) {
        userFiles.remove(user);
    }

    @Override
    public void delete(UUID id) {
        getFile(id).delete();
        deletedFiles.add(id);
        onUnload(id);
    }

    @Override
    public File getFile(UUID id) {
        File result = userFiles.get(id);
        if(result != null) return result;

        return new File(USER_DIR, id + ".json");
    }

    public void writeJson(JsonWrapper json, UUID id) {
        File f = getFile(id);
        if(!f.exists()) return;

        try {
            JsonUtils.writeFile(json.getSource(), f);
        } catch (IOException e){
            LOGGER.error("Exception while attempting write user data for " + id, e);
        }
    }

    private void addDefaults(FtcUser id, JsonWrapper json) {
        json.add("name", id.getName());
        json.add("lastOnlineName", id.getName());

        JsonWrapper timeStamps = JsonWrapper.empty();
        timeStamps.add("lastLoad", id.getOfflinePlayer().getLastLogin());
        timeStamps.add("nextReset", System.currentTimeMillis() + FtcVars.userDataResetInterval.get());

        json.add("timeStamps", timeStamps);
    }

    public JsonWrapper readJson(FtcUser id) {
        File f = getFile(id.getUniqueId());
        JsonWrapper json;

        if(!f.exists()) {
            json = JsonWrapper.empty();
            addDefaults(id, json);

            try {
                f.createNewFile();
                LOGGER.info("Created user file for " + id);

                writeJson(json, id.getUniqueId());
            } catch (IOException e) {
                LOGGER.error("Could not create user file for " + id, e);
            }
        } else {
            try {
                JsonObject jsonO = JsonUtils.readFileObject(f);
                json = JsonWrapper.of(jsonO);
            } catch (IOException e) {
                LOGGER.error("Exception while reading user data for " + id, e);
                return null;
            }
        }

        return json;
    }

    public void clearFiles() {
        userFiles.clear();
    }
}