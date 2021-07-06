package net.forthecrown.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.user.FtcUser;
import net.forthecrown.user.data.SoldMaterialData;
import net.forthecrown.user.data.UserProperty;
import net.forthecrown.user.enums.Branch;
import net.forthecrown.user.enums.Pet;
import net.forthecrown.user.enums.Rank;
import net.forthecrown.user.enums.SellAmount;
import net.forthecrown.utils.CrownUtils;
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

    private static final Logger logger = CrownCore.logger();
    public static final File USER_DIR;

    static {
        USER_DIR = new File(CrownCore.dataFolder().getPath() + File.separator + "users");

        if(!USER_DIR.exists()) USER_DIR.mkdir();
        else if(!USER_DIR.isDirectory()) {
            USER_DIR.delete();
            USER_DIR.mkdir();
        }
    }

    @Override
    public void serialize(FtcUser user) {
        if(deletedFiles.contains(user.getUniqueId())) return;

        JsonBuf json = JsonBuf.empty();

        //Basic stuff
        json.add("name", user.getName());
        json.add("lastOnlineName", user.lastOnlineName);
        json.addEnum("branch", user.branch);
        json.addEnum("rank", user.currentRank);
        json.addEnum("sellAmount", user.sellAmount);

        if(!user.ranks.isEmpty()){
            Set<Rank> ranks = new HashSet<>(user.ranks);
            ranks.remove(Rank.DEFAULT);

            if(!ranks.isEmpty()) json.addList("ranks", ranks);
        }

        //Stuff
        if(user.hasNickname()) json.add("nickname", ChatUtils.toJson(user.nickname));
        if(!CrownUtils.isNullOrBlank(user.ip)) json.add("ipAddress", user.ip);
        if(user.totalEarnings > 0) json.add("totalEarnings", user.totalEarnings);
        if(user.getGems() != 0) json.add("gems", user.getGems());
        if(!user.pets.isEmpty()) json.addList("pets", user.pets);

        //Properties
        if(!user.properties.isEmpty()) json.addList("properties", user.properties);

        //Time stamps
        JsonBuf timeStamps = JsonBuf.empty();
        timeStamps.add("nextReset", user.nextResetTime);
        timeStamps.add("lastLoad", user.lastLoad);
        if(!user.canSwapBranch()) timeStamps.add("nextBranchSwap", user.nextAllowedBranchSwap);

        json.add("timeStamps", timeStamps);

        //Last location, /back
        Location lastLoc = user.lastLocation;
        if(lastLoc != null) json.addLocation("lastLocation", lastLoc);

        //Last known location
        Location entLoc = user.entityLocation;
        if(entLoc != null) json.addLocation("lastKnowLoc", entLoc);

        //Grave items
        JsonArray graveItems = user.grave.serialize();
        if(graveItems != null) json.add("grave", graveItems);

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
        if(cosmetics != null){
            json.add("cosmeticData", cosmetics);
        }

        //Homes
        JsonObject homeData = user.homes.serialize();
        if(homeData != null) json.add("homes", homeData);

        //interactions
        JsonObject interactions = user.interactions.serialize();
        if(interactions != null) json.add("interactions", interactions);

        //Data container
        JsonObject dataContainer = user.dataContainer.serialize();
        if(dataContainer != null) json.add("dataContainer", dataContainer);

        writeJson(json, user.getUniqueId());
    }

    @Override
    public void deserialize(FtcUser user) {
        if(deletedFiles.contains(user.getUniqueId())) return;

        JsonBuf json = readJson(user);
        if(json == null) return;

        user.name = json.getString("name");
        user.lastOnlineName = json.getString("lastOnlineName");
        user.branch = json.getEnum("branch", Branch.class, Branch.DEFAULT);
        user.currentRank = json.getEnum("rank", Rank.class, Rank.DEFAULT);
        user.sellAmount = json.getEnum("sellAmount", SellAmount.class, SellAmount.PER_1);
        user.ip = json.getString("ipAddress");
        user.totalEarnings = json.getLong("totalEarnings");
        user.setGems(json.getInt("gems"));

        //ranks
        user.ranks.clear();
        user.ranks.add(Rank.DEFAULT);
        if(json.has("ranks")){
            List<Rank> ranks = ListUtils.fromIterable(json.getAsJsonArray("ranks"), e -> JsonUtils.readEnum(Rank.class, e));
            user.ranks.addAll(ranks);
        }

        //Nickname
        user.nickname = null;
        if(json.has("nickname")){
            user.nickname = ChatUtils.fromJson(json.get("nickname"));
        }

        //Pets
        user.pets.clear();
        if(json.has("pets")){
            user.pets.addAll(json.getList("pets", e -> JsonUtils.readEnum(Pet.class, e)));
        }

        //Properties
        user.properties.clear();
        if(json.has("properties")){
            user.properties.addAll(json.getList("properties", e -> JsonUtils.readEnum(UserProperty.class, e)));
        }

        //Time stamps
        JsonBuf timeStamps = json.getBuf("timeStamps");
        user.lastLoad = timeStamps.getLong("lastLoad");
        user.nextResetTime = timeStamps.getLong("nextReset");
        user.nextAllowedBranchSwap = timeStamps.getLong("nextBranchSwap");

        //Last location
        if(json.has("lastLocation")){
            user.lastLocation = json.getLocation("lastLocation");
        }

        //Last known location
        if(json.has("lastKnownLoc")){
            user.entityLocation = json.getLocation("lastKnownLoc");
        }

        //User data type things
        user.grave.deserialize(json.get("grave"));
        user.cosmeticData.deserialize(json.get("cosmeticData"));
        user.homes.deserialize(json.get("homes"));
        user.interactions.deserialize(json.get("interactions"));
        user.dataContainer.deserialize(json.get("dataContainer"));

        //Material data
        user.matData.clear();
        if(json.has("soldData")){
            JsonObject soldData = json.getAsJsonObject("soldData");

            for (Map.Entry<String, JsonElement> e: soldData.entrySet()){
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
    public void onUnload(FtcUser user) {
        userFiles.remove(user.getUniqueId());
    }

    @Override
    public void delete(FtcUser id) {
        getFile(id.getUniqueId()).delete();
        deletedFiles.add(id.getUniqueId());
        onUnload(id);
    }

    @Override
    public File getFile(UUID id) {
        File result = userFiles.get(id);
        if(result != null) return result;

        return new File(USER_DIR, id.toString() + ".json");
    }

    public void writeJson(JsonBuf json, UUID id){
        File f = getFile(id);
        if(!f.exists()) return;

        try {
            JsonUtils.writeFile(json.getSource(), f);
        } catch (IOException e){
            logger.severe("Exception while attempting write user data for " + id.toString());
            e.printStackTrace();
        }
    }

    private void addDefaults(FtcUser id, JsonBuf json){
        json.add("name", id.getName());
        json.add("lastOnlineName", id.getName());

        JsonBuf timeStamps = JsonBuf.empty();
        timeStamps.add("lastLoad", System.currentTimeMillis());
        timeStamps.add("nextReset", System.currentTimeMillis() + CrownCore.getUserResetInterval());

        json.add("timeStamps", timeStamps);
    }

    public JsonBuf readJson(FtcUser id){
        File f = getFile(id.getUniqueId());
        JsonBuf json;

        if(!f.exists()){
            json = JsonBuf.empty();
            addDefaults(id, json);

            try {
                f.createNewFile();
                logger.info("Created user file for " + id.toString());

                writeJson(json, id.getUniqueId());
            } catch (IOException e){
                logger.severe("Could not create user file for " + id.toString());
                e.printStackTrace();
            }
        } else {
            try {
                JsonObject jsonO = JsonUtils.readFile(f);
                json = JsonBuf.of(jsonO);
            } catch (IOException e){
                logger.severe("Exception while reading user data for " + id.toString());
                e.printStackTrace();
                return null;
            }
        }

        return json;
    }

    public void clearFiles(){
        userFiles.clear();
    }
}
