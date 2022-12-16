package net.forthecrown.commands.arguments;

import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.grenadier.CommandSource;

@FunctionalInterface
public interface SuggestionFunction {
    void suggest(SuggestionsBuilder builder, CommandSource source);
}