package net.forthecrown.vikings.valhalla.triggers.checks;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.emperor.utils.CrownBoundingBox;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.vikings.Vikings;
import net.forthecrown.vikings.valhalla.active.ActiveRaid;
import net.forthecrown.vikings.valhalla.triggers.TriggerCheck;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.CompletableFuture;

public class CheckBoundingBox implements TriggerCheck<PlayerMoveEvent> {
    public static final Key ENTER_KEY = Key.key(Vikings.namespaced(), "enter_region");
    public static final Key EXIT_KEY = Key.key(Vikings.namespaced(), "exit_region");

    private CrownBoundingBox box;
    private final boolean exit;

    public CheckBoundingBox(boolean exit){
        this.exit = exit;
    }

    @Override
    public void deserialize(JsonElement element) throws CommandSyntaxException {

    }

    @Override
    public void parse(StringReader reader) throws CommandSyntaxException {

    }

    @Override
    public boolean check(Player player, ActiveRaid raid, PlayerMoveEvent event) {
        return false;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return PositionArgument.position().listSuggestions(context, builder);
    }

    @Override
    public JsonElement serialize() {
        return null;
    }

    @Override
    public @NonNull Key key() {
        return exit ? EXIT_KEY : ENTER_KEY;
    }
}
