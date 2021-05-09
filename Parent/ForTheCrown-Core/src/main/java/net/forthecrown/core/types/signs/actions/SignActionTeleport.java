package net.forthecrown.core.types.signs.actions;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.types.signs.SignAction;
import net.forthecrown.core.utils.JsonUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.WorldArgument;
import net.forthecrown.grenadier.types.pos.Position;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class SignActionTeleport implements SignAction {
    private Location location;

    @Override
    public void parse(JsonElement json) throws CommandSyntaxException {
        location = JsonUtils.deserializeLocation(json.getAsJsonObject());
    }

    @Override
    public void parse(CommandContext<CommandSource> context, StringReader reader) throws CommandSyntaxException {
        World world = WorldArgument.world().parse(reader);
        reader.skipWhitespace();
        Position pos = PositionArgument.position().parse(reader);

        Location l = pos.getLocation(context.getSource());
        l.setWorld(world);

        location = l;
    }

    @Override
    public void onInteract(Player player) {
        player.teleport(location);
    }

    @Override
    public String getRegistrationName() {
        return "teleport_user";
    }

    @Override
    public String asString() {
        return "SignActionTeleport{location=" + location + "}";
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.serializeLocation(location);
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        int index = builder.getRemaining().indexOf(' ');
        if(index == -1) return WorldArgument.world().listSuggestions(context, builder);

        builder = builder.createOffset(builder.getInput().length() + index + 1);
        return PositionArgument.position().listSuggestions(context, builder);
    }
}
