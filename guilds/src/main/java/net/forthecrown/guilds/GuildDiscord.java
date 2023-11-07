package net.forthecrown.guilds;

import static net.forthecrown.guilds.GuildSettings.ROLE_COLOR;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.commons.io.FilenameUtils;
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
import github.scarsz.discordsrv.dependencies.jda.api.requests.restaction.ChannelAction;
import github.scarsz.discordsrv.dependencies.jda.api.utils.MarkdownSanitizer;
import github.scarsz.discordsrv.util.DiscordUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.Loggers;
import net.forthecrown.discord.FtcDiscord;
import net.forthecrown.guilds.unlockables.DiscordUnlocks;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PluginJar;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

@Getter
public class GuildDiscord {
  private static final Logger LOGGER = Loggers.getLogger();

  public static final String KEY_CHANNEL = "channelId";
  public static final String KEY_ROLE = "roleId";
  public static final String KEY_WEBHOOK = "webhookId";
  public static final String KEY_CHANNEL_UPDATE = "lastChannelUpdate";
  public static final String KEY_ROLE_UPDATE = "lastRoleUpdate";
  public static final String KEY_ANNOUNCEMENTS = "forwardAnnouncements";

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

  private static final Instant UNSET = Instant.ofEpochMilli(-1);

  public static EnumSet<MentionType> ALLOWED_MENTIONS;

  public static final String WEBHOOK_NAME = "Steven, the Guild Commissioner";

  private final Guild guild;

  private long channelId = NULL_ID;
  private long roleId = NULL_ID;
  private long webhookId = NULL_ID;

  private Instant lastChannelUpdate = UNSET;
  private Instant lastRoleUpdate = UNSET;

  @Setter
  @Accessors(fluent = true)
  private boolean forwardAnnouncements = true;

  static {
    var all = EnumSet.allOf(MentionType.class);
    all.remove(MentionType.USER);
    all.remove(MentionType.EVERYONE);
    ALLOWED_MENTIONS = all;
  }

  public GuildDiscord(Guild guild) {
    this.guild = guild;
    clear();
  }

  /* ------------------------------ METHODS ------------------------------- */

  public void clear() {
    lastChannelUpdate = UNSET;
    lastRoleUpdate = UNSET;
    channelId = NULL_ID;
    roleId = NULL_ID;
  }

  public static JDA getJda() {
    return DiscordUtil.getJda();
  }

  public static github.scarsz.discordsrv.dependencies.jda.api.entities.Guild
  getDiscordGuild() {
    return DiscordSRV.getPlugin().getMainGuild();
  }

