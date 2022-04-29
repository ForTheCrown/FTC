package net.forthecrown.core.battlepass;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;
import net.forthecrown.core.FtcVars;
import net.forthecrown.core.Keys;
import net.forthecrown.core.battlepass.challenges.BattlePassChallenge;
import net.forthecrown.core.battlepass.challenges.Challenges;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.util.Mth;

import java.util.*;

public class BattlePassImpl extends AbstractJsonSerializer implements BattlePass {
    private final Map<UUID, Progress> progressMap = new Object2ObjectOpenHashMap<>();
    private final Map<Category, CurrentChallenges> challengeMap = new Object2ObjectOpenHashMap<>();
    private final Long2ObjectMap<RewardInstance> currentRewards = new Long2ObjectOpenHashMap<>();

    public BattlePassImpl() {
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
        challengeMap.get(category).onReset();

        clearCategory(category);
        populateCategory(category);
    }

    @Override
    public void populateCategory(Category category) {
        List<BattlePassChallenge> allPossible = new ObjectArrayList<>(Registries.GOAL_BOOK.values());
        allPossible.removeIf(c -> c.getCategory() != category);

        CurrentChallenges challenges = new CurrentChallenges(category);

        challenges.addAll(
                category.changes() ?
                        FtcUtils.RANDOM.pickRandomEntries(allPossible, category.getChallengeAmount())
                        : allPossible.subList(0, Math.min(category.getChallengeAmount(), allPossible.size()))
        );
        challenges.setListening(true);

        challengeMap.put(category, challenges);
    }

    @Override
    public CurrentChallenges getChallenges(Category category) {
        return challengeMap.get(category);
    }

    @Override
    public void clearChallenges() {
        Set<Category> categories = new ObjectOpenHashSet<>(challengeMap.keySet());

        for (Category c: categories) {
            clearCategory(c);
        }
    }

