package net.forthecrown.sellshop.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.command.Exceptions;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.sellshop.ItemPriceMap;
import net.forthecrown.sellshop.ItemSellData;
import org.bukkit.Material;
import org.bukkit.Registry;

public class SellMaterialArgument implements ArgumentType<Material> {

  private final ItemPriceMap map;
  private final ArgumentType<Material> matParser;

  public SellMaterialArgument(ItemPriceMap map) {
    this.map = map;
    this.matParser = ArgumentTypes.registry(Registry.MATERIAL, "Material");
  }

  @Override
  public Material parse(StringReader reader) throws CommandSyntaxException {
    int start = reader.getCursor();

    Material material = matParser.parse(reader);
    ItemSellData data = map.get(material);

    if (data == null) {
      reader.setCursor(start);
      throw Exceptions.formatWithContext("Material {0} is not sellable", reader, material);
    }

    return material;
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    return Completions.suggest(builder, map::keyIterator);
  }
}
