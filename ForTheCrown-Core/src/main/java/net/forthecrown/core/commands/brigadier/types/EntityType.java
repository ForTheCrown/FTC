package net.forthecrown.core.commands.brigadier.types;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.v1_16_R3.ArgumentEntity;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntityType {

    public static ArgumentEntity enitity(){
        return ArgumentEntity.a();
    }

    public static ArgumentEntity entities(){
        return ArgumentEntity.multipleEntities();
    }

    public static ArgumentEntity player(){
        return ArgumentEntity.c();
    }

    public static ArgumentEntity players(){
        return ArgumentEntity.d();
    }

    public static Player getPlayer(CommandContext<CommandListenerWrapper> c, String argument) throws CommandSyntaxException {
        return ArgumentEntity.e(c, argument).getBukkitEntity();
    }

    public static Entity getEntity(CommandContext<CommandListenerWrapper> c, String argument) throws CommandSyntaxException {
        net.minecraft.server.v1_16_R3.Entity e = ArgumentEntity.a(c, argument);
        return e.getBukkitEntity();
    }

    public static Collection<Player> getPlayers(CommandContext<CommandListenerWrapper> c, String argument) throws CommandSyntaxException {
        Collection<EntityPlayer> convert = ArgumentEntity.f(c, argument);
        List<Player> result = new ArrayList<>();
        for (EntityPlayer p: convert){
            result.add(p.getBukkitEntity());
        }
        return result;
    }

    public static Collection<? extends Entity> getEntities(CommandContext<CommandListenerWrapper> c, String argument) throws CommandSyntaxException {
        Collection<? extends net.minecraft.server.v1_16_R3.Entity> convert = ArgumentEntity.b(c, argument);
        Collection<Entity> result = new ArrayList<>();
        for (net.minecraft.server.v1_16_R3.Entity e: convert){
            result.add(e.getBukkitEntity());
        }
        return result;
    }
}
