package net.forthecrown.economy.shops;

import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;

public interface ShopComponent {
    String getSerialKey();

    @Nullable Tag save();
    void load(@Nullable Tag tag);
}