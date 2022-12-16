package net.forthecrown.inventory.weapon.upgrades;

import net.forthecrown.inventory.weapon.RoyalSword;
import net.kyori.adventure.text.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v1_19_R2.util.CraftNamespacedKey;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public record ModifierUpgrade(double speed, double attack) implements WeaponUpgrade {
    @Override
    public void apply(RoyalSword sword, ItemStack item, ItemMeta meta) {

        // Thank you Bukkit, for not providing an API for this
        // Good lord, all this for some base attribute values
        // This was a pain to figure out btw
        Item type = BuiltInRegistries.ITEM
                .get(CraftNamespacedKey.toMinecraft(item.getType().getKey()));
        SwordItem swordItem = (SwordItem) type;

        double speedBase = -2.1D;

        applyModifier(meta, Attribute.GENERIC_ATTACK_DAMAGE, attack + swordItem.getDamage());
        applyModifier(meta, Attribute.GENERIC_ATTACK_SPEED, speedBase + speed);
    }

    void applyModifier(ItemMeta meta, Attribute attribute, double val) {
        meta.removeAttributeModifier(attribute);
        meta.addAttributeModifier(
                attribute,
                new AttributeModifier(
                        UUID.randomUUID(),
                        attribute.getKey().value(),
                        val,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlot.HAND
                )
        );
    }

    @Override
    public Component loreDisplay() {
        return Component.text("Increased attack damage")
                .append(speedGiven() ? Component.text(" and speed.") : Component.text("."));
    }

    boolean speedGiven() {
        return speed > 0;
    }

    @Override
    public Component[] statusDisplay() {
        Component[] result = new Component[speedGiven() ? 2 : 1];
        result[0] = status(Attribute.GENERIC_ATTACK_DAMAGE, attack);

        if(speedGiven()) result[1] = status(Attribute.GENERIC_ATTACK_SPEED, speed);

        return result;
    }

    Component status(Attribute attribute, double val) {
        return Component.text(" +" + String.format("%.1f", val) + " ")
                .append(Component.translatable(attribute));
    }
}