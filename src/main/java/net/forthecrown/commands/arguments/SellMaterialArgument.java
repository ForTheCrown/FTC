package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Crown;
import net.forthecrown.economy.sell.ItemSellData;
import net.forthecrown.grenadier.CmdUtil;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.royalgrenadier.VanillaMappedArgument;
import org.bukkit.Material;

import java.util.concurrent.CompletableFuture;

public class SellMaterialArgument implements ArgumentType<Material>, VanillaMappedArgument {
    final EnumArgument<Material> parser = EnumArgument.of(Material.class);

    @Override
    public Material parse(StringReader reader) throws CommandSyntaxException {
        Material material = parser.parse(reader);

        if (!Crown.getEconomy()
                .getSellShop()
                .getPriceMap()
                .contains(material)
        ) {
            throw Exceptions.notSellable(material);
        }

        return material;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        var token = builder.getRemainingLowerCase();

        var priceMap = Crown.getEconomy()
                .getSellShop()
                .getPriceMap();

        for (var d: priceMap) {
            suggest(d, d.getMaterial(), token, builder);

            if (d.canBeCompacted()) {
                suggest(d, d.getCompactMaterial(), token, builder);
            }
        }

        return builder.buildFuture();
    }

    private void suggest(ItemSellData d, Material label, String token, SuggestionsBuilder builder) {
        var suggestion = label.name().toLowerCase();

        if (!CompletionProvider.startsWith(token, suggestion)) {
            return;
        }

        var tooltip = String.format("type='%s', price=%s, maxEarnable=%s, compactSize=%s, compactType='%s'",
                d.getMaterial(),
                d.getPrice(),
                d.getMaxEarnings(),
                d.getCompactMultiplier(),
                d.getCompactMaterial()
        );

        builder.suggest(suggestion, CmdUtil.toTooltip(tooltip));
    }

    @Override
    public ArgumentType<?> getVanillaArgumentType() {
        return StringArgumentType.word();
    }
}