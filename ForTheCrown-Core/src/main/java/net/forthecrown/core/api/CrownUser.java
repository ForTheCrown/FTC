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

    List<Particle> getParticleArrowAvailable();

    void setParticleArrowAvailable(List<Particle> particleArrowAvailable);

    String getDeathParticle();

    void setDeathParticle(String particleDeathActive);

    List<String> getParticleDeathAvailable();

    void setParticleDeathAvailable(List<String> particleDeathAvailable);

    boolean allowsRidingPlayers();

    void setAllowsRidingPlayers(boolean allowsRidingPlayers);

    int getGems();

    void setGems(int gems);

    void addGems(int gems);

    boolean allowsEmotes();

    void setAllowsEmotes(boolean allowsEmotes);

    Integer getItemPrice(Material item);

    void setItemPrice(Material item, int price);

    Map<Material, Integer> getItemPrices();

    void setItemPrices(@Nonnull Map<Material, Integer> itemPrices);

    Integer getAmountEarned(Material material);

    void setAmountEarned(Material material, Integer amount);

    Map<Material, Integer> getAmountEarnedMap();

    void setAmountEarnedMap(@Nonnull Map<Material, Integer> amountSold);

    boolean isBaron();

    void setBaron(boolean baron);

    SellAmount getSellAmount();

    void setSellAmount(SellAmount sellAmount);

    long getTotalEarnings();

    void setTotalEarnings(long amount);

    void addTotalEarnings(long amount);

    long getNextResetTime();

    void setNextResetTime(long nextResetTime);

    void resetEarnings();

    String getName();

    Branch getBranch();

    void setBranch(Branch branch);

    void setTabPrefix(String s);

    void clearTabPrefix();

    void sendMessage(String message);

    boolean isOnline();

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    @Override
    String toString();
}
