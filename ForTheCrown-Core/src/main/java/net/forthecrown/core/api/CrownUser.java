package net.forthecrown.core.api;

import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.enums.SellAmount;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.command.MessageCommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface CrownUser extends CrownFileManager, MessageCommandSender {

    /**
     * Unloads the file
     * <p>Removes it from the loadedUsers list and YEETs the object to the mercy of the Java Trash Collector</p>
     */
    void unload();

    /**
     * Configures the price for a material
     * <p>Credit to Wout for this method as this uses a mathematical calculation written by him</p>
     * @param item The material which will have it's price recalculated
     * @return The new price for the item
     */
    int configurePriceForItem(Material item);

    /**
     * Gets the base of the object
     * <p>The base is the UUID that the object is connected to, and the data file for this user is also saved under the UUID</p>
     * @return The UUID base
     */
    UUID getBase();

    /**
     * Gets the player tied to this user
     * <p>Will return null if the player is not online</p>
     * @return The player with the same UUID as the user
     */
    Player getPlayer();

    /**
     * Gets the offlinePlayer tied to this user
     * @return The offlinePlayer with the same UUID as the user
     */
    OfflinePlayer getOfflinePlayer();

    /**
     * Gets all the ranks the user has
     * @return The user's ranks
     */
    Set<Rank> getAvailableRanks();

    /**
     * Sets their ranks
     * @param ranks The new rank Set
     */
    void setAvailableRanks(Set<Rank> ranks);

    /**
     * Checks if the user has a certain rank
     * @param rank The rank to check for
     * @return Whether they have it or not
     */
    boolean hasRank(Rank rank);

    /**
     * Gives the user a rank
     * @param rank The rank to give
     */
    void addRank(Rank rank);

    /**
     * Removes a rank from the user
     * @param rank The rank to remove
     */
    void removeRank(Rank rank);

    /**
     * Gets the user's currently active rank
     * @return The user's active rank
     */
    Rank getRank();

    /**
     * Sets the user's active rank
     * @param rank The user's new rank
     */
    void setRank(Rank rank);

    /**
     * Sets the user's active rank
     * @param rank The user's new rank
     * @param setPrefix Whether the user's TabPrefix should be updated
     */
    void setRank(Rank rank, boolean setPrefix);

    /**
     * Gets if the user is allowed to swap branches
     * @return Whether the user is allowed to swap branches
     */
    boolean getCanSwapBranch();

    /**
     * Sets if the user is allowed to swap branches
     * @param canSwapBranch Whether the user is allowed to swap branches
     */
    void setCanSwapBranch(boolean canSwapBranch);

    /**
     * Gets a list of all pets belonging to the user
     * <p>The strings are arbitrary, don't try to guess them :(</p>
     * @return The list of pets belonging to the user
     */
    List<String> getPets();

    /**
     * Sets the list of pets belonging to the user
     * @param pets The new list of pets
     */
    void setPets(List<String> pets);

    /**
     * Gets the user's currently active arrow particle
     * @return The user's active arrow particle
     */
    Particle getArrowParticle();

    /**
     * Sets the user's active arrow particle
     * @param particleArrowActive The new arrow particle
     */
    void setArrowParticle(Particle particleArrowActive);

    /**
     * Gets all arrow particles available to the user
     * @return All the user's arrow particles
     */
    List<Particle> getParticleArrowAvailable();

    /**
     * Sets the particles the player has access to
     * @param particleArrowAvailable The new list
     */
    void setParticleArrowAvailable(List<Particle> particleArrowAvailable);

    /**
     * Gets the effect / particle that will appear when the user dies
     * <p>It's stored as a string because DeathParticle has both effects and particles in it, and I couldn't be bothered splitting them up</p>
     * @return The user's death particle
     */
    String getDeathParticle();

    /**
     * Sets the user's death particle
     * @param particleDeathActive The new death particle
     */
    void setDeathParticle(String particleDeathActive);

    /**
     * Gets all the user's available death particles
     * @return The user's death particles
     */
    List<String> getParticleDeathAvailable();

    /**
     * Sets the user's available death particles
     * @param particleDeathAvailable The new list of death particles
     */
    void setParticleDeathAvailable(List<String> particleDeathAvailable);

    /**
     * Gets if the user allows player riding
     * @return Whether the user can be ride and be ridden or not
     */
    boolean allowsRidingPlayers();

    /**
     * Sets if the player can ride and be ridden ;)
     * @param allowsRidingPlayers
     */
    void setAllowsRidingPlayers(boolean allowsRidingPlayers);

    /**
     * Gets the user's gem amount
     * @return The amount of gems held by the user
     */
    int getGems();

    /**
     * Sets the amount of gems a player has
     * @param gems The new amount of gems
     */
    void setGems(int gems);

    /**
     * Adds to the user's gem amount
     * @param gems The amount of gems to add
     */
    void addGems(int gems);

    /**
     * Gets if the user allows emotes
     * @return Whether the user can send and receive emotes or not
     */
    boolean allowsEmotes();

    /**
     * Sets whether the user allows emotes or not
     * @param allowsEmotes kinda obvious lol
     */
    void setAllowsEmotes(boolean allowsEmotes);

    /**
     * Gets the price of an item
     * <p>Will always return a value if the item is an item contained in the config's item prices section, even if the player doesn't have a set price for it</p>
     * @param item The item to get the price of
     * @return The price of the item
     */
    Integer getItemPrice(Material item);

    /**
     * Sets the price of an item
     * <p>Note: materials can be set here that are not in the config's item prices section and it will be saved</p>
     * @param item The item to set the price of
     * @param price The new price of the item
     */
    void setItemPrice(Material item, int price);

    /**
     * Gets all the item prices of the user
     * @return The map of materials and their prices, unique to the user
     */
    Map<Material, Integer> getItemPrices();

    /**
     * Sets the map of prices
     * @param itemPrices The new map of item prices
     */
    void setItemPrices(@Nonnull Map<Material, Integer> itemPrices);

    /**
     * Gets the amount the player has earned from an item
     * @param material The material to get the value for
     * @return The amount the user has earned from that item
     */
    Integer getAmountEarned(Material material);

    /**
     * Sets the amount the user has earned from a material
     * <p>Note: materials can be set here that are not in the config's item prices section and it will be saved</p>
     * @param material The material to set the price of
     * @param amount
     */
    void setAmountEarned(Material material, Integer amount);

    /**
     * Gets the map of shop earnings
     * @return The map of items and the amounts earned from those items
     */
    Map<Material, Integer> getAmountEarnedMap();

    /**
     * Sets the amount earned map
     * @param amountSold The new Map of earnings
     */
    void setAmountEarnedMap(@Nonnull Map<Material, Integer> amountSold);

    /**
     * Gets if the player is a baron
     * <p>Note: Why does this exist???? we have <code>hasRank(Rank rank)</code></p>
     * @return Whether the user is a baron or not
     */
    boolean isBaron();

    /**
     * Sets if the user is a baron or not
     * @param baron Whether the user is a baron or not
     */
    void setBaron(boolean baron);

    /**
     * Gets the user's sell amount in /shop
     * @return The user's sell amount
     */
    SellAmount getSellAmount();

    /**
     * Sets the user's sell amount
     * @param sellAmount The new sell amount
     */
    void setSellAmount(SellAmount sellAmount);

    /**
     * Gets the total earnings of the player, aka shop earnings + all other earnings lol
     * @return The total amount of Rhines earned by the user
     */
    long getTotalEarnings();

    /**
     * Sets the total amount earned by the use
     * @param amount The new total earned amount
     */
    void setTotalEarnings(long amount);

    /**
     * Adds to the total amount of Rhines earned by the player
     * @param amount The amount to add
     */
    void addTotalEarnings(long amount);

    /**
     * Gets the time when the user's earnings and shop prices will be reset next
     * <p>Note: By default this interval between resets is 2 months, this is changeable in the config</p>
     * @return The next user earnings reset time
     */
    long getNextResetTime();

    /**
     * Sets the next time the user's earnings will be reset
     * @param nextResetTime The user's next reset time
     */
    void setNextResetTime(long nextResetTime);

    /**
     * Resets all the earnings of the user, including shop earnings and all other earnings
     * <p>I don't know why this is a public method but alright lol</p>
     * <p>Can't be arsed to change it</p>
     */
    void resetEarnings();

    /**
     * Gets the user's name
     * @return The user's IRL name xD (player name)
     */
    String getName();

    /**
     * Gets the user's branch
     * @return The current branch of the player
     */
    Branch getBranch();

    /**
     * Sets the user's branch
     * @param branch The new branch of the user
     */
    void setBranch(Branch branch);

    /**
     * Sets the user's TabPrefix, keep in mind, you do need a space at the end
     * @param s The new prefix
     */
    void setTabPrefix(String s);

    /**
     * Clears the user's TabPrefix
     */
    void clearTabPrefix();

    /**
     * Sends the user a message, works just like the sendMessage in Player, but it also translates hexcodes and & chars as color codes
     * @param message The message to send to the user
     */
    void sendMessage(String message);

    /**
     * Gets if the user is online
     * <p>online in this case means if getPlayer doesn't return null lol</p>
     * @return Whether the user is online
     */
    boolean isOnline();

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    @Override
    String toString();
}