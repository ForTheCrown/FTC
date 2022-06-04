package net.forthecrown.useables.actions;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.Keys;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.WorldArgument;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.UserTeleport;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ActionTeleport implements UsageAction<ActionTeleport.ActionInstance> {
    public static final NamespacedKey KEY = Keys.forthecrown("teleport_user");

    @Override
    public ActionInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        if(!reader.canRead()) return new ActionInstance(source.getLocation());

        Location location = PositionArgument.position().parse(reader).getLocation(source);
        float yaw = location.getYaw();
        float pitch = location.getPitch();

        if(reader.canRead() && reader.peek() == ' ') {
            reader.skipWhitespace();

            World world = WorldArgument.world().parse(reader);
            location.setWorld(world);
        }

        if(reader.canRead()) {
            reader.skipWhitespace();
            yaw = reader.readFloat();

            if(reader.canRead()) {
                reader.skipWhitespace();
                pitch = reader.readFloat();
            }
        }

        location.setYaw(yaw);
        location.setPitch(pitch);

        return new ActionInstance(location);
    }

    @Override
    public ActionInstance deserialize(JsonElement element) throws CommandSyntaxException {
        return new ActionInstance(JsonUtils.readLocation(element.getAsJsonObject()));
    }

    @Override
    public JsonElement serialize(ActionInstance value) {
        return JsonUtils.writeLocation(value.getLocation());
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }

    @Override
    public boolean requiresInput() {
        return false;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return PositionArgument.position().listSuggestions(context, builder);
    }

    public static class ActionInstance implements UsageActionInstance {
        private final Location location;

        public ActionInstance(Location location) {
            this.location = location;
        }

        @Override
        public void onInteract(Player player) {
            CrownUser user = UserManager.getUser(player);

            user.createTeleport(this::getLocation, true, true, UserTeleport.Type.TELEPORT)
                    .start(true);
        }

        @Override
        public Key typeKey() {
            return KEY;
        }

        @Override
        public String asString() {
            return typeKey().asString() + "{location=" + location + "}";
        }

        public Location getLocation() {
            return location;
        }
    }
}