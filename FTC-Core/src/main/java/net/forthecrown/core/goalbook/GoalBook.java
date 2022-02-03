package net.forthecrown.core.goalbook;

import com.google.gson.JsonElement;
import net.forthecrown.core.DayChangeListener;
import net.forthecrown.serializer.CrownSerializer;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.math.MathUtil;

import java.util.Set;
import java.util.UUID;

public interface GoalBook extends DayChangeListener, CrownSerializer {
    int MAX_LEVEL   = 30,
        EXP_PER_LVL = 1000;

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

        private final int challengeAmount;
        private final boolean changes;

        Category(int challengeAmount, boolean changes) {
            this.challengeAmount = challengeAmount;
            this.changes = changes;
        }

        public int getChallengeAmount() {
            return challengeAmount;
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

        int progress(GoalBookChallenge c);
        void progress(GoalBookChallenge c, int progress);

        default void increment(GoalBookChallenge c) {
            increment(c, 1);
        }

        default void increment(GoalBookChallenge c, int amount) {
            int newAmount = progress(c);
            newAmount += amount;

            if(newAmount >= c.getTarget()) {
                addExp(c.getExp());
            }

            progress(c, MathUtil.clamp(newAmount, 0, c.getTarget()));
        }

        Set<Reward> availableRewards();
        Set<Reward> claimedRewards();

        void onCategoryReset(Category category);

        void resetProgress();

        JsonWrapper extraData();

        @Override
        JsonElement serialize();
    }
}
