package net.forthecrown.commands.user;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.text.Text;
import net.forthecrown.user.property.Properties;
import net.forthecrown.user.property.TextProperty;
import net.kyori.adventure.text.Component;

class UserTabNode extends UserCommandNode {
    public UserTabNode() {
        super("user_tab", "tab");
    }

    @Override
    protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command, UserProvider provider) {
        command
                .then(propertyArgument(Properties.PREFIX, provider))
                .then(propertyArgument(Properties.SUFFIX, provider))
                .then(propertyArgument(Properties.TAB_NAME, provider));
    }

    private LiteralArgumentBuilder<CommandSource> propertyArgument(TextProperty property, UserProvider provider) {
        return literal(property.getKey())
                .executes(c -> {
                    var user = provider.get(c);
                    var text = user.get(property);

                    if (text == null || text == Component.empty()) {
                        throw Exceptions.format("{0, user} has no set {1}.", user, property.getKey());
                    }

                    c.getSource().sendMessage(
                            Text.format("{0, user}'s {1}: {2}",
                                    user, property.getKey(),
                                    text
                            )
                    );
                    return 0;
                })

                .then(argument("value", Arguments.CHAT)
                        .executes(c -> {
                            var user = provider.get(c);
                            Component value = c.getArgument("value", Component.class);

                            if (Text.isDashClear(value)) {
                                user.set(property, null);

                                c.getSource().sendAdmin(
                                        Text.format("Cleared {0, user}'s {1}", user, property.getKey())
                                );
                            } else {
                                user.set(property, value);

                                c.getSource().sendAdmin(
                                        Text.format("Set {0, user} {1} to {2}",
                                                user, property.getKey(),
                                                value
                                        )
                                );
                            }

                            return 0;
                        })
                );
    }
}