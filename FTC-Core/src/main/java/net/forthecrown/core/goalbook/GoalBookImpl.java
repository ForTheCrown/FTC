package net.forthecrown.core.goalbook;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.*;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.math.MathUtil;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;

public class GoalBookImpl extends AbstractJsonSerializer implements GoalBook {
    private final Map<UUID, Progress> progressMap = new Object2ObjectOpenHashMap<>();
    private final Map<Category, CurrentChallenges> challengeMap = new Object2ObjectOpenHashMap<>();

    public GoalBookImpl() {
        super("battle_pass");

        Challenges.init();
        Rewards.init();

        reload();
    }

    @Override
    public void onDayChange() {
        Calendar calendar = Calendar.getInstance();
        int monthDay = calendar.get(Calendar.DAY_OF_MONTH);
        int weekDay  = calendar.get(Calendar.DAY_OF_WEEK);

        resetCategory(Category.DAILY);

        if(weekDay == 1) resetCategory(Category.WEEKLY);

        // Reset monthly challenges and all user
        // progress, so they can start over :D
        if(monthDay == 1) {
            resetCategory(Category.MONTHLY);
            progressMap.values().forEach(Progress::resetProgress);
        }
    }

    @Override
    public Progress getProgress(UUID uuid) {
        return progressMap.computeIfAbsent(uuid, ProgressImpl::new);
    }

    @Override
    public void resetCategory(Category category) {
        clearCategory(category);
        populateCategory(category);
    }

    @Override
    public void populateCategory(Category category) {
        List<GoalBookChallenge> allPossible = new ObjectArrayList<>(Registries.GOAL_BOOK.values());
        allPossible.removeIf(c -> c.getCategory() != category);

        CurrentChallenges challenges = new CurrentChallenges(category);

        challenges.addAll(
                category.changes() ?
                        FtcUtils.RANDOM.pickRandomEntries(allPossible, category.getChallengeAmount())
                        : allPossible.subList(0, Math.min(category.getChallengeAmount(), allPossible.size()))
        );
        challenges.startListeners();

        challengeMap.put(category, challenges);
    }

    @Override
    public CurrentChallenges getChallenges(Category category) {
        return challengeMap.get(category);
    }

    @Override
    public void clearChallenges() {
        for (Category c: Category.values()) {
            clearCategory(c);
        }
    }

    @Override
    public void clearCategory(Category category) {
        CurrentChallenges c = challengeMap.remove(category);
        c.stopListeners();

        for (Progress p: progressMap.values()) {
            p.onCategoryReset(category);
        }
    }

    private Calendar getZeroCalendar() {
        Calendar result = Calendar.getInstance();
        result.set(Calendar.HOUR_OF_DAY, 0);
        result.set(Calendar.MINUTE, 0);
        result.set(Calendar.MILLISECOND, 1);

        return result;
    }

    private long getNextReset(int dayField, int field) {
        Calendar calendar = getZeroCalendar();
        calendar.set(dayField, calendar.getActualMinimum(dayField));

        int unit = calendar.get(field) + 1;
        if(unit > calendar.getActualMaximum(field)) unit = calendar.getActualMinimum(field);

        calendar.set(field, unit);

        return calendar.getTimeInMillis();
    }

    @Override
    public long getNextWeeklyReset() {
        return getNextReset(Calendar.DAY_OF_WEEK, Calendar.WEEK_OF_YEAR);
    }

    @Override
    public long getNextMonthlyReset() {
        return getNextReset(Calendar.DAY_OF_MONTH, Calendar.MONTH);
    }

    @Override
    protected void save(JsonWrapper json) {
        if(!challengeMap.isEmpty()) {
            JsonWrapper challenges = JsonWrapper.empty();

            for (Map.Entry<Category, CurrentChallenges> e: challengeMap.entrySet()) {
                challenges.add(e.getKey().name().toLowerCase(), e.getValue());
            }

            json.add("current_challenges", challenges);
        }

        if(!progressMap.isEmpty()) {
            json.addList("progress", progressMap.values());
        }
    }

    @Override
    protected void reload(JsonWrapper json) {
        clearChallenges();
        progressMap.clear();

        if(json.has("current_challenges")) {
            JsonWrapper challenges = json.getWrapped("current_challenges");

            for (Map.Entry<String, JsonElement> e: challenges.entrySet()) {
                Category category = Category.valueOf(e.getKey().toUpperCase());
                CurrentChallenges current = CurrentChallenges.of(e.getValue(), category);
                current.startListeners();

                challengeMap.put(category, current);
            }
        }

        if(json.has("progress")) {
            JsonArray array = json.getArray("progress");

            for (JsonElement e: array) {
                Progress progress = ProgressImpl.of(e);
                progressMap.put(progress.holder(), progress);
            }
        }
    }


    @Override
    protected void createDefaults(JsonWrapper json) {
        for (Category c: Category.values()) {
            populateCategory(c);
        }

        save(json);
    }

    public static class ProgressImpl implements Progress {
        private final UUID uuid;

        private final Object2IntMap<GoalBookChallenge> progress = new Object2IntOpenHashMap<>();
        private final Set<Reward> claimed = new ObjectOpenHashSet<>();

