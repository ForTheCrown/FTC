package net.forthecrown.emperor.useables.warps;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.useables.CheckableBase;
import net.forthecrown.emperor.useables.UsageCheck;
import net.forthecrown.emperor.utils.CrownUtils;
import net.forthecrown.emperor.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class FtcWarp extends CheckableBase implements Warp {
    private final Key key;
    private Location location;

    public FtcWarp(JsonElement element) throws CommandSyntaxException {
        JsonObject json = element.getAsJsonObject();

        this.key = CrownUtils.parseKey(json.get("key").getAsString());
        this.location = JsonUtils.deserializeLocation(json.getAsJsonObject("location"));
        reloadChecksFrom(json);
    }

    public FtcWarp(Key name, Location location){
        this.key = name;
        this.location = location;
    }

    @Override
    public void setDestination(Location location) {
        this.location = location;
    }

    @Override
    public Location getDestination() {
        return location;
    }

    @Override
    public Key key() {
        return key;
    }

    @Override
    public boolean testSilent(Player player) {
        for (UsageCheck c: checks.values()){
            if(!c.test(player)) return false;
        }
        return true;
    }

    @Override
    public boolean test(Player player) {
        List<Consumer<Player>> onSuccess = new ArrayList<>();
        for (UsageCheck c: checks.values()){
            if(!c.test(player)){
                if(c.getPersonalizedFailMessage(player) != null) player.sendMessage(c.getPersonalizedFailMessage(player));
                return false;
            }

            Consumer<Player> con = c.onSuccess();
            if(con != null) onSuccess.add(con);
        }

        onSuccess.forEach(c -> c.accept(player));
        return true;
    }

    @Override
    public void delete() {
        CrownCore.getWarpRegistry().remove(key);
    }

    @Override
    public JsonElement serialize() {
        JsonObject result = new JsonObject();

        result.add("key", new JsonPrimitive(key.asString()));
        result.add("location", JsonUtils.serializeLocation(location));

        saveChecksInto(result);
        return result;
    }

    @Override
    public @Nullable Component displayName() {
        return Component.text(key.value())
                .hoverEvent(this)
                .clickEvent(ClickEvent.runCommand("/warp " + key.value()));
    }

    @Override
    public @NonNull HoverEvent<Component> asHoverEvent(@NonNull UnaryOperator<Component> op) {
        return Component.text("Destination: ")
                .append(Component.newline())
                .append(Component.text("world: " + location.getWorld().getName()))

                .append(Component.newline())
                .append(Component.text("x: " + location.getBlockX()))

                .append(Component.newline())
                .append(Component.text("y: " + location.getBlockY()))

                .append(Component.newline())
                .append(Component.text("z: " + location.getBlockZ())).asHoverEvent();
    }
}