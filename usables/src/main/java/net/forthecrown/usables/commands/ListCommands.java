package net.forthecrown.usables.commands;

import static net.forthecrown.grenadier.Nodes.argument;
import static net.forthecrown.grenadier.Nodes.literal;
import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import net.forthecrown.command.Commands;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.RegistryArguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandContexts;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.IntRangeArgument.IntRange;
import net.forthecrown.registry.Holder;
import net.forthecrown.registry.Registry;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextWriters;
import net.forthecrown.usables.ComponentList;
import net.forthecrown.usables.UsableComponent;
import net.forthecrown.usables.UsageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;

public class ListCommands<T extends UsableComponent> {

  private final Registry<UsageType<T>> registry;
  private final RegistryArguments<UsageType<T>> argument;

  private final String displayName;
  private final String argumentName;

  public ListCommands(
      String argumentName,
      String displayName,
      Registry<UsageType<? extends T>> registry
  ) {
    this.argumentName = argumentName;
    this.displayName = displayName;
    this.registry = (Registry) registry;

    this.argument = new RegistryArguments<>(this.registry, displayName);
  }

  public void createUsages(UsageFactory factory) {
    createUsagesNoPrefix(factory.withPrefix(argumentName));
  }

  public void createUsagesNoPrefix(UsageFactory factory) {
    factory.usage("").addInfo("Shows all the existing %ss", displayName);
    factory.usage("clear").addInfo("Clears the %s list", displayName);

    factory.usage("remove <indices>")
        .addInfo("Removes a %s from the %s list", displayName, displayName)
        .addInfo("Examples:")
        .addInfo("- remove 1: Removes an element at index 1")
        .addInfo("- remove 1..3: Removes all elements between indices 1 through 3");

    factory.usage("add <type> [<input>]")
        .addInfo("Adds a %s", displayName);

    factory.usage("add -first <type> [<input>]")
        .addInfo("Adds a %s to the front of the %ss list", displayName, displayName);
  }

  public LiteralArgumentBuilder<CommandSource> create(ListAccess<T> access) {
    var literal = literal(argumentName);
    addArguments(access, literal);
    return literal;
  }

  private Component show(ListHolder<T> holder) {
    ComponentList<T> list = holder.getList();

    var writer = TextWriters.newWriter();
    writer.setFieldStyle(Style.style(NamedTextColor.GRAY));

    var name = holder.object().displayName();

    writer.field(name.append(text(" " + displayName + "s")));
    list.write(writer, holder.object().getCommandPrefix() + " " + argumentName);

    return writer.asComponent();
  }

  public void addArguments(
      ListAccess<T> access,
      ArgumentBuilder<CommandSource, ? extends ArgumentBuilder> builder
  ) {
    builder.executes(context -> {
      ListHolder<T> holder = access.getHolder(context);
      context.getSource().sendMessage(show(holder));
      return 0;
    });

    builder.then(literal("add")
        .then(literal("-first").then(addArgument(access, true)))
        .then(addArgument(access, false))
    );

    builder.then(literal("remove").then(argument("range", ArgumentTypes.intRange())
        .suggests((context, sBuilder) -> {
          var holder = access.getHolder(context);
          var list = holder.getList();

          if (list.isEmpty()) {
            return Suggestions.empty();
          }

          for (int i = 1; i <= list.size(); i++) {
            sBuilder.suggest(i);
          }

          return sBuilder.buildFuture();
        })

        .executes(c -> {
          var holder = access.getHolder(c);
          var list = holder.getList();

          if (list.isEmpty()) {
            throw Exceptions.format("No {0}s to remove", displayName);
          }

          IntRange range = c.getArgument("range", IntRange.class);

          int minIndex = range.min().orElse(1);
          int maxIndex = range.max().orElse(list.size());

          Commands.ensureIndexValid(minIndex, list.size());
          Commands.ensureIndexValid(maxIndex, list.size());

          Component message;

          if (minIndex == maxIndex) {
            list.remove(minIndex - 1);

            message = Text.format("Removed {0} at index &f{1, number}&r.",
                NamedTextColor.GRAY,
                displayName, minIndex
            );
          } else {
            list.removeBetween(minIndex - 1, maxIndex);

            message = Text.format(
                "Removed all {0}s between &f{1, number}&r and &f{2, number}&r.",
                NamedTextColor.GRAY,
                displayName, minIndex, maxIndex
            );
          }

          holder.postEdit();

          c.getSource().sendSuccess(message);
          c.getSource().sendMessage(show(holder));

          return 0;
        })
    ));

    builder.then(literal("set").then(argument("index", IntegerArgumentType.integer(1))
        .then(argument("type", argument)
            .executes(c -> {
              StringReader reader = new StringReader(c.getInput());
              reader.setCursor(reader.getTotalLength());

              doSet(c, access, reader);
              return 0;
            })

            .then(argument("input", StringArgumentType.greedyString())
                .suggests((context, builder1) -> {
                  Holder<UsageType<T>> holder = context.getArgument("type", Holder.class);
                  return holder.getValue().getSuggestions(context, builder1);
                })

                .executes(c -> {
                  StringRange range = CommandContexts.getNodeRange(c, "input");
                  StringReader reader = new StringReader(c.getInput());
                  reader.setCursor(range.getStart());

                  doSet(c, access, reader);
                  return 0;
                })
            )
        )
    ));

    builder.then(literal("clear").executes(c -> {
      var holder = access.getHolder(c);
      var list = holder.getList();

      if (list.isEmpty()) {
        throw Exceptions.create(displayName + " list already empty");
      }

      list.clear();
      holder.postEdit();

      c.getSource().sendSuccess(
          Text.format("Cleared {0}s list for &e{1}&r.",
              NamedTextColor.GRAY,
              displayName,
              holder.object().displayName()
          )
      );
      return 0;
    }));
  }

