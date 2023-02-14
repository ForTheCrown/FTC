package net.forthecrown.useables.command;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.function.UnaryOperator;
import net.forthecrown.user.UserTeleport;
import net.forthecrown.user.Users;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Warp extends CommandUsable {

  private Location destination;

  public Warp(String name, Location destination) {
    super(name);
    this.destination = destination;
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
  public boolean onInteract(Player player, boolean adminInteraction) {
    var user = Users.get(player);

    if (!user.canTeleport()) {
      player.sendMessage("Cannot teleport right now!");
      return false;
    }

    user.createTeleport(this::getDestination, UserTeleport.Type.WARP)
        .setDelayed(!adminInteraction)
        .start();

    return true;
  }

  @Override
  public @NotNull HoverEvent<Component> asHoverEvent(@NotNull UnaryOperator<Component> op) {
    return text("Destination: ")
        .append(newline())
        .append(text("world: "))
        .append(text(Text.formatWorldName(getDestination().getWorld())))

        .append(newline())
        .append(text("x: " + getDestination().getBlockX()))

        .append(newline())
        .append(text("y: " + getDestination().getBlockY()))

        .append(newline())
        .append(text("z: " + getDestination().getBlockZ()))

        .asHoverEvent();
  }

  public Location getDestination() {
    return destination.clone();
  }

  public void setDestination(Location destination) {
    this.destination = Validate.notNull(destination);
  }
}