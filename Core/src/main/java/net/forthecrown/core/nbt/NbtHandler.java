package net.forthecrown.core.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NbtHandler {
    public static NBT ofItem(ItemStack item){
        net.minecraft.world.item.ItemStack nms = nmsItem(item);
        return NBT.of(nms.save(new CompoundTag()));
    }

    public static NBT ofItemTags(ItemStack item){
        net.minecraft.world.item.ItemStack nms = nmsItem(item);
        return NBT.of(nms.getOrCreateTag());
    }

    public static ItemStack applyTags(ItemStack item, NBT nbt){
        net.minecraft.world.item.ItemStack nms = nmsItem(item);
        nms.tag = nbt.tag;
        return nms.asBukkitMirror();
    }

    private static net.minecraft.world.item.ItemStack nmsItem(ItemStack item){
        return CraftItemStack.asNMSCopy(item);
    }

    public static ItemStack itemFromNBT(NBT nbt){
        return net.minecraft.world.item.ItemStack.of(nbt.tag).asBukkitMirror();
    }

    public static NBT of(Entity entity){
        net.minecraft.world.entity.Entity nms = nmsEnt(entity);

        CompoundTag tag = new CompoundTag();
        nms.save(tag);

        return NBT.of(tag);
    }

    private static net.minecraft.world.entity.Entity nmsEnt(Entity entity){
        return ((CraftEntity)entity).getHandle();
    }

    public static void write(OutputStream writer, NBT nbt) throws IOException {
        NbtIo.writeCompressed(nbt.tag, writer);
    }

    public static NBT read(InputStream stream) throws IOException {
        return NBT.of(NbtIo.readCompressed(stream));
    }
}
