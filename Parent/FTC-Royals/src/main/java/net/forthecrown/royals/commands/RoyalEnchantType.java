package net.forthecrown.royals.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.royalgrenadier.types.EnchantArgumentImpl;
import net.forthecrown.royals.enchantments.CrownEnchant;
import net.forthecrown.royals.enchantments.RoyalEnchants;
import net.kyori.adventure.text.Component;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class RoyalEnchantType implements ArgumentType<CrownEnchant> {
    public static RoyalEnchantType ENCHANT = new RoyalEnchantType();

    @Override
    public CrownEnchant parse(StringReader reader) throws CommandSyntaxException {
        String name = reader.readUnquotedString();

        switch (name.toLowerCase()){
            case "swim":
            case "dolphin_swimmer":
                return RoyalEnchants.dolphinSwimmer();

            case "aim":
            case "strong_aim":
                return RoyalEnchants.strongAim();

            case "crit":
            case "poison_crit":
                return RoyalEnchants.poisonCrit();

            case "block":
            case "healing_block":
                return RoyalEnchants.healingBlock();

            default:
                throw EnchantArgumentImpl.UNKNOWN_ENCHANTMENT.createWithContext(reader, Component.text(name));
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(builder, Arrays.asList("swim", "crit", "aim", "block"));
    }
}
