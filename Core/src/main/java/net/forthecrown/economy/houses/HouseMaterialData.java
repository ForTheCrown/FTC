package net.forthecrown.economy.houses;

import com.google.gson.JsonElement;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import org.bukkit.Material;

public class HouseMaterialData implements JsonSerializable {
    private final Material material;
    private int price;
    private int demand;
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

    public int getDemand() {
        return demand;
    }

    public void setDemand(int demand) {
        this.demand = demand;
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

    public void addDemand(int amount) {
        this.demand += amount;
    }

    public void addSupply(int amount) {
        this.supply += amount;
    }

    public void recalculate() {

    }

    public void reset() {
        this.demand = ComVars.getHousesStartingDemand();
        this.supply = ComVars.getHousesStartingSupply();

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
