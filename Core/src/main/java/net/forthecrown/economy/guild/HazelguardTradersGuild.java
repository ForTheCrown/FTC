package net.forthecrown.economy.guild;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.economy.guild.topics.PostVoteTask;
import net.forthecrown.economy.guild.topics.VoteTopic;
import net.forthecrown.economy.guild.topics.VoteTopicType;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.TimeUtil;
import net.forthecrown.core.Worlds;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

import java.util.Date;
import java.util.UUID;

public class HazelguardTradersGuild extends AbstractJsonSerializer implements TradersGuild {

    private final World world = Worlds.OVERWORLD;
    private final ObjectList<UUID> members = new ObjectArrayList<>();

    private VoteState currentState;
    private BukkitTask stateEndTask;

    private long lastVoteEnd;

    private final ObjectList<PostVoteTask> tasks = new ObjectArrayList<>();

    public HazelguardTradersGuild() {
        super("traders_guild");

        reload();
        scheduleTasks();

        Crown.logger().info("Trader's guild loaded");
    }

    private void scheduleTasks() {
        if(!tasks.isEmpty()) {
            ObjectListIterator<PostVoteTask> iterator = tasks.iterator();

            while (iterator.hasNext()) {
                PostVoteTask t = iterator.next();

                boolean remove = scheduleDelayedTask(t);
                if(remove) iterator.remove();
            }
        }

        scheduleVoteFinish();
    }

    private boolean scheduleDelayedTask(PostVoteTask t) {
        long executeAt = t.executionDate.getTime() - System.currentTimeMillis();
        if(executeAt <= 0) {
            VoteTopicType type = Registries.VOTE_TOPICS.get(t.topicType);
            type.runTask(t);

            return true;
        }

        Bukkit.getScheduler().runTaskLater(Crown.inst(), () -> {
            VoteTopicType type = Registries.VOTE_TOPICS.get(t.topicType);
            type.runTask(t);
            tasks.remove(t);

        }, TimeUtil.millisToTicks(executeAt));

        return false;
    }

    private void scheduleVoteFinish() {
        if (!isCurrentlyVoting()) return;

        VoteState state = getVoteState();
        long runIn = (state.started.getTime() + ComVars.getVoteTime()) - System.currentTimeMillis();

        if(runIn <= 0) {
            finishVoting();
            return;
        }

        stateEndTask = Bukkit.getScheduler().runTaskLater(Crown.inst(), this::finishVoting, TimeUtil.millisToTicks(runIn));
    }

    @Override
    public VoteState getVoteState() {
        return currentState;
    }

    @Override
    public long getLastVoteEnd() {
        return lastVoteEnd;
    }

    @Override
    public boolean canStartVote() {
        Date now = new Date();
        Date nextValid = new Date(lastVoteEnd + ComVars.getVoteInterval());

        return now.after(nextValid);
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public void createVote(VoteTopic topic) {
        currentState = new VoteState(this);

        getVoteBox().createBallotBox(getWorld(), topic);

        currentState.started = new Date();
        currentState.topic = topic;

        scheduleVoteFinish();
        topic.onVotingBegin();
    }

    @Override
    public void finishVoting() {
        VoteResult result = currentState.countVotes();
        getVoteBox().remove(getWorld());

        lastVoteEnd = System.currentTimeMillis();

        PostVoteTask task = currentState.getTopic().onVoteEnd(result);

        if(task != null) {
            tasks.add(task);
            scheduleDelayedTask(task);
        }

        if(stateEndTask != null && !stateEndTask.isCancelled()) {
            stateEndTask.cancel();
        }

        for (UUID id: getMembers()) {
            CrownUser user = UserManager.getUser(id);

            user.sendOrMail(
                    Component.translatable("guilds.voteResult",
                            NamedTextColor.GRAY,
                            currentState.topic.signDisplay(),
                            result
                    )
            );
        }

        currentState = null;
        stateEndTask = null;
    }

    @Override
    public ObjectList<UUID> getMembers() {
        return members;
    }

    @Override
    public void addMember(UUID member) {
        members.add(member);
    }

    @Override
    public void removeMember(UUID member) {
        members.remove(member);
    }

    @Override
    public int getMaxMembers() {
        return ComVars.getMaxGuildMembers();
    }

    @Override
    public VoteBox getVoteBox() {
        return Guilds.VOTE_BOX;
    }

    @Override
    protected void save(JsonWrapper json) {
        json.addList("members", members, JsonUtils::writeUUID);
        if(isCurrentlyVoting()) json.add("currentVote", currentState);

        if(!tasks.isEmpty()) json.addList("tasks", tasks);
    }

    @Override
    protected void reload(JsonWrapper json) {
        members.clear();
        if(json.has("members")) members.addAll(json.getList("members", JsonUtils::readUUID));

        if(!json.missingOrNull("currentVote")) currentState.deserialize(json.get("currentVote"));
        else {
            currentState = null;
            getVoteBox().remove(getWorld());
            if(stateEndTask != null && stateEndTask.isCancelled()) stateEndTask.cancel();
        }

        tasks.clear();
        if(json.has("tasks")) {
            tasks.addAll(json.getList("tasks", PostVoteTask::fromJson));
        }
    }
}
