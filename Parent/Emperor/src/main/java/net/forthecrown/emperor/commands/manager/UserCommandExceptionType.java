package net.forthecrown.emperor.commands.manager;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;

import java.util.function.Function;

public class UserCommandExceptionType implements CommandExceptionType {
    private final Function<CrownUser, Component> function;

    public UserCommandExceptionType(Function<CrownUser, Component> function){
        this.function = function;
    }

    public CommandSyntaxException create(CrownUser user){
        return new CommandSyntaxException(this, getNMS(user));
    }

    public CommandSyntaxException createWithContext(StringReader reader, CrownUser user){
        return new CommandSyntaxException(this, getNMS(user), reader.getString(), reader.getCursor());
    }

    public IChatBaseComponent getNMS(CrownUser user){
        return ChatUtils.adventureToVanilla(function.apply(user));
    }
}
