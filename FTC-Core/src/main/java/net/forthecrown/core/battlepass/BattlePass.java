package net.forthecrown.core.battlepass;

import com.google.gson.JsonElement;
import net.forthecrown.core.Crown;
import net.forthecrown.core.DayChangeListener;
import net.forthecrown.core.battlepass.challenges.BattlePassChallenge;
import net.forthecrown.serializer.CrownSerializer;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.vars.Var;
import net.forthecrown.vars.VarRegistry;
import net.forthecrown.vars.types.VarTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.util.Mth;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The battle pass, a set of challenges and rewards to keep players busy, literally the most
 * basic form of game design right here: do task -> get reward
 */
public interface BattlePass extends DayChangeListener, CrownSerializer {
    boolean ENABLED = Crown.inDebugMode();

    Var<Integer>
        MAX_LEVEL = VarRegistry.def("bp_maxLevel", VarTypes.INT, 50),
        EXP_PER_LEVEL = VarRegistry.def("bp_expPerLevel", VarTypes.INT, 1000);

    /**
     * Gets the progress of a player
     * @param uuid the player's UUID
     * @return The player's progress
     */
    Progress getProgress(UUID uuid);

    /**
     * Resets the category.
     * It empties the category, unregistering all listeners,
     * then it re populates the category with new challenges
     * @param category The category to reset
     */
    void resetCategory(Category category);

    /**
     * Populates the category with challenges
     * and enables their listeners
     * @param category The category to populate
     */
    void populateCategory(Category category);

    /**
     * Gets the current challenges of a category
     * @param category The category
     * @return The challenges active in that category
     */
    CurrentChallenges getChallenges(Category category);

    /**
     * Clears all challenges
     */
    void clearChallenges();

    /**
     * Clears all challenges in a given category
     * @param category The category to clear
     */
    void clearCategory(Category category);

    /**
     * Gets the timestamp of the next weekly
     * reset
     * @return The next weekly challenges reset timestamp
     */
    long getNextWeeklyReset();

    /**
     * Gets the timestamp of the next monthly
     * challenges reset
     * @return The next monthly challenge reset
     */
    long getNextMonthlyReset();

    /**
     * Gets all the current rewards
     * @return All current rewards, null, if there are no rewards :(
     */
    Collection<RewardInstance> getCurrentRewards();

    /**
     * Gets a reward by its instance ID
     * @param id the ID to use
     * @return The reward instance by the given ID, or null, if no reward by the ID exists
     */
    RewardInstance getReward(long id);

    /**
     * Gets a stream of all current rewards
     * @return A reward stream
     */
    default Stream<RewardInstance> rewardStream() {
        return getCurrentRewards().stream();
    }

    /**
     * Gets all rewards for the given level
     * @param level The level to get rewards for
     * @return All rewards at that level
     */
    default Set<RewardInstance> forLevel(int level) {
        return rewardStream()
                .filter(instance -> instance.level() == level)
                .collect(Collectors.toSet());
    }

    /**
     * Gets all rewards for and below the given level
     * @param level The level to get the rewards for
     * @return All rewards for the given level and below
     */
    default Set<RewardInstance> forLevelAndBelow(int level) {
        return rewardStream()
                .filter(instance -> instance.level() <= level)
                .collect(Collectors.toSet());
    }

    /**
     * The category a challenge can have
     */
    enum Category {
        DAILY (4, false),
        WEEKLY (8, false),
        MONTHLY (3, false);

        private final Var<Integer> challengeAmount;
        private final boolean changes;

        Category(int challengeAmount, boolean changes) {
            this.challengeAmount = VarRegistry.def("bp_challengeAmount_" + name().toLowerCase(), VarTypes.INT, challengeAmount);
            this.changes = changes;
        }

        /**
         * Gets the amount of challenges the category should have at minimum
         * @return The challenge amount this category requires
         */
        public int getChallengeAmount() {
            return challengeAmount.get();
        }

