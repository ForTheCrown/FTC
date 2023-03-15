/**
 * The Challenge system for FTC
 * <p>
 * Firstly, there are 2 types of challenges, normal challenges and item
 * challenges. They share similarities, such as being loaded from {@code .toml}
 * files by the plugin.
 * <p>
 * Both types of challenges do share some common elements, those being
 * displayNames; used to display the challenge's name with the hover text, a
 * {@link net.forthecrown.core.challenge.ResetInterval}, which determines the
 * length of time between each time the challenge is reset, activated or
 * deactivated. And finally, a {@link net.forthecrown.core.challenge.StreakCategory}
 * that determines what streak category the challenge contributes to. In
 * reality, only the {@link net.forthecrown.core.challenge.StreakCategory#ITEMS}
 * matters. At least at present.
 *
 * <h2>Normal Challenges</h2>
 * Normal challenges are loaded from the {@code challenges.toml} file in the
 * challenge directory, specified by {@link net.forthecrown.core.challenge.ChallengeDataStorage#getDirectory()}.
 * <p>
 * These challenges are script-based, meaning the events that trigger these
 * challenges are listened to by scripts specified in the {@code .toml} file
 * they are loaded from.
 * <p>
 * For specifics on the schema, see {@link net.forthecrown.core.challenge.ChallengeParser}.
 * And for more info on normal challenges and challenge scripting, see {@link net.forthecrown.core.challenge.JsonChallenge}
 *
 * <h2>Item Challenges</h2>
 * Item challenges require players to attain items and then hand them in via the
 * {@code /shop} menu.
 * <p>
 * Item challenges get their items from set chests in the real world. Items are
 * gotten from these chests and then stored in NBT files. These NBT files are
 * accessed by {@link net.forthecrown.core.challenge.ChallengeItemContainer}
 *
 * <h2>Player entries</h2>
 * Per-Player entries are stored in
 * {@link net.forthecrown.core.challenge.ChallengeEntry}, which records the
 * progress and streak of each player.
 * <p>
 * Player entries also store and manage the {@link net.forthecrown.core.challenge.Streak}
 * of a player. Streaks can be accessed with {@link net.forthecrown.core.challenge.ChallengeEntry#getStreak(net.forthecrown.core.challenge.StreakCategory)}.
 *
 *
 *
 * @see net.forthecrown.core.challenge.ChallengeParser
 * Normal challenge schema details
 *
 * @see net.forthecrown.core.challenge.ItemChallengeParser
 * Item challenge schema details
 *
 * @see net.forthecrown.core.challenge.ChallengeItemContainer
 * Details on how item challenges handle items
 */
package net.forthecrown.core.challenge;