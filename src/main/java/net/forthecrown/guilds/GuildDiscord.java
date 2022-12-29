package net.forthecrown.guilds;

import static net.forthecrown.guilds.GuildSettings.ROLE_COLOR;
import static net.forthecrown.user.data.UserTimeTracker.UNSET;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import github.scarsz.discordsrv.dependencies.jda.api.Permission;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Category;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.jda.api.managers.ChannelManager;
import github.scarsz.discordsrv.util.WebhookUtil;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.core.FTC;
import net.forthecrown.user.User;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;

@Getter
public class GuildDiscord {
  private static final Logger LOGGER = FTC.getLogger();

  public static final String
      KEY_CHANNEL = "channelId",
      KEY_ROLE = "roleId",
      KEY_CHANNEL_UPDATE = "lastChannelUpdate",
      KEY_ROLE_UPDATE = "lastRoleUpdate",
      KEY_ANNOUNCEMENTS = "forwardAnnouncements";

  public static final long NULL_ID = 0;

  public static final String CHANNEL_OPENING_MESSAGE =
      """
      Welcome to your very own guild chat!
      
      Any messages sent in this channel will be forwarded to the in-game
      guild chat, and any messages sent in-game with `/gc` will also be
      forwarded to this chat!
      
      Guild announcements and broadcasts can be forwarded to this chat too,
      that can be enabled/disabled with `/g set discordAnnouncements`
      
        - Love, Steven
      """;

  private final Guild guild;

  private long channelId = NULL_ID;
  private long roleId = NULL_ID;

  private long lastChannelUpdate = UNSET;
  private long lastRoleUpdate = UNSET;

  @Setter
  @Accessors(fluent = true)
  private boolean forwardAnnouncements = true;

  public GuildDiscord(Guild guild) {
    this.guild = guild;
    clear();
  }

  @RequiredArgsConstructor
  public enum ChannelVisibility {
    FULLY_PUBLIC (memberOverridePerms(), null),
    PRIVATE (null, memberOverridePerms()),

    READONLY_PUBLIC (
        EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_READ),
        null
    );

    private final EnumSet<Permission> allowPermissions;
    private final EnumSet<Permission> denyPermissions;
  }

  /* ------------------------------ METHODS ------------------------------- */

  public void clear() {
    lastChannelUpdate = UNSET;
    lastRoleUpdate = UNSET;
    channelId = NULL_ID;
    roleId = NULL_ID;
  }

  public static JDA getJDA() {
    return DiscordSRV.getPlugin().getJda();
  }

  public static github.scarsz.discordsrv.dependencies.jda.api.entities.Guild
  getDiscordGuild() {
    return DiscordSRV.getPlugin().getMainGuild();
  }

  public void forEachDiscordMember(Consumer<Member> memberConsumer) {
    guild.getMembersList().forEach(member -> {
      try {
        member.getUser()
            .getDiscordMember()
            .ifPresent(memberConsumer);
      } catch (Exception e) {
        LOGGER.error(
            "Couldn't iterate over discord member of {}",
            member.getId(), e
        );
      }
    });
  }

  /* ----------------------------- FORWARDING ----------------------------- */

  /**
   * Forwards an announcement to the guild's discord server.
   * <p>
   * If {@link #forwardAnnouncements} is set to false, then this method
   * will do nothing
   *
   * @param msg The message to forward
   */
  public void forwardAnnouncement(Component msg) {
    if (!forwardAnnouncements()) {
      return;
    }

    getChannel().ifPresent(channel -> {
      String text = Text.toDiscord(msg);
      webHookMessage(channel, text, true);
    });
  }

  public void forwardGuildChat(User sender, Component message) {
    getChannel().ifPresent(channel -> {
      String text = Text.toDiscord(message);
      String name = sender.getNickOrName();

      webHookMessage(channel, name + " **>** " + text, false);
    });
  }

  private void webHookMessage(TextChannel channel, String message, boolean asEmbeded) {
    WebhookUtil.deliverMessage(
        channel,
        "Steven, the Guild Commissioner",

        // Avatar URL
        getJDA().getSelfUser()
            .getEffectiveAvatarUrl(),

        // Dumb quick thing to quickly toggle between
        // embedded messages and normal text messages
        asEmbeded ? null : message,
        !asEmbeded ? null : new EmbedBuilder()
            .setTitle(message)
            .setColor(
                guild.getSettings()
                    .getPrimaryColor()
                    .getTextColor()
                    .value()
            )
            .build()
    );
  }

  /* ------------------------------- ROLES -------------------------------- */

  public Optional<Role> getRole() {
    if (roleId == NULL_ID) {
      return Optional.empty();
    }

    return Optional.ofNullable(getJDA().getRoleById(roleId));
  }

  public void deleteRole() {
    getRole().ifPresent(role -> {
      role.delete().submit();
      roleId = NULL_ID;
      lastRoleUpdate = System.currentTimeMillis();
    });
  }

  public void createRole() {
    if (getRole().isPresent()) {
      return;
    }

    getDiscordGuild()
        .createRole()
        .setName("Guild: " +  guild.getName())
        .setColor(
            guild.getSettings().hasFlags(ROLE_COLOR)
                ? guild.getSettings().getPrimaryColor().getTextColor().value()
                : null
        )
        .setMentionable(true)
        .setHoisted(true)
        .submit()
        .whenComplete((role, throwable) -> {
          if (throwable != null) {
            return;
          }

          roleId = role.getIdLong();

          // Add role to all members
          var guild = role.getGuild();
          lastRoleUpdate = System.currentTimeMillis();

          forEachDiscordMember(member -> {
            if (member.getRoles().contains(role)) {
              return;
            }

            guild.addRoleToMember(member, role)
                .submit();
          });
        });
  }

  /* ----------------------------- CHANNELS ------------------------------- */

  public Optional<TextChannel> getChannel() {
    if (channelId == 0) {
      return Optional.empty();
    }

    return Optional.ofNullable(getJDA().getTextChannelById(channelId));
  }

  public Optional<TextChannel> channelIfNotArchived() {
    return getChannel()
        .map(channel -> isArchived(channel) ? null : channel);
  }

  public static ChannelVisibility getVisibility(TextChannel channel) {
    var override = channel.getPermissionOverride(
        channel.getGuild().getPublicRole()
    );

    if (override == null) {
      return ChannelVisibility.PRIVATE;
    }

    var set = override.getAllowed();

    if (set.contains(Permission.MESSAGE_WRITE)) {
      return ChannelVisibility.FULLY_PUBLIC;
    }

    if (set.contains(Permission.VIEW_CHANNEL)) {
      return ChannelVisibility.READONLY_PUBLIC;
    }

    return ChannelVisibility.PRIVATE;
  }

  public static void setVisibility(ChannelManager manager,
                                   ChannelVisibility visibility
  ) {
    var publicRole = manager.getGuild().getPublicRole();

    manager.putPermissionOverride(
        publicRole,
        visibility.allowPermissions,
        visibility.denyPermissions
    );
  }

  public static boolean isArchived(TextChannel channel) {
    return channel.getMemberPermissionOverrides()
        .isEmpty();
  }

  public static Category guildsTextCategory() {
    return getGuildCategory().orElseGet(() -> {
      var action = DiscordSRV.getPlugin().getMainGuild()
          .createCategory("Guilds");

      // Disable public viewing of text channel
      action = action.addPermissionOverride(
          action.getGuild().getPublicRole(),
          null,

          // Set the perms given to users
          // per-channel here to be denied
          memberOverridePerms()
      );

      var cat = action.complete();
      GuildConfig.guildsChannelCategory = cat.getIdLong();

      return cat;
    });
  }

  private static Optional<Category> getGuildCategory() {
    long id = GuildConfig.guildsChannelCategory;

    if (id == 0) {
      return Optional.empty();
    }

    return Optional.ofNullable(
        getJDA().getCategoryById(id)
    );
  }

  public static EnumSet<Permission> memberOverridePerms() {
    return EnumSet.of(
        Permission.VIEW_CHANNEL,
        Permission.MESSAGE_WRITE,
        Permission.MESSAGE_READ
    );
  }

  public void deleteChannel() {
    getChannel().ifPresent(channel -> {
      channel.delete().submit();
      channelId = 0L;
    });
  }

  public void archiveChannel(final String reason) {
    channelIfNotArchived().ifPresent(channel -> {
      String suffix = Strings.isNullOrEmpty(reason)
          ? ""
          : "-" + reason.replaceAll(" ", "-");

      var manager = channel.getManager()
          .setName(guild.getName() + "-Archived" + suffix);

      forEachDiscordMember(manager::removePermissionOverride);

      webHookMessage(channel, CHANNEL_OPENING_MESSAGE, false);

      manager.submit();
      lastChannelUpdate = System.currentTimeMillis();
    });
  }

  public void unarchiveChannel() {
    getChannel().ifPresent(channel -> {
      if (!isArchived(channel)) {
        return;
      }

      lastChannelUpdate = System.currentTimeMillis();
      var mananger = channel.getManager()
          .setName(guild.getName());

      forEachDiscordMember(member -> {
        mananger.putPermissionOverride(
            member,
            memberOverridePerms(),
            null
        );
      });

      mananger.submit();
    });
  }

  public void createChannel() {
    if (getChannel().isPresent()) {
      return;
    }

    var action = guildsTextCategory()
        .createTextChannel(guild.getName());

    forEachDiscordMember(member -> {
      action.addPermissionOverride(
          member,
          memberOverridePerms(),
          null
      );
    });

    action.submit().thenAccept(channel -> {
      channelId = channel.getIdLong();
      lastChannelUpdate = System.currentTimeMillis();

      webHookMessage(channel, CHANNEL_OPENING_MESSAGE, false);
    });
  }

  /* --------------------------- SERIALIZATION ---------------------------- */

  public JsonElement serialize() {
    JsonWrapper json = JsonWrapper.create();

    if (roleId != NULL_ID) {
      json.add(KEY_ROLE, roleId);
    }

    if (lastRoleUpdate != UNSET) {
      json.addTimeStamp(KEY_ROLE_UPDATE, lastRoleUpdate);
    }

    if (channelId != NULL_ID) {
      json.add(KEY_CHANNEL, channelId);
    }

    if (lastChannelUpdate != UNSET) {
      json.addTimeStamp(KEY_CHANNEL_UPDATE, lastChannelUpdate);
    }

    if (!forwardAnnouncements) {
      json.add(KEY_ANNOUNCEMENTS, false);
    }

    return json.getSource();
  }

  public void deserialize(JsonElement element) {
    clear();

    if (element == null) {
      return;
    }

    var json = JsonWrapper.wrap(element.getAsJsonObject());

    roleId = json.getLong(KEY_ROLE, NULL_ID);
    channelId = json.getLong(KEY_CHANNEL, NULL_ID);
    lastRoleUpdate = json.getTimeStamp(KEY_ROLE_UPDATE, UNSET);
    lastChannelUpdate = json.getTimeStamp(KEY_CHANNEL_UPDATE, UNSET);
    forwardAnnouncements = json.getBool(KEY_ANNOUNCEMENTS, true);
  }
}