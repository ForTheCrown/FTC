package net.forthecrown.economy.pirates;

import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.forthecrown.core.CrownCore;
import net.forthecrown.economy.pirates.merchants.*;
import net.forthecrown.registry.CloseableRegistry;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.utils.CrownRandom;
import net.kyori.adventure.key.Key;

import java.io.*;

public class CrownPirateEconomy implements PirateEconomy {
    private final CrownRandom random;

    private final EnchantMerchant enchantMerchant;
    private final HeadMerchant headMerchant;
    private final ParrotMerchant parrotMerchant;
    private final MaterialMerchant miningMerchant;
    private final MaterialMerchant dropsMerchant;
    private final MaterialMerchant cropsMerchant;
    private final GrapplingHookMerchant ghMerchant;

    public final CloseableRegistry<UsablePirateNpc> registry = Registries.createCloseable("black_market_npcs");

    private int maxEarnings = 500000;

    public CrownPirateEconomy(){
        this.random = new CrownRandom();

        CrownCore.getDayUpdate().addListener(this::updateDate);

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
        registry.register(parrotMerchant.key(), parrotMerchant);
        registry.register(ghMerchant.key(), ghMerchant);

        registry.register(enchantMerchant.key(), enchantMerchant);
        registry.register(headMerchant.key(), headMerchant);

        registry.register(miningMerchant.key(), miningMerchant);
        registry.register(dropsMerchant.key(), dropsMerchant);
        registry.register(cropsMerchant.key(), cropsMerchant);

        registry.close();
    }

    @Override
    public void updateDate(){
        registry.values().forEach(usable -> {
            if(!(usable instanceof BlackMarketMerchant)) return;

            BlackMarketMerchant merchant = (BlackMarketMerchant) usable;
            merchant.update(random, CrownCore.getDayUpdate().getDay());
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

        FileReader reader = new FileReader(file);
        JsonParser parser = new JsonParser();

        return parser.parse(reader).getAsJsonObject();
    }

    private void setJson(JsonObject json) throws IOException {
        File file = getFile();

        FileWriter writer = new FileWriter(file);
        writer.write(json.toString());
        writer.close();
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
    @Override public GrapplingHookMerchant getGhMerchant() { return ghMerchant; }

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
        return registry.get(Key.key(id));
    }

    @Override
    public Registry<UsablePirateNpc> getNpcRegistry() {
        return registry;
    }
}
