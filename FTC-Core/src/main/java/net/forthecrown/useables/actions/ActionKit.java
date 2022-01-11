package net.forthecrown.useables.actions;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.arguments.KitArgument;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.kits.Kit;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ActionKit implements UsageAction<ActionKit.ActionInstance> {
    public static final Key KEY = Keys.forthecrown("give_kit");

    @Override
    public ActionInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        Kit kit = Crown.getKitManager().get(KitArgument.kit().parse(reader));

        return new ActionInstance(kit.key());
    }

    @Override
    public ActionInstance deserialize(JsonElement element) throws CommandSyntaxException {
        return new ActionInstance(JsonUtils.readKey(element));
    }

    @Override
    public JsonElement serialize(ActionInstance value) {
        return JsonUtils.writeKey(value.getKitKey());
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return Crown.getKitManager().getSuggestions(context, builder);
    }

    public static class ActionInstance implements UsageActionInstance {
        private final Key kitKey;

        public ActionInstance(Key kitKey) {
            this.kitKey = kitKey;
        }

        @Override
        public void onInteract(Player player) {
            Kit kit = Crown.getKitManager().get(kitKey);
            if(kit == null){
                Crown.logger().warning("Null kit in action!");
                return;
            }

            kit.attemptItemGiving(player);
        }

        @Override
        public String asString() {
            return typeKey().asString() + "{key= " + kitKey + "}";
        }

        @Override
        public @NonNull Key typeKey() {
            return KEY;
        }
        public Key getKitKey() {
            return kitKey;
        }
    }
}
