package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.royalgrenadier.types.EnchantArgumentImpl;
import net.forthecrown.squire.enchantment.RoyalEnchant;
import net.forthecrown.squire.enchantment.RoyalEnchants;
import net.kyori.adventure.text.Component;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class RoyalEnchantArgument implements ArgumentType<RoyalEnchant> {
    public static RoyalEnchantArgument ENCHANT = new RoyalEnchantArgument();

    @Override
    public RoyalEnchant parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String name = reader.readUnquotedString();

        return switch (name.toLowerCase()) {
            case "swim", "dolphin_swimmer" -> RoyalEnchants.dolphinSwimmer();
            case "aim", "strong_aim" -> RoyalEnchants.strongAim();
            case "crit", "poison_crit" -> RoyalEnchants.poisonCrit();
            case "block", "healing_block" -> RoyalEnchants.healingBlock();
            default -> throw EnchantArgumentImpl.UNKNOWN_ENCHANTMENT.createWithContext(GrenadierUtils.correctReader(reader, cursor), Component.text(name));
        };
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CompletionProvider.suggestMatching(builder, Arrays.asList("swim", "crit", "aim", "block"));
    }
}
