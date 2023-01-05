package net.forthecrown.guilds;

import static net.forthecrown.guilds.GuildRank.ID_LEADER;
import static net.forthecrown.guilds.GuildSettings.UNLIMITED_MEMBERS;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.Getter;
import net.forthecrown.commands.guild.GuildInvite;
import net.forthecrown.core.Messages;
import net.forthecrown.core.admin.BannedWords;
import net.forthecrown.core.admin.EavesDropper;
import net.forthecrown.core.admin.Mute;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.guilds.unlockables.UnlockableChunkUpgrade;
import net.forthecrown.guilds.unlockables.Upgradable;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;
import net.forthecrown.waypoint.Waypoint;
import net.forthecrown.waypoint.WaypointProperties;
import net.forthecrown.waypoint.Waypoints;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class Guild
    implements ForwardingAudience,
    InventoryHolder,
    HoverEventSource<Component>
{

  // Json keys
  static final String
      TOTAL_EXP_KEY = "totalExp",
      CREATION_TS_KEY = "createdOn",
      SETTINGS_KEY = "settings",
      UNLOCKABLES_KEY = "unlockables",
      MEMBERS_KEY = "members",
      POSTS_KEY = "boardPosts",
      ACTIVE_EFFECTS_KEY = "activeEffects",
      CHEST_KEY = "chest",
      DISCORD_KEY = "discord";

  public static final short
      MAX_NAME_SIZE = 12,
      MIN_NAME_SIZE = 3;

  @Getter
  private final UUID id;

  @Getter
  private long totalExp;

  @Getter
  private final long creationTimeStamp;

  final Map<UUID, GuildMember> members = new Object2ObjectOpenHashMap<>();

  @Getter
  private final GuildSettings settings;

  @Getter
  private final GuildUnlockables unlockables = new GuildUnlockables();

  /**
   * Collective inventory shared by the guild that all members can access
   */
  @Getter
  private Inventory inventory;

  private final ObjectSet<UnlockableChunkUpgrade>
      activeEffects = new ObjectOpenHashSet<>();

  @Getter
  private final ObjectList<GuildMessage> msgBoardPosts
      = new ObjectArrayList<>();

  private final ObjectList<GuildInvite> outgoingInvites
      = new ObjectArrayList<>();

  @Getter
  private final GuildDiscord discord;

  public Guild(UUID id, long totalExp, long creationTimeStamp) {
    this.id = id;
    this.settings = new GuildSettings(this);
    this.discord = new GuildDiscord(this);

    this.totalExp = totalExp;
    this.creationTimeStamp = creationTimeStamp;

    // Guild chest
    refreshGuildChest();
  }

  /* ------------------------------ METHODS ------------------------------- */

  public void moveWaypoint(Waypoint waypoint, User user) {
    var current = getSettings().getWaypoint();

    if (current != null) {
      current.set(WaypointProperties.GUILD_OWNER, null);
      Waypoints.removeIfPossible(current);
    }

    announce(
        Messages.guildSetCenter(waypoint.getPosition(), user)
    );

    getSettings().setWaypoint(waypoint.getId());
    waypoint.set(WaypointProperties.GUILD_OWNER, getId());
  }

  public void announce(Component message) {
    sendMessage(message);
    discord.forwardAnnouncement(message);
  }

  public void chat(User user, Component message) {
    var mute = Punishments.checkMute(user);

    if (BannedWords.checkAndWarn(user.getPlayer(), message)) {
      mute = Mute.HARD;
    }

    EavesDropper.reportGuildChat(user, mute, this, message);

    if (!mute.isVisibleToOthers()) {
      return;
    }

    // Forward message to discord
    getDiscord()
        .forwardGuildChat(user, message);

    Mute finalMute = mute;
    getMembers()
        .values()
        .stream()
        .filter(member -> {
          if (finalMute == Mute.SOFT) {
            return member.getId().equals(user.getUniqueId());
          }

          var viewer = member.getUser();

          if (!viewer.isOnline()) {
            viewer.unloadIfOffline();
            return false;
          }

          return !Users.areBlocked(user, viewer);
        })

        .forEach(member -> {
          var viewer = member.getUser();
          boolean showRank = viewer.get(Properties.GUILD_RANKED_TAGS);
          var rank = getSettings().getRank(member.getRankId());

          Component displayName = Users.createListName(
              user,
              text()
                  .append(showRank ?
                      rank.getFormattedName().append(space())
                      : Component.empty()
                  )
                  .append(user.getTabName())
                  .build(),
              false
          );

          viewer.sendMessage(
              Messages.guildChat(this, displayName, message)
          );
        });
  }

  /* ------------------------------- CHEST -------------------------------- */

  public int getGuildChestSize() {
    return Upgradable.GUILD_CHEST_SIZE.currentLimit(this);
  }

  public void refreshGuildChest() {
    ItemStack[] content;

    if (inventory != null) {
      content = inventory.getContents();
    } else {
      content = null;
    }

    inventory = Bukkit.createInventory(this, getGuildChestSize(), text("Guild Chest"));

    if (content != null) {
      inventory.setContents(content);
    }
  }

  /* ------------------------------ EFFECTS ------------------------------- */

  public boolean hasActiveEffect(UnlockableChunkUpgrade effect) {
    return activeEffects.contains(effect);
  }

  public void activateEffect(UnlockableChunkUpgrade effect) {
    activeEffects.add(effect);
  }

  public void deactivateEffect(UnlockableChunkUpgrade effect) {
    activeEffects.remove(effect);
  }

  public int activeEffectCount() {
    return activeEffects.size();
  }

  public ObjectSet<UnlockableChunkUpgrade> getActiveEffects() {
    return ObjectSets.unmodifiable(activeEffects);
  }

  public ObjectSet<PotionEffectType> getActivePotionEffectTypes() {
    return activeEffects.stream()
        .map(UnlockableChunkUpgrade::getPotionEffectType)
        .filter(Objects::nonNull)
        .collect(ObjectOpenHashSet.toSet());
  }

  /* --------------------------- NAMING/DISPLAY --------------------------- */

  public String getName() {
    return this.settings.getName();
  }

  public Component displayName() {
    return text()
        .append(
            getSettings().getNameFormat()
                .apply(this)
        )

        .clickEvent(ClickEvent.runCommand("/g info " + getName()))
        .hoverEvent(this)
        .build();
  }

  public Component getPrefix() {
    return displayName().append(space());
  }

  @Override
  public void sendMessage(@NotNull Identified source, @NotNull Component message,
                          @NotNull MessageType type
  ) {
    sendMessage(source.identity(), message, type);
  }

  @Override
  public void sendMessage(@NotNull Identity source, @NotNull Component message,
                          @NotNull MessageType type
  ) {
    // Prepend guild prefix
    getOnlineMembers().forEach(user -> {
      user.sendMessage(
          source,
          text().append(getPrefix(), message).build(),
          type
      );
    });
  }

  @Override
  public void sendMessage(@NotNull SignedMessage signedMessage,
                          @NotNull ChatType.Bound boundChatType
  ) {
    sendMessage(signedMessage.identity(), signedMessage.unsignedContent(), MessageType.CHAT);
  }

  @Override
  public void sendMessage(@NotNull Component message,
                          @NotNull ChatType.Bound boundChatType
  ) {
    sendMessage(Identity.nil(), message, MessageType.CHAT);
  }

  @Override
  public @NotNull HoverEvent<Component> asHoverEvent(
      @NotNull UnaryOperator<Component> op
  ) {
    var writer = TextWriters.newWriter();
    writer.setFieldStyle(Style.style(NamedTextColor.GRAY));
    writer.write(
        getSettings()
            .getNameFormat().
            apply(this)
    );
    writer.newLine();
    writer.newLine();

    writeHover(writer);

    return writer.asComponent()
        .asHoverEvent(op);
  }

  public void writeDiscoverInfo(TextWriter writer, User viewer) {
    if (Objects.equals(viewer.getGuild(), this)) {
      writer.line("Your guild!", NamedTextColor.YELLOW);
    }

    writer.line(
        settings.isPublic()
            ? text("Guild Public", NamedTextColor.GREEN)
            : text("Guild Private", NamedTextColor.RED)
    );
    writer.newLine();
    writer.newLine();

    writeHover(writer);
  }

  public void writeHover(TextWriter writer) {
    User leader = getLeader().getUser();

    writer.field("Leader", leader.getTabName());
    writer.field("Total Exp", Text.formatNumber(getTotalExp()));
    writer.field("Created", Text.formatDate(getCreationTimeStamp()));
    writer.field("Members", Text.formatNumber(getMemberSize()));

    if (!settings.hasFlags(UNLIMITED_MEMBERS)) {
      writer.write(
          "/" + Text.NUMBER_FORMAT.format(Upgradable.MAX_MEMBERS.currentLimit(this)),
          writer.getFieldValueStyle()
      );
    }

    var waypoint = getSettings().getWaypoint();
    if (waypoint == null) {
      writer.field("Waypoint", "Not set");
    } else {
      writer.field("Waypoint", "/vr " + getName());
    }
  }

  @Override
  public @NotNull Iterable<? extends Audience> audiences() {
    return getOnlineMembers();
  }

  /* ------------------------------ MEMBERS ------------------------------- */

  public long getTotalTodayExp() {
    return this.members.values()
        .stream()
        .map(GuildMember::getExpEarnedToday)
        .reduce(0, Integer::sum);
  }

  public User getTopContributor() {
    return this.members
        .values()
        .stream()
        .max(Comparator.comparingInt(GuildMember::getTotalExpEarned))
        .orElseThrow()
        .getUser();
  }

  public void addExp(int amount) {
    if (amount < 0) {
      return;
    }

    this.totalExp += amount;

    GuildManager.get().getExpTop()
        .set(getId(), (int) totalExp);
  }

  public GuildMember getMember(UUID id) {
    return this.members.get(id);
  }

  public boolean isMember(UUID id) {
    var member = getMember(id);

    if (member == null) {
      return false;
    }

    return member.isInGuild();
  }

  public Object2ObjectMap<UUID, GuildMember> getMembers() {
    return new Object2ObjectOpenHashMap<>(
        this.members.entrySet()
            .stream()
            .filter(e -> e.getValue().isInGuild())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
    );
  }

  public List<GuildMember> getMembersList() {
    return new ObjectArrayList<>(getMembers().values());
  }

  public int getMemberSize() {
    return Math.toIntExact(this.members.entrySet().stream()
        .filter(e -> e.getValue().isInGuild())
        .count());
  }

  public ObjectSet<User> getOnlineMembers() {
    return members.values()
        .stream()
        .filter(GuildMember::isInGuild)
        .map(GuildMember::getUser)
        .filter(User::isOnline)
        .collect(ObjectOpenHashSet.toSet());
  }

  public boolean isLeader(User user) {
    GuildMember member = this.members.get(user.getUniqueId());

    if (member == null) {
      return false;
    } else {
      return member.getRankId() == ID_LEADER;
    }
  }

  public GuildMember getLeader() {
    for (GuildMember member : this.members.values()) {
      if (member.getRankId() == ID_LEADER) {
        return member;
      }
    }

    throw new IllegalStateException(
        "No leader for guild " + getName() + " or " + getId()
    );
  }

  public Component getMemberPrefix(UUID id) {
    GuildRank rank = this.settings.getRank(getMember(id).getRankId());

    if (rank.hasFormattedName()) {
      return rank.getFormattedName();
    }

    return Component.empty();
  }

  public boolean isFull() {
    return !getSettings().hasFlags(UNLIMITED_MEMBERS)
        && getMemberSize() >= Upgradable.MAX_MEMBERS.currentLimit(this);
  }

  public void rename(String name) {
    GuildManager.get().onRename(name, this);
    settings.name = name;

    discord.getRole().ifPresent(role -> {
      role.getManager()
          .setName("Guild: " + name)
          .submit();
    });

    discord.channelIfNotArchived().ifPresent(channel -> {
      channel.getManager()
          .setName(name)
          .submit();
    });
  }

  public void addMember(User user) {
    GuildMember member = members.computeIfAbsent(user.getUniqueId(), uuid -> {
      var m = new GuildMember(uuid);
      m.setJoinDate(System.currentTimeMillis());
      return m;
    });

    member.hasLeft(false);
    user.setGuild(this);

    var role = discord.getRole();
    if (role.isEmpty()) {
      return;
    }

    user.getDiscordMember().ifPresent(m -> {
      discord.channelIfNotArchived().ifPresent(channel -> {
        channel.getManager()
            .putPermissionOverride(
                m,
                GuildDiscord.memberOverridePerms(),
                null
            )
            .submit();
      });

      m.getGuild()
          .addRoleToMember(m, role.get())
          .submit();
    });
  }

  public void join(User user) {
    addMember(user);

    announce(
        Messages.guildJoinAnnouncement(user)
    );
    user.sendMessage(
        Messages.guildJoin(this)
    );

    Tasks.runLaterAsync(() -> user.sendMessage(Messages.GUILD_JOINED_HELP), 60);
  }

  public void removeMember(UUID id) {
    var member = getMember(id);

    if (member == null) {
      return;
    }

    member.hasLeft(true);
    var user = Users.get(id);
    user.setGuild(null);

    var role = discord.getRole();
    if (role.isEmpty()) {
      return;
    }

    user.getDiscordMember().ifPresent(m -> {
      m.getGuild()
          .removeRoleFromMember(m, role.get())
          .submit();

      discord.channelIfNotArchived().ifPresent(channel -> {
        channel.getManager()
            .removePermissionOverride(m)
            .submit();
      });
    });
  }

  public void addInvite(GuildInvite invite) {
    this.outgoingInvites.add(invite);
  }

  public GuildInvite getInviteFor(User user) {
    for (GuildInvite invite : this.outgoingInvites) {
      if (invite.getReceiver().equals(user)) {
        return invite;
      }
    }

    return null;
  }

  public void closeInvite(GuildInvite invite) {
    this.outgoingInvites.removeIf(i -> i.equals(invite));
  }

  public void addMsgBoardPost(GuildMessage post) {
    this.msgBoardPosts.add(0, post);
  }

  public void removeMsgBoardPost(GuildMessage post) {
    this.msgBoardPosts.remove(post);
  }

  /* --------------------------- SERIALIZATION ---------------------------- */

  // Get Guild from Json
  public static Guild deserialize(UUID id, JsonWrapper json) {
    long totalExp = json.get(TOTAL_EXP_KEY).getAsLong();
    long creationTimeStamp = json.getTimeStamp(CREATION_TS_KEY);

    // Guild members
    JsonArray membersJson = json.get(MEMBERS_KEY).getAsJsonArray();
    Object2ObjectMap<UUID, GuildMember> guildMembers = new Object2ObjectOpenHashMap<>();
    membersJson.forEach(m -> {
      GuildMember member = GuildMember.deserialize(m.getAsJsonObject());
      guildMembers.put(member.getId(), member);
    });

    // Guild message board posts
    ObjectList<GuildMessage> msgBoardPosts = new ObjectArrayList<>();
    if (json.has(POSTS_KEY)) {
      JsonArray boardJson = json.get(POSTS_KEY).getAsJsonArray();
      for (JsonElement msgJson : boardJson) {
        msgBoardPosts.add(GuildMessage.deserialize(msgJson.getAsJsonObject()));
      }
    }

    // Active chunk effects
    JsonArray effectsJson = json.get(ACTIVE_EFFECTS_KEY).getAsJsonArray();
    ObjectSet<UnlockableChunkUpgrade> activeEffects = new ObjectOpenHashSet<>();
    effectsJson.forEach(e -> {
      UnlockableChunkUpgrade effect = JsonUtils.readEnum(UnlockableChunkUpgrade.class, e);
      activeEffects.add(effect);
    });

    var g = new Guild(id, totalExp, creationTimeStamp);
    g.unlockables.deserialize(json.getObject(UNLOCKABLES_KEY));
    g.activeEffects.addAll(activeEffects);

    g.members.putAll(guildMembers);
    g.msgBoardPosts.addAll(msgBoardPosts);

    g.settings.deserialize(json.getObject(SETTINGS_KEY));
    g.discord.deserialize(json.get(DISCORD_KEY));

    // Read inventory
    g.refreshGuildChest();
    Guilds.readInventory(g.inventory, json.get(CHEST_KEY));

    return g;
  }

  // Get Json from Guild
  // Jules: Use JsonWrapper
  public void serialize(JsonWrapper result) {
    result.add(TOTAL_EXP_KEY, this.totalExp);

    // Jules: use addTimeStamp(), I wrote a reason for this in
    //   GuildMember's serialize() function
    result.addTimeStamp(CREATION_TS_KEY, this.creationTimeStamp);
    result.add(SETTINGS_KEY, this.settings.serialize());
    result.add(UNLOCKABLES_KEY, this.unlockables.serialize());

    // Guild members
    JsonArray membersJson = new JsonArray();
    this.members.values().forEach(m -> membersJson.add(m.serialize()));
    result.add(MEMBERS_KEY, membersJson);

    // Guild message board posts
    if (!this.msgBoardPosts.isEmpty()) {
      JsonArray boardJson = new JsonArray();
      this.msgBoardPosts.forEach(p -> boardJson.add(p.serialize()));
      result.add(POSTS_KEY, boardJson);
    }

    // Active effects
    JsonArray activeEffectsJson = new JsonArray();
    this.activeEffects.forEach(e -> activeEffectsJson.add(JsonUtils.writeEnum(e)));
    result.add(ACTIVE_EFFECTS_KEY, activeEffectsJson);

    JsonElement discord = this.discord.serialize();
    if (discord != null) {
      result.add(DISCORD_KEY, discord);
    }

    JsonArray chest = Guilds.writeInventory(inventory);
    if (!chest.isEmpty()) {
      result.add(CHEST_KEY, chest);
    }
  }

  @Override
  public String toString() {
    return "%s{id=%s, name=%s}".formatted(
        getClass().getSimpleName(),
        getId(),
        settings.getName()
    );
  }

  public boolean equals(Guild other) {
    if (other == null) {
      return false;
    }

    return id.equals(other.getId());
  }
}