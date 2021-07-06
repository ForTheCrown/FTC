package net.forthecrown.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.user.data.UserProperty;
import net.forthecrown.user.enums.Branch;
import net.forthecrown.user.enums.Pet;
import net.forthecrown.user.enums.Rank;
import net.forthecrown.user.enums.SellAmount;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.ListUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserYamlToJson {
    public static final File LEGACY_USER_DIR = new File(CrownCore.dataFolder().getPath() + File.separator + "playerdata");

    private final Logger logger;
    private final OutputStreamWriter writer;

    public static void activateAsync(){
        if(Bukkit.getOnlinePlayers().size() > 0) throw new IllegalStateException("Cannot start user data fixer with players online");

        Bukkit.getScheduler().runTaskAsynchronously(CrownCore.inst(), () -> {
            try {
                new UserYamlToJson().loop().finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private UserYamlToJson() throws IOException {
        this.logger = CrownCore.logger();

        File log = new File(CrownCore.dataFolder(), "user_yamlconversion_log.txt");
        log.createNewFile();

        this.writer = new OutputStreamWriter(new FileOutputStream(log));
    }

    public UserYamlToJson loop(){
        for (File f: LEGACY_USER_DIR.listFiles((dir, name) -> name.endsWith(".yml"))){
            log(Level.INFO, "Updating file: " + f.getName());

            if(f.length() < 1){
                log(Level.WARNING, "Found empty file, deleting");
                f.delete();
                continue;
            }

            YamlUserInstance yamlUser = new YamlUserInstance(f);

            if(yamlUser.isNonPlayer()){
                log(Level.WARNING, "Found non player data, deleting");
                f.delete();
                continue;
            }

            writeJson(yamlUser.getConverted(), yamlUser.id, f);
        }

        Runtime.getRuntime().gc();
        return this;
    }

    private void writeJson(JsonBuf json, UUID id, File origFile){
        try {
            File newFile = new File(UserJsonSerializer.USER_DIR, id.toString() + ".json");
            newFile.createNewFile();

            JsonUtils.writeFile(json.getSource(), newFile);

            origFile.delete();
            log(Level.INFO, "Saved user data for " + id.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void finish(){
        try {
            writer.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void log(Level level, String message){
        logger.log(level, message);

        try {
            writer.write(level.getName() + " " + message + "\n");
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static class YamlUserInstance {

        private final UUID id;
        private final YamlConfiguration yaml;

        public YamlUserInstance(File file) {

            this.id = UUID.fromString(file.getName().replaceAll(".yml", ""));
            this.yaml = YamlConfiguration.loadConfiguration(file);
        }

        public boolean isNonPlayer(){
            OfflinePlayer player = Bukkit.getOfflinePlayer(id);

            return player.getName() == null;
        }

        //Beyond tiring
        public JsonBuf getConverted(){
            JsonBuf json = JsonBuf.empty();

            json.add("name", yaml.getString("PlayerName"));
            json.add("lastOnlineName", yaml.getString("PlayerName"));

            //Rank
            Rank rank = Rank.valueOf(yaml.getString("CurrentRank").toUpperCase());
            if(rank != Rank.DEFAULT) json.addEnum("rank", rank);

            //Branch
            Branch branch = Branch.valueOf(yaml.getString("Branch").toUpperCase());
            if(branch != Branch.DEFAULT) json.add("branch", branch);

            //Properties
            Set<UserProperty> properties = convertProperties();
            if(!properties.isEmpty()) json.addList("properties", properties);

            //Pets
            List<Pet> pets = ListUtils.convertToList(yaml.getStringList("Pets"), str -> Pet.valueOf(str.toUpperCase()));
            if(!pets.isEmpty()) json.addList("pets", pets);

            //gems
            int gems = yaml.getInt("Gems", 0);
            if(gems > 0) json.add("gems", gems);

            //Total earnings
            long totalEarnings = yaml.getLong("TotalEarnings");
            if(totalEarnings > 0) json.add("totalEarnings", totalEarnings);

            //Sell amount
            SellAmount amount = SellAmount.valueOf(yaml.getString("SellAmount").toUpperCase());
            if(amount != SellAmount.PER_1) json.addEnum("sellAmount", amount);

            //Last known location
            Location lastKnownLoc = yaml.getLocation("LastLocation");
            if(lastKnownLoc != null) json.addLocation("lastKnownLoc", lastKnownLoc);

            //IP adress
            String ip = yaml.getString("IPAddress");
            if(ip != null) json.add("ipAddress", ip);

            //Nick name
            String nick = yaml.getString("NickName");
            if(nick != null) {
                Component nickC = ChatUtils.convertString(nick);
                json.add("nickname", ChatUtils.toJson(nickC));
            }

            //Time stamps
            json.add("timeStamps", convertTimeStamps());

            //Ranks
            List<String> rankList = yaml.getStringList("AvailableRanks");
            if(!ListUtils.isNullOrEmpty(rankList)){
                Set<Rank> ranks = ListUtils.convertToSet(rankList, s -> Rank.valueOf(s.toUpperCase()));
                ranks.remove(Rank.DEFAULT);

                json.addList("ranks", ranks);
            }

            //Homes
            ConfigurationSection homesSection = yaml.getConfigurationSection("Homes");
            if(homesSection != null){
                JsonBuf jsonHomes = JsonBuf.empty();

                for (String s: homesSection.getKeys(false)){
                    jsonHomes.addLocation(s, homesSection.getLocation(s));
                }

                json.add("homes", jsonHomes);
            }

            //Material data
            ConfigurationSection matSection = yaml.getConfigurationSection("AmountEarned");
            if(matSection != null){
                JsonBuf matData = JsonBuf.empty();

                for (String s: matSection.getKeys(false)){
                    int amountSold = matSection.getInt(s);
                    matData.add(s.toLowerCase(), amountSold);
                }

                json.add("soldData", matData);
            }

            //Interactions
            JsonBuf interactions = JsonBuf.empty();

            ConfigurationSection marriageSec = yaml.getConfigurationSection("Marriage");
            if(marriageSec != null){
                boolean cToggled = marriageSec.getBoolean("MarriageChat", false);
                boolean acceptsProp = marriageSec.getBoolean("AcceptingProposals", true);

                String marriedToString = marriageSec.getString("MarriedTo");
                UUID marriedTo;
                if(marriedToString == null) marriedTo = null;
                else marriedTo = UUID.fromString(marriedToString);

                long lastAction = marriageSec.getLong("LastMarriageAction");

                if(cToggled) interactions.add("marriageChat", true);
                if(acceptsProp) interactions.add("acceptingProposals", false);
                if(marriedTo != null) interactions.addUUID("marriedTo", marriedTo);
                if(lastAction != 0) interactions.add("lastmarriage", lastAction);
            }

            List<String> blocked = yaml.getStringList("BlockedUsers");
            if(!ListUtils.isNullOrEmpty(blocked)){
                interactions.addList("blocked", blocked, JsonPrimitive::new);
            }

            json.add("interactions", interactions);

            //Grave
            try {
                List<ItemStack> graveItems = (List<ItemStack>) yaml.getList("Grave");
                JsonArray array = JsonUtils.writeCollection(graveItems, JsonUtils::writeItem);

                json.add("grave", array);
            } catch (Exception ignored ){}

            return json;
        }

        private JsonBuf convertTimeStamps(){
            long lastLoad = yaml.getLong("TimeStamps.LastLoad");
            long nextReset = yaml.getLong("TimeStamps.NextResetTime");
            long nextBranchSwap = yaml.getLong("TimeStamps.NextBranchSwap");

            JsonBuf json = JsonBuf.empty();

            json.add("lastLoad", lastLoad);
            json.add("nextReset", nextReset);
            if(nextBranchSwap != 0) json.add("nextBranchSwap", nextBranchSwap);

            return json;
        }

        private Set<UserProperty> convertProperties(){
            Set<UserProperty> result = new HashSet<>();

            addPropertyIfNeg("CanSwapBranch", UserProperty.CANNOT_SWAP_BRANCH, result);
            addPropertyIfNeg("AllowsRidingPlayers", UserProperty.FORBIDS_RIDING, result);
            addPropertyIfNeg("AllowsEmotes", UserProperty.FORBIDS_EMOTES, result);
            addPropertyIfNeg("ProfilePublic", UserProperty.PROFILE_PRIVATE, result);
            addPropertyIfNeg("AllowsTPA", UserProperty.FORBIDS_TPA, result);
            addPropertyIfNeg("AcceptsPay", UserProperty.FORBIDS_PAY, result);

            addPropertyIfPos("EavesDropping", UserProperty.LISTENING_TO_EAVESDROPPER, result);
            addPropertyIfPos("Vanished", UserProperty.VANISHED, result);
            addPropertyIfPos("Flying", UserProperty.FLYING, result);
            addPropertyIfPos("GodMode", UserProperty.GOD_MODE, result);

            return result;
        }

        private void addPropertyIfNeg(String path, UserProperty property, Set<UserProperty> list){
            boolean check = yaml.getBoolean(path, true);
            if(!check) list.add(property);
        }

        private void addPropertyIfPos(String path, UserProperty property, Set<UserProperty> list){
            boolean check = yaml.getBoolean(path, false);
            if(check) list.add(property);
        }
    }
}
