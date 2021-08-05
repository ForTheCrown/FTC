package net.forthecrown.user.data;

import com.google.gson.JsonPrimitive;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.serializer.JsonSerializable;
import org.bukkit.Material;

public class SoldMaterialData implements JsonSerializable {

    private final Material material;
    private int earned;
    private short price;

    public SoldMaterialData(Material material) {
        this.material = material;

        this.earned = 0;
        this.price = -1;
    }

    public void recalculate(){
        if(getEarned() > ComVars.getMaxShopEarnings()) {
            price = 0;
            return;
        }

        short startPrice = getOriginalPrice();

        if(earned <= 0) price = -1;
        else price = (short) Math.ceil((1+startPrice)*Math.exp(-earned*Math.log(1+startPrice)/ ComVars.getMaxShopEarnings())-1);
    }

    public Material getMaterial() {
        return material;
    }

    public void addEarned(int amount){
        earned += amount;
    }

    public int getEarned() {
        return earned;
    }

    public void setEarned(int sold) {
        this.earned = sold;
    }

    public short getPrice() {
        if(price == -1) return getOriginalPrice(); //Return default price if one hasn't been set
        return price;
    }

    public void setPrice(short price) {
        this.price = price;
    }

    public boolean isPriceSet(){
        return getPrice() != getOriginalPrice();
    }

    public short getOriginalPrice(){
        return ForTheCrown.getPriceMap().get(getMaterial());
    }

    @Override
    public JsonPrimitive serialize() {
        if(earned < 1) return null;
        return new JsonPrimitive(earned);
    }
}
