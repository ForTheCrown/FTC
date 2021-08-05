package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.manager.CoreCommands;
import net.forthecrown.cosmetics.arrows.ArrowEffect;
import net.forthecrown.grenadier.exceptions.MutableCommandExceptionType;
import net.forthecrown.grenadier.types.KeyArgument;
import net.forthecrown.registry.Registries;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.commands.manager.FtcSuggestionProvider;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Parses an arrow effect type from its key.
 * <p>Only used in {@link net.forthecrown.commands.CommandCosmeticEffects}</p>
 */
public class ArrowEffectArgument implements ArgumentType<ArrowEffect> {
    private static final ArrowEffectArgument INSTANCE = new ArrowEffectArgument();
    private ArrowEffectArgument() {}

    //Throw when parsing fails
    public static final MutableCommandExceptionType UNKNOWN_EFFECT = new MutableCommandExceptionType(o ->
            Component.text()
                    .append(Component.text("Invalid ArrowEffect: "))
                    .append(Component.text(o.toString()))
                    .build()
    );

    //Key type to use to parse
    private final KeyArgument keyType = CoreCommands.ftcKeyType();

    //Static instance getter, looks nicer than using the variable itself
    public static ArrowEffectArgument arrowEffect(){
        return INSTANCE;
    }

    @Override
    public ArrowEffect parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        Key key = keyType.parse(reader);

        ArrowEffect effect = Registries.ARROW_EFFECTS.get(key);
        if(effect == null) throw UNKNOWN_EFFECT.createWithContext(GrenadierUtils.correctReader(reader, cursor), key);

        return effect;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return FtcSuggestionProvider.suggestRegistry(builder, Registries.ARROW_EFFECTS);
    }
}
