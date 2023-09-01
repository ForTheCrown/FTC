package net.forthecrown.core.commands.item;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.ArrayList;
import net.forthecrown.command.Commands;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.core.CoreExceptions;
import net.forthecrown.core.CoreMessages;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextWriters;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.Range;

public class ItemLoreNode extends ItemModifierNode {

  public ItemLoreNode() {
    super(
        "lore",
        "itemlore", "lores", "itemlores"
    );
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("clear")
        .addInfo("Clears your held item's lore");

    ItemNameNode.namingNote(
        factory.usage("add <text>")
            .addInfo("Adds <text> to your held item's lore")
    );

    factory.usage("display")
        .addInfo("Displays the lore of the item you're holding with index numbers");

    factory.usage("set <index> <text>")
        .addInfo("Sets the lore on the specified line of the item you're holding");

    factory.usage("remove <index>")
        .addInfo("Removes the lore on the given line");

    factory.usage("remove at <index>")
        .addInfo("Removes the lore on the given line");

    factory.usage("remove between <start index> <end index>")
        .addInfo("Removes all lore between the 2 lines");
  }

  @Override
  public void create(LiteralArgumentBuilder<CommandSource> command) {
    command
        .then(literal("display")
            .executes(c -> {
              var held = getHeld(c.getSource());
              var lore = held.lore();

              if (lore == null || lore.isEmpty()) {
                throw Exceptions.NOTHING_TO_LIST;
              }

              var writer = TextWriters.newWriter();
              writer.setStyle(Style.style(NamedTextColor.DARK_PURPLE, TextDecoration.ITALIC));

              for (int i = 0; i < lore.size(); i++) {
                var line = lore.get(i);
                int viewerIndex = i + 1;

                writer.formattedLine("&7{0, number})&r {1}", viewerIndex, line);
              }

              c.getSource().sendMessage(writer.asComponent());
              return 0;
            })
        )

        .then(literal("clear")
            .executes(c -> {
              var held = getHeld(c.getSource());
              held.lore(null);

              c.getSource().sendSuccess(CoreMessages.CLEARED_LORE);
              return 0;
            })
        )

        .then(literal("add")
            .then(argument("text", Arguments.CHAT)
                .executes(c -> {
                  var held = getHeld(c.getSource());
                  var lore = held.lore();

                  if (lore == null) {
                    lore = new ArrayList<>();
                  }

                  var message = Arguments.getMessage(c, "text").asComponent();

                  lore.add(optionallyWrap(message, c, "text"));
                  held.lore(lore);

                  c.getSource().sendSuccess(CoreMessages.addedLore(message));
                  return 0;
                })
            )
        )

        .then(literal("set")
            .then(argument("index", IntegerArgumentType.integer(1))
                .suggests(suggestLoreIndexes())

                .then(argument("text", Arguments.CHAT)
                    .executes(c -> {
                      var held = getHeld(c.getSource());
                      var lore = held.lore();

                      if (lore == null || lore.isEmpty()) {
                        throw Exceptions.create("No lore on the item");
                      }

                      int index = c.getArgument("index", Integer.class);
                      Component text = Arguments.getMessage(c, "text").asComponent();

                      Commands.ensureIndexValid(index, lore.size());

                      lore.set(index-1, text);
                      held.lore(lore);

                      c.getSource().sendSuccess(
                          Text.format("Set lore line &e{0, number}&r to '&f{1}&r'",
                              NamedTextColor.GRAY, index, text
                          )
                      );
                      return 0;
                    })
                )
            )
        )

        .then(literal("remove")
            .then(removeIndex())

            .then(literal("at")
                .then(removeIndex())
            )

            .then(literal("between")
                .then(argument("start_index", IntegerArgumentType.integer(1))
                    .suggests(suggestLoreIndexes())

                    .then(argument("end_index", IntegerArgumentType.integer(1))
                        .suggests(suggestLoreIndexes())

                        .executes(c -> {
                          var start = c.getArgument("start_index", Integer.class);
                          var end = c.getArgument("end_index", Integer.class);

                          if (start > end) {
                            throw Exceptions.invalidBounds(start, end);
                          }

                          var range = Range.between(start, end);
                          return removeLore(c, range);
                        })
                    )
                )
            )
        );
  }

  private RequiredArgumentBuilder<CommandSource, ?> removeIndex() {
    return argument("remove_index", IntegerArgumentType.integer(1))
        .suggests(suggestLoreIndexes())

        .executes(c -> {
          int index = c.getArgument("remove_index", Integer.class);
          var range = Range.between(index, index);

          return removeLore(c, range);
        });
  }

  private SuggestionProvider<CommandSource> suggestLoreIndexes() {
    return (context, builder) -> {
      var held = getHeld(context.getSource());
      var lore = held.lore();

      if (lore == null || lore.isEmpty()) {
        return Suggestions.empty();
      }

      var token = builder.getRemainingLowerCase();

      for (int i = 1; i <= lore.size(); i++) {
        var suggestion = i + "";

        if (Completions.matches(token, suggestion)) {
          builder.suggest(suggestion);
        }
      }

      return builder.buildFuture();
    };
  }

  private int removeLore(CommandContext<CommandSource> c, Range<Integer> removeRange)
      throws CommandSyntaxException
  {
    var held = getHeld(c.getSource());
    var lore = held.lore();

    if (lore == null || lore.isEmpty()) {
      throw CoreExceptions.NO_LORE;
    }

    var size = lore.size();

    Commands.ensureIndexValid(removeRange.getMinimum(), size);
    Commands.ensureIndexValid(removeRange.getMaximum(), size);

    lore.subList(removeRange.getMinimum() - 1, removeRange.getMaximum())
        .clear();

    held.lore(lore);

    boolean singleIndex = removeRange.getMaximum().equals(removeRange.getMinimum());

    if (singleIndex) {
      c.getSource().sendSuccess(
          CoreMessages.removedLoreIndex(removeRange.getMinimum())
      );
    } else {
      c.getSource().sendSuccess(
          CoreMessages.removedLoreRange(removeRange)
      );
    }

    return 0;
  }
}