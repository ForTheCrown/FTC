package net.forthecrown.user;

import com.google.gson.JsonPrimitive;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.serializer.JsonSerializable;
import org.bukkit.Material;

/**
 * Data for the material a user has sold.
 */
public class SoldMaterialData implements JsonSerializable {

    private final Material material;
    private int earned;
    private short price;

    public SoldMaterialData(Material material) {
        this.material = material;

        this.earned = 0;
        this.price = -1;
    }

    /**
     * Recalculates the price of this material based on the
     * amount earned thus far.
     * <p></p>
     * Uses Wout's math function to determine the price.
     * I, Julie, am dumb and don't know math, so I'm super
     * grateful I didn't have to write this :D.
     */
    public void recalculate(){
        if(getEarned() > ComVars.getMaxShopEarnings()) {
            price = 0;
            return;
        }

        short startPrice = getOriginalPrice();

        if(earned <= 0) price = -1;
        else price = (short) Math.ceil((1+startPrice)*Math.exp(-earned*Math.log(1+startPrice)/ ComVars.getMaxShopEarnings())-1);
    }

    /**
     * Gets the material this data is for
     * @return The material this data is for
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Adds to the earnings of this data
     * @param amount The amount to add
     */
    public void addEarned(int amount){
        earned += amount;
    }

    /**
     * Gets the amount that's been earned from this material
     * @return This material's earnings
     */
    public int getEarned() {
        return earned;
    }

    /**
     * Sets the amount that's been earned from this material
     * @param earned The amount earned
     */
    public void setEarned(int earned) {
        this.earned = earned;
    }

    /**
     * Gets the price of the material
     * <p></p>
     * Will return {@link SoldMaterialData#getOriginalPrice()} if no price is set
     * @return
     */
    public short getPrice() {
        return price == -1 ? getOriginalPrice() : price;
    }

    /**
     * Sets the price of the material
     * @param price The material's price
     */
    public void setPrice(short price) {
        this.price = price;
    }

    /**
     * Returns whether the material has a set price
     * @return price != originalPrice
     */
    public boolean isPriceSet(){
        return getPrice() != getOriginalPrice();
    }

    /**
     * Gets the original price for this material in
     * {@link Crown#getPriceMap()}
     * @return The material's original price
     */
    public short getOriginalPrice(){
        return Crown.getPriceMap().get(getMaterial());
    }

    @Override
    public JsonPrimitive serialize() {
        if(earned < 1) return null;
        return new JsonPrimitive(earned);
    }
}