        /**
         * Checks if this category should randomize the challenges
         * it has everytime it is reset
         * @return Just read the text above
         */
        public boolean changes() {
            return changes;
        }
    }

    interface Progress extends JsonSerializable {
        /**
         * Gets the holder of this progress
         * @return The holder player's UUID
         */
        UUID holder();

        default CrownUser holderUser() {
            return UserManager.getUser(holder());
        }

        /**
         * Gets the level of the progress
         * @return The progress level
         */
        int level();

        /**
         * Sets the progress level
         * @param level The new level
         */
        void level(int level);

        /**
         * Increments the level of this progress
         */
        void incrementLevel();

        /**
         * Gets the total amount of exp this progress
         * has
         * @return The total EXP of this progress
         */
        default int totalExp() {
            return (level() * EXP_PER_LEVEL.get()) + exp();
        }

        /**
         * Gets the EXP the user currently has
         * @return
         */
        int exp();

        /**
         * Sets the amount of exp this progress has.
         * <p></p>
         * Note: This will get clamped to {@link BattlePass#EXP_PER_LEVEL}, if you
         * want this to add levels, use {@link Progress#addExp(int)}
         * @param amount The EXP amount
         */
        void exp(int amount);

        /**
         * Adds EXP to this progress,
         * <p></p>
         * Note: This will increment the level if given amount + the current exp
         * is over the exp limit
         * @param amount The amount to add
         */
        void addExp(int amount);

        /**
         * Gets the progress with the given challenge
         * @param c The challenge
         * @return The progress in that challenge
         */
        int progress(BattlePassChallenge c);

        /**
         * Sets the progress with the given challenge
         * @param c The challenge
         * @param progress the new progress
         */
        void progress(BattlePassChallenge c, int progress);

        /**
         * Increments the progress with the given challenge by 1
         * @param c The challenge to increment
         */
        default void increment(BattlePassChallenge c) {
            increment(c, 1);
        }

        /**
         * Increments the progress of the given challenge by the given amount
         * @param c The challenge to increment the progress of
         * @param amount The amount to increase it by
         */
        default void increment(BattlePassChallenge c, int amount) {
            int newAmount = progress(c);

            // If we already finished this challenge
            if(newAmount >= c.getTarget()) return;

            newAmount += amount;

            // If we beat the challenge
            if(newAmount >= c.getTarget()) {
                holderUser().sendMessage(
                        Component.translatable("battlePass.beatChallenge",
                                NamedTextColor.GRAY,
                                c.displayName().color(NamedTextColor.YELLOW)
                        )
                );

                addExp(c.getExp());
            }

            progress(c, Mth.clamp(newAmount, 0, c.getTarget()));
        }

        /**
         * Gets all available rewards to this progress
         * <p></p>
         * Note: This returns both claimed and unclaimed rewards
         * this progress has, if you want to get the claimed
         * rewards use {@link Progress#claimedRewards()}, or
         * {@link Progress#unclaimedRewards()} for the unclaimed
         * rewards
         *
         * @return All available rewards available
         */
        Set<RewardInstance> availableRewards();

        /**
         * Gets all the rewards this progress has claimed
         * @return All claimed rewards
         */
        Set<RewardInstance> claimedRewards();

        /**
         * Gets all the unclaimed, but still available rewards this
         * progress has
         * @return All unclaimed, yet still available, rewards
         */
        default Set<RewardInstance> unclaimedRewards() {
            Set<RewardInstance> rewards = availableRewards();
            rewards.removeAll(claimedRewards());

            return rewards;
        }

        /**
         * NOT API, called when the category is reset
         * @param category The category being reset
         */
        void onCategoryReset(Category category);

        /**
         * Resets all progress
         */
        void resetProgress();

        /**
         * Gets any extra data being stored in this progress
         * @return The Progress' extra data
         */
        JsonWrapper extraData();

        /**
         * Serializes this Progress into JSON
         * @return The serialized progress
         */
        @Override
        JsonElement serialize();
    }
}