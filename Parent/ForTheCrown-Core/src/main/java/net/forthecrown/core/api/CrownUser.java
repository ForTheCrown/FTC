package net.forthecrown.core.api;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.enums.SellAmount;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEventSource;
import net.minecraft.server.v1_16_R3.ChatMessageType;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface CrownUser extends CrownSerializer<FtcCore>, CommandSender, HoverEventSource<Component>, Nameable {

    /**
     * Saves and then unloads the file
     * <p>Removes it from the loadedUsers list and YEETs the object to the mercy of the Java Trash Collector</p>
     */
    void unload();

    /**
     * Configures the price for a material
     * <p>Credit to Wout for this method as this uses a mathematical calculation written by him</p>
     * @param item The material which will have it's price recalculated
     * @return The new price for the item
     */
    short configurePriceForItem(Material item);

    /**
     * @deprecated In favour of getUniqueId
     * @return the User's UUID
     */
    @Deprecated
    default UUID getBase(){
        return getUniqueId();
    }

    /**
     * Gets the user's UUID
     * @return The User's UUID
     */
    UUID getUniqueId();

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

    void addRank(Rank rank, boolean givePermission);

    /**
     * Removes a rank from the user
     * @param rank The rank to remove
     */
    void removeRank(Rank rank);

    void removeRank(Rank rank, boolean removePermission);

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
    void setCanSwapBranch(boolean canSwapBranch, boolean addToCooldown);

    /**
     * Gets when the branch swapping cooldown will expire in milli seconds
     * @return The system time of the next time the player is allowed to swap branches
     */
    long getNextAllowedBranchSwap();

    /**
     *
     * @return
     */
    boolean performBranchSwappingCheck();

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
    Short getItemPrice(Material item);

    /**
     * Sets the price of an item
     * <p>Note: materials can be set here that are not in the config's item prices section and it will be saved</p>
     * @param item The item to set the price of
     * @param price The new price of the item
     */
    void setItemPrice(Material item, short price);

    /**
     * Gets all the item prices of the user
     * @return The map of materials and their prices, unique to the user
     */
    Map<Material, Short> getItemPrices();

    /**
     * Sets the map of prices
     * @param itemPrices The new map of item prices
     */
    void setItemPrices(@Nonnull Map<Material, Short> itemPrices);

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
     * Sends the user a message, works just like the sendMessage in Player, but it also translates hexcodes and '&amp;' chars as color codes
     * @param message The message to send to the user
     */
    void sendMessage(@Nonnull String message);

    /**
     * Sends the user a chat message
     * @param message The NMS component to send
     */
    void sendMessage(IChatBaseComponent message);

    /**
     * No clue, appears to work the same as sendMessage(IChatBaseComponent message);
     * @param id ??????
     * @param message The NMS component to send
     */
    void sendMessage(UUID id, IChatBaseComponent message);

    /**
     * Sends the user an NMS component message
     * @param message The message to send
     * @param type The type to send, note: SYSTEM and CHAT are the same, GAME_INFO is action bar
     */
    void sendMessage(IChatBaseComponent message, ChatMessageType type);

    void sendMessage(UUID id, IChatBaseComponent message, ChatMessageType type);

    void sendAdminMessage(CrownCommandBuilder command, CommandSender sender, Component message);

    /**
     * Gets if the user is online
     * <p>online in this case means if getPlayer doesn't return null lol</p>
     * @return Whether the user is online
     */
    boolean isOnline();

    /**
     * Gets if the user is a king
     * <p>Note: whether a user is a king is not stored on a per player basis, that'd be stupid. But rather this sees if FtcCore.getKing equals getBase</p>
     * @return Whether the user is the king or queen
     */
    boolean isKing();

    void setKing(boolean king, boolean setPrefix);

    void setKing(boolean king, boolean setPrefix, boolean isFemale);

    /**
     * Sets if the user is the king
     * <p>Again, just does FtcCore.setKing and sets it to be getBase</p>
     * @param king Whether the user is to be king
     */
    void setKing(boolean king);

    /**
     * Sends an array of messages to the user
     * @param messages The messsages to send
     */
    @Override
    void sendMessage(@Nonnull String... messages);

    /**
     * Deletes the user's data
     */
    void delete();

    /**
     * Gets the user's scoreboard
     * @return The main scoreboard
     */
    Scoreboard getScoreboard();

    /**
     * Gets if the user's profile is private or public
     * @return ^^^^
     */
    boolean isProfilePublic();

    /**
     * Sets the user's profile to either public or private
     * @param publicProfile ^^^^
     */
    void setProfilePublic(boolean publicProfile);

    /**
     * Gets the object which hold data for this user
     * @return The user's data container
     */
    UserDataContainer getDataContainer();

    /**
     * Gets the user's grave
     * @return The user's grave
     */
    Grave getGrave();

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    @Override
    String toString();
}
