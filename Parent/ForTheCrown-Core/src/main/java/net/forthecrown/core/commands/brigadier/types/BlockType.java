package net.forthecrown.core.commands.brigadier.types;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.v1_16_R3.ArgumentTile;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import net.minecraft.server.v1_16_R3.IBlockData;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public class BlockType {

    public static ArgumentTile material(){
        return ArgumentTile.a();
    }

    public static Material getMaterial(CommandContext<CommandListenerWrapper> c, String argument){
        return getData(c, argument).getBukkitMaterial();
    }

    public static IBlockData getData(CommandContext<CommandListenerWrapper> c, String argument){
        return ArgumentTile.a(c, argument).a();
    }

    public static BlockData getBlockData(CommandContext<CommandListenerWrapper> c, String argument){
        return getData(c, argument).createCraftBlockData();
    }

}
