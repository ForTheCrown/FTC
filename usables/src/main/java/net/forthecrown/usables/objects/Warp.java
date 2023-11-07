package net.forthecrown.usables.objects;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextWriter;
import net.forthecrown.usables.Condition.TransientCondition;
import net.forthecrown.usables.Interaction;
import net.forthecrown.user.User;
import net.forthecrown.user.UserTeleport;
import net.forthecrown.user.UserTeleport.Type;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Locations;
import net.forthecrown.utils.io.TagUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Warp extends CommandUsable {

  private Location destination;

  @Getter @Setter
  private boolean instant;

  public Warp(String name) {
    super(name);
  }

  @Override
  public String getCommandPrefix() {
    return "/warp " + getName();
  }

  @Override
  protected void onInteract(Player player, boolean adminInteraction) {
    User user = Users.get(player);
    UserTeleport teleport = user.createTeleport(this::getDestination, Type.WARP);

    if (adminInteraction || instant) {
      teleport.setDelay(null);
    }

    teleport.start();
  }

  public Location getDestination() {
    return Locations.clone(destination);
  }

  public void setDestination(Location destination) {
    Objects.requireNonNull(destination);
    this.destination = Locations.clone(destination);
  }

  @Override
  public void save(CompoundTag tag) {
    super.save(tag);
    tag.put("location", TagUtil.writeLocation(destination));
    tag.putBoolean("instant", instant);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    setDestination(TagUtil.readLocation(tag.get("location")));
    setInstant(tag.getBoolean("instant", false));
  }

  @Override
  public void write(TextWriter writer) {
    writer.write(Text.prettyLocation(destination, false));
  }

  @Override
  protected TransientCondition additionalCondition() {
    return new TeleportCondition();
  }

  private static class TeleportCondition implements TransientCondition {

    @Override
    public boolean test(Interaction interaction) {
      return interaction.user().canTeleport();
    }

    @Override
    public Component failMessage(Interaction interaction) {
      return interaction.user().checkTeleportMessage();
    }
  }
}
