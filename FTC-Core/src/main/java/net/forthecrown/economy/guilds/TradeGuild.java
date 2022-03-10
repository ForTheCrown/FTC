package net.forthecrown.economy.guilds;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.forthecrown.core.FtcVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.DayChangeListener;
import net.forthecrown.core.Worlds;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.core.chat.PagedDisplay;
import net.forthecrown.economy.guilds.topics.VoteData;
import net.forthecrown.economy.houses.House;
import net.forthecrown.economy.houses.HouseUtil;
import net.forthecrown.economy.houses.Houses;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.UserMarketData;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.TimeUtil;
import net.forthecrown.utils.math.Vector3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.Validate;
import org.bukkit.World;
import org.bukkit.block.BlockFace;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TradeGuild extends AbstractJsonSerializer implements DayChangeListener {
    public static final World GUILD_WORLD = Worlds.OVERWORLD;
    private static final int HISTORY_PAGE_SIZE = 10;

    private VoteState currentState;
    private BallotBox ballotBox;

    private final List<VoteHistoryEntry> history = new ObjectArrayList<>();
    private final List<DelayedVoteTask> tasks = new ObjectArrayList<>();
    private final Set<UUID> members = new ObjectOpenHashSet<>();

    private long nextAllowedVote;
    private int daysSinceWagePay;

    public TradeGuild() {
        super("guild");

        // Initialize ballot box to null
        ballotBox = new BallotBox(Vector3i.ZERO, Vector3i.ZERO, BlockFace.values()[0]);

        reload();
        GuildMaster.init();

        checkVoteShouldContinue();

        Crown.logger().info("Trade guild loaded");
    }

    @Override
    protected void save(JsonWrapper json) {
        if(!members.isEmpty()) json.addList("members", members, JsonUtils::writeUUID);
        if(!tasks.isEmpty()) json.addList("delayed_tasks", tasks);

        json.add("ballot_box", ballotBox);
        json.add("next_allowed_vote", nextAllowedVote);

        if(isVoteOngoing()) {
            json.add("vote_state", currentState);
        }

        if(!history.isEmpty()) {
            json.addList("history", history);
        }
    }

    @Override
    protected void reload(JsonWrapper json) {
        cancelSchedules();
        tasks.clear();
        members.clear();
        history.clear();

        this.nextAllowedVote = json.getLong("next_allowed_vote");

        if(json.has("ballot_box")) setBallotBox(BallotBox.of(json.get("ballot_box")));
        if(json.has("members")) members.addAll(json.getList("members", JsonUtils::readUUID));
        if(json.has("delayed_tasks")) tasks.addAll(json.getList("tasks", DelayedVoteTask::of));

        if(json.has("vote_state")) {
            currentState = VoteState.of(json.get("vote_state"), this);
        }

        if(json.has("history")) {
            history.addAll(json.getList("history", VoteHistoryEntry::ofJson));
        }

        schedule();
    }

    private void schedule() {
        if(isVoteOngoing()) {
            currentState.schedule();
        }

        for (DelayedVoteTask t: tasks) {
            t.schedule();
        }
    }

    private void cancelSchedules() {
        if(isVoteOngoing()) currentState.cancelSchedule();

        for (DelayedVoteTask t: tasks) {
            t.unSchedule();
        }
    }

    public VoteState getCurrentState() {
        return currentState;
    }

    public long getNextAllowedVote() {
        return nextAllowedVote;
    }

    public void createVote(VoteData data, @Nullable UUID voteStarter) throws IllegalStateException {
        Validate.isTrue(!isVoteOngoing(), "There is already an ongoing vote");

        currentState = new VoteState(this, data, System.currentTimeMillis(), System.currentTimeMillis() + FtcVars.voteTime.get());
        currentState.setVoteStarter(voteStarter);

        if(Houses.ENABLED) {
            HouseUtil.randomizeVoteTimes(currentState);
        }

        getBallotBox().onVoteStart(currentState, data, GUILD_WORLD);

        // Inform all members of vote starting
        Component starterDisplay = voteStarter == null ? Component.text("Server") : UserManager.getUser(voteStarter).displayName();
        Component message = Component.translatable("guilds.voteStart",
                NamedTextColor.GRAY,
                starterDisplay.color(NamedTextColor.YELLOW),
                GuildUtil.display(data).color(NamedTextColor.GOLD)
        );

        forEachMember(user -> user.sendAndMail(message));
    }

    public BallotBox getBallotBox() {
        return ballotBox;
    }

    public void setBallotBox(BallotBox ballotBox) {
        this.ballotBox = Validate.notNull(ballotBox, "ballotBox was null");

        if(isVoteOngoing()) {
            ballotBox.onVoteStart(currentState, currentState.getData(), GUILD_WORLD);
            ballotBox.onVote(currentState, GUILD_WORLD);
        } else {
            ballotBox.onVoteEnd(GUILD_WORLD);
        }
    }

    public void endVoting(boolean instant) throws NullPointerException {
        Validate.notNull(currentState, "No active vote");

        if(!instant) {
            VoteCount result = countVotes();
            nextAllowedVote = System.currentTimeMillis() + FtcVars.voteInterval.get();

            DelayedVoteTask task = currentState.getTopic().onEnd(currentState.getData(), result);
            if(task != null) tasks.add(task);

            history.add(0, VoteHistoryEntry.ofFinished(currentState, result));

            // Tell all members what happened
            Component message = Component.translatable("guilds.voteResult",
                    NamedTextColor.GRAY,

                    GuildUtil.display(currentState.getData())
                            .color(NamedTextColor.YELLOW),

                    result.getResult().asComponent()
                            .color(NamedTextColor.GOLD),

                    result.shortDisplay()
            ).append(GuildUtil.extraVictoryText(currentState.getData(), currentState.getTopic(), result));

            forEachMember(user -> user.sendAndMail(message));
        } else {
            history.add(0, VoteHistoryEntry.ofUnfinished(currentState));

            // Tell everyone why it ended
            Component message = Component.translatable("guilds.voteResult.instant",
                    NamedTextColor.GRAY,
                    GuildUtil.display(currentState.getData())
                            .color(NamedTextColor.YELLOW)
            );

            forEachMember(user -> user.sendAndMail(message));
        }

        getBallotBox().onVoteEnd(GUILD_WORLD);
        currentState = null;
        save();
    }

    public VoteCount countVotes() throws NullPointerException {
        Validate.notNull(currentState, "There is currently no vote");

        int pro = currentState.totalProCount();
        int against = currentState.totalAgainstCount();
        int abstentions = currentState.totalAbstainCount();

        return new VoteCount(pro, against, abstentions);
    }

    public boolean isVoteOngoing() {
        return currentState != null;
    }

    public boolean canStartVote() {
        if(isVoteOngoing()) return false;
        return TimeUtil.isPast(getNextAllowedVote());
    }

    public int memberCount() {
        return members.size();
    }

    public void addMember(UUID uuid) {
        members.add(uuid);

        if(isVoteOngoing()) {
            getBallotBox().onVote(getCurrentState(), GUILD_WORLD);
        }
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);

        if(isVoteOngoing()) {
            currentState.removeVote(uuid);
        }
    }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    public void checkVoteShouldContinue() {
        if(!isVoteOngoing()) return;
        if(!getCurrentState().getTopic().shouldContinueVote(this, Crown.getMarkets(), getCurrentState().getData())) {
            endVoting(true);
        }
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public void forEachMember(Consumer<CrownUser> consumer) {
        for (UUID id: members) {
            CrownUser user = UserManager.getUser(id);

            consumer.accept(user);
            user.unloadIfOffline();
        }
    }

    public Component displayName() {
        return Component.translatable("guilds.displayName")
                .hoverEvent(Component.translatable("guilds.displayName.hover"));
    }

    public Component displayHistory(int page) {
        final Component brdr = Component.text("                                  ")
                .color(NamedTextColor.GRAY)
                .decorate(TextDecoration.STRIKETHROUGH);

        return PagedDisplay.create(page, HISTORY_PAGE_SIZE, history,
                (val, index) -> Component.text()
                        .append(Component.text(index + ") ").color(NamedTextColor.YELLOW))
                        .append(val.asComponent())
                        .build(),

                // Header
                () -> Component.text()
                        .append(brdr)
                        .append(Component.space())
                        .append(Component.translatable("guilds.history.header"))
                        .append(Component.space())
                        .append(brdr)
                        .build(),

                // Footer
                (currentPage, lastPage, firstPage, maxPage) -> {
                    Component prev = firstPage ? Component.space() : Component.text(" < ")
                            .clickEvent(ClickEvent.runCommand("/votehistory " + (currentPage - 1)))
                            .color(NamedTextColor.YELLOW)
                            .decorate(TextDecoration.BOLD);

                    Component next = lastPage ? Component.space() : Component.text(" > ")
                            .clickEvent(ClickEvent.runCommand("/votehistory " + (currentPage + 1)))
                            .color(NamedTextColor.YELLOW)
                            .decorate(TextDecoration.BOLD);

                    return Component.text()
                            .append(brdr)
                            .append(prev)
                            .append(Component.text(currentPage + "/" + maxPage))
                            .append(next)
                            .append(brdr)
                            .build();
                }
        );
    }

    public List<VoteHistoryEntry> getHistory() {
        return history;
    }

    @Override
    public void onDayChange() {
        daysSinceWagePay++;

        if(daysSinceWagePay > FtcVars.guildPayIntervalDays.get()) {
            Crown.logger().info("Been " + FtcVars.guildPayIntervalDays.get() + " day(s) since last guild wage payout, paying");
            payMembers();
        }
    }

    private void payMembers() {
        daysSinceWagePay = 0;

        int base = FtcVars.guildBaseWage.get();
        float mod = FtcVars.guildWageModifier.get();

        forEachMember(user -> {
            UserMarketData data = user.getMarketData();
            long memberTime = System.currentTimeMillis() - data.getGuildJoinDate();
            memberTime = (long) (TimeUnit.MILLISECONDS.toDays(memberTime) / 3.5);

            float userMod = memberTime * mod;
            int amount = (int) (base + (base * userMod));

            Crown.logger().info("Paying " + amount + " to " + user.getName());
            user.addBalance(amount);

            user.sendOrMail(
                    Component.translatable("economy.pay.receiver",
                            NamedTextColor.GRAY,
                            FtcFormatter.rhines(amount).color(NamedTextColor.GOLD),
                            displayName().color(NamedTextColor.YELLOW),
                            Component.text(".")
                    )
            );
        });

        if(Houses.ENABLED) {
            for (House h: Registries.HOUSES) {
                h.addBalance(base);
            }
        }
    }
}
