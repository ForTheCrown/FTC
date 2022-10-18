package net.forthecrown.commands.manager;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.kyori.adventure.text.Component;

public class OpenExceptionType implements CommandExceptionType {
    public static final OpenExceptionType INSTANCE = new OpenExceptionType();

    public RoyalCommandException create(Component text) {
        return new RoyalCommandException(this, text);
    }

    public RoyalCommandException createWithContext(Component text, ImmutableStringReader reader) {
        return new RoyalCommandException(this, text, reader.getString(), reader.getCursor());
    }
}
