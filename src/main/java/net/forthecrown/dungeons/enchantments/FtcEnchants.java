package net.forthecrown.dungeons.enchantments;

import net.forthecrown.core.module.OnEnable;
import net.forthecrown.utils.VanillaAccess;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class FtcEnchants {

    public static final DolphinSwimmer DOLPHIN_SWIMMER = new DolphinSwimmer();
    public static final HealingBlock HEALING_BLOCK = new HealingBlock();
    public static final PoisonCrit POISON_CRIT = new PoisonCrit();
    public static final StrongAim STRONG_AIM = new StrongAim();

    @OnEnable
    private static void init() {
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);

            MappedRegistry<net.minecraft.world.item.enchantment.Enchantment>
                    enchantRegistry = (MappedRegistry) BuiltInRegistries.ENCHANTMENT;

            VanillaAccess.unfreeze(enchantRegistry);

            register(DOLPHIN_SWIMMER);
            register(HEALING_BLOCK);
            register(POISON_CRIT);
            register(STRONG_AIM);

            enchantRegistry.freeze();
            Enchantment.stopAcceptingRegistrations();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static <T extends FtcEnchant> T register(final T enchant) {
        Registry.register(BuiltInRegistries.ENCHANTMENT, enchant.getKey().asString(), enchant.getHandle());

        if (Enchantment.getByKey(enchant.getKey()) == null) {
            Enchantment.registerEnchantment(enchant);
        }

        return enchant;
    }

    public static void addEnchant(ItemStack item, FtcEnchant enchant, int level) {
        var meta = item.getItemMeta();
        addEnchant(meta, enchant, level);

        item.setItemMeta(meta);
    }

    public static void addEnchant(ItemMeta meta, FtcEnchant enchant, int level) {
        meta.addEnchant(enchant, level, true);

        List<Component> lore = new ArrayList<>();

        lore.add(
                enchant.displayName(level)
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        );

        if(meta.lore() != null) {
            lore.addAll(meta.lore());
        }

        meta.lore(lore);
    }
}