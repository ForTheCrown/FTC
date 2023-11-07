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
import net.forthecrown.discord.FtcDiscord;
import net.forthecrown.guilds.commands.GuildInvite;
import net.forthecrown.guilds.unlockables.UnlockableChunkUpgrade;
import net.forthecrown.guilds.unlockables.Upgradable;
import net.forthecrown.guilds.waypoints.GuildWaypoints;
import net.forthecrown.leaderboards.Leaderboards;
import net.forthecrown.text.channel.ChannelledMessage;
import net.forthecrown.text.PlayerMessage;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextWriter;
import net.forthecrown.text.TextWriters;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.name.DisplayIntent;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.Waypoints;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
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

@SuppressWarnings("deprecation")
public class Guild implements InventoryHolder, HoverEventSource<Component> {

  // Json keys
  static final String TOTAL_EXP_KEY = "totalExp";
  static final String CREATION_TS_KEY = "createdOn";
  static final String SETTINGS_KEY = "settings";
  static final String UNLOCKABLES_KEY = "unlockables";
  static final String MEMBERS_KEY = "members";
  static final String POSTS_KEY = "boardPosts";
  static final String ACTIVE_EFFECTS_KEY = "activeEffects";
  static final String CHEST_KEY = "chest";
  static final String DISCORD_KEY = "discord";

  public static final short MAX_NAME_SIZE = 12;
  public static final short MIN_NAME_SIZE = 3;

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

  private final ObjectSet<UnlockableChunkUpgrade> activeEffects = new ObjectOpenHashSet<>();

  @Getter
  private final ObjectList<GuildMessage> msgBoardPosts = new ObjectArrayList<>();

  private final ObjectList<GuildInvite> outgoingInvites = new ObjectArrayList<>();

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
      current.set(GuildWaypoints.GUILD_OWNER, null);
      Waypoints.removeIfPossible(current);
    }

    announce(GuildMessages.guildSetCenter(waypoint.getPosition(), user));

    getSettings().setWaypoint(waypoint.getId());
    waypoint.set(GuildWaypoints.GUILD_OWNER, getId());
  }

  public void announce(Component message) {
    sendMessage(message);
    discord.forwardAnnouncement(message);
  }

  public void chat(User user, PlayerMessage message) {
    GuildChat.send(this, user, message);
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

  public void sendMessage(ComponentLike like) {
    ChannelledMessage.create(viewer -> Text.valueOf(like, viewer))
        .setChannelName("guild_forwarding/" + id)
        .setAnnouncement(true)
        .addTargets(getOnlineMembers())
        .setRenderer((viewer, baseMessage) -> {
          return Component.textOfChildren(getPrefix(), baseMessage);
        })
        .send();
  }

  @Override
  public @NotNull HoverEvent<Component> asHoverEvent(
      @NotNull UnaryOperator<Component> op
  ) {
    var writer = TextWriters.newWriter();
    writer.setFieldStyle(Style.style(NamedTextColor.GRAY));
    writer.write(getSettings().getNameFormat().apply(this));
    writer.newLine();
    writer.newLine();

    writeHover(writer);

    return writer.asComponent()
        .asHoverEvent(op);
  }

  public void writeDiscoverInfo(TextWriter writer, User viewer) {
    if (Objects.equals(Guilds.getGuild(viewer), this)) {
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

    writer.field("Leader", leader.displayName(writer.viewer(), DisplayIntent.HOVER_TEXT));
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
    setExp(this.totalExp + amount);
  }

  public void setExp(long amount) {
    this.totalExp = amount;

    Guilds.getManager()
        .getExpTop()
        .set(getId(), (int) totalExp);

    Leaderboards.updateWithSource("guilds/exp");
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
    Guilds.getManager().onRename(name, this);
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
      var m = new GuildMember(uuid, this);
      m.setJoinDate(System.currentTimeMillis());
      return m;
    });

    member.hasLeft(false);
    user.set(GUserProperties.GUILD, getId());

    var role = discord.getRole();
    if (role.isEmpty()) {
      return;
    }

    FtcDiscord.getUserMember(user).ifPresent(m -> {
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

    Leaderboards.updateWithSource("guilds/members");
  }

  public void join(User user) {
    addMember(user);

    announce(GuildMessages.guildJoinAnnouncement(user));
    user.sendMessage(GuildMessages.guildJoin(this));

    Tasks.runLaterAsync(() -> {
      user.sendMessage(GuildMessages.GUILD_JOINED_HELP);

      if (discord.channelIfNotArchived().isPresent()) {
        FtcDiscord.getUserMember(user).ifPresentOrElse(member -> {
          user.sendMessage(
              Text.format("You've been added to &f{0}&r's discord channel",
                  NamedTextColor.GRAY,
                  displayName()
              )
          );
        }, () -> {
          user.sendMessage(
              Text.format(
                  "Link your discord account with '&e/discord link&r' to "
                      + "join the guild's private Discord channel",
                  NamedTextColor.GRAY
              )
          );
        });

      }
    }, 60);
  }

  public void removeMember(UUID id) {
    var member = getMember(id);

    if (member == null) {
      return;
    }

    member.hasLeft(true);
    var user = Users.get(id);
    user.set(GUserProperties.GUILD, null);

    var role = discord.getRole();
    if (role.isEmpty()) {
      return;
    }

    FtcDiscord.getUserMember(user).ifPresent(m -> {
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

    var g = new Guild(id, totalExp, creationTimeStamp);

    // Guild members
    JsonArray membersJson = json.get(MEMBERS_KEY).getAsJsonArray();
    Object2ObjectMap<UUID, GuildMember> guildMembers = new Object2ObjectOpenHashMap<>();
    membersJson.forEach(m -> {
      GuildMember member = GuildMember.deserialize(m.getAsJsonObject(), g);
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