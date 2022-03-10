package net.forthecrown.core.battlepass;

import com.google.gson.JsonElement;
import net.forthecrown.vars.Var;
import net.forthecrown.vars.VarRegistry;
import net.forthecrown.vars.types.VarTypes;
import net.forthecrown.core.DayChangeListener;
import net.forthecrown.serializer.CrownSerializer;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.minecraft.util.Mth;

import java.util.Set;
import java.util.UUID;

public interface BattlePass extends DayChangeListener, CrownSerializer {
    Var<Integer>
        MAX_LEVEL = VarRegistry.getSafe("bp_maxLevel", VarTypes.INT, 30),
        EXP_PER_LEVEL = VarRegistry.getSafe("bp_expPerLevel", VarTypes.INT, 1000);

    Progress getProgress(UUID uuid);

    void resetCategory(Category category);
    void populateCategory(Category category);

    CurrentChallenges getChallenges(Category category);

    void clearChallenges();
    void clearCategory(Category category);

    long getNextWeeklyReset();
    long getNextMonthlyReset();

    enum Category {
        DAILY (4, false),
        WEEKLY (8, false),
        MONTHLY (3, false);

        private final Var<Integer> challengeAmount;
        private final boolean changes;

        Category(int challengeAmount, boolean changes) {
            this.challengeAmount = VarRegistry.getSafe("bp_challengeAmount_" + name().toLowerCase(), VarTypes.INT, challengeAmount);
            this.changes = changes;
        }

        public int getChallengeAmount() {
            return challengeAmount.get();
        }

        public boolean changes() {
            return changes;
        }
    }

    interface Progress extends JsonSerializable {
        UUID holder();

        int level();
        void level(int level);

        void incrementLevel();

        int exp();
        void exp(int amount);
        void addExp(int amount);

        int progress(BattlePassChallenge c);
        void progress(BattlePassChallenge c, int progress);

        default void increment(BattlePassChallenge c) {
            increment(c, 1);
        }

        default void increment(BattlePassChallenge c, int amount) {
            int newAmount = progress(c);
            newAmount += amount;

            if(newAmount >= c.getTarget()) {
                addExp(c.getExp());
            }

            progress(c, Mth.clamp(newAmount, 0, c.getTarget()));
        }

        Set<RewardInstance> availableRewards();
        Set<RewardInstance> claimedRewards();

        default Set<RewardInstance> unclaimedRewards() {
            Set<RewardInstance> rewards = availableRewards();
            rewards.removeAll(claimedRewards());

            return rewards;
        }

        void onCategoryReset(Category category);

        void resetProgress();

        JsonWrapper extraData();

        @Override
        JsonElement serialize();
    }
}
