package net.forthecrown.guilds;

import com.google.gson.JsonObject;
import lombok.Data;
import lombok.experimental.Accessors;
import net.forthecrown.guilds.unlockables.Unlockable;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.io.JsonWrapper;

import java.util.UUID;

import static net.forthecrown.guilds.GuildRank.ID_MEMBER;
import static net.forthecrown.guilds.Guilds.NO_EXP;

@Data
public class GuildMember {

    private static final String
            ID_KEY = "id",
            LEFT_KEY = "left",                          // True if member left
            TOTAL_EXP_EARNED_KEY = "totalExpEarned",    // The sum of all exp earned while being member
            EXP_EARNED_TODAY_KEY = "expEarnedToday",    // The sum of all exp earned this day
            EXP_AVAILABLE_KEY = "expAvailable",         // The exp available to spend to unlockables
            JOIN_TS_KEY = "joinDate",
            RANK_KEY = "rankId";

    private final UUID id;

    @Accessors(fluent = true)
    private boolean hasLeft;

    private int
            totalExpEarned = NO_EXP,
            expEarnedToday = NO_EXP,
            expAvailable = NO_EXP;

    private long joinDate;
    private int rankId = ID_MEMBER;

    public boolean isInGuild() {
        return !this.hasLeft;
    }

    public Guild getGuild() {
        return Users.get(this.id).getGuild();
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
        getGuild().addExp(amount);
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
        int expAvailable = json.getInt(EXP_AVAILABLE_KEY);

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

        return result.getSource();
    }
}