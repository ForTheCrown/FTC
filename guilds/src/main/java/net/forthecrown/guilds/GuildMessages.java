package net.forthecrown.guilds;

import static net.forthecrown.text.Messages.CLICK_ME;
import static net.forthecrown.text.Messages.chatMessage;
import static net.forthecrown.text.Messages.crossButton;
import static net.forthecrown.text.Messages.tickButton;
import static net.forthecrown.text.Text.format;
import static net.kyori.adventure.text.Component.text;

import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.math.vector.Vector2i;
import org.spongepowered.math.vector.Vector3i;

public interface GuildMessages {

  TextComponent GUILD_DELETED_EMPTY = text("Guild was deleted, because it was empty");

  TextComponent CHUNK_UNCLAIMED = text("Chunk unclaimed!", NamedTextColor.GRAY);

  TextComponent GUILD_WAYPOINT_LOST
      = text("Waypoint was lost due to chunk unclaiming!", NamedTextColor.GRAY);

  /**
   * The message shown to first-time joining guild members to inform them about guild commands.
   * <p>
   * Used by {@link net.forthecrown.guilds.Guild}
   */
  TextComponent GUILD_JOINED_HELP = text()
      .color(NamedTextColor.GRAY)
      .append(
          text("Use "),
          text("/g", NamedTextColor.YELLOW)
              .clickEvent(ClickEvent.runCommand("/g"))
              .hoverEvent(CLICK_ME),
          text(" to get started.\n Use "),
          text("/g help", NamedTextColor.YELLOW)
              .clickEvent(ClickEvent.runCommand("/g help"))
              .hoverEvent(CLICK_ME),
          text(" to learn about guild commands.")
      )
      .build();

  TextComponent WEEKEND_MULTIPLIER_INACTIVE = text(
      "Weekend multiplier is no longer active :(",
      NamedTextColor.YELLOW
  );

  static Component leftGuild(Guild guild) {
    return format("You've left the &f{0}&r guild.",
        NamedTextColor.GRAY,
        guild.getName()
    );
  }

  static Component leftGuildAnnouncement(User user) {
    return format("&e{0, user}&r has left the guild.",
        NamedTextColor.GRAY,
        user
    );
  }

  static Component guildChunkClaimed(Guild guild) {
    return format("&f{0}&r Claimed chunk",
        NamedTextColor.GRAY,
        guild.displayName()
    );
  }

  static Component changedRank(boolean wasPromoted,
      User promoted,
      GuildRank rank,
      Guild guild
  ) {
    return format("&f{0}&r {3} &e{1, user}&r to &f{2}&r",
        NamedTextColor.GRAY,

        guild.displayName(),
        promoted,
        rank.getName(),
        wasPromoted ? "Promoted" : "Demoted"
    );
  }

  static Component rankChangeAnnouncement(boolean wasPromoted,
      User promoter,
      User promoted,
      GuildRank rank
  ) {
    return format("&e{0, user}&r {3} &6{1, user}&r to {2}",
        NamedTextColor.GRAY,
        promoter, promoted,
        rank.getName(),
        wasPromoted ? "promoted" : "demoted"
    );
  }

  static Component guildDeletedAnnouncement(User deleter) {
    return format("&e{0, user}&r has deleted the guild.",
        NamedTextColor.GRAY,
        deleter
    );
  }

  static Component guildDeleted(Guild guild) {
    return format("Deleted the &e{0}&r guild.",
        NamedTextColor.GRAY,
        guild.displayName()
    );
  }

  static Component guildKickedTarget(Guild guild, User sender) {
    return format("&e{0, user}&r kicked you from the &6{1}&r guild.",
        NamedTextColor.GRAY,
        sender, guild.displayName()
    );
  }

  static Component guildKickedSender(Guild guild, User target) {
    return format("Kicked &e{0, user}&r from the &6{1}&r guild.",
        NamedTextColor.GRAY,
        target, guild.displayName()
    );
  }

  static Component guildKickAnnouncement(User sender, User target) {
    return format("&e{0, user}&r kicked &6{1, user}&r from the guild.",
        NamedTextColor.GRAY,
        sender, target
    );
  }

  static Component guildSetCenter(Vector3i pos, User user) {
    return format("&e{0, user}&r set the guild's waypoint to &6{1, vector}&r.",
        NamedTextColor.GRAY,
        user, pos
    );
  }

  static Component guildUnclaimAllAnnouncement(User user) {
    return format("&e{0, user}&r unclaimed &lall&r of the guild's chunks!",
        NamedTextColor.GRAY,
        user
    );
  }

  static Component guildUnclaimAll(Guild guild) {
    return format("Unclaimed &lall&r &f{0}&r's chunks",
        NamedTextColor.GRAY,
        guild.displayName()
    );
  }

  static Component guildUnclaimAnnouncement(Vector2i chunkAbs, User unclaimer) {
    return format("Chunk at &e{0, vector}&r was unclaimed by &6{1, user}&r.",
        NamedTextColor.GRAY,
        chunkAbs, unclaimer
    );
  }

  static Component guildJoin(Guild guild) {
    return format("You've joined the &6{0}&r guild!",
        NamedTextColor.YELLOW,
        guild.displayName()
    );
  }

  static Component guildJoinAnnouncement(User user) {
    return format("&6{0, user}&r has joined the guild!",
        NamedTextColor.YELLOW,
        user
    );
  }

  static Component guildInviteTarget(Guild guild) {
    return format("You've been invited to join the &f{0}&r guild! &a{1} &4{2}",
        NamedTextColor.GRAY,

        guild.displayName(),
        tickButton("/g invite accept %s", guild.getName()),
        crossButton("/g invite deny %s", guild.getName())
    );
  }

  static Component guildChat(Guild guild,
      Component displayName,
      Component message
  ) {
    return text()
        .append(
            guild.getPrefix(),
            chatMessage(displayName, message)
        )
        .build();
  }

  static Component multiplierNowActive(float mod) {
    return format(
        "&6{0, number}x multiplier on all earned &6Guild Exp&r now active!",
        NamedTextColor.YELLOW,
        mod
    );
  }

  static Component multiplierIncremented(float mod) {
    return format(
        "Guild Exp multiplier increased to &6{0, number}x&r!",
        NamedTextColor.YELLOW,
        mod
    );
  }

  static Component multiplierDecremented(float mod) {
    return format(
        "Guild Exp multiplier decreased to &e{0, number}x&r.",
        NamedTextColor.GRAY,
        mod
    );
  }

  static Component weekendMultiplierActive(float mod) {
    return format(
        "Weekend Guild Exp multiplier is now active! &6(now {0, number}x)",
        NamedTextColor.YELLOW,
        mod
    );
  }

  static Component guildMultiplierActive(float mod) {
    return format(
        "There's a &6{0, number}x multiplier&r on all earned Guild Exp!",
        NamedTextColor.YELLOW,
        mod
    );
  }

  static Component guildAutoLevelUp(User user, GuildRank rank) {
    return format("&e{0, user}&r was automatically leveled up to rank &f{1}&r",
        NamedTextColor.GRAY,
        user, rank.getFormattedName()
    );
  }
}
