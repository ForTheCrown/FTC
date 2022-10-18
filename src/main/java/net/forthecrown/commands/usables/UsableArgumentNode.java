package net.forthecrown.commands.usables;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import lombok.RequiredArgsConstructor;
import net.forthecrown.commands.manager.CmdValidate;
import net.forthecrown.commands.manager.Commands;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Crown;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.grenadier.CmdUtil;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.text.Text;
import net.forthecrown.text.writer.TextWriters;
import net.forthecrown.useables.UsageInstance;
import net.forthecrown.useables.UsageType;
import net.forthecrown.useables.UsageTypeHolder;

@RequiredArgsConstructor
public class UsableArgumentNode<T extends UsageInstance, H extends UsageTypeHolder> extends CmdUtil {
    private final UsageTypeAccessor<T, H> accessor;

    public LiteralArgumentBuilder<CommandSource> createArguments(UsageHolderProvider<? extends H> provider) {
        var result = literal(accessor.getName().toLowerCase() + "s")
                // View
                .executes(c -> {
                    var obj = provider.get(c);
                    var list = accessor.getList(obj);

                    var writer = TextWriters.newWriter();

                    writer.write(accessor.getName() + "s: ");
                    list.write(writer);

                    c.getSource().sendMessage(writer);
                    return 0;
                })

                // Add check/action
                .then(literal("add")
                        // Unless specified, add it last
                        .then(addArg(provider, false))

                        // Insert at beginning
                        .then(literal("-first")
                                .then(addArg(provider, true))
                        )
                )

                .then(literal("remove")
                        .then(removeIndexArg(provider))

                        .then(literal("-at")
                                .then(removeIndexArg(provider))
                        )

                        .then(literal("-between")
                                .then(argument("start_index", IntegerArgumentType.integer(1))
                                        .suggests(suggestListIndexes(provider))

                                        .then(argument("end_index", IntegerArgumentType.integer(1))
                                                .suggests(suggestListIndexes(provider))

                                                .executes(context -> remove(context, provider, (list, c) -> {
                                                    var start = c.getArgument("start_index", Integer.class);
                                                    var end = c.getArgument("end_index", Integer.class);

                                                    if (start > end) {
                                                        throw Exceptions.invalidBounds(start, end);
                                                    }

                                                    CmdValidate.index(start, list.size());
                                                    CmdValidate.index(end, list.size());

                                                    list.subList(start - 1, end).clear();
                                                }))
                                        )
                                )
                        )

                        .then(literal("-with_type")
                                .then(argument("remove_type", accessor.getArgumentType())
                                        .executes(context -> remove(context, provider, (list, c) -> {
                                            Holder<UsageType<T>> holder = c.getArgument("remove_type", Holder.class);
                                            UsageType<T> type = holder.getValue();

                                            if (!list.removeType(type)) {
                                                throw Exceptions.REMOVED_NO_DATA;
                                            }
                                        }))
                                )
                        )
                )

                .then(literal("clear")
                        .executes(c -> {
                            var obj = provider.get(c);
                            accessor.getList(obj).clear();

                            c.getSource().sendAdmin(
                                    Text.format("Cleared {0}s", accessor.getName())
                            );
                            return 0;
                        })
                );

        addExtraArguments(result, provider);
        return result;
    }

    protected void addExtraArguments(LiteralArgumentBuilder<CommandSource> command,
                                     UsageHolderProvider<? extends H> provider
    ) {

    }

    // --- ADDITION ARGUMENTS ---

    private RequiredArgumentBuilder<CommandSource, ?> addArg(UsageHolderProvider<? extends H> provider, boolean first) {
        return argument("add_type", accessor.getArgumentType())
                .executes(c -> add(c, provider, Commands.EMPTY_READER, first))

                .then(argument("type_input", StringArgumentType.greedyString())
                        .suggests((context, builder) -> {
                            Crown.logger().info("input='{}'", context.getInput());

                            var parsed = getParsedType(context);
                            return parsed.getSuggests().getSuggestions(context, builder);
                        })

                        .executes(c -> {
                            var input = c.getArgument("type_input", String.class);

                            return add(c, provider, new StringReader(input), first);
                        })
                );
    }

    private UsageType<T> getParsedType(CommandContext<CommandSource> c) {
        Holder<UsageType<T>> holder = c.getArgument("add_type", Holder.class);
        return holder.getValue();
    }

    private int add(CommandContext<CommandSource> c, UsageHolderProvider<? extends H> provider, StringReader reader, boolean first)
            throws CommandSyntaxException
    {
        var holder = provider.get(c);
        var list = accessor.getList(holder);

        var parsed = getParsedType(c);
        var instance = parsed.parse(reader, c.getSource());

        if (reader.canRead()) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
                    .dispatcherExpectedArgumentSeparator()
                    .createWithContext(reader);
        }

        if (first) {
            list.add(0, instance);
        } else {
            list.add(instance);
        }

        c.getSource().sendAdmin(
                Text.format(
                        "Added {0}: {1}: {2}",
                        accessor.getName(),
                        list.getRegistry().getKey(parsed).orElse("UNKNOWN"),
                        instance.displayInfo()
                )
        );
        return 0;
    }

    // --- REMOVAL ARGUMENTS ---

    private RequiredArgumentBuilder<CommandSource, ?> removeIndexArg(UsageHolderProvider<? extends H> provider) {
        return argument("remove_index", IntegerArgumentType.integer(1))
                .suggests(suggestListIndexes(provider))

                .executes(c -> remove(c, provider, (list, c1) -> {
                    var index = c1.getArgument("remove_index", Integer.class);

                    if (index > list.size()) {
                        throw Exceptions.invalidIndex(index, list.size());
                    }

                    list.remove(index - 1);
                }));
    }

    private SuggestionProvider<CommandSource> suggestListIndexes(UsageHolderProvider<? extends H> provider) {
        return (context, builder) -> {
            var holder = provider.get(context);
            var list = accessor.getList(holder);

            if (list.isEmpty()) {
                return Suggestions.empty();
            }

            var token = builder.getRemainingLowerCase();
            for (int i = 1; i <= list.size(); i++) {
                var suggestion = i + "";

                if (CompletionProvider.startsWith(token, suggestion)) {
                    builder.suggest(suggestion, toTooltip(list.listInfo(i - 1)));
                }
            }

            return builder.buildFuture();
        };
    }

    private int remove(CommandContext<CommandSource> c,
                       UsageHolderProvider<? extends H> provider,
                       UsageListRemover<T> remover
    ) throws CommandSyntaxException {
        var holder = provider.get(c);
        var list = accessor.getList(holder);

        remover.remove(list, c);

        c.getSource().sendAdmin(Text.format("Removed {0}", accessor.getName()));
        return 0;
    }
}