package net.forthecrown.core.nbt;

import net.minecraft.server.v1_16_R3.NBTBase;
import net.minecraft.server.v1_16_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagList;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftInventoryCustom;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.*;

public class NbtHandler {
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

    public static Inventory toInventory(NBT nbt, String name){
        CraftInventoryCustom custom = new CraftInventoryCustom(null, 54);
        NBTTagList list = nbt.getAsList(name);

        for (NBTBase base: list){
            NBTTagCompound compound = (NBTTagCompound) base;

            ItemStack item = CraftItemStack.asBukkitCopy(net.minecraft.server.v1_16_R3.ItemStack.a(compound));
            byte slot = compound.getByte("Slot");

            if(slot == -106) slot = 49;
            else if(slot > 100){
                slot -= 100;
                slot = (byte) (45 + slot);
            }

            custom.setItem(slot, item);
        }

        return custom;
    }

    public static Inventory offlinePlayerInv(OfflinePlayer player) throws IOException {
        return toInventory(ofOfflinePlayer(player), "Inventory");
    }

    public static NBT ofOfflinePlayer(OfflinePlayer player) throws IOException {
        File file = new File("world" + File.separator + "playerdata" + File.separator + player.getUniqueId().toString() + "dat");

        return read(new FileInputStream(file));
    }

    public static void serializeOfflinePlayer(OfflinePlayer player, NBT tag) throws IOException {
        File plrFile = new File("world" + File.separator + "playerdata" + File.separator + player.getUniqueId().toString() + "dat");
        FileOutputStream outputStream = new FileOutputStream(plrFile);

        write(outputStream, tag);
        outputStream.close();
    }

    public static void write(OutputStream writer, NBT nbt) throws IOException {
        NBTCompressedStreamTools.writeNBT(nbt.tag, writer);
    }

    public static NBT read(InputStream stream) throws IOException {
        return NBT.of(NBTCompressedStreamTools.readNBT(stream));
    }
}
