package net.forthecrown.commands.item;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.ArrayList;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Commands;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Messages;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
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

    factory.usage("add <text>")
        .addInfo("Adds the <text> to your held item's lore");

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
        .then(literal("clear")
            .executes(c -> {
              var held = getHeld(c.getSource());
              held.lore(null);

              c.getSource().sendAdmin(Messages.CLEARED_LORE);
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

                  var message = c.getArgument("text", Component.class);

                  lore.add(Text.wrapForItems(message));
                  held.lore(lore);

                  c.getSource().sendAdmin(Messages.addedLore(message));
                  return 0;
                })
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

        if (CompletionProvider.startsWith(token, suggestion)) {
          builder.suggest(suggestion);
        }
      }

      return builder.buildFuture();
    };
  }

  private int removeLore(CommandContext<CommandSource> c, Range<Integer> removeRange)
      throws CommandSyntaxException {
    var held = getHeld(c.getSource());
    var lore = held.lore();

    if (lore == null || lore.isEmpty()) {
      throw Exceptions.NO_LORE;
    }

    var size = lore.size();

    Commands.ensureIndexValid(removeRange.getMinimum(), size);
    Commands.ensureIndexValid(removeRange.getMaximum(), size);

    lore.subList(removeRange.getMinimum() - 1, removeRange.getMaximum())
        .clear();

    held.lore(lore);

    boolean singleIndex = removeRange.getMaximum().equals(removeRange.getMinimum());

    if (singleIndex) {
      c.getSource().sendAdmin(
          Messages.removedLoreIndex(removeRange.getMinimum())
      );
    } else {
      c.getSource().sendAdmin(
          Messages.removedLoreRange(removeRange)
      );
    }

    return 0;
  }
}