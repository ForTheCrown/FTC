package net.forthecrown.utils.inventory;

import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.util.Collection;

public class PotionItemBuilder extends BaseItemBuilder<PotionItemBuilder> {
    public static final PotionData EMPTY = new PotionData(PotionType.UNCRAFTABLE, false, false);

    PotionItemBuilder(Material material, int amount) {
        super(material, amount);

        Validate.isTrue(
                Bukkit.getItemFactory().getItemMeta(material) instanceof PotionMeta,
                "Invalid material for potion builder: '%s'", material.name()
        );
    }

    PotionItemBuilder(ItemStack stack, ItemMeta baseMeta) {
        super(stack, baseMeta);
    }

    private PotionMeta meta() {
        return (PotionMeta) baseMeta;
    }

    public PotionItemBuilder setBaseEffect(PotionData baseEffect) {
        if (baseEffect != null) {
            meta().setBasePotionData(baseEffect);
        } else {
            meta().setBasePotionData(EMPTY);
        }

        return this;
    }

    public PotionItemBuilder setColor(Color color) {
        meta().setColor(color);
        return this;
    }

    public PotionItemBuilder addEffect(PotionEffect effect) {
        meta().addCustomEffect(effect, true);
        return this;
    }

    public PotionItemBuilder setEffects(Collection<PotionEffect> effects) {
        meta().clearCustomEffects();

        for (var e: effects) {
            addEffect(e);
        }

        return this;
    }

    @Override
    protected PotionItemBuilder getThis() {
        return this;
    }
}