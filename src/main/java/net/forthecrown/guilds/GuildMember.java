package net.forthecrown.guilds;

import static net.forthecrown.guilds.GuildRank.ID_LEADER;
import static net.forthecrown.guilds.GuildRank.ID_MEMBER;
import static net.forthecrown.guilds.GuildRank.NOT_SET;
import static net.forthecrown.guilds.Guilds.NO_EXP;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Messages;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.guilds.unlockables.Unlockable;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.io.JsonWrapper;
import org.apache.logging.log4j.Logger;

@Data
public class GuildMember {

  private static final String
      ID_KEY = "id",
      LEFT_KEY = "left",                          // True if member left
      TOTAL_EXP_EARNED_KEY = "totalExpEarned",    // The sum of all exp earned while being member
      EXP_EARNED_TODAY_KEY = "expEarnedToday",    // The sum of all exp earned this day
      EXP_AVAILABLE_KEY = "expAvailable",         // The exp available to spend to unlockables
      JOIN_TS_KEY = "joinDate",
      RANK_KEY = "rankId",
      CLAIMED_CHUNKS_KEY = "claimedChunks";

  private static final Logger LOGGER = Loggers.getLogger();

  private final UUID id;

  @Accessors(fluent = true)
  private boolean hasLeft;

  private int
      totalExpEarned = NO_EXP,
      expEarnedToday = NO_EXP,
      expAvailable = NO_EXP;

  private long joinDate;
  private int rankId = ID_MEMBER;

  private int claimedChunks = 0;

  public boolean isInGuild() {
    return !this.hasLeft;
  }

  public Guild getGuild() {
    return getUser().getGuild();
  }

  public User getUser() {
    return Users.get(id);
  }

  public boolean hasPermission(GuildPermission perm) {
    return getGuild()
        .getSettings()
        .getRank(this.rankId)
        .hasPermission(perm);
  }

  public void addExpEarned(int amount) {
    this.totalExpEarned += amount;
    this.expEarnedToday += amount;
    this.expAvailable += amount;

    var guild = getGuild();
    guild.addExp(amount);

    var rank = guild.getSettings().getRank(getRankId());

    LOGGER.debug("autoLevelUp={}, totalExpEarned={}",
        rank.getTotalExpLevelUp(),
        totalExpEarned
    );

    if (rank.getTotalExpLevelUp() == NOT_SET
        || rank.getTotalExpLevelUp() > totalExpEarned
    ) {
      return;
    }

    if (promote().isPresent()) {
      LOGGER.debug("Couldn't promote {}", getUser());
      return;
    }

    guild.announce(
        Messages.guildAutoLevelUp(
            getUser(),
            guild.getSettings().getRank(getRankId())
        )
    );
  }

  public Optional<CommandSyntaxException> promote() {
    return changeRank(true);
  }

  public Optional<CommandSyntaxException> demote() {
    return changeRank(false);
  }

  private Optional<CommandSyntaxException> changeRank(boolean promote) {
    int move = promote ? 1 : -1;
    int nextId = getRankId();
    GuildRank rank = null;

    var user = getUser();
    var guild = user.getGuild();

    // While the Next rank's ID is in the valid bounds, shift
    // the ID either up or down, depending on if we're promoting
    // or demoting
    while ((nextId += move) >= ID_MEMBER && nextId < ID_LEADER) {
      if (guild.getSettings().hasRank(nextId)) {
        rank = guild.getSettings().getRank(nextId);
        break;
      }
    }

    if (rank == null || rank.getId() == ID_LEADER) {
      return Optional.of(
          promote
              ? Exceptions.cannotPromote(user)
              : Exceptions.cannotDemote(user)
      );
    }

    setRankId(rank.getId());
    return Optional.empty();
  }

  public void resetExpEarnedToday() {
    this.expEarnedToday = 0;
  }

  public int spendExp(Unlockable unlockable, int amount) {
    Guild guild = getGuild();

    if (unlockable.isUnlocked(guild)) {
      return 0;
    }

    int toSpend = Math.min(amount, this.expAvailable);
    int leftOver = unlockable.progress(guild, toSpend);
    int actuallySpent = toSpend - leftOver;
    this.expAvailable -= actuallySpent;

    return actuallySpent;
  }

  // Get GuildMember from Json
  public static GuildMember deserialize(JsonObject obj) {
    JsonWrapper json = JsonWrapper.wrap(obj);
    UUID id = UUID.fromString(json.get(ID_KEY).getAsString());

    boolean left;
    if (json.has(LEFT_KEY)) {
      left = json.get(LEFT_KEY).getAsBoolean();
    } else {
      left = false;
    }

    int totalExpEarned = json.getInt(TOTAL_EXP_EARNED_KEY);
    int expEarnedToday = json.getInt(EXP_EARNED_TODAY_KEY);
    int expAvailable   = json.getInt(EXP_AVAILABLE_KEY);
    int claimedChunks  = json.getInt(CLAIMED_CHUNKS_KEY);

    // Jules: Use getTimeStamp()
    long joinTs = json.getTimeStamp(JOIN_TS_KEY);
    int rankId = json.getInt(RANK_KEY);

    var member = new GuildMember(id);
    member.hasLeft(left);
    member.setTotalExpEarned(totalExpEarned);
    member.setExpEarnedToday(expEarnedToday);
    member.setExpAvailable(expAvailable);
    member.setJoinDate(joinTs);
    member.setRankId(rankId);
    member.setClaimedChunks(claimedChunks);

    return member;
  }

  // Get Json from GuildMember
  public JsonObject serialize() {
    // Jules: Use JsonWrapper
    JsonWrapper result = JsonWrapper.create();

    result.add(ID_KEY, this.id.toString());

    // Jules: use addTimeStamp, this uses a formatted date
    //  instead of just a long, makes it more readable if
    //  if you're ever reading through the file itself
    result.addTimeStamp(JOIN_TS_KEY, this.joinDate);
    result.add(RANK_KEY, this.rankId);

    if (hasLeft) {
      result.add(LEFT_KEY, true);
    }

    if (totalExpEarned != NO_EXP) {
      result.add(TOTAL_EXP_EARNED_KEY, this.totalExpEarned);
    }

    if (expEarnedToday != NO_EXP) {
      result.add(EXP_EARNED_TODAY_KEY, this.expEarnedToday);
    }

    if (expAvailable != NO_EXP) {
      result.add(EXP_AVAILABLE_KEY, this.expAvailable);
    }

    if (claimedChunks > 0) {
      result.add(CLAIMED_CHUNKS_KEY, claimedChunks);
    }

    return result.getSource();
  }
}