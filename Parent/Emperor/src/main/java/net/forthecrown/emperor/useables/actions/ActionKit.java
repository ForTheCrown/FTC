package net.forthecrown.emperor.useables.actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.commands.arguments.KitType;
import net.forthecrown.emperor.useables.UsageAction;
import net.forthecrown.emperor.useables.kits.Kit;
import net.forthecrown.emperor.utils.CrownUtils;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.CompletableFuture;

public class ActionKit implements UsageAction {
    public static final Key KEY = Key.key(CrownCore.getNamespace(), "give_kit");

    private Key kitKey;

    @Override
    public void parse(JsonElement json) throws CommandSyntaxException {

        parse(new StringReader(json.getAsString()));
    }

    @Override
    public void parse(CommandContext<CommandSource> context, StringReader reader) throws CommandSyntaxException {
        parse(reader);
    }

    public void parse(StringReader reader) throws CommandSyntaxException {
        Kit kit = CrownCore.getKitRegistry().get(CrownUtils.parseKey(reader));
        this.kitKey = kit.key();
    }

    @Override
    public void onInteract(Player player) {
        Kit kit = CrownCore.getKitRegistry().get(kitKey);
        if(kit == null){
            CrownCore.logger().warning("Null kit in action!");
            return;
        }

        kit.attemptItemGiving(player);
    }

    @Override
    public String asString() {
        return getClass().getSimpleName() + "{key= " + kitKey + "}";
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(kitKey.value());
    }

    @Override
    public @NonNull Key key() {
        return KEY;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return KitType.kit().listSuggestions(context, builder, true);
    }

    public Key getKitKey() {
        return kitKey;
    }

    public void setKitKey(Key kitKey) {
        this.kitKey = kitKey;
    }
}
