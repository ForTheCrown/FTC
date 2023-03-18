package net.forthecrown.useables.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.minecraft.commands.CommandBuildContext;
import org.bukkit.inventory.ItemStack;

class ItemParser implements ArgumentType<ItemStack>, VanillaMappedArgument {

  @Override
  public ItemStack parse(StringReader reader) throws CommandSyntaxException {
    if (!reader.canRead()) {
      throw Exceptions.format(
          "Expected input! '{}' to use the item your holding\n" +
              "or <amount> <item> to read an item"
      );
    }

    int amount = 1;

    if (StringReader.isAllowedNumber(reader.peek())) {
      amount = reader.readInt();
      reader.skipWhitespace();
    }

    var parsedItem = ArgumentTypes.item().parse(reader);
    return parsedItem.create(amount, true);
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context,
                                                            SuggestionsBuilder builder
  ) {
    int lastSpace = builder.getRemainingLowerCase().lastIndexOf(' ');

    if (lastSpace == -1) {
      if (builder.getRemainingLowerCase().isBlank()
          || StringReader.isAllowedNumber(builder.getRemainingLowerCase().charAt(0))
      ) {
        Completions.suggest(builder, "64", "32", "16", "8", "1");
      }

      return ArgumentTypes.item().listSuggestions(context, builder);
    } else {
      return ArgumentTypes.item().listSuggestions(
          context,
          builder.createOffset(builder.getInput().lastIndexOf(' ') + 1)
      );
    }
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return StringArgumentType.greedyString();
  }
}