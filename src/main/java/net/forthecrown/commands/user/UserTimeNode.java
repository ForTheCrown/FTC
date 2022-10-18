package net.forthecrown.commands.user;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.text.Text;
import net.forthecrown.text.writer.TextWriters;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.User;
import net.forthecrown.user.data.TimeField;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;

class UserTimeNode extends UserCommandNode {
    public UserTimeNode() {
        super("user_timestamps", "timestamps");
    }

    @Override
    protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command,
                                                                        UserProvider provider
    ) {
        command
                .executes(c -> {
                    var user = provider.get(c);
                    var writer = TextWriters.newWriter();
                    writer.setFieldStyle(Style.style(NamedTextColor.GRAY));
                    writer.setFieldSeparator(Component.text(": ", NamedTextColor.DARK_GRAY));

                    writer.formatted("{0, user}'s time stamps:", user);

                    var time = user.getTimeTracker();

                    for (var field : TimeField.values()) {
                        if (time.isSet(field)) {
                            writer.field(
                                    field.getKey(),
                                    Text.format("{0, date}", time.get(field))
                            );
                        } else {
                            writer.field(field.getKey(), "Unset");
                        }
                    }

                    c.getSource().sendMessage(writer);
                    return 0;
                })

                .then(literal("set")
                        .then(argument("field", RegistryArguments.TIME_FIELDS)
                                .then(literal("-now")
                                        .executes(c -> setTime(c, provider, true))
                                )

                                .then(argument("value", LongArgumentType.longArg(1))
                                        .executes(c -> setTime(c, provider, false))
                                )
                        )
                )

                .then(literal("unset")
                        .then(argument("field", RegistryArguments.TIME_FIELDS)
                                .executes(c -> {
                                    User user = provider.get(c);
                                    Holder<TimeField> holder = c.getArgument("field", Holder.class);
                                    var field = holder.getValue();

                                    user.getTimeTracker().remove(field);

                                    c.getSource().sendMessage(
                                            Text.format("Unset time field '{0}' for user {1, user}",
                                                    field.getKey(), user
                                            )
                                    );
                                    return 0;
                                })
                        )
                );
    }

    private int setTime(CommandContext<CommandSource> c,
                        UserProvider provider,
                        boolean current
    ) throws CommandSyntaxException {
        var user = provider.get(c);

        Holder<TimeField> field = c.getArgument("field", Holder.class);
        long value;

        if (current) {
            value = System.currentTimeMillis();
        } else {
            value = c.getArgument("value", Long.class);
        }

        user.setTime(field.getValue(), value);

        c.getSource().sendAdmin(
                Text.format("Set field '{0}' to {1} for {2, user}",
                        field.getKey(), value, user
                )
        );
        return 0;
    }
}