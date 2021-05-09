package net.forthecrown.royals.enchantments;

import net.forthecrown.royals.Royals;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.lang.reflect.Field;

public class RoyalEnchants {

    private final Royals plugin;

    private static DolphinSwimmer dolphinSwimmer;
    private static HealingBlock healingBlock;
    private static PoisonCrit poisonCrit;
    private static StrongAim strongAim;

    public RoyalEnchants(Royals plugin){
        this.plugin = plugin;
    }

    public static RoyalEnchants init(){
        RoyalEnchants e = new RoyalEnchants(Royals.inst);
        e.registerEnchantments();

        return e;
    }

    public void registerEnchantments(){
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);

            strongAim = new StrongAim(new NamespacedKey(plugin, "strongaim"), plugin);
            healingBlock = new HealingBlock(new NamespacedKey(plugin, "healingblock"), plugin);
            poisonCrit = new PoisonCrit(new NamespacedKey(plugin, "criticalpoison"), plugin);
            dolphinSwimmer = new DolphinSwimmer(new NamespacedKey(plugin, "dolphinswimmer"), plugin);

            Enchantment.registerEnchantment(strongAim);
            Enchantment.registerEnchantment(healingBlock);
            Enchantment.registerEnchantment(poisonCrit);
            Enchantment.registerEnchantment(dolphinSwimmer);

            //Only time that exception is thrown is when we reload
            //And it tries to register them twice
        } catch (Exception ignored) {}
    }

    public static DolphinSwimmer dolphinSwimmer() {
        return dolphinSwimmer;
    }

    public static HealingBlock healingBlock() {
        return healingBlock;
    }

    public static PoisonCrit poisonCrit() {
        return poisonCrit;
    }

    public static StrongAim strongAim() {
        return strongAim;
    }
}
