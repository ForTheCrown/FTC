package net.forthecrown.economy.pirates;

import com.google.common.io.Files;
import com.google.gson.JsonObject;
import net.forthecrown.core.Crown;
import net.forthecrown.economy.pirates.merchants.*;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static net.forthecrown.registry.Registries.NPCS;

public class FtcPirateEconomy implements PirateEconomy {
    private final CrownRandom random;

    private final EnchantMerchant enchantMerchant;
    private final HeadMerchant headMerchant;
    private final ParrotMerchant parrotMerchant;

    private final MaterialMerchant miningMerchant;
    private final MaterialMerchant dropsMerchant;
    private final MaterialMerchant cropsMerchant;

    private final GrapplingHookMerchant ghMerchant;

    private int maxEarnings = 500000;

    public FtcPirateEconomy(){
        this.random = new CrownRandom();

        Crown.getDayUpdate().addListener(this::updateDate);

        this.enchantMerchant = new EnchantMerchant();
        this.headMerchant = new HeadMerchant();
        this.parrotMerchant = new ParrotMerchant();
        this.miningMerchant = new MaterialMerchant("Mining");
        this.dropsMerchant = new MaterialMerchant("Drops");
        this.cropsMerchant = new MaterialMerchant("Crops");
        this.ghMerchant = new GrapplingHookMerchant();

        registerMerchants();
        reload();
    }

    private void registerMerchants(){
        NPCS.register(parrotMerchant.key(), parrotMerchant);
        NPCS.register(ghMerchant.key(), ghMerchant);

        NPCS.register(enchantMerchant.key(), enchantMerchant);
        NPCS.register(headMerchant.key(), headMerchant);

        NPCS.register(miningMerchant.key(), miningMerchant);
        NPCS.register(dropsMerchant.key(), dropsMerchant);
        NPCS.register(cropsMerchant.key(), cropsMerchant);
    }

    @Override
    public void updateDate(){
        NPCS.values().forEach(usable -> {
            if(!(usable instanceof BlackMarketMerchant)) return;

            BlackMarketMerchant merchant = (BlackMarketMerchant) usable;
            merchant.update(random, Crown.getDayUpdate().getDay());
        });
    }

    public void save(){
        JsonObject json = new JsonObject();

        json.addProperty("maxEarnings", maxEarnings);

        json.add("enchants", enchantMerchant.serialize());
        json.add("mining", miningMerchant.serialize());
        json.add("crops", cropsMerchant.serialize());
        json.add("drops", dropsMerchant.serialize());
        json.add("heads", headMerchant.serialize());
        json.add("ghMerchant", ghMerchant.serialize());

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

        enchantMerchant.load(json.get("enchants"));
        miningMerchant.load(json.get("mining"));
        cropsMerchant.load(json.get("crops"));
        dropsMerchant.load(json.get("drops"));
        headMerchant.load(json.get("heads"));

        if(json.has("ghMerchant")) ghMerchant.load(json.get("ghMerchant"));
    }

    private JsonObject getJson() throws IOException {
        File file = getFile();

        return JsonUtils.readFile(file);
    }

    private void setJson(JsonObject json) throws IOException {
        File file = getFile();

        JsonUtils.writeFile(json, file);
    }

    private File getFile() throws IOException {
        File file = new File(Crown.dataFolder().getPath() + File.separator + "blackmarket.json");
        if(!file.exists()){
            file.createNewFile();

            InputStream stream = Crown.resource("blackmarket.json");
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
    @Override public GrapplingHookMerchant getGhMerchant() { return ghMerchant; }

    @Override
    public int getMaxEarnings() {
        return maxEarnings;
    }

    @Override
    public void setMaxEarnings(int maxEarnings) {
        this.maxEarnings = maxEarnings;
    }
}
