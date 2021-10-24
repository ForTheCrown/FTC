package net.forthecrown.inventory.weapon;

import net.forthecrown.inventory.weapon.goals.WeaponGoal;
import net.forthecrown.serializer.NbtSerializable;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class RoyalWeapon implements NbtSerializable {
    private final UUID owner;
    private final ItemStack item;

    private WeaponGoal currentGoal;
    private int progress;

    public UUID getOwner() {
        return owner;
    }

    public ItemStack getItem() {
        return item;
    }

    public WeaponGoal getCurrentGoal() {
        return currentGoal;
    }

    @Override
    public Tag saveAsTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("goal", currentGoal.key().asString());
        tag.putUUID("owner", owner);

        return tag;
    }
}