    @Override
    public void clearCategory(Category category) {
        CurrentChallenges c = challengeMap.remove(category);
        c.setListening(false);

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
    public Collection<RewardInstance> getCurrentRewards() {
        return currentRewards.values();
    }

    @Override
    public RewardInstance getReward(long id) {
        return currentRewards.get(id);
    }

    @Override
    protected void save(JsonWrapper json) {
        // Challenges
        if(!challengeMap.isEmpty()) {
            JsonWrapper challenges = JsonWrapper.empty();

            for (Map.Entry<Category, CurrentChallenges> e: challengeMap.entrySet()) {
                challenges.add(e.getKey().name().toLowerCase(), e.getValue());
            }

            json.add("current_challenges", challenges);
        }

        // Rewards
        if(!FtcUtils.isNullOrEmpty(currentRewards)) {
            JsonArray arr = new JsonArray();

            for (RewardInstance r: currentRewards.values()) {
                if(r == null) continue;
                arr.add(r.serialize());
            }
        }

        // Progress
        if(!progressMap.isEmpty()) {
            json.addList("progress", progressMap.values());
        }
    }

    @Override
    protected void reload(JsonWrapper json) {
        // Challenges
        clearChallenges();
        if(json.has("current_challenges")) {
            JsonWrapper challenges = json.getWrapped("current_challenges");

            for (Map.Entry<String, JsonElement> e: challenges.entrySet()) {
                Category category = Category.valueOf(e.getKey().toUpperCase());
                CurrentChallenges current = CurrentChallenges.of(e.getValue(), category);
                current.setListening(true);

                challengeMap.put(category, current);
            }
        }

        // Rewards
        currentRewards.clear();
        if(json.has("rewards")) {
            JsonArray rewardArray = json.getArray("rewards");

            for (JsonElement e: rewardArray) {
                RewardInstance instance = RewardInstance.read(e);
                currentRewards.put(instance.id(), instance);
            }
        }

        // Progress
        progressMap.clear();
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

        currentRewards.clear();
        currentRewards.putAll(createTemplateRewards());

        save(json);
    }

    private Long2ObjectMap<RewardInstance> createTemplateRewards() {
        Long2ObjectMap<RewardInstance> result = new Long2ObjectOpenHashMap<>();

        for (int i = 0; i <= MAX_LEVEL.get(); i += 10) {

        }

        return result;
    }

    public static class ProgressImpl implements Progress {
        private final UUID uuid;

        private final Object2IntMap<BattlePassChallenge> progress = new Object2IntOpenHashMap<>();
        private final Set<RewardInstance> claimed = new ObjectOpenHashSet<>();

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
            this.level = Mth.clamp(level, 1, MAX_LEVEL.get());
        }

        @Override
        public void incrementLevel() {
            if(level >= MAX_LEVEL.get()) return;
            level++;

            CrownUser user = UserManager.getUser(holder());
            if(user.isOnline()) {
                user.sendMessage(
                        Component.translatable("battlePass.levelUp", NamedTextColor.YELLOW)
                                .append(Component.newline())
                                .append(
                                        Component.translatable(
                                                "battlePass.levelUp.now",
                                                NamedTextColor.GRAY,
                                                Component.text(FtcUtils.arabicToRoman(level))
                                        )
                                )
                );

                if(level == MAX_LEVEL.get()) {
                    user.sendMessage(
                            Component.translatable("battlePass.levelLimitReached", NamedTextColor.YELLOW, Component.text(MAX_LEVEL.get()).color(NamedTextColor.GOLD))
                                    .append(Component.newline())
                                    .append(Component.translatable("battlePass.levelLimitReached.rhines").color(NamedTextColor.GRAY))
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
            this.exp = Mth.clamp(amount, 0, EXP_PER_LEVEL.get());
        }

        private void giveExpRhines(int amount) {
            if(!FtcVars.bp_extraExpGivesRhines.get()) return;
            CrownUser user = holderUser();
            user.addBalance(amount);

            if(user.isOnline()) {
                user.sendMessage(
                        Component.translatable(
                                "battlePass.extraExpRhines",
                                NamedTextColor.YELLOW,
                                FtcFormatter.rhines(amount)
                                        .color(NamedTextColor.GOLD)
                        )
                );
            }
        }

        @Override
        public void addExp(int amount) {
            if(level >= MAX_LEVEL.get()) {
                giveExpRhines(amount);
                return;
            }

            int newTotalExp = exp() + amount;
            int newExp = newTotalExp % EXP_PER_LEVEL.get();
            int levelAdd = newTotalExp / EXP_PER_LEVEL.get();

            if((level() + levelAdd) >= MAX_LEVEL.get()) {
                int currentLevel = level();

                level(MAX_LEVEL.get());
                exp(0);

                int rhineAmount = ((MAX_LEVEL.get() - currentLevel) * EXP_PER_LEVEL.get()) + newExp;

                giveExpRhines(rhineAmount);
                return;
            }

            for (int i = 0; i < levelAdd; i++) {
                incrementLevel();
            }

            exp(newExp);
        }

        @Override
        public int progress(BattlePassChallenge c) {
            return progress.getOrDefault(c, 0);
        }

        @Override
        public void progress(BattlePassChallenge c, int progress) {
            this.progress.put(c, progress);
        }

        @Override
        public Set<RewardInstance> availableRewards() {
            return Rewards.getForLevel(level());
        }

        @Override
        public Set<RewardInstance> claimedRewards() {
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
            return extraData == null ? extraData = JsonWrapper.empty() : extraData;
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

                for (Object2IntMap.Entry<BattlePassChallenge> c: progress.object2IntEntrySet()) {
                    progJson.add(c.getKey().key().asString(), c.getIntValue());
                }

                json.add("progress", progJson);
            }

            if(extraData != null && !extraData.isEmpty()) {
                json.add("extra_data", extraData);
            }

            return json.getSource();
        }

        static ProgressImpl of(JsonElement element) {
            JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());
            ProgressImpl result = new ProgressImpl(json.getUUID("holder"));

            result.exp   = json.getInt("exp");
            result.level = json.getInt("level");

            result.claimed.addAll(json.getList("claimed", RewardInstance::read, new ArrayList<>()));

            if(json.has("progress")) {
                JsonWrapper progJson = json.getWrapped("progress");

                for (Map.Entry<String, JsonElement> e: progJson.entrySet()) {
                    Key key = Keys.parse(e.getKey());
                    BattlePassChallenge c = Registries.GOAL_BOOK.get(key);
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