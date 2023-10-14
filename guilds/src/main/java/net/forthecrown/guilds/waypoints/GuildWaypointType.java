package net.forthecrown.guilds.waypoints;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.forthecrown.command.Exceptions;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildExceptions;
import net.forthecrown.guilds.GuildMember;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.guilds.GuildPermissions;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.user.User;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.type.PlayerWaypointType;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.math.vector.Vector3i;

public class GuildWaypointType extends PlayerWaypointType {

  public GuildWaypointType() {
    super(
        "Guild-Made",
        new Material[]{
            Material.STONE_BRICKS,
            Material.STONE_BRICKS,
            Material.LODESTONE,
        }
    );
  }

  @Override
  public void onCreate(User creator, Vector3i topPos, boolean copy) throws CommandSyntaxException {
    var guild = Guilds.getGuild(creator);

    if (guild == null) {
      throw GuildExceptions.NOT_IN_GUILD;
    }

    // Ensure member has relocation permission
    GuildMember member = guild.getMember(creator.getUniqueId());
    if (member == null) {
      if (!creator.hasPermission(GuildPermissions.GUILD_ADMIN)) {
        throw Exceptions.NO_PERMISSION;
      }
    }
    else if (!member.hasPermission(GuildPermission.CAN_RELOCATE)) {
      throw GuildExceptions.G_NO_PERM_WAYPOINT;
    }

    // Ensure chunk is owned by the user's guild
    Guild chunkOwner = Guilds.getManager().getOwner(Vectors.getChunk(topPos));

    if (!Objects.equals(guild, chunkOwner)) {
      throw GuildExceptions.G_EXTERNAL_WAYPOINT;
    }

    if (copy) {
      return;
    }

    var waypoint = guild.getSettings().getWaypoint();
    if (waypoint != null) {
      throw GuildExceptions.waypointAlreadySet(waypoint);
    }
  }

  @Override
  public void onPostCreate(Waypoint waypoint, User creator) {
    var guild = Guilds.getGuild(creator);

    if (guild == null) {
      return;
    }

    guild.moveWaypoint(waypoint, creator);
  }

  @Override
  public TextColor getNameColor(Waypoint waypoint) {
    return getGuild(waypoint)
        .map(guild -> guild.getSettings().getPrimaryColor().getTextColor())
        .orElse(NamedTextColor.GOLD);
  }

  @Override
  public String getEffectiveName(Waypoint waypoint) {
    return getGuild(waypoint).map(Guild::getName).orElse(null);
  }

  @Override
  public boolean canEdit(User user, Waypoint waypoint) {
    if (super.canEdit(user, waypoint)) {
      return true;
    }

    if (user.hasPermission(GuildPermissions.GUILD_ADMIN)) {
      return true;
    }

    var opt = getGuild(waypoint);
    if (opt.isEmpty()) {
      return false;
    }

    Guild guild = opt.get();
    GuildMember member = guild.getMember(user.getUniqueId());

    if (member == null || member.hasLeft()) {
      return false;
    }

    return member.hasPermission(GuildPermission.CAN_RELOCATE);
  }

  @Override
  public ItemStack getDisplayItem(Waypoint waypoint) {
    return getGuild(waypoint)
        .map(guild -> guild.getSettings().getBanner())
        .map(ItemStack::clone)
        .orElse(null);
  }

  Optional<Guild> getGuild(Waypoint waypoint) {
    UUID guildId = waypoint.get(GuildWaypoints.GUILD_OWNER);

    if (guildId == null) {
      return Optional.empty();
    }

    Guild guild = Guilds.getManager().getGuild(guildId);
    return Optional.ofNullable(guild);
  }
}
