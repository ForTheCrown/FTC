package net.forthecrown.user.data;

import net.forthecrown.core.CrownCore;
import org.bukkit.Material;

public class SoldMaterialData {

    private final Material material;
    private int earned;
    private short price;

    public SoldMaterialData(Material material) {
        this.material = material;

        this.earned = 0;
        this.price = -1;
    }

    public void recalculate(){
        short startPrice = CrownCore.getItemPrice(material);

        if(earned <= 0) price = startPrice;

        price = (short) Math.ceil((1+startPrice)*Math.exp(-earned*Math.log(1+startPrice)/500000 )-1 );
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
        if(price == -1) return CrownCore.getItemPrice(material); //Return default price if one hasn't been set
        return price;
    }

    public void setPrice(short price) {
        this.price = price;
    }

    public boolean isPriceSet(){
        return getPrice() != CrownCore.getItemPrice(material);
    }
}
