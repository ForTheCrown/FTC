package net.forthecrown.dungeons.usables;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.squire.Squire;
import net.forthecrown.useables.actions.UsageAction;
import net.forthecrown.useables.actions.UsageActionInstance;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.translation.Translatable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ActionEntranceInfo implements UsageAction<ActionEntranceInfo.ActionInstance> {
    public static final Key KEY = Squire.createRoyalKey("entrance_info");
    public static final EnumArgument<Type> TYPE_PARSER = EnumArgument.of(Type.class);

    @Override
    public ActionInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new ActionInstance(TYPE_PARSER.parse(reader));
    }

    @Override
    public ActionInstance deserialize(JsonElement element) throws CommandSyntaxException {
        return new ActionInstance(JsonUtils.readEnum(Type.class, element));
    }

    @Override
    public JsonElement serialize(ActionInstance value) {
        return JsonUtils.writeEnum(value.type);
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return TYPE_PARSER.listSuggestions(context, builder);
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    public record ActionInstance(Type type) implements UsageActionInstance {
        @Override
        public String asString() {
            return typeKey().asString() + "{type=" + type.name().toLowerCase() + "}";
        }

        @Override
        public Key typeKey() {
            return KEY;
        }

        @Override
        public void onInteract(Player player) {
            player.sendMessage(
                    Component.translatable(type, NamedTextColor.YELLOW  )
            );
        }
    }

    public enum Type implements Translatable {
        BOSS_SPAWN,
        DEATH,
        LEVEL_INFO;

        private final String translationKey;

        Type() {
            this.translationKey = "dungeons.info." + name().toLowerCase();
        }

        @Override
        public @NotNull String translationKey() {
            return translationKey;
        }
    }
}
