package net.forthecrown.economy.houses;

import com.google.gson.JsonElement;
import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcVars;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.minecraft.util.Mth;
import org.bukkit.Material;

public class HouseMaterialData implements JsonSerializable {
    private final Material material;
    private float demand;
    private int price;
    private int supply;

    public HouseMaterialData(Material material) {
        this.material = material;
        this.price = -1;

        reset();
    }

    public HouseMaterialData(JsonElement e, Material material) {
        JsonWrapper json = JsonWrapper.of(e.getAsJsonObject());

        this.material = material;
        this.price = json.getInt("price");
        this.supply = json.getInt("supply");
        this.demand = json.getInt("demand");

        recalculate();
    }

    public Material getMaterial() {
        return material;
    }

    public float getDemand() {
        return demand;
    }

    public void setDemand(float demand) {
        this.demand = Mth.clamp(demand, -1F, 1F);
    }

    public int getSupply() {
        return supply;
    }

    public void setSupply(int supply) {
        this.supply = supply;
    }

    public int getPrice() {
        return price == -1 ? getBasePrice() : price;
    }

    public boolean priceChanged() {
        return getPrice() != getBasePrice();
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void addDemand(float amount) {
        setDemand(demand + amount);
    }

    public void addSupply(int amount) {
        this.supply += amount;
    }

    public void recalculate() {
        // uhuh yeah, how doe
    }

    public void reset() {
        this.demand = FtcVars.houses_startingDemand.get();
        this.supply = FtcVars.houses_startingSupply.get();

        recalculate();
    }

    public int getBasePrice() {
        return Crown.getPriceMap().get(material);
    }

    @Override
    public JsonElement serialize() {
        JsonWrapper json = JsonWrapper.empty();

        json.add("price", price);
        json.add("supply", supply);
        json.add("demand", demand);

        return json.getSource();
    }
}
