package net.forthecrown.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.user.FtcUser;
import net.forthecrown.user.data.*;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.ListUtils;
import net.forthecrown.utils.MapUtils;
import org.bukkit.Location;
import org.bukkit.Material;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class UserJsonSerializer implements UserSerializer {
    private final Map<UUID, File> userFiles = new HashMap<>();
    private final Set<UUID> deletedFiles = new HashSet<>();

    private static final Logger logger = Crown.logger();
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

        //Stuff
        if(user.hasNickname()) json.add("nickname", ChatUtils.toJson(user.nickname));
        if(!FtcUtils.isNullOrBlank(user.ip)) json.add("ipAddress", user.ip);
        if(user.totalEarnings > 0) json.add("totalEarnings", user.totalEarnings);
        if(user.getGems() != 0) json.add("gems", user.getGems());
        if(!user.pets.isEmpty()) json.addList("pets", user.pets);
        if(user.currentPrefix != null) json.add("currentPrefix", ChatUtils.toJson(user.currentPrefix));

        //Properties
        if(!user.prefs.isEmpty()) json.addList("properties", user.prefs);

        //Time stamps
        JsonWrapper timeStamps = JsonWrapper.empty();
        timeStamps.add("nextReset", user.nextResetTime);
        timeStamps.add("lastLoad", user.lastLoad);

        json.add("timeStamps", timeStamps);

        //PrevNames
        if(!user.previousNames.isEmpty()) json.addList("previousNames", user.previousNames, JsonPrimitive::new);

        //Last location, /back
        Location lastLoc = user.lastLocation;
        if(lastLoc != null) json.addLocation("lastLocation", lastLoc);

        //Last known location
        Location entLoc = user.entityLocation;
        if(entLoc != null) json.addLocation("lastKnowLoc", entLoc);

        //Sold materials
        if(!MapUtils.isNullOrEmpty(user.matData)){
            JsonObject sold = new JsonObject();

            for (SoldMaterialData d: user.matData.values()){
                if(d.getEarned() < 1) continue;

                sold.addProperty(d.getMaterial().name().toLowerCase(), d.getEarned());
            }

            json.add("soldData", sold);
        }

        //Cosmetic data
        JsonObject cosmetics = user.cosmeticData.serialize();
        if(cosmetics != null) json.add("cosmeticData", cosmetics);

        //Homes
        JsonObject homeData = user.homes.serialize();
        if(homeData != null) json.add("homes", homeData);

        //interactions
        JsonObject interactions = user.interactions.serialize();
        if(interactions != null) json.add("interactions", interactions);

        //Data container
        JsonObject dataContainer = user.dataContainer.serialize();
        if(dataContainer != null) json.add("dataContainer", dataContainer);

        //Market ownership
        JsonObject ownership = user.marketOwnership.serialize();
        if(ownership != null) json.add("marketOwnership", ownership);

        //Mail
        JsonElement mail = user.mail.serialize();
        if(mail != null) json.add("mail", mail);

        writeJson(json, user.getUniqueId());
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

        //ranks
        user.titles.clear();
        user.titles.addAll(RankTitle.getDefaultsFor(user.tier));
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

        //Pets
        user.pets.clear();
        if(json.has("pets")) {
            user.pets.addAll(json.getList("pets", e -> JsonUtils.readEnum(Pet.class, e)));
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
        user.nextAllowedBranchSwap = timeStamps.getLong("nextBranchSwap");

        //Last location
        if(json.has("lastLocation")) {
            user.lastLocation = json.getLocation("lastLocation");
        }

        //Last known location
        if(json.has("lastKnowLoc")) {
            user.entityLocation = json.getLocation("lastKnowLoc");
        }

        //User data type things
        user.cosmeticData.deserialize(json.get("cosmeticData"));
        user.homes.deserialize(json.get("homes"));
        user.interactions.deserialize(json.get("interactions"));
        user.dataContainer.deserialize(json.get("dataContainer"));
        user.marketOwnership.deserialize(json.get("marketOwnership"));
        user.mail.deserialize(json.get("mail"));

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
            logger.severe("Exception while attempting write user data for " + id);
            e.printStackTrace();
        }
    }

    private void addDefaults(FtcUser id, JsonWrapper json) {
        json.add("name", id.getName());
        json.add("lastOnlineName", id.getName());

        JsonWrapper timeStamps = JsonWrapper.empty();
        timeStamps.add("lastLoad", id.getOfflinePlayer().getLastLogin());
        timeStamps.add("nextReset", System.currentTimeMillis() + ComVars.getUserResetInterval());

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
                logger.info("Created user file for " + id);

                writeJson(json, id.getUniqueId());
            } catch (IOException e) {
                logger.severe("Could not create user file for " + id);
                e.printStackTrace();
            }
        } else {
            try {
                JsonObject jsonO = JsonUtils.readFile(f);
                json = JsonWrapper.of(jsonO);
            } catch (IOException e) {
                logger.severe("Exception while reading user data for " + id);
                e.printStackTrace();
                return null;
            }
        }

        return json;
    }

    public void clearFiles() {
        userFiles.clear();
    }
}
