package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.types.ComponentArgument;
import net.kyori.adventure.text.Component;

public class ChatArgument implements ArgumentType<Component> {
    private static final ChatArgument INSTANCE = new ChatArgument();

    public static ChatArgument chat() {
        return INSTANCE;
    }

    @Override
    public Component parse(StringReader reader) throws CommandSyntaxException {
        char peek = reader.peek();

        if(peek == '{' || peek == '[' || peek == '"') {
            Component c = ComponentArgument.component().parse(reader);

            // If there's any leftover text
            if(reader.canRead()) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
                        .dispatcherExpectedArgumentSeparator()
                        .createWithContext(reader);
            }

            return c;
        }

        String all = reader.getRemaining();
        reader.setCursor(reader.getTotalLength());

        return FtcFormatter.formatString(all);
    }
}