        private JsonWrapper extraData;
        private int level = 1;
        private int exp;

        public ProgressImpl(UUID uuid) {
            this.uuid = uuid;
        }

        @Override
        public UUID holder() {
            return uuid;
        }

        @Override
        public int level() {
            return level;
        }

        @Override
        public void level(int level) {
            this.level = MathUtil.clamp(level, 1, MAX_LEVEL);
        }

        @Override
        public void incrementLevel() {
            if(level == MAX_LEVEL) return;
            level++;

            CrownUser user = UserManager.getUser(holder());
            if(user.isOnline()) {
                user.sendMessage(
                        Component.translatable("goalBook.levelUp", NamedTextColor.YELLOW)
                                .append(Component.newline())
                                .append(
                                        Component.translatable(
                                                "goalBook.levelUp.now",
                                                NamedTextColor.GRAY,
                                                Component.text(FtcUtils.arabicToRoman(level))
                                        )
                                )
                );

                if(level == MAX_LEVEL) {
                    user.sendMessage(
                            Component.translatable("goalBook.levelLimitReached", NamedTextColor.YELLOW, Component.text(MAX_LEVEL).color(NamedTextColor.GOLD))
                                    .append(Component.newline())
                                    .append(Component.translatable("goalBook.levelLimitReached.rhines").color(NamedTextColor.GRAY))
                    );
                }
            }
        }

        @Override
        public int exp() {
            return exp;
        }

        @Override
        public void exp(int amount) {
            this.exp = MathUtil.clamp(amount, 0, EXP_PER_LVL);
        }

        @Override
        public void addExp(int amount) {
            int total = exp + amount;

            // If the total EXP we're at is above
            // the max exp one can have
            if(total >= EXP_PER_LVL) {
                // Bring the amount down by one level's worth
                amount = total - EXP_PER_LVL;

                // If we're at or above the level limit
                if(level >= MAX_LEVEL) {

                    // If we're allowed to reward money for any extra exp earned
                    // reward rhines
                    if(ComVars.goalBookExtraExpGivesRhines()) {
                        Crown.getEconomy().add(holder(), amount);

                        CrownUser user = UserManager.getUser(holder());

                        if(user.isOnline()) {
                            user.sendMessage(
                                    Component.translatable("goalBook.extraExpRhines",
                                            NamedTextColor.YELLOW,
                                            FtcFormatter.rhines(amount)
                                                    .color(NamedTextColor.GOLD)
                                    )
                            );
                        }
                    }

                    // Stop executing, because we're over limit
                    return;
                }

                incrementLevel();
            } else {
                // We're not over limit, make exp = total
                // and stop
                this.exp = total;
                return;
            }

            // Recursively call this method
            // until the given amount is finally
            // below the level limit
            addExp(amount);
        }

        @Override
        public int progress(GoalBookChallenge c) {
            return progress.getOrDefault(c, 0);
        }

        @Override
        public void progress(GoalBookChallenge c, int progress) {
            this.progress.put(c, progress);
        }

        @Override
        public Set<Reward> availableRewards() {
            return Rewards.getForLevel(level());
        }

        @Override
        public Set<Reward> claimedRewards() {
            return claimed;
        }

        @Override
        public void onCategoryReset(Category category) {
            progress.object2IntEntrySet().removeIf(entry -> entry.getKey().getCategory() == category);
        }

        @Override
        public void resetProgress() {
            this.exp = 0;
            this.level = 1;
            this.claimed.clear();
        }

        @Override
        public JsonWrapper extraData() {
            return extraData;
        }

        @Override
        public JsonElement serialize() {
            JsonWrapper json = JsonWrapper.empty();

            json.addUUID("holder", uuid);

            json.add("exp", exp);
            json.add("level", level);

            if(!claimed.isEmpty()) json.addList("claimed", claimed);

            if(!progress.isEmpty()) {
                JsonWrapper progJson = JsonWrapper.empty();

                for (Object2IntMap.Entry<GoalBookChallenge> c: progress.object2IntEntrySet()) {
                    progJson.add(c.getKey().key().asString(), c.getIntValue());
                }

                json.add("progress", progJson);
            }

            if(!extraData.isEmpty()) {
                json.add("extra_data", extraData);
            }

            return json.getSource();
        }

        static ProgressImpl of(JsonElement element) {
            JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());
            ProgressImpl result = new ProgressImpl(json.getUUID("holder"));

            result.exp   = json.getInt("exp");
            result.level = json.getInt("level");

            result.claimed.addAll(json.getList("claimed", Rewards::read, new ArrayList<>()));

            if(json.has("progress")) {
                JsonWrapper progJson = json.getWrapped("progress");

                for (Map.Entry<String, JsonElement> e: progJson.entrySet()) {
                    Key key = Keys.parse(e.getKey());
                    GoalBookChallenge c = Registries.GOAL_BOOK.get(key);
                    int prog = e.getValue().getAsInt();

                    result.progress.put(c, prog );
                }
            }

            if(json.has("extra_data")) {
                result.extraData = json.getWrapped("extra_data");
            }

            return result;
        }
    }
}
