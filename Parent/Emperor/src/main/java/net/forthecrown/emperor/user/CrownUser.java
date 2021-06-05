package net.forthecrown.emperor.user;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.user.data.SoldMaterialData;
import net.forthecrown.emperor.utils.Nameable;
import net.forthecrown.emperor.serializer.CrownSerializer;
import net.forthecrown.emperor.serializer.Deleteable;
import net.forthecrown.emperor.user.enums.Branch;
import net.forthecrown.emperor.user.enums.CrownGameMode;
import net.forthecrown.emperor.user.enums.Pet;
import net.forthecrown.emperor.user.enums.Rank;
import net.forthecrown.emperor.user.enums.SellAmount;
import net.forthecrown.emperor.user.data.UserTeleport;
import net.forthecrown.emperor.utils.ChatFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.minecraft.server.v1_16_R3.ChatMessageType;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

/**
 * Represents a user's profile, all their ranks, effects and such
 */
public interface CrownUser extends CrownSerializer<CrownCore>, CommandSender, HoverEventSource<Component>, Nameable, Deleteable {

    /**
     * Saves and then unloads the file
     * <p>Removes it from the loadedUsers list and YEETs the object to the mercy of the Java Trash Collector</p>
     */
    void unload();

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
    List<Pet> getPets();

    /**
     * Sets the list of pets belonging to the user
     * @param pets The new list of pets
     */
    void setPets(List<Pet> pets);

    void onJoin();

    void onJoinLater();

    /**
     * Checks if the user has the specified pet
     * @param pet The pet to look for
     * @return It says above lol
     */
    boolean hasPet(Pet pet);

    /**
     * Adds a pet
     * @param pet Pet
     */
    void addPet(Pet pet);

    /**
     * Removes a pet
     * @param pet Pet
     */
    void removePet(Pet pet);

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

    SoldMaterialData getMatData(Material material);

    boolean hasMatData(Material material);

    void setMatData(SoldMaterialData data);

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

    /**
     * Gets the location of the user, or the last known location if they're not online. Will return null if no location was found
     * @return The user's location, or last known location
     */
    Location getLocation();

    /**
     * Gets the world the user is in, or the last known world the player was in
     * @return User's last known world
     */
    World getWorld();

    /**
     * NOT API, executes the code to make sure everything that's needed to be saved is
     */
    void onLeave();

    default void unloadIfNotOnline(){
        if(!isOnline()) unload();
    }

    default ClickEvent asClickEvent(){
        return ClickEvent.runCommand("/" + (isOnline() ? "tell " + getName() + " " : "profile " + getName()));
    }

    default Component displayName(){
        return Component.text(getName())
                .hoverEvent(this)
                .clickEvent(asClickEvent());
    }

    default Component nickDisplayName(){
        return Component.text(getNickOrName())
                .hoverEvent(this)
                .clickEvent(asClickEvent());
    }

    default Component  coloredNickDisplayName(){
        return nickDisplayName().color(ChatFormatter.getUserColor(this));
    }

    default boolean hasNickname(){
        return getNickname() != null;
    }

    default String getNickOrName(){
        return hasNickname() ? getNickname() : getName();
    }

    default Rank getHighestTierRank(){
        Rank highest = null;

        for (Rank r: getAvailableRanks()){
            if(highest == null){
                highest = r;
                continue;
            }

            if(r.getTier().isHigherTierThan(highest.getTier())) highest = r;
        }

        return highest;
    }

    /**
     * Gets the last teleport of the player, null if no teleport exists or if it ended
     * @return The last teleport
     */
    UserTeleport getLastTeleport();

    UserTeleport createTeleport(Supplier<Location> destination, boolean tell, UserTeleport.Type type);

    UserTeleport createTeleport(Supplier<Location> destination, boolean tell, boolean bypassCooldown, UserTeleport.Type type);

    boolean isTeleporting();

    boolean canTeleport();

    void onTpComplete();

    boolean checkTeleporting();

    Location getLastLocation();

    void setLastLocation(Location lastLocation);

    boolean allowsTPA();

    void setAllowsTPA(boolean allowsTPA);

    boolean isEavesDropping();

    void setEavesDropping(boolean listeningToSocialSpy);

    CommandSource getLastMessage();

    void setLastMessage(CommandSource lastMessage);

    void setNickname(String nick);

    void setNickname(Component component);

    void updateDisplayName();

    String getNickname();

    Component nickname();

    boolean isVanished();

    void setVanished(boolean vanished);

    boolean isAfk();

    void setAfk(boolean afk);

    void updateAfk();

    void updateVanished();

    boolean isFlying();

    void setFlying(boolean flying);

    void updateFlying();

    boolean godMode();

    void setGodMode(boolean godMode);

    void updateGodMode();

    UserInteractions getInteractions();

    UserHomes getHomes();

    CrownGameMode getGameMode();

    void setGameMode(CrownGameMode gameMode);

    boolean allowsPaying();

    void setAllowsPay(boolean acceptsPay);

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    @Override
    String toString();
}
