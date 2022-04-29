package net.forthecrown.economy.guilds;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.forthecrown.core.Crown;
import net.forthecrown.economy.guilds.topics.VoteData;
import net.forthecrown.economy.guilds.topics.VoteTopic;
import net.forthecrown.economy.houses.House;
import net.forthecrown.economy.houses.HouseUtil;
import net.forthecrown.economy.houses.Houses;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.TimeUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class VoteState implements JsonSerializable {
    private final TradeGuild guild;
    private final VoteData data;
    private final long started, ends;
    private UUID voteStarter;

    private final Set<UUID>
            pro     = new ObjectOpenHashSet<>(),
            against = new ObjectOpenHashSet<>();

    private final Set<House>
            hPro        = new ObjectOpenHashSet<>(),
            hAgainst    = new ObjectOpenHashSet<>();

    private BukkitTask task;

    public VoteState(TradeGuild guild, VoteData data, long started, long ends) {
        this.guild = guild;
        this.data = data;
        this.started = started;
        this.ends = ends;
    }

    public void votePro(UUID id) { userVote(pro, id); }
    public void voteAgainst(UUID id) { userVote(against, id); }

    public void votePro(House h) { houseVote(hPro, h); }
    public void voteAgainst(House h) { houseVote(hAgainst, h); }

    private void userVote(Set<UUID> tracker, UUID id) {
        tracker.add(id);
        checkVotes();
    }

    private void houseVote(Set<House> tracker, House h) {
        tracker.add(h);
        checkVotes();
    }

    private void checkVotes() {
        guild.getBallotBox().onVote(this, TradeGuild.GUILD_WORLD);
        
        // If all users and houses have voted: end voting
        if (totalUserVotes() < guild.memberCount() || totalHouseVotes() < Registries.HOUSES.size()) {
            return;
        }

        guild.endVoting(false);
    }

    public int totalUserVotes() {
        return pro.size() + against.size();
    }

    public int totalHouseVotes() {
        return hAgainst.size() + hPro.size();
    }

    public int totalProCount() {
        return pro.size() + (Houses.ENABLED ? hPro.size() : 0);
    }

    public int totalAgainstCount() {
        return against.size() + (Houses.ENABLED ? hAgainst.size() : 0);
    }

    public int totalAbstainCount() {
        return userAbstainCount() + (Houses.ENABLED ? houseAbstainCount() : 0);
    }

    public int userAbstainCount() {
        return guild.getMembers().stream()
                .filter(uuid -> !hasVoted(uuid))
                .mapToInt(value -> 1)
                .sum();
    }

    public int houseAbstainCount() {
        int result = 0;

        for (House h: Registries.HOUSES) {
            if(!hasVoted(h)) result++;
        }

        return result;
    }

    public long getEnds() {
        return ends;
    }

    public long getStarted() {
        return started;
    }

    public boolean hasVoted(UUID uuid) {
        return pro.contains(uuid) || against.contains(uuid);
    }

    public boolean hasVoted(House h) {
        return hPro.contains(h) || hAgainst.contains(h);
    }

    public UUID getVoteStarter() {
        return voteStarter;
    }

    void setVoteStarter(UUID voteStarter) {
        this.voteStarter = voteStarter;
    }

    public VoteData getData() {
        return data;
    }

    public VoteTopic<VoteData> getTopic() {
        return Registries.VOTE_TOPICS.get(getData().typeKey());
    }

    public Component display() {
        return GuildUtil.display(getData());
    }

    void removeVote(UUID uuid) {
        // Remove id from either pro or against list
        // If we removed, update ballot box
        if(pro.remove(uuid) || against.remove(uuid)) checkVotes();
    }

    void schedule() {
        cancelSchedule();

        long execute = TimeUtil.timeUntil(ends);
        if(execute <= 0) {
            guild.endVoting(false);
            return;
        }

        task = Bukkit.getScheduler().runTaskLater(Crown.inst(), () -> guild.endVoting(false), TimeUtil.millisToTicks(execute));
    }

    void cancelSchedule() {
        if(task == null || task.isCancelled()) return;
        task.cancel();
        task = null;
    }

    @Override
    public JsonElement serialize() {
        JsonWrapper json = JsonWrapper.empty();

        // Save basic details
        json.add("started", started);
        json.add("ends", ends);

        // Save topic data
        VoteTopic<VoteData> topic = (VoteTopic<VoteData>) Registries.VOTE_TOPICS.get(data.typeKey());

        JsonWrapper topicJson = JsonWrapper.empty();
        topicJson.addKey("type", topic.key());
        topicJson.add("data", topic.serialize(data));

        json.add("topic", topicJson);

        if(voteStarter != null) {
            json.addUUID("vote_starter", voteStarter);
        }

        // Save user votes
        if(pro.size() > 0 || against.size() > 0) {
            JsonWrapper userVotes = JsonWrapper.empty();

            if(!pro.isEmpty()) userVotes.addList("pro", pro, JsonUtils::writeUUID);
            if(!against.isEmpty()) userVotes.addList("against", against, JsonUtils::writeUUID);

            json.add("user_votes", userVotes);
        }

        // Save house votes
        if(hPro.size() > 0 || against.size() > 0) {
            JsonWrapper houseVotes = JsonWrapper.empty();

            if(!hPro.isEmpty()) houseVotes.addList("pro", hPro);
            if(!hAgainst.isEmpty()) houseVotes.addList("against", hAgainst);

            json.add("user_votes", houseVotes);
        }

        return json.getSource();
    }

    public static VoteState of(JsonElement element, TradeGuild guild) {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        JsonWrapper topicJson = json.getWrapped("topic");
        VoteTopic<VoteData> topic = Registries.VOTE_TOPICS.get(topicJson.getKey("type"));
        VoteData data = topic.deserialize(topicJson.get("data"));

        VoteState state = new VoteState(guild, data, json.getLong("started"), json.getLong("ends"));

        if(json.has("vote_starter")) {
            state.voteStarter = json.getUUID("vote_starter");
        }

        if(json.has("user_votes")) {
            JsonWrapper userVotes = json.getWrapped("user_votes");

            state.pro.addAll(userVotes.getList("pro", JsonUtils::readUUID, new ArrayList<>()));
            state.against.addAll(userVotes.getList("against", JsonUtils::readUUID, new ArrayList<>()));
        }

        if(json.has("house_votes")) {
            JsonWrapper houseVotes = json.getWrapped("house_votes");

            state.hPro.addAll(houseVotes.getList("pro", HouseUtil::read, new ArrayList<>()));
            state.hAgainst.addAll(houseVotes.getList("against", HouseUtil::read, new ArrayList<>()));
        }

        return state;
    }
}