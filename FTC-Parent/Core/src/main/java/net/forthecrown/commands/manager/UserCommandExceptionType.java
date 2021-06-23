package net.forthecrown.commands.manager;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.kyori.adventure.text.Component;

import java.util.function.Function;

public class UserCommandExceptionType implements CommandExceptionType {
    private final Function<CrownUser, Component> function;

    public UserCommandExceptionType(Function<CrownUser, Component> function){
        this.function = function;
    }

    public RoyalCommandException create(CrownUser user){
        return new RoyalCommandException(this, function.apply(user));
    }

    public RoyalCommandException createWithContext(StringReader reader, CrownUser user){
        return new RoyalCommandException(this, function.apply(user), reader.getString(), reader.getCursor());
    }
}