  public void forEachDiscordMember(Consumer<Member> memberConsumer) {
    guild.getMembersList().forEach(member -> {
      try {
        FtcDiscord.getUserMember(member.getUser()).ifPresent(memberConsumer);
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

  public void sendAnnouncement(Message message) {
    if (!forwardAnnouncements()) {
      return;
    }

    channelIfNotArchived().ifPresent(channel -> {
      webHookMessage(channel, message);
    });
  }

  public void forwardGuildChat(User sender, Component message) {
    LOGGER.debug("forwardGuildChat: sender={}, message={}", sender, Text.plain(message));

    // Only forward to non archived channels
    channelIfNotArchived().ifPresent(channel -> {

      LOGGER.debug("Inside");

      String text = MarkdownSanitizer.escape(Text.toDiscord(message));
      String name = MarkdownSanitizer.escape(Text.toDiscord(sender.displayName()));

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

  private void webHookMessage(TextChannel channel, String message, boolean asEmbeded) {
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

    var config = Guilds.getConfig();

    if (config.useWebhooks()) {
      getOrCreateWebhook(channel).thenAccept(webhook -> {
        WebhookClient<Void> client = (WebhookClient<Void>) webhook;
        client.sendMessage(message).submit();
      });
    } else {
      channel.sendMessage(message).submit();
    }
  }

  /* ----------------------------- WEBHOOKS ------------------------------- */

  private CompletableFuture<Webhook> getOrCreateWebhook(TextChannel channel) {
    return getWebhook().orElseGet(() -> createWebhook(channel));
  }

  private CompletableFuture<Webhook> createWebhook(TextChannel channel) {
    var path = PluginJar.resourcePath(Guilds.getConfig().webhookAvatarPath());

    Icon icon = null;
    try {
      var input = Files.newInputStream(path);

      var extension = FilenameUtils.getExtension(
          path.getFileName().toString()
      );
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
          if (throwable != null) {
            LOGGER.error("Error creating webhook for {}", guild, throwable);
          }

          webhookId = webhook.getIdLong();
          LOGGER.debug("Created webhook for guild {}, id={}", guild, webhookId);
        });
  }

  public Optional<CompletableFuture<Webhook>> getWebhook() {
    if (webhookId == NULL_ID) {
      return Optional.empty();
    }

    return Optional.of(
        getJda().retrieveWebhookById(webhookId)
            .submit()
    );
  }

  /* ------------------------------- ROLES -------------------------------- */

  public Optional<Role> getRole() {
    if (roleId == NULL_ID) {
      return Optional.empty();
    }

    var role = getJda().getRoleById(roleId);
    return Optional.ofNullable(role);
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
          lastRoleUpdate = Instant.now();
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
        || !DiscordUnlocks.COLOR.isUnlocked(guild) // Check if donator
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
          lastRoleUpdate = Instant.now();

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
    var manager = Guilds.getManager();
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

    TextChannel channel = getJda().getTextChannelById(channelId);
    return Optional.ofNullable(channel);
  }

  public Optional<TextChannel> channelIfNotArchived() {
    return getChannel().filter(channel -> !isArchived(channel));
  }

  public static boolean isArchived(TextChannel channel) {
    return channel.getMemberPermissionOverrides().isEmpty();
  }

  private static Optional<Role> getStaffRole() {
    var discordGuild = getDiscordGuild();
    List<Role> roles = discordGuild.getRolesByName("Staff", true);

    if (roles.isEmpty()) {
      LOGGER.error(
          "Cannot add staff override for guild channel! No 'staff' role found"
      );

      return Optional.empty();
    }

    if (roles.size() > 1) {
      LOGGER.warn("More than 1 'staff' role found, selecting first");
    }

    Role role = roles.iterator().next();
    return Optional.of(role);
  }

  private static void addStaffAccess(ChannelAction<?> action) {
    getStaffRole().ifPresent(role -> {
      action.addRolePermissionOverride(
          role.getIdLong(), memberOverridePerms(), null
      );
    });
  }

  private static Category updateStaffAccess(Category category) {
    var opt = getStaffRole();

    if (opt.isEmpty()) {
      return category;
    }

    Role role = opt.get();

    var existingOverride = category.getPermissionOverride(role);

    if (existingOverride != null) {
      return category;
    }

    var override = category.createPermissionOverride(role)
        .setAllow(memberOverridePerms())
        .setDeny((Collection<Permission>) null)
        .complete();

    return (Category) override.getChannel();
  }

  private static Optional<Category> getGuildCategory() {
    long id = Guilds.getConfig().guildsChannelCategory();

    if (id == 0) {
      return Optional.empty();
    }

    var cat = getJda().getCategoryById(id);

    if (cat == null) {
      return Optional.empty();
    }

    return Optional.of(updateStaffAccess(cat));
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

    manager.reason("FTC: " + reason);

    forEachDiscordMember(manager::removePermissionOverride);

    webHookMessage(channel, CHANNEL_OPENING_MESSAGE, false);

    return manager.submit().whenComplete((unused, throwable) -> {
      if (throwable != null) {
        LOGGER.error("Error archiving channel for guild {}", guild, throwable);
        return;
      }

      lastChannelUpdate = Instant.now();
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

      lastChannelUpdate = Instant.now();
    });
  }

  public CompletableFuture<TextChannel> createChannel() {
    var opt = getChannel();
    if (opt.isPresent()) {
      return CompletableFuture.completedFuture(opt.get());
    }

    Optional<ChannelAction<TextChannel>> actionOpt = getGuildCategory()
        .map(category -> category.createTextChannel(guild.getName()));

    if (actionOpt.isEmpty()) {
      LOGGER.error("No guild channel category set in FTC-Guilds/config.toml, cannot make channel");
      LOGGER.error("If a channel is set but this error persists, the ID is invalid");

      return CompletableFuture.failedFuture(
          new RuntimeException("No guild channel category found, cannot create channel")
      );
    }

    var action = actionOpt.get();

    LOGGER.debug("Creating channel for guild {}", guild);

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

          LOGGER.debug("Created channel, channelId={}, channelId.string={}",
              channelId, channel.getId()
          );

          lastChannelUpdate = Instant.now();

          LOGGER.debug("Post create");

          webHookMessage(channel, CHANNEL_OPENING_MESSAGE, false);

          LOGGER.debug("Post webhook message");
        });
  }

  /* --------------------------- SERIALIZATION ---------------------------- */

  public JsonElement serialize() {
    JsonWrapper json = JsonWrapper.create();

    if (roleId != NULL_ID) {
      json.add(KEY_ROLE, roleId);
    }

    if (lastRoleUpdate != UNSET) {
      json.addInstant(KEY_ROLE_UPDATE, lastRoleUpdate);
    }

    if (channelId != NULL_ID) {
      json.add(KEY_CHANNEL, channelId);
    }

    if (webhookId != NULL_ID) {
      json.add(KEY_WEBHOOK, webhookId);
    }

    if (lastChannelUpdate != UNSET) {
      json.addInstant(KEY_CHANNEL_UPDATE, lastChannelUpdate);
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
    lastRoleUpdate = json.getInstant(KEY_ROLE_UPDATE, UNSET);
    lastChannelUpdate = json.getInstant(KEY_CHANNEL_UPDATE, UNSET);
    forwardAnnouncements = json.getBool(KEY_ANNOUNCEMENTS, true);
  }
}