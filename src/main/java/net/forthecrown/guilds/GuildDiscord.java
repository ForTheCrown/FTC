package net.forthecrown.guilds;

import static net.forthecrown.guilds.GuildSettings.ROLE_COLOR;
import static net.forthecrown.user.data.UserTimeTracker.UNSET;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import github.scarsz.discordsrv.dependencies.jda.api.MessageBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.Permission;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Category;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild.BoostTier;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Icon;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Icon.IconType;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message.MentionType;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Webhook;
import github.scarsz.discordsrv.dependencies.jda.api.entities.WebhookClient;
import github.scarsz.discordsrv.dependencies.jda.api.managers.ChannelManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.core.FTC;
import net.forthecrown.guilds.unlockables.UnlockableRoleColor;
import net.forthecrown.user.User;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.io.FtcJar;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Logger;

@Getter
public class GuildDiscord {
  private static final Logger LOGGER = FTC.getLogger();

  public static final String
      KEY_CHANNEL = "channelId",
      KEY_ROLE = "roleId",
      KEY_WEBHOOK = "webhookId",
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

  public static EnumSet<MentionType> ALLOWED_MENTIONS = Util.make(() -> {
    var all = EnumSet.allOf(MentionType.class);
    all.remove(MentionType.USER);
    all.remove(MentionType.EVERYONE);
    return all;
  });

  public static final String WEBHOOK_NAME = "Steven, the Guild Commissioner";

  private final Guild guild;

  private long channelId = NULL_ID;
  private long roleId = NULL_ID;
  private long webhookId = NULL_ID;

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

    // Only forward to non archived channels
    channelIfNotArchived().ifPresent(channel -> {
      String text = Text.toDiscord(msg);
      webHookMessage(channel, text, true);
    });
  }

  public void forwardGuildChat(User sender, Component message) {
    // Only forward to non archived channels
    channelIfNotArchived().ifPresent(channel -> {
      String text = Text.toDiscord(message);
      String name = Text.toDiscord(sender.getTabName());

      MessageBuilder builder = new MessageBuilder();

//      Silent mentioning doesn't exist in discord :(
//      https://support.discord.com/hc/en-us/community/posts/360039210411--Suggestion-Mentioning-without-pinging
//      It's been asked for doe lol
//
//      This only works on PC, on phones the mentioned user shows up as
//      'invalid-user' Not good enough. This would've been great for guild chat
//      so the name in the message would actually be up-to-date
//
//      var discordId = sender.getDiscordId();
//      if (!Strings.isNullOrEmpty(discordId)) {
//        Member member = getDiscordGuild().getMemberById(discordId);
//
//        builder.setAllowedMentions(ALLOWED_MENTIONS)
//            .append(member);
//      } else {
        builder.append(name);
//      }

      builder.append(" **>** ").append(text);

      webHookMessage(channel, builder.build());
    });
  }

  private void webHookMessage(TextChannel channel,
                              String message,
                              boolean asEmbeded
  ) {
    MessageBuilder builder = new MessageBuilder();

    if (asEmbeded) {
      EmbedBuilder embedBuilder = new EmbedBuilder();
      embedBuilder.setDescription(message);
      builder.setEmbeds(embedBuilder.build());
    } else {
      builder.setContent(message);
    }

    webHookMessage(channel, builder.build());
  }

  @SuppressWarnings("unchecked")
  private void webHookMessage(TextChannel channel, Message message) {
    Objects.requireNonNull(message, "Message null");

    if (channel == null) {
      channel = getChannel().orElseThrow();
    }

    getOrCreateWebhook(channel)
        .thenAccept(webhook -> {
          WebhookClient<Void> client = (WebhookClient<Void>) webhook;
          client.sendMessage(message)
              .submit()
              .exceptionally(throwable -> {
                return null;
              });
        });
  }

  /* ----------------------------- WEBHOOKS ------------------------------- */

  private CompletableFuture<Webhook> getOrCreateWebhook(TextChannel channel) {
    return getWebhook().orElseGet(() -> createWebhook(channel));
  }

  private CompletableFuture<Webhook> createWebhook(TextChannel channel) {
    var path = FtcJar.resourcePath(GuildConfig.webhookAvatarPath);

    Icon icon = null;
    try {
      var input = Files.newInputStream(path);

      var extension =  FilenameUtils.getExtension(path.getFileName().toString());
      icon = Icon.from(input, IconType.fromExtension(extension));
    } catch (IOException exc) {
      LOGGER.error("Couldn't read icon from path {}",
          path.toString(), exc
      );
    }

    return channel.createWebhook(WEBHOOK_NAME)
        .setAvatar(icon)
        .reason("FTC: Guild webhook")
        .submit()
        .whenComplete((webhook, throwable) -> {
          if (throwable == null) {
            return;
          }

          LOGGER.error("Error creating webhook for {}", guild, throwable);
        });
  }

  public Optional<CompletableFuture<Webhook>> getWebhook() {
    if (webhookId == NULL_ID || getChannel().isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(
        getJDA().retrieveWebhookById(webhookId)
            .submit()
    );
  }

  /* ------------------------------- ROLES -------------------------------- */

  public Optional<Role> getRole() {
    if (roleId == NULL_ID) {
      return Optional.empty();
    }

    return Optional.ofNullable(getJDA().getRoleById(roleId));
  }

  public CompletableFuture<Void> deleteRole() {
    var role = getRole().orElseThrow();

    return role.delete()
        .submit()
        .whenComplete((unused, throwable) -> {
          if (throwable != null) {
            LOGGER.error("Couldn't delete role of guild {}", guild, throwable);
            return;
          }

          roleId = NULL_ID;
          lastRoleUpdate = System.currentTimeMillis();
        });
  }

  public CompletableFuture<Role> createRole() {
    if (getRole().isPresent()) {
      return CompletableFuture.failedFuture(new NullPointerException());
    }

    Icon icon = null;
    try {
      icon = getIcon();
    } catch (IOException exc) {
      LOGGER.error("Couldn't get icon for {}", guild.getId(), exc);
    }

    var discordGuild = getDiscordGuild();
    var boost = discordGuild.getBoostTier();

    if (boost == BoostTier.NONE
        || boost == BoostTier.TIER_1
        || UnlockableRoleColor.COLOR.isUnlocked(guild) // Check if donator
    ) {
      icon = null;
    }

    return discordGuild
        .createRole()
        .setName("Guild: " +  guild.getName())
        .setColor(
            guild.getSettings().hasFlags(ROLE_COLOR)
                ? guild.getSettings().getPrimaryColor().getTextColor().value()
                : null
        )
        .setMentionable(true)
        .setHoisted(true)
        .setIcon(icon)
        .reason("FTC: Guild role")
        .submit()
        .whenComplete((role, throwable) -> {
          if (throwable != null) {
            LOGGER.error("Error creating role for {}",
                guild.getId(), throwable
            );

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

  public Icon getIcon() throws IOException {
    var manager = GuildManager.get();
    var storage = manager.getStorage();

    var image = manager.getRenderer()
        .renderSquare(guild.getSettings().getBanner());

    storage.saveIcon(guild, image);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "png", outputStream);

    byte[] bytes = outputStream.toByteArray();
    return Icon.from(bytes, IconType.PNG);
  }

  /* ----------------------------- CHANNELS ------------------------------- */

  public Optional<TextChannel> getChannel() {
    if (channelId == 0) {
      return Optional.empty();
    }

    return Optional.ofNullable(getJDA().getTextChannelById(channelId));
  }

  public Optional<TextChannel> channelIfNotArchived() {
    return getChannel().filter(channel -> !isArchived(channel));
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
    ).submit();
  }

  public static boolean isArchived(TextChannel channel) {
    return channel.getMemberPermissionOverrides().isEmpty();
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

      var cat = action.reason("FTC: Create guild category").complete();
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

  public CompletableFuture<Void> archiveChannel(final String reason) {
    var channel = channelIfNotArchived().orElseThrow();
    String suffix = Strings.isNullOrEmpty(reason)
        ? ""
        : "-" + reason.replaceAll(" ", "-");

    var manager = channel.getManager()
        .setName(guild.getName() + "-Archived" + suffix);

    forEachDiscordMember(manager::removePermissionOverride);

    webHookMessage(channel, CHANNEL_OPENING_MESSAGE, false);

    return manager.submit().whenComplete((unused, throwable) -> {
      if (throwable != null) {
        LOGGER.error("Error archiving channel for guild {}", guild, throwable);
        return;
      }

      lastChannelUpdate = System.currentTimeMillis();
    });
  }

  public CompletableFuture<Void> unarchiveChannel() {
    var channel = getChannel().orElseThrow();
    if (!isArchived(channel)) {
      return CompletableFuture.completedFuture(null);
    }

    var mananger = channel.getManager()
        .setName(guild.getName());

    forEachDiscordMember(member -> {
      mananger.putPermissionOverride(
          member,
          memberOverridePerms(),
          null
      );
    });

    return mananger.submit().whenComplete((unused, throwable) -> {
      if (throwable != null) {
        LOGGER.error("Error archiving channel for guild {}", guild, throwable);
        return;
      }

      lastChannelUpdate = System.currentTimeMillis();
    });
  }

  public CompletableFuture<TextChannel> createChannel() {
    var opt = getChannel();
    if (opt.isPresent()) {
      return CompletableFuture.completedFuture(opt.get());
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

    return action.reason("FTC: Create channel for guild")
        .submit()
        .whenComplete((channel, throwable) -> {
          if (throwable != null) {
            LOGGER.error(
                "Couldn't create channel for guild {}", guild, throwable
            );

            return;
          }

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

    if (webhookId != NULL_ID) {
      json.add(KEY_WEBHOOK, webhookId);
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
    webhookId = json.getLong(KEY_WEBHOOK, NULL_ID);
    lastRoleUpdate = json.getTimeStamp(KEY_ROLE_UPDATE, UNSET);
    lastChannelUpdate = json.getTimeStamp(KEY_CHANNEL_UPDATE, UNSET);
    forwardAnnouncements = json.getBool(KEY_ANNOUNCEMENTS, true);
  }
}