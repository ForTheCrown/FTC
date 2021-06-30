package net.forthecrown.squire.enchantment;

import net.forthecrown.squire.Squire;
import net.minecraft.core.Registry;
import org.bukkit.enchantments.Enchantment;

import java.lang.reflect.Field;

public class RoyalEnchants {

    private static DolphinSwimmer dolphinSwimmer;
    private static HealingBlock healingBlock;
    private static PoisonCrit poisonCrit;
    private static StrongAim strongAim;

    public void registerEnchants(){
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);

            dolphinSwimmer = register(new DolphinSwimmer());
            poisonCrit = register(new PoisonCrit());
            healingBlock = register(new HealingBlock());
            strongAim = register(new StrongAim());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public <T extends RoyalEnchant> T register(final T enchant){
        Registry.register(Registry.ENCHANTMENT, enchant.getKey().asString(), enchant.getHandle());
        org.bukkit.enchantments.Enchantment.registerEnchantment(enchant);

        Squire.inst().getLogger().info("Registered enchantment: " + enchant.getKey().asString());
        return enchant;
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
