package net.forthecrown.useables.actions;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.CrownCore;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.WorldArgument;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActionTeleport implements UsageAction<ActionTeleport.ActionInstance> {
    public static final Key KEY = Key.key(CrownCore.inst(), "teleport_user");

    @Override
    public ActionInstance parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        Location location = PositionArgument.position().parse(reader).getLocation(source);

        if(reader.canRead() && reader.peek() == ' '){
            reader.skipWhitespace();

            World world = WorldArgument.world().parse(reader);
            location.setWorld(world);
        }

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

    public static class ActionInstance implements UsageActionInstance {
        private final Location location;

        public ActionInstance(Location location) {
            this.location = location;
        }

        @Override
        public void onInteract(Player player) {
            player.teleport(location);
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