  private RequiredArgumentBuilder<CommandSource, ?> addArgument(
      ListAccess<T> access,
      boolean first
  ) {
    return argument("type", argument)

        .suggests((context, builder) -> {
          var entries = registry.entries();
          var holder = access.getHolder(context);

          return Completions.suggest(builder,
              entries.stream()
                  .filter(typeHolder -> {
                    return typeHolder.getValue().canApplyTo(holder.object());
                  })

                  .map(Holder::getKey)
          );
        })

        .executes(c -> {
          StringReader reader = new StringReader(c.getInput());
          reader.setCursor(reader.getTotalLength());

          doAdd(c, access, first, reader);
          return 0;
        })

        .then(argument("input", StringArgumentType.greedyString())
            .suggests((context, builder1) -> {
              Holder<UsageType<T>> holder = context.getArgument("type", Holder.class);
              return holder.getValue().getSuggestions(context, builder1);
            })

            .executes(c -> {
              StringRange range = CommandContexts.getNodeRange(c, "input");
              StringReader reader = new StringReader(c.getInput());
              reader.setCursor(range.getStart());

              doAdd(c, access, first, reader);
              return 0;
            })
        );
  }

  private void doSet(
      CommandContext<CommandSource> c,
      ListAccess<T> access,
      StringReader reader
  ) throws CommandSyntaxException {
    ListHolder<T> listHolder = access.getHolder(c);
    ComponentList<T> list = listHolder.getList();

    Holder<UsageType<T>> holder = c.getArgument("type", Holder.class);
    UsageType<T> type = holder.getValue();

    validateApplicable(holder, listHolder);

    T instance = type.parse(reader, c.getSource());

    if (reader.canRead()) {
      Commands.ensureCannotRead(reader);
    }

    int index = c.getArgument("index", Integer.class);
    Commands.ensureIndexValid(index, list.size());

    list.set(index - 1, instance);
    listHolder.postEdit();

    c.getSource().sendSuccess(Text.format("Set {0} at index &e{1, number}&r: &6{2}&r.",
        NamedTextColor.GRAY,
        displayName, index, holder.getKey()
    ));
  }

  private void validateApplicable(Holder<UsageType<T>> holder, ListHolder<T> listHolder)
      throws CommandSyntaxException
  {
    if (!holder.getValue().canApplyTo(listHolder.object())) {
      throw Exceptions.format("Type {0} cannot be applied to {1}",
          holder.getKey(),
          listHolder.object().displayName()
      );
    }
  }

  private void doAdd(
      CommandContext<CommandSource> c,
      ListAccess<T> access,
      boolean first,
      StringReader reader
  ) throws CommandSyntaxException {
    ListHolder<T> listHolder = access.getHolder(c);
    ComponentList<T> list = listHolder.getList();

    Holder<UsageType<T>> holder = c.getArgument("type", Holder.class);
    UsageType<T> type = holder.getValue();

    validateApplicable(holder, listHolder);

    T instance = type.parse(reader, c.getSource());

    if (reader.canRead()) {
      Commands.ensureCannotRead(reader);
    }

    if (first) {
      list.addFirst(instance);
    } else {
      list.addLast(instance);
    }

    listHolder.postEdit();

    c.getSource().sendSuccess(Text.format("Added {0}: {1}", displayName, holder.getKey()));
  }
}
