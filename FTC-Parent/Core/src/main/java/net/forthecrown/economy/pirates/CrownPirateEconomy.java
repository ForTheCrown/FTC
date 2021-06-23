package net.forthecrown.economy.blackmarket;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import net.forthecrown.core.CrownCore;
import net.forthecrown.economy.blackmarket.merchants.EnchantMerchant;
import net.forthecrown.economy.blackmarket.merchants.MaterialMerchant;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.CrownUtils;
import org.bukkit.Material;

import java.io.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CrownPirateEconomy implements PirateEconomy {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private final CrownRandom random;

    private final EnchantMerchant enchantMerchant;
    private final MaterialMerchant miningMerchant;
    private final MaterialMerchant dropsMerchant;
    private final MaterialMerchant cropsMerchant;

    private int maxEarnings = 500000;
    private byte day = 0;

    public CrownPirateEconomy(){
        this.random = new CrownRandom();

        this.enchantMerchant = new EnchantMerchant();
    }

    @Override
    public void updateDate(){
        this.day = (byte) Calendar.getInstance(CrownUtils.SERVER_TIME_ZONE).get(Calendar.DAY_OF_WEEK);

        enchantMerchant.update(random, day);
    }

    public void save(){
        JsonObject json = new JsonObject();

        json.addProperty("maxEarnings", maxEarnings);
        json.addProperty("day", day);

        json.add("enchants", enchantMerchant.serialize());


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

    @Override
    public EnchantMerchant getEnchantMerchant() {
        return enchantMerchant;
    }

    @Override
    public MaterialMerchant getMiningMerchant() {
        return miningMerchant;
    }

    @Override
    public MaterialMerchant getDropsMerchant() {
        return dropsMerchant;
    }

    @Override
    public MaterialMerchant getCropsMerchant() {
        return cropsMerchant;
    }

    @Override
    public int getMaxEarnings() {
        return maxEarnings;
    }
}
