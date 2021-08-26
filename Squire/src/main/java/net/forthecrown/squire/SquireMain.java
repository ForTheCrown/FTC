package net.forthecrown.squire;

import net.forthecrown.squire.enchantment.RoyalEnchants;
import org.bukkit.plugin.java.JavaPlugin;

public class SquireMain extends JavaPlugin implements Squire {

    static SquireMain main;
    static RoyalEnchants enchants;

    @Override
    public void onLoad() {
        main = this;

        enchants = new RoyalEnchants();
        enchants.registerEnchants();
    }
}
