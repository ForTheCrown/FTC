package net.forthecrown.core.nbt;

import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class NbtGetter {
    public static NBT ofItem(ItemStack item){
        net.minecraft.server.v1_16_R3.ItemStack nms = nmsItem(item);
        return NBT.of(nms.save(new NBTTagCompound()));
    }

    public static NBT ofItemTags(ItemStack item){
        net.minecraft.server.v1_16_R3.ItemStack nms = nmsItem(item);
        return NBT.of(nms.hasTag() ? nms.getTag() : new NBTTagCompound());
    }

    public static ItemStack applyTags(ItemStack item, NBT nbt){
        net.minecraft.server.v1_16_R3.ItemStack nms = nmsItem(item);
        nms.tag = nbt.tag;
        return nms.asBukkitMirror();
    }

    private static net.minecraft.server.v1_16_R3.ItemStack nmsItem(ItemStack item){
        return CraftItemStack.asNMSCopy(item);
    }

    public static ItemStack itemFromNBT(NBT nbt){
        return net.minecraft.server.v1_16_R3.ItemStack.a(nbt.tag).asBukkitMirror();
    }

    public static NBT of(Entity entity){
        net.minecraft.server.v1_16_R3.Entity nms = nmsEnt(entity);
        return NBT.of(nms.save(new NBTTagCompound()));
    }

    private static net.minecraft.server.v1_16_R3.Entity nmsEnt(Entity entity){
        return ((CraftEntity )entity).getHandle();
    }
}
