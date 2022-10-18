package net.forthecrown.useables.command;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.user.UserTeleport;
import net.forthecrown.user.Users;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.TagUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public class Warp extends CommandUsable {
    private Location destination;

    public Warp(String name, Location destination) {
        super(name);
        this.destination = destination;
    }

    public Warp(String name, JsonObject json) throws CommandSyntaxException {
        super(name, json);
        setDestination(JsonUtils.readLocation(json.getAsJsonObject("location")));
    }

    public Warp(String name, CompoundTag tag) throws CommandSyntaxException {
        super(name, tag);
        setDestination(TagUtil.readLocation(tag.get("location")));
    }

    @Override
    protected void save(CompoundTag tag) {
        tag.put("location", TagUtil.writeLocation(getDestination()));
    }

    @Override
    public boolean onInteract(Player player) {
        var user = Users.get(player);

        if (!user.canTeleport()) {
            player.sendMessage("Cannot teleport right now!");
            return false;
        }

        user.createTeleport(this::getDestination, UserTeleport.Type.WARP)
                .start();
        return true;
    }

    @Override
    public @NotNull HoverEvent<Component> asHoverEvent(@NotNull UnaryOperator<Component> op) {
        return Component.text("Destination: ")
                .append(Component.newline())
                .append(Component.text("world: " + getDestination().getWorld().getName()))

                .append(Component.newline())
                .append(Component.text("x: " + getDestination().getBlockX()))

                .append(Component.newline())
                .append(Component.text("y: " + getDestination().getBlockY()))

                .append(Component.newline())
                .append(Component.text("z: " + getDestination().getBlockZ())).asHoverEvent();
    }

    public Location getDestination() {
        return destination.clone();
    }

    public void setDestination(Location destination) {
        this.destination = Validate.notNull(destination);
    }
}