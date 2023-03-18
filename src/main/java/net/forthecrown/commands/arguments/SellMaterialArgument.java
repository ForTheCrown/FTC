package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.economy.Economy;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.EnumArgument;
import net.minecraft.commands.CommandBuildContext;
import org.bukkit.Material;

public class SellMaterialArgument 
    implements ArgumentType<Material>, VanillaMappedArgument 
{

  final EnumArgument<Material> parser 
      = ArgumentTypes.enumType(Material.class);

  @Override
  public Material parse(StringReader reader) throws CommandSyntaxException {
    Material material = parser.parse(reader);

    if (!Economy.get()
        .getSellShop()
        .getPriceMap()
        .contains(material)
    ) {
      throw Exceptions.notSellable(material);
    }

    return material;
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context,
                                                            SuggestionsBuilder builder
  ) {
    var token = builder.getRemainingLowerCase();

    var priceMap = Economy.get()
        .getSellShop()
        .getPriceMap()
        .keyIterator();

    priceMap.forEachRemaining(s -> {
      if (!Completions.matches(token, s)) {
        return;
      }

      builder.suggest(s);
    });

    return builder.buildFuture();
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return StringArgumentType.word();
  }
}