package net.forthecrown.guilds;

import static net.forthecrown.guilds.GuildRank.ID_LEADER;
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
import java.util.stream.Collectors;
import lombok.Getter;
import net.forthecrown.commands.guild.GuildInvite;
import net.forthecrown.core.Messages;
import net.forthecrown.guilds.unlockables.UnlockableChunkUpgrade;
import net.forthecrown.guilds.unlockables.Upgradable;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.text.Text;
import net.forthecrown.waypoint.Waypoint;
import net.forthecrown.waypoint.WaypointProperties;
import net.forthecrown.waypoint.Waypoints;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class Guild implements ForwardingAudience, InventoryHolder {

  // Json keys
  static final String
      TOTAL_EXP_KEY = "totalExp",
      CREATION_TS_KEY = "createdOn",
      SETTINGS_KEY = "settings",
      UNLOCKABLES_KEY = "unlockables",
      MEMBERS_KEY = "members",
      POSTS_KEY = "boardPosts",
      ACTIVE_EFFECTS_KEY = "activeEffects",
      CHEST_KEY = "chest";

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
  private final ObjectList<GuildMessage>
      msgBoardPosts = new ObjectArrayList<>();

  private final ObjectList<GuildInvite>
      outgoingInvites = new ObjectArrayList<>();

  public Guild(UUID id, long totalExp, long creationTimeStamp) {
    this.id = id;
    this.settings = new GuildSettings(this);
    this.totalExp = totalExp;
    this.creationTimeStamp = creationTimeStamp;

    // Guild chest
    refreshGuildChest();
  }

  public Component getFormattedCreationDate() {
    return Text.formatDate(creationTimeStamp);
  }

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

  public void moveWaypoint(Waypoint waypoint, User user) {
    var current = getSettings().getWaypoint();

    if (current != null) {
      current.set(WaypointProperties.GUILD_OWNER, null);
      Waypoints.removeIfPossible(current);
    }

    sendMessage(
        Messages.guildSetCenter(waypoint.getPosition(), user)
    );

    getSettings().setWaypoint(waypoint.getId());
    waypoint.set(WaypointProperties.GUILD_OWNER, getId());
  }

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
        .build();
  }

  public Component getPrefix() {
    return displayName().append(space());
  }

  @Override
  public @NotNull Iterable<? extends Audience> audiences() {
    return getOnlineMembers();
  }

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
    return getMemberSize() >= Upgradable.MAX_MEMBERS.currentLimit(this);
  }

  public void rename(String name) {
    GuildManager.get().onRename(name, this);
    settings.name = name;
  }

  public void addMember(User user) {
    GuildMember member = members.computeIfAbsent(user.getUniqueId(), uuid -> {
      var m = new GuildMember(uuid);
      m.setJoinDate(System.currentTimeMillis());
      return m;
    });

    member.hasLeft(false);
    user.setGuild(this);
  }

  public void join(User user) {
    addMember(user);

    sendMessage(
        Messages.guildJoinAnnouncement(user)
    );
    user.sendMessage(
        Messages.guildJoin(this)
    );
  }

  public void removeMember(UUID id) {
    var member = getMember(id);

    if (member == null) {
      return;
    }

    member.hasLeft(true);
    Users.get(id).setGuild(null);
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

    JsonArray chest = Guilds.writeInventory(inventory);
    if (!chest.isEmpty()) {
      result.add(CHEST_KEY, chest);
    }
  }

  @Override
  public String toString() {
    return "Guild{" +
        "id=" + id +
        ", totalExp=" + totalExp +
        ", members=" + members.keySet() +
        ", outgoingInvites=" + outgoingInvites +
        '}';
  }

  public boolean equals(Guild other) {
    if (other == null) {
      return false;
    }

    return id.equals(other.getId());
  }

}