package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.cosmetics.deaths.AbstractDeathEffect;
import net.forthecrown.grenadier.exceptions.MutableCommandExceptionType;
import net.forthecrown.registry.Registries;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.commands.manager.FtcSuggestionProvider;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

import java.util.concurrent.CompletableFuture;

public class DeathEffectType implements ArgumentType<AbstractDeathEffect> {
    private static final DeathEffectType INSTANCE = new DeathEffectType();
    private DeathEffectType() {}

    public static final MutableCommandExceptionType UNKNOWN_EFFECT = new MutableCommandExceptionType(o ->
            Component.text()
                    .append(Component.text("Unknown DeathEffect: "))
                    .append(Component.text(o.toString()))
                    .build()
    );

    private final KeyType keyType = KeyType.ftc();

    public static DeathEffectType deathEffect(){
        return INSTANCE;
    }

    @Override
    public AbstractDeathEffect parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        Key key = keyType.parse(reader);

        AbstractDeathEffect effect = Registries.DEATH_EFFECTS.get(key);
        if(effect == null) throw UNKNOWN_EFFECT.createWithContext(GrenadierUtils.correctCursorReader(reader, cursor), key);

        return effect;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return FtcSuggestionProvider.suggestRegistry(builder, Registries.DEATH_EFFECTS);
    }
}
