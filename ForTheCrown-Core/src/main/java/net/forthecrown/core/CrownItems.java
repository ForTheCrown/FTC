package net.forthecrown.core;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class CrownItems{

    public static ItemStack getCoins(int amount){
        return FtcCore.makeItem(Material.SUNFLOWER, 1, true, "&eRhines", "&6Worth " + amount + " Rhines", "&8Do /deposit to add this to your balance");
    }

}
