package net.forthecrown.economy.pirates;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import net.forthecrown.core.CrownCore;
import net.forthecrown.economy.pirates.merchants.*;
import net.forthecrown.registry.BaseRegistry;
import net.forthecrown.registry.Registry;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.CrownUtils;
import net.kyori.adventure.key.Key;

import java.io.*;
import java.util.Calendar;

public class CrownPirateEconomy implements PirateEconomy {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private final CrownRandom random;

    private final EnchantMerchant enchantMerchant;
    private final HeadMerchant headMerchant;
    private final ParrotMerchant parrotMerchant;
    private final MaterialMerchant miningMerchant;
    private final MaterialMerchant dropsMerchant;
    private final MaterialMerchant cropsMerchant;

    public final Registry<UsablePirateNpc> byId = new BaseRegistry<>();

    private int maxEarnings = 500000;
    private byte day = 0;

    public CrownPirateEconomy(){
        this.random = new CrownRandom();

        this.enchantMerchant = new EnchantMerchant();
        this.headMerchant = new HeadMerchant();
        this.parrotMerchant = new ParrotMerchant();
        this.miningMerchant = new MaterialMerchant("Mining");
        this.dropsMerchant = new MaterialMerchant("Drops");
        this.cropsMerchant = new MaterialMerchant("Crops");

        registerMerchants();
    }

    private void registerMerchants(){
        byId.register(enchantMerchant.key(), enchantMerchant);
        byId.register(headMerchant.key(), headMerchant);
        byId.register(parrotMerchant.key(), parrotMerchant);
        byId.register(miningMerchant.key(), miningMerchant);
        byId.register(dropsMerchant.key(), dropsMerchant);
        byId.register(cropsMerchant.key(), cropsMerchant);
    }

    @Override
    public boolean shouldUpdateDate(){
        return day != Calendar.getInstance(CrownUtils.SERVER_TIME_ZONE).get(Calendar.DAY_OF_WEEK);
    }

    @Override
    public void updateDate(){
        this.day = (byte) Calendar.getInstance(CrownUtils.SERVER_TIME_ZONE).get(Calendar.DAY_OF_WEEK);

        byId.getEntries().forEach(usable -> {
            if(!(usable instanceof BlackMarketMerchant)) return;

            BlackMarketMerchant merchant = (BlackMarketMerchant) usable;
            merchant.update(random, day);
        });
    }

    public void save(){
        JsonObject json = new JsonObject();

        json.addProperty("maxEarnings", maxEarnings);
        json.addProperty("day", day);

        json.add("enchants", enchantMerchant.serialize());
        json.add("mining", miningMerchant.serialize());
        json.add("crops", cropsMerchant.serialize());
        json.add("drops", dropsMerchant.serialize());
        json.add("heads", headMerchant.serialize());

        try {
            setJson(json);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void reload(){
        JsonObject json;
        try {
            json = getJson();
        } catch (IOException e){
            e.printStackTrace();
            return;
        }

        maxEarnings = json.get("maxEarnings").getAsInt();
        day = json.get("day").getAsByte();

        enchantMerchant.load(json.get("enchants"));
        miningMerchant.load(json.get("mining"));
        cropsMerchant.load(json.get("crops"));
        dropsMerchant.load(json.get("drops"));
        headMerchant.load(json.get("heads"));

        if(shouldUpdateDate()) updateDate();
    }

    private JsonObject getJson() throws IOException {
        File file = getFile();

        FileReader reader = new FileReader(file);
        JsonParser parser = new JsonParser();

        return parser.parse(reader).getAsJsonObject();
    }

    private void setJson(JsonObject json) throws IOException {
        File file = getFile();

        FileWriter fWriter = new FileWriter(file);
        JsonWriter writer = new JsonWriter(fWriter);

        GSON.toJson(json, writer);
    }

    private File getFile() throws IOException {
        File file = new File(CrownCore.dataFolder().getPath() + File.separator + "blackmarket.json");
        if(!file.exists()){
            file.createNewFile();

            InputStream stream = CrownCore.resource("blackmarket.json");
            Files.write(stream.readAllBytes(), file);
        }

        return file;
    }

    @Override public EnchantMerchant getEnchantMerchant() { return enchantMerchant; }
    @Override public MaterialMerchant getMiningMerchant() { return miningMerchant; }
    @Override public MaterialMerchant getDropsMerchant() { return dropsMerchant; }
    @Override public MaterialMerchant getCropsMerchant() { return cropsMerchant; }
    @Override public HeadMerchant getHeadMerchant() { return headMerchant; }
    @Override public ParrotMerchant getParrotMerchant() { return parrotMerchant; }

    @Override
    public int getMaxEarnings() {
        return maxEarnings;
    }

    @Override
    public void setMaxEarnings(int maxEarnings) {
        this.maxEarnings = maxEarnings;
    }

    @Override
    public UsablePirateNpc getNpcById(String id){
        return byId.get(Key.key(id));
    }
}
