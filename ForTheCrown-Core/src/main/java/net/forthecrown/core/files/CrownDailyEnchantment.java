package net.forthecrown.core.files;

import net.forthecrown.core.api.BlackMarket;
import net.forthecrown.core.api.DailyEnchantment;
import org.bukkit.enchantments.Enchantment;

public class CrownDailyEnchantment implements DailyEnchantment {

    private final CrownBlackMarket owner;
    private final Enchantment enchantment;
    private final int level;
    private final int price;
    private final int basePrice;

    public CrownDailyEnchantment(CrownBlackMarket owner, Enchantment ench, int basePrice, int level){
        this.owner = owner;
        this.enchantment = ench;
        this.level = level;
        this.basePrice = basePrice;

        price = basePrice*level;
    }

    @Override
    public int getBasePrice() {
        return basePrice;
    }

    @Override
    public int getLevel(){
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
