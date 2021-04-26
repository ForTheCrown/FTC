package net.forthecrown.core.commands.brigadier.types;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.utils.ListUtils;
import net.minecraft.server.v1_16_R3.ArgumentEntity;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;

public class TargetSelectorType {

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
        return ListUtils.convert(ArgumentEntity.f(c, argument), EntityPlayer::getBukkitEntity);
    }

    public static Collection<CrownUser> getUsers(CommandContext<CommandListenerWrapper> c, String argument) throws CommandSyntaxException {
        return ListUtils.convert(getPlayers(c, argument), UserManager::getUser);
    }

    public static Collection<? extends Entity> getEntities(CommandContext<CommandListenerWrapper> c, String argument) throws CommandSyntaxException {
        return ListUtils.convert(ArgumentEntity.b(c, argument), net.minecraft.server.v1_16_R3.Entity::getBukkitEntity);
    }
}
