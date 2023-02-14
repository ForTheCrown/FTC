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
import net.forthecrown.commands.manager.Commands;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand.UsageFactory;
import net.forthecrown.commands.manager.Readers;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.grenadier.CmdUtil;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.useables.UsageInstance;
import net.forthecrown.useables.UsageType;
import net.forthecrown.useables.UsageTypeHolder;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriters;

@RequiredArgsConstructor
public class UsableArgumentNode<T extends UsageInstance, H extends UsageTypeHolder>
    extends CmdUtil
{

  private final UsageTypeAccessor<T, H> accessor;

  protected void addExtraUsages(UsageFactory factory, String holderName) {

  }

  public final void populateUsages(UsageFactory factory, String holderName) {
    String name = accessor.getName().toLowerCase();
    String plural = name + "s";

    factory = factory.withPrefix(plural);
    addExtraUsages(factory, holderName);

    factory.usage("")
        .addInfo("Lists all %s a %s has", plural, holderName);

    factory.usage("clear")
        .addInfo("Clears all %s in a %s", plural, holderName);

    factory.usage(String.format("add <%s type> [<%s value>]", name, name))
        .addInfo("Adds a <%s type> to a %s", name, holderName)
        .addInfo(
            "<%s value> allows you to specify data for the <%s type>",
            name, name
        )
        .addInfo("to use. The format of data required here, is determined")
        .addInfo("by the <%s type>", name);

    factory.usage(String.format("add -first <%s type> [<%s value>]", name, name))
        .addInfo(
            "Inserts a <%s type> to a %s's %s list's first place",
            name, holderName, name
        )
        .addInfo(
            "[<%s value>] allows you to specify data for the <%s type>",
            name, name
        )
        .addInfo("to use. The format of data required here, is determined")
        .addInfo("by the <%s type>", name);

    var remove = factory.withPrefix("remove");
    remove.usage("<index: number(1..)> [-at <index: number(1..)>]")
        .addInfo(
            "Removes a %s value from a %s at an <index>",
            name, holderName
        );

    remove.usage("-between <start index> <end index>")
        .addInfo("Removes all %s values between the <start index>", name)
        .addInfo("and <end index>");
  }

  public LiteralArgumentBuilder<CommandSource> createArguments(
      UsageHolderProvider<? extends H> provider,
      UsableSaveCallback<? extends H> saveCallback
  ) {
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
            .then(addArg(provider, false, saveCallback))

            // Insert at beginning
            .then(literal("-first")
                .then(addArg(provider, true, saveCallback))
            )
        )

        .then(literal("remove")
            .then(removeIndexArg(provider, saveCallback))

            .then(literal("-at")
                .then(removeIndexArg(provider, saveCallback))
            )

            .then(literal("-between")
                .then(argument("start_index", IntegerArgumentType.integer(1))
                    .suggests(suggestListIndexes(provider))

                    .then(argument("end_index", IntegerArgumentType.integer(1))
                        .suggests(suggestListIndexes(provider))

                        .executes(context -> remove(context, provider, saveCallback, (list, c) -> {
                          var start = c.getArgument("start_index", Integer.class);
                          var end = c.getArgument("end_index", Integer.class);

                          if (start > end) {
                            throw Exceptions.invalidBounds(start, end);
                          }

                          Commands.ensureIndexValid(start, list.size());
                          Commands.ensureIndexValid(end, list.size());

                          list.subList(start - 1, end).clear();
                        }))
                    )
                )
            )

            .then(literal("-with_type")
                .then(argument("remove_type", accessor.getArgumentType())
                    .executes(context -> remove(context, provider, saveCallback, (list, c) -> {

                      @SuppressWarnings("unchecked")
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

              saveCallback.dumbHack(obj);

              c.getSource().sendAdmin(
                  Text.format("Cleared {0}s", accessor.getName())
              );
              return 0;
            })
        );

    addExtraArguments(result, provider, saveCallback);
    return result;
  }

  protected void addExtraArguments(LiteralArgumentBuilder<CommandSource> command,
                                   UsageHolderProvider<? extends H> provider,
                                   UsableSaveCallback<? extends H> saveCallback
  ) {

  }

  // --- ADDITION ARGUMENTS ---

  private RequiredArgumentBuilder<CommandSource, ?> addArg(
      UsageHolderProvider<? extends H> provider,
      boolean first,
      UsableSaveCallback<? extends H> saveCallback
  ) {
    return argument("add_type", accessor.getArgumentType())
        .executes(c -> add(c, provider, saveCallback, Commands.EMPTY_READER, first))

        .then(argument("type_input", StringArgumentType.greedyString())
            .suggests((context, builder) -> {
              var parsed = getParsedType(context);
              return parsed.getSuggests().getSuggestions(context, builder);
            })

            .executes(c -> {
              var input = c.getArgument("type_input", String.class);

              return add(c, provider, saveCallback, new StringReader(input), first);
            })
        );
  }

  private UsageType<T> getParsedType(CommandContext<CommandSource> c) {
    @SuppressWarnings("unchecked")
    Holder<UsageType<T>> holder = c.getArgument("add_type", Holder.class);
    return holder.getValue();
  }

  private int add(CommandContext<CommandSource> c,
                  UsageHolderProvider<? extends H> provider,
                  UsableSaveCallback<? extends H> saveCallback,
                  StringReader reader,
                  boolean first
  )
      throws CommandSyntaxException {
    var holder = provider.get(c);
    var list = accessor.getList(holder);

    @SuppressWarnings("unchecked")
    Holder<UsageType<T>> parseHolder = c.getArgument("add_type", Holder.class);
    var parsed = parseHolder.getValue();
    boolean canRead = reader.canRead();

    if (!canRead && parsed.requiresInput()) {
      throw Exceptions.format("Type {0} requires input", parseHolder.getKey());
    }

    T instance = parsed.parse(reader, c.getSource());
    Readers.ensureCannotRead(reader);

    if (first) {
      list.add(0, instance);
    } else {
      list.add(instance);
    }

    saveCallback.dumbHack(holder);

    c.getSource().sendAdmin(
        Text.format(
            "Added {0}: {1}: {2}",
            accessor.getName(),
            parseHolder.getKey(),
            instance.displayInfo()
        )
    );
    return 0;
  }

  // --- REMOVAL ARGUMENTS ---

  private RequiredArgumentBuilder<CommandSource, ?> removeIndexArg(
      UsageHolderProvider<? extends H> provider,
      UsableSaveCallback<? extends H> saveCallback
  ) {
    return argument("remove_index", IntegerArgumentType.integer(1))
        .suggests(suggestListIndexes(provider))

        .executes(c -> remove(c, provider, saveCallback, (list, c1) -> {
          var index = c1.getArgument("remove_index", Integer.class);

          if (index > list.size()) {
            throw Exceptions.invalidIndex(index, list.size());
          }

          list.remove(index - 1);
        }));
  }

  private SuggestionProvider<CommandSource> suggestListIndexes(
      UsageHolderProvider<? extends H> provider
  ) {
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
                     UsableSaveCallback<? extends H> saveCallback,
                     UsageListRemover<T> remover
  ) throws CommandSyntaxException {
    var holder = provider.get(c);
    var list = accessor.getList(holder);

    remover.remove(list, c);
    saveCallback.dumbHack(holder);

    c.getSource().sendAdmin(Text.format("Removed {0}", accessor.getName()));
    return 0;
  }
}