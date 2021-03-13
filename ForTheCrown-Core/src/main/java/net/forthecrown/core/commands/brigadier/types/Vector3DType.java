package net.forthecrown.core.commands.brigadier.types;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.v1_16_R3.ArgumentVec3;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import net.minecraft.server.v1_16_R3.IVectorPosition;
import net.minecraft.server.v1_16_R3.Vec3D;
import org.bukkit.Location;
import org.bukkit.World;

public class Vector3DType {

    public static ArgumentVec3 vec3(){
        return ArgumentVec3.a();
    }

    public static ArgumentVec3 vec3(boolean centerIntegers){
        return ArgumentVec3.a(centerIntegers);
    }

    public static Location getLocation(CommandContext<CommandListenerWrapper> c, String argument, World world) throws CommandSyntaxException {
        Vec3D vec = ArgumentVec3.a(c, argument);
        return new Location(world, vec.x, vec.y, vec.z);
    }

    public static IVectorPosition getPosArgument(CommandContext<CommandListenerWrapper> c, String argument){
        return ArgumentVec3.b(c, argument);
    }
}
