package net.forthecrown.utils.inventory;

import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PotionItemBuilder extends BaseItemBuilder<PotionItemBuilder> {
    private PotionData baseEffect;
    private Color color;
    private final List<PotionEffect> effects = new ArrayList<>();

    public PotionItemBuilder(Material material, int amount) {
        super(material, amount);

        Validate.isTrue(
                material == Material.GLASS_BOTTLE
                || material == Material.SPLASH_POTION
                || material == Material.TIPPED_ARROW
                || material == Material.LINGERING_POTION,

                "Invalid material for potion builder: '%s'", material.name()
        );
    }

    public PotionItemBuilder setBaseEffect(PotionData baseEffect) {
        this.baseEffect = baseEffect;
        return this;
    }

    public PotionItemBuilder setColor(Color color) {
        this.color = color;
        return this;
    }

    public PotionItemBuilder addEffect(PotionEffect effect) {
        this.effects.add(effect);
        return this;
    }

    @Override
    protected PotionItemBuilder getThis() {
        return this;
    }

    @Override
    protected void onBuild(ItemStack item, ItemMeta meta) {
        var potionMeta = (PotionMeta) meta;

        potionMeta.setBasePotionData(baseEffect);
        potionMeta.setColor(color);
        potionMeta.clearCustomEffects();

        for (var e: this.effects) {
            potionMeta.addCustomEffect(e, true);
        }
    }
}