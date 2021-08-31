package net.forthecrown.user;

import com.sk89q.worldedit.math.BlockVector2;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.economy.selling.UserSellResult;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.AbstractCommand;
import net.forthecrown.regions.RegionPos;
import net.forthecrown.serializer.Deletable;
import net.forthecrown.user.data.SoldMaterialData;
import net.forthecrown.user.data.UserPref;
import net.forthecrown.user.data.UserTeleport;
import net.forthecrown.user.enums.*;
import net.forthecrown.utils.Nameable;
import net.forthecrown.utils.math.ImmutableVector3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.minecraft.core.Position;
import net.minecraft.network.chat.ChatType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Represents a user's profile, all their ranks, effects and such
 */
public interface CrownUser extends
        CommandSender, Nameable,
        HoverEventSource<Component>,
        Deletable
{

    /**
     * Reloads the user's data
     */
    void reload();

    /**
     * Saves the user's data
     */
    void save();

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
     * Creates a command source with the given command
     * @param command The command to create with
     * @return The command source for this user
     */
    CommandSource getCommandSource(AbstractCommand command);

    /**
     * creates a command source with no command
     * @return The user's command source
     */
    default CommandSource getCommandSource() {
        return getCommandSource(null);
    }

    /**
     * Gets the player tied to this user
     * <p>Will return null if the player is not online</p>
     * @return The player with the same UUID as the user
     */
    Player getPlayer() throws UserNotOnlineException;

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
     * Checks if the user has a certain rank
     * @param rank The rank to check for
     * @return Whether they have it or not
     */
    boolean hasRank(Rank rank);

    /**
     * Gives the user a rank
     * @param rank The rank to give
     */
    default void addRank(Rank rank) {
        addRank(rank, true);
    }

    /**
     * Gives the user a rank, Also gives the permission
     * @param rank The rank to give
     * @param givePermission Whether to also give the rank's permission
     */
    void addRank(Rank rank, boolean givePermission);

    /**
     * Removes a rank from the user along with the ranks permission
     * @param rank The rank to remove
     */
    default void removeRank(Rank rank) {
        removeRank(rank, true);
    }

    /**
     * Removes a rank from the user
     * @param rank The ran to remove
     * @param removePermission Whether to also remove the permission
     */
    void removeRank(Rank rank, boolean removePermission);

    /**
     * Gets the user's currently active rank
     * @return The user's active rank
     */
    Rank getRank();

    /**
     * Sets the user's active rank and tab prefix
     * @param rank The user's new rank
     */
    default void setRank(Rank rank) {
        setRank(rank, true);
    }

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
    boolean canSwapFaction();

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
     * Checks if the user can swap branches and sends them a message if they can't
     * <p>Used by the Smith and Jerome interactions</p>
     * @return Whether the user is allowed to swap branches
     */
    boolean performBranchSwappingCheck();

    /**
     * Gets a list of all pets belonging to the user
     * <p>The strings are arbitrary, don't try to guess them :(</p>
     * @return The list of pets belonging to the user
     */
    ObjectList<Pet> getPets();

    /**
     * NOT API, Executes required code when a user joins
     * @return Whether the user's name has changed since they last joined
     */
    boolean onJoin();

    /**
     * NOT API, like on join, except for things that need to be delayed a single tick
     */
    void onJoinLater();

    /**
     * Gets the hover event text of this user
     * @return The user's hover text
     */
    default Component hoverEventText() {
        return hoverEventText(UnaryOperator.identity());
    }

    /**
     * Gets the hover text of this user
     * @param operator The component manipulator
     * @return The user's hover text
     */
    Component hoverEventText(UnaryOperator<Component> operator);

    /**
     * Updates the display name of this user in the Tab List
     */
    void updateTabName();

    /**
     * Gets the list display name of this user
     * @return The user's tab list display name
     */
    Component listDisplayName();

    /**
     * Gets the current prefix of this user
     * @return The user's current tab prefix
     */
    Component getCurrentPrefix();

    /**
     * Sets the current prefix of this user
     * @param component The user's tab prefix
     */
    void setCurrentPrefix(Component component);

    /**
     * Sets the last name this user had while online
     * @param lastOnlineName The last name this user had while online
     */
    void setLastOnlineName(String lastOnlineName);

    /**
     * Gets the last name this user had while online
     * @return last online name
     */
    String getLastOnlineName();

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
     * Checks whether the user has the given property
     * @param property The property to check
     * @return Whether they have it
     */
    boolean hasPref(UserPref property);

    /**
     * Adds the given property to the user
     * @param property The property to add
     */
    void addPref(UserPref property);

    /**
     * Removes the given property from the user
     * @param property The property to remove
     */
    void removePref(UserPref property);

    /**
     * Sets the property value of the given property.
     * @param add Whether to add or remove the property
     * @param property The property to set
     */
    default void setPref(boolean add, UserPref property) {
        if(add) addPref(property);
        else removePref(property);
    }

    /**
     * Checks whether the user is ignoring broadcasts
     * @return ^^^^^^^^^
     */
    default boolean ignoringBroadcasts(){
        return hasPref(UserPref.IGNORING_BROADCASTS);
    }

    /**
     * Sets whether the user is ignoring broadcasts
     * @param ignoring ^^^^
     */
    default void setIgnoringBroadcasts(boolean ignoring){
        setPref(ignoring, UserPref.IGNORING_BROADCASTS);
    }

    /**
     * Gets if the user allows player riding
     * @return Whether the user can be ride and be ridden or not
     */
    default boolean allowsRiding() {
        return !hasPref(UserPref.FORBIDS_RIDING);
    }

    /**
     * Sets if the player can ride and be ridden ;)
     * @param allowsRidingPlayers
     */
    default void setAllowsRiding(boolean allowsRidingPlayers) {
        setPref(!allowsRidingPlayers, UserPref.FORBIDS_RIDING);
    }

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
    default boolean allowsEmotes() {
        return !hasPref(UserPref.FORBIDS_EMOTES);
    }

    /**
     * Sets whether the user allows emotes or not
     * @param allowsEmotes kinda obvious lol
     */
    default void setAllowsEmotes(boolean allowsEmotes) {
        setPref(!allowsEmotes, UserPref.FORBIDS_EMOTES);
    }

    /**
     * Gets the material data of the given material
     * <p>Will return a default empty data if the user has no data for this material</p>
     * @param material The material to get the data of
     * @return The given material's data, empty data if the the user doesn't have data for the material
     */
    SoldMaterialData getMatData(Material material);

    /**
     * Checks whether the user has any material data for the given material
     * @param material The material to check for
     * @return Whether the user has data for the material
     */
    boolean hasMatData(Material material);

    /**
     * Sets the material data of the given material
     * @param data The data to set
     */
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
    Faction getFaction();

    /**
     * Sets the user's branch
     * @param faction The new branch of the user
     */
    void setFaction(Faction faction);

    /**
     * Sends the user a message, works just like the sendMessage in Player, but it also translates hexcodes and '&amp;' chars as color codes
     * @param message The message to send to the user
     */
    void sendMessage(@Nonnull String message);

    /**
     * Sends the user a chat message
     * @param message The NMS component to send
     */
    void sendMessage(net.minecraft.network.chat.Component message);

    /**
     * No clue, appears to work the same as sendMessage(IChatBaseComponent message);
     * @param id ??????
     * @param message The NMS component to send
     */
    void sendMessage(UUID id, net.minecraft.network.chat.Component message);

    /**
     * Sends the user an NMS component message
     * @param message The message to send
     * @param type The type to send, note: SYSTEM and CHAT are the same, GAME_INFO is action bar
     */
    void sendMessage(net.minecraft.network.chat.Component message, ChatType type);

    /**
     * Sends the user an NMS message.
     * @param id The UUID of the sender, if not null, will let the client
     *           decide if it should show the message, depending on if the UUID is blocked or not
     * @param message The message to send
     * @param type The message's type
     */
    void sendMessage(UUID id, net.minecraft.network.chat.Component message, ChatType type);

    /**
     * Send a message that won't be displayed to the user if the given UUID is blocked by the client
     * @param id The UUID of the sender
     * @param message the message
     */
    void sendBlockableMessage(UUID id, Component message);

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
    default boolean isProfilePublic() {
        return !hasPref(UserPref.PROFILE_PRIVATE);
    }

    /**
     * Sets the user's profile to either public or private
     * @param publicProfile ^^^^
     */
    default void setProfilePublic(boolean publicProfile) {
        setPref(!publicProfile, UserPref.PROFILE_PRIVATE);
    }

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

    /**
     * Unloads the user if they're not online
     */
    default void unloadIfNotOnline(){
        if(!isOnline()) unload();
    }

    /**
     * Gets the user's click event, '/tell [name] ' if online, '/profile [name]' if offline
     * @return The user's click event
     */
    default ClickEvent asClickEvent(){
        return ClickEvent.suggestCommand("/" + (isOnline() ? "tell " + getName() + " " : "profile " + getName()));
    }

    /**
     * Gets the user's display name, click event and all
     * @return The user's display name
     */
    default Component displayName(){
        return Component.text(getName())
                .hoverEvent(this)
                .clickEvent(asClickEvent());
    }

    /**
     * Gets the user's display name, with the nickname, if they have it
     * @return The user's display name, possibly with a nickname
     */
    default Component nickDisplayName(){
        return Component.text(getNickOrName())
                .hoverEvent(this)
                .clickEvent(asClickEvent());
    }

    /**
     * Same as {@link CrownUser#nickDisplayName()} except it'll have the rank tier's color
     * @return The user's display name
     */
    default Component coloredNickDisplayName(){
        return nickDisplayName().color(getHighestTierRank().tier.color);
    }

    /**
     * Whether the user has a nickname or not
     * @return ^^^^^
     */
    default boolean hasNickname(){
        return nickname() != null;
    }

    /**
     * Gets either the name of the user, or their nickname
     * @return The user's nickname or name
     */
    default String getNickOrName(){
        return hasNickname() ? getNickname() : getName();
    }

    /**
     * Same as {@link CrownUser#getNickOrName()} except component
     * @return The user's nick or name
     */
    default Component nickOrName(){
        return Component.text(getNickOrName());
    }

    /**
     * Gets the user's highest tier rank
     * @return The user's highest tier rank
     */
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

    /**
     * Creates a teleport to the given location
     * @param destination The destination
     * @param tell Whether to tell the player about the teleport
     * @param type The teleport type
     * @return The created teleport
     */
    UserTeleport createTeleport(Supplier<Location> destination, boolean tell, UserTeleport.Type type);

    /**
     * Creates a teleport to the given location
     * @param destination The destination
     * @param tell Whether to tell the player about the teleport
     * @param type The teleport type
     * @param bypassCooldown whether to bypass the teleport cooldown
     * @return The created teleport
     */
    UserTeleport createTeleport(Supplier<Location> destination, boolean tell, boolean bypassCooldown, UserTeleport.Type type);

    /**
     * Checks if the user is teleporting
     * @return Whether the user is teleporting
     */
    boolean isTeleporting();

    /**
     * Gets whether the user is allowed to teleport
     * @return Whether the user is allowed to teleport
     */
    boolean canTeleport();

    /**
     * Runs the code when a teleport is completed
     */
    void onTpComplete();

    /**
     * Checks teleporting
     * @return Like {@link CrownUser#canTeleport()} but it also sends the user a message
     */
    boolean checkTeleporting();

    /**
     * Gets the last location of the user, used for /back
     * @return The user's last location
     */
    Location getLastLocation();

    /**
     * Sets the user's last location
     * @param lastLocation The last location
     */
    void setLastLocation(Location lastLocation);

    /**
     * Gets whether the user allows TPA requests
     * @return ^^^^^^^^^
     */
    default boolean allowsTPA() {
        return !hasPref(UserPref.FORBIDS_TPA);
    }

    /**
     * Sets whether the user allows TPA requests
     * @param allowsTPA Whether the user allows TPA requests
     */
    default void setAllowsTPA(boolean allowsTPA) {
        setPref(!allowsTPA, UserPref.FORBIDS_TPA);
    }

    /**
     * Gets whether the user is eaves dropping
     * @return ^^^
     */
    default boolean isEavesDropping() {
        return hasPref(UserPref.LISTENING_TO_EAVESDROPPER);
    }

    /**
     * Sets whether the user is eaves dropping
     * @param eavesDropping Whether the user is eaves dropping
     */
    default void setEavesDropping(boolean eavesDropping) {
        setPref(eavesDropping, UserPref.LISTENING_TO_EAVESDROPPER);
    }

    /**
     * Gets the last message reply target
     * @return Last reply target
     */
    CommandSource getLastMessage();

    /**
     * Sets the last message reply target
     * @param lastMessage The last message reply target
     */
    void setLastMessage(CommandSource lastMessage);

    /**
     * Sets the nickname
     * @param nick New nickname
     */
    default void setNickname(String nick) {
        setNickname(nick == null ? null : ChatUtils.convertString(nick, true));
    }

    /**
     * Sets the user's nickname
     * @param component The user's new nickname
     */
    void setNickname(Component component);

    /**
     * Gets the user's nickname
     * @return The user's nickname
     */
    default String getNickname() {
        return nickname() == null ? null : ChatUtils.getString(nickname());
    }

    /**
     * Gets the nickname
     * @return ^^^^
     */
    Component nickname();

    default boolean isVanished() {
        return hasPref(UserPref.VANISHED);
    }

    default void setVanished(boolean vanished) {
        setPref(vanished, UserPref.VANISHED);

        if(isOnline()) updateVanished();
    }

    void updateVanished();

    boolean isAfk();

    @Nullable String getAfkReason();

    void setAfk(boolean afk, String reason);

    void updateAfk();

    default boolean isFlying() {
        return hasPref(UserPref.FLYING);
    }

    default void setFlying(boolean flying) {
        setPref(flying, UserPref.FLYING);

        if(isOnline()) updateFlying();
    }

    void updateFlying();

    default boolean godMode() {
        return hasPref(UserPref.GOD_MODE);
    }

    default void setGodMode(boolean godMode) {
        setPref(godMode, UserPref.GOD_MODE);

        if(isOnline()) updateGodMode();
    }

    void updateGodMode();

    CosmeticData getCosmeticData();

    UserInteractions getInteractions();

    UserHomes getHomes();

    FtcGameMode getGameMode();

    void setGameMode(FtcGameMode gameMode);

    default boolean allowsPaying() {
        return !hasPref(UserPref.FORBIDS_PAY);
    }

    default void setAllowsPay(boolean acceptsPay) {
        setPref(!acceptsPay, UserPref.FORBIDS_PAY);
    }

    default boolean allowsRegionInvites() {
        return !hasPref(UserPref.FORBIDS_REGION_INVITES);
    }

    default void setAllowsRegionInvites(boolean val) {
        setPref(!val, UserPref.FORBIDS_REGION_INVITES);
    }

    default boolean hulkSmashesPoles() {
        return !hasPref(UserPref.NON_HULK_SMASHER);
    }

    default void setHulkPoles(boolean hulk) {
        setPref(!hulk, UserPref.NON_HULK_SMASHER);
    }

    /**
     * Creates a sell result by attempting to sell items in the user's inventory
     * @param material The material to sell aka remove
     * @param targetAmount The amount of items to remove, -1 for unlimited
     * @return The sell result
     */
    UserSellResult sellMaterial(Material material, int targetAmount);

    /**
     * Creates a sell result and uses the user's sell amount for the target
     * @param material The material to sell
     * @return The sell result
     */
    default UserSellResult sellMaterial(Material material) {
        return sellMaterial(material, getSellAmount() == SellAmount.ALL ? -1 : getSellAmount().getValue());
    }

    default BlockVector2 get2DLocation() {
        Location l = getLocation();

        return BlockVector2.at(l.getX(), l.getZ());
    }

    default RegionPos getRegionCords() {
        return RegionPos.of(getLocation());
    }

    void setVelocity(double x, double y, double z);

    default void setVelocity(Vector velocity) {
        setVelocity(velocity.getX(), velocity.getY(), velocity.getZ());
    }

    default void setVelocity(ImmutableVector3i vec) {
        setVelocity(vec.getX(), vec.getY(), vec.getZ());
    }

    default void setVelocity(Position pos) {
        setVelocity(pos.x(), pos.y(), pos.z());
    }

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    @Override
    String toString();
}
