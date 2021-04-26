package net.forthecrown.core.types;

import net.forthecrown.core.api.BlackMarket;
import net.forthecrown.core.api.DailyEnchantment;
import net.forthecrown.core.comvars.ComVar;
import org.bukkit.enchantments.Enchantment;

public class CrownDailyEnchantment implements DailyEnchantment {

    private final CrownBlackMarket owner;
    private final Enchantment enchantment;
    private final byte level;
    private int price;
    private final ComVar<Integer> basePrice;

    public CrownDailyEnchantment(CrownBlackMarket owner, Enchantment ench, ComVar<Integer> basePrice, byte level){
        this.owner = owner;
        this.enchantment = ench;
        this.level = level;
        this.basePrice = basePrice;

        basePrice.setOnUpdate(integer -> price = integer*level);
        price = basePrice.getValue()*level;
    }

    @Override
    public int getBasePrice() {
        return basePrice.getValue();
    }

    @Override
    public byte getLevel(){
        return level;
    }

    @Override
    public int getPrice(){
        return price;
    }

    @Override
    public Enchantment getEnchantment(){
        return enchantment;
    }

    @Override
    public BlackMarket getOwner() {
        return owner;
    }
}
