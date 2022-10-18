package net.forthecrown.commands.manager;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Permissions;
import net.forthecrown.text.Messages;
import net.forthecrown.text.Text;
import net.forthecrown.core.Vars;
import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.core.admin.PunishType;
import net.forthecrown.core.holidays.Holiday;
import net.forthecrown.economy.market.MarketDisplay;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.user.User;
import net.forthecrown.user.data.RankTitle;
import net.forthecrown.user.data.TimeField;
import net.forthecrown.utils.Time;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import static net.forthecrown.commands.manager.OpenExceptionType.INSTANCE;

/**
 * Factory class for creating exceptions. This class is set up similarly
 * to {@link Messages} with it being the main class where exceptions are
 * stored, not anywhere else.
 * <p>
 * Exceptions which have no dynamic aspects, ie, they are a single message
 * that requires no command context, should be stored as exception constants
 */
public interface Exceptions {
    // ------------------------------------------------
    // --- SECTION: EXCEPTION FACTORIES / UTILITIES ---
    // ------------------------------------------------

    /**
     * Creates an exception with the given message
     * @param message The message to create an exception with
     * @return The created exception
     */
    private static RoyalCommandException create(String message) {
        return INSTANCE.create(Text.renderString(message));
    }

    /**
     * Creates an exception by formatting the given format with the
     * given arguments
     * @param format The message format to use
     * @param args The args to format with
     * @return The created exception
     * @see Text#format(Component, Object...)
     */
    static CommandSyntaxException format(String format, Object... args) {
        return INSTANCE.create(Text.format(format, args));
    }

    /**
     * Creates an exception by formatting the given message format with
     * the given arguments.
     * @param format The message format to use
     * @param color The color to apply onto the format
     * @param args The arguments to format with
     * @return The created exception
     * @see Text#format(Component, Object...)
     */
    private static CommandSyntaxException format(String format, TextColor color, Object... args) {
        return INSTANCE.create(Text.format(format, color, args));
    }

    /**
     * Creates an exception by formatting the given message format with
     * the given arguments.
     * @param format The message format to use
     * @param reader The reader to use for the
     *               exception's context
     * @param args The arguments to format with
     * @return The created exception
     * @see Text#format(Component, Object...)
     */
    private static CommandSyntaxException formatWithContext(String format, ImmutableStringReader reader, Object... args) {
        return INSTANCE.createWithContext(Text.format(format, args), reader);
    }

    /**
     * Handles the given {@link CommandSyntaxException} by formatting it
     * and sending it to the given <code>sender</code>
     * @param sender The sender to send the formatted message to
     * @param exception The exception to format
     */
    static void handleSyntaxException(Audience sender, CommandSyntaxException exception) {
        sender.sendMessage(GrenadierUtils.formatCommandException(exception));
    }

    // ---------------------------------------
    // --- SECTION: COMMON / UNCATEGORIZED ---
    // ---------------------------------------

    /**
     * Exception which states your inventory is full
     */
    CommandSyntaxException INVENTORY_FULL = create("Your inventory is full.");

    CommandSyntaxException INVENTORY_EMPTY = create("Your inventory is empty.");

    /**
     * Exception which states you must be holding any kind of item
     */
    CommandSyntaxException MUST_HOLD_ITEM = create("You must be holding an item.");

    /**
     * Exception which states there's nothing to list.
     * This is intentionally really vague as to allow for maximum
     * amount of usability
     */
    CommandSyntaxException NOTHING_TO_LIST = create("Nothing to list!");

    /**
     * Exception stating the end is currently closed
     */
    CommandSyntaxException END_CLOSED = create("The end is currently closed!");

    /**
     * Exception stating that a given item is not repairable
     */
    CommandSyntaxException NOT_REPAIRABLE = create("Given item is not repairable");

    /**
     * Exception stating the server is empty
     */
    CommandSyntaxException EMPTY_SERVER = create("No one is online!");

    /**
     * Exception stating that the viewer cannot use the <code>/leave</code>
     * command there
     */
    CommandSyntaxException CANNOT_USE_LEAVE = create("Cannot use /leave here!");

    CommandSyntaxException NO_REGION_SELECTION = create("No region selection!");

    CommandSyntaxException NO_PERMISSION = INSTANCE.create(Messages.NO_PERMISSION);

    CommandSyntaxException DONT_HAVE_TITLE = create("You don't have this title.");

    CommandSyntaxException ALREADY_YOUR_TITLE = create("This is already your title");

    /**
     * Creates an exception which says the given user is not online
     * @param user The user that isn't online
     * @return The created exception
     */
    static CommandSyntaxException notOnline(User user) {
        return INSTANCE.create(Messages.notOnline(user));
    }

    /**
     * A generic exception factory.
     * <p>
     * This will created a "Missing name" message with
     * the given context, the first name parameter is
     * the missing object's type
     *
     * @param name The type name
     * @param reader The context of the exception
     * @param missing The key of the unknown value
     * @return The created exception
     */
    static CommandSyntaxException unknown(String name, ImmutableStringReader reader, String missing) {
        return formatWithContext("Unknown {0}: '{1}'", reader, name, missing);
    }

    /**
     * Creates an exception which states that the given
     * index is not valid for the max size
     * @param index The index
     * @param max The maximum index value
     * @return The created exception
     */
    static CommandSyntaxException invalidIndex(int index, int max) {
        return format("Invalid index: {0, number}, max: {1, number}", index, max);
    }

    /**
     * Creates an exception which states that the given
     * page is invalid
     * @param page The page
     * @param maxPage The max page
     * @return The created exception
     */
    static CommandSyntaxException invalidPage(int page, int maxPage) {
        return format("Invalid page: {0, number}, max: {1, number}", page, maxPage);
    }

    /**
     * Creates an exception stating the viewer can only
     * perform an action every <code>millis</code> delay.
     * @param millis The millis cooldown length
     * @return The created exception
     */
    static CommandSyntaxException onCooldown(long millis) {
        return format("You can only do this every: {0, time}", millis);
    }

    static CommandSyntaxException alreadySetCosmetic(Component cosmeticName, Component typeName) {
        return format("{0} is already your {1} effect",
                cosmeticName, typeName
        );
    }

    static CommandSyntaxException alreadyExists(String name, Object value) {
        return format("{0} named '{1}' already exists",
                name, value
        );
    }

    static CommandSyntaxException noIncoming(User user) {
        return format("You haven't received any requests from {0, user}.", user);
    }

    static CommandSyntaxException noOutgoing(User user) {
        return format("You haven't sent any requests to {0, user}.", user);
    }

    static CommandSyntaxException requestAlreadySent(User target) {
        return format("You've already sent a request to {0, user}.", target);
    }

    // -------------------------------------
    // --- SECTION: ADMIN ARGUMENT TYPES ---
    // -------------------------------------

    /**
     * Exception which states the parsed reward range is invalid
     * <p>
     * Used by {@link net.forthecrown.commands.arguments.RewardRangeArgument}
     * when parsing the reward range object.
     */
    CommandSyntaxException INVALID_REWARD_RANGE = create("Max or Min bounds cannot be below 100");

    /**
     * Exception which states that no Var by the given name exists
     * <p>
     * Used by {@link net.forthecrown.commands.arguments.GlobalVarArgument}
     * @param reader The reader to get the context from
     * @param name The unknown variable's name
     * @return The created exception
     */
    static CommandSyntaxException unknownVar(ImmutableStringReader reader, String name) {
        return unknown("variable", reader, name);
    }

    /**
     * Creates an exception stating that no {@link Holiday}
     * exists by the given name
     * <p>
     * Used by {@link net.forthecrown.commands.arguments.HolidayArgument}
     * @param reader The reader to get the context from
     * @param name The holiday's name
     * @return The created exception
     */
    static CommandSyntaxException unknownHoliday(ImmutableStringReader reader, String name) {
        return unknown("holiday", reader, name);
    }

    /**
     * Creates an exception stating that no {@link net.forthecrown.useables.UsableTrigger}
     * by the given name exists.
     * <p>
     * Used by {@link net.forthecrown.commands.usables.CommandInteractable}
     * @param reader The reader to get the context of
     * @param cursor The cursor position to move the reader to
     * @param name The name of the trigger
     * @return The created exception
     */
    static CommandSyntaxException unknownTrigger(StringReader reader, int cursor, String name) {
        return unknown("trigger", GrenadierUtils.correctReader(reader, cursor), name);
    }

    // ----------------------------
    // --- SECTION: PUNISHMENTS ---
    // ----------------------------

    /**
     * Exception stating an offline player cannot be kicked
     * <p>
     * Used by {@link net.forthecrown.commands.punish.PunishmentCommand.CommandKick}
     */
    CommandSyntaxException CANNOT_KICK_OFFLINE = create("Cannot kick offline player");

    /**
     * Exception stating that a given jail spawn position is invalid
     * due to it being outside the selected jail-cell area.
     * <p>
     * Used by {@link net.forthecrown.commands.punish.CommandJails}
     */
    CommandSyntaxException INVALID_JAIL_SPAWN = create(
            "Jail spawn point (The place you're standing at) " +
            "isn't inside the cell room"
    );

    /**
     * Creates an exception stating the given user
     * cannot be punished.
     * <p>
     * This will most likely be thrown because a
     * staff member attempted to use a punishment
     * on a staff member of higher rank.
     * <p>
     * Used by {@link net.forthecrown.commands.punish.PunishmentCommand}
     * @param user The user that cannot be punished
     * @return The created exception
     */
    static CommandSyntaxException cannotPunish(User user) {
        return format("Cannot punish: {0, user}", user);
    }

    /**
     * Creates an exception stating the given user has already
     * received the given type of punishment
     * <p>
     * Used by {@link net.forthecrown.commands.punish.PunishmentCommand}
     * @param user The user that has been punished
     * @param type The punishment
     * @return The created exception
     */
    static CommandSyntaxException alreadyPunished(User user, PunishType type) {
        return format("{0, user} has already been {1}!", user, type.nameEndingED());
    }

    /**
     * Creates an exception stating that a jail with the given
     * <code>key</code> already exists and a new one cannot be
     * created
     * <p>
     * Used by {@link net.forthecrown.commands.punish.CommandJails}
     * @param key The name of the jail cell
     * @return The created exception
     */
    static CommandSyntaxException jailExists(String key) {
        return format("Jail named '{0, key}' already exists", key);
    }

    static CommandSyntaxException noNotes(PunishEntry entry) {
        return format("{0, user} has no staff notes", entry.getHolder());
    }

    static CommandSyntaxException notPunished(User user, PunishType type) {
        return format("{0, user} is not {1}",
                user, type.nameEndingED()
        );
    }

    static CommandSyntaxException cannotPardon(PunishType type) {
        return format("You do not have enough permissions to pardon a {0}",
                type.presentableName()
        );
    }

    // ------------------------
    // --- SECTION: ECONOMY ---
    // ------------------------

    CommandSyntaxException NO_ITEM_TO_SELL = create("Not enough items to sell");

    CommandSyntaxException CANNOT_SELL_MORE = create("Cannot sell more, price would drop to 0");

    /**
     * Creates an exception which states the user cannot afford
     * the given amount of Rhines
     * @param amount The amount the user cannot afford
     * @return The created exception
     */
    static CommandSyntaxException cannotAfford(Number amount) {
        return format("Cannot afford {0, rhines}", amount);
    }

    // ------------------------
    // --- SECTION: MARKETS ---
    // ------------------------

    /**
     * Exception which states that the viewer does not
     * own a shop
     */
    CommandSyntaxException NO_SHOP_OWNED = create("You don't own a shop.");

    /**
     * Exception which states that NO shops exist at all
     */
    CommandSyntaxException NO_SHOPS_EXIST = create("No shops exist");

    CommandSyntaxException MARKET_ALREADY_OWNED = create("This shop is already owned.");

    CommandSyntaxException MARKET_ALREADY_OWNER = create("You already own a shop.");

    CommandSyntaxException NOT_MERGED = create("Your shop is not merged.");

    CommandSyntaxException MERGE_SELF = create("Cannot merge with your own shop");

    CommandSyntaxException ALREADY_MERGED = create("Your shop is already merged");

    CommandSyntaxException MARKED_EVICTION = create("That shop is already marked for eviction");

    CommandSyntaxException NOT_MARKED_EVICTION = create("Shop is not marked for eviction");

    CommandSyntaxException NON_AUTO_APPEAL = create("Can only appeal automatic evictions, this is a staff-created eviction");

    static CommandSyntaxException marketTargetStatus(User target) {
        return format("{0, user} cannot change shop ownership at this time.", target);
    }

    static CommandSyntaxException marketTargetHasShop(User target) {
        return format("{0, user} already owns a shop.", target);
    }

    static CommandSyntaxException marketTargetMerged(User target) {
        return format("{0, user} has already merged their shop", target);
    }

    static CommandSyntaxException marketNotConnected(User target) {
        return format("Not connected to {0, user}'s shop", target);
    }

    static CommandSyntaxException shopNotOwned(MarketShop shop) {
        return format("{0} has no owner", MarketDisplay.displayName(shop));
    }

    static CommandSyntaxException noShopOwned(User target) {
        return format("{0, user} does not own a shop", target);
    }

    // ---------------------------
    // --- SECTION: SIGN SHOPS ---
    // ---------------------------

    CommandSyntaxException OUT_OF_STOCK = create("This shop is out of stock.");

    CommandSyntaxException SHOP_NO_SPACE = create("This shop doesn't have enough space.");

    CommandSyntaxException SHOP_NO_PRICE = create("The last line must contain a price!");

    CommandSyntaxException SHOP_NO_DESC = create("You must provide a description of the shop's items!");

    /**
     * Exception which states the user must be looking at a shop they own
     */
    CommandSyntaxException LOOK_AT_SHOP = create("You must be looking at a shop you own");

    /**
     * Exception which simply says 'Invalid shop'
     * <p>
     * Used by {@link net.forthecrown.commands.economy.CommandShopHistory} to
     * state a given shop's name is invalid
     */
    CommandSyntaxException INVALID_SHOP = create("Invalid shop");

    CommandSyntaxException TRANSFER_SELF = create("Cannot transfer a shop to yourself");

    static CommandSyntaxException unknownShop(StringReader reader, int cursor, String name) {
        return unknown("Shop", GrenadierUtils.correctReader(reader, cursor), name);
    }

    static CommandSyntaxException dontHaveItemForShop(ItemStack item) {
        return format("You don't have {0, item} to sell.", item);
    }

    static CommandSyntaxException shopOwnerCannotAfford(int amount) {
        return format(
                "The owner of this shop cannot afford &e{0, rhines}&r.",
                NamedTextColor.GRAY, amount
        );
    }

    static CommandSyntaxException shopMaxPrice() {
        return format("Shop price exceeded max price of {0, rhines}.", Vars.maxSignShopPrice);
    }

    // ----------------------
    // --- SECTION: USERS ---
    // ----------------------

    CommandSyntaxException NO_USERS_FOUND = create("No users found");

    static CommandSyntaxException unknownUser(StringReader reader, int cursor, String name) {
        return unknown("user",
                GrenadierUtils.correctReader(reader, cursor),
                name
        );
    }

    // ---------------------------
    // --- SECTION: USER HOMES ---
    // ---------------------------

    CommandSyntaxException NO_DEF_HOME = create("No default home set. Use /sethome.");

    CommandSyntaxException CANNOT_SET_HOME = create("Cannot set home here.");

    CommandSyntaxException NO_RETURN = create("No location to return to.");

    CommandSyntaxException CANNOT_RETURN = create("Cannot return to previous location");

    static CommandSyntaxException overHomeLimit(User user) {
        return format("Cannot create more homes (Over limit of {0, number}).",
                Permissions.MAX_HOMES.getTier(user)
        );
    }

    static CommandSyntaxException unknownHome(ImmutableStringReader reader, String name) {
        return unknown("home", reader, name);
    }

    static CommandSyntaxException notInvited(User user) {
        return format("{0, user} has not invited you.", user);
    }

    static CommandSyntaxException noHomeRegion(User user) {
        return format("{0, user} does not have a home region");
    }

    static CommandSyntaxException badWorldHome(String name){
        return format("Cannot teleport to {0}.", name);
    }

    // ------------------------
    // --- SECTION: REGIONS ---
    // ------------------------

    CommandSyntaxException REGIONS_WRONG_WORLD = create("Regions do not exist in this world.");

    CommandSyntaxException NO_HOME_REGION = create("You do not have a home region");

    CommandSyntaxException CANNOT_INVITE_SELF = create("Cannot invite yourself");

    CommandSyntaxException BAD_POLE_POSITION = create("Invalid position for pole, too close to the edge");

    CommandSyntaxException REGION_NO_DATA = create("Region has no data");

    CommandSyntaxException ONLY_NAMED_HAVE_DESC = create("Only named regions may have descriptions");

    CommandSyntaxException RESIDENTS_HIDDEN = create("This region's residents are hidden");

    CommandSyntaxException IN_UNNAMED_REGION = create("You are not in a named region.");

    CommandSyntaxException CANNOT_BE_RIDDEN = create("Cannot have players riding you");

    CommandSyntaxException ONLY_IN_VEHICLE = create("Can only teleport in a vehicle");

    static CommandSyntaxException unknownRegion(StringReader reader, int cursor) {
        return format(
                "There's no region or online player named '{0}'.\nUse /listregions to list all regions.",
                reader.getString().substring(cursor, reader.getCursor())
        );
    }

    /**
     * Creates an exception stating the given region name
     * is unknown
     * @param name The region's name
     * @return The created exception
     */
    static CommandSyntaxException unknownRegion(String name) {
        return format("Unknown region: '{0}'", name);
    }

    static CommandSyntaxException farFromPole(int x, int z) {
        return format("Too far from a region post to teleport." +
                        "\nClosest pole is at x={0}, z={1}.",
                x, z
        );
    }

    static CommandSyntaxException inviteNotSent(User target) {
        return format("You haven't invited {0, user} to your region.", target);
    }

    // -----------------------------------------
    // --- SECTION: USER TELEPORTATION / TPA ---
    // -----------------------------------------

    CommandSyntaxException CANNOT_TP = create("Cannot currently teleport.");

    CommandSyntaxException NO_TP_REQUESTS = create("You don't have any tp requests.");

    CommandSyntaxException CANNOT_TP_SELF = create("You cannot teleport to yourself.");

    CommandSyntaxException NOT_CURRENTLY_TELEPORTING = create("You aren't currently teleporting");

    CommandSyntaxException TPA_DISABLED_SENDER = create("You have TPA requests disabled.\nUse /tpatoggle to enable them.");

    CommandSyntaxException CANNOT_TP_HERE = create("Cannot tpa here.");

    static CommandSyntaxException tpaDisabled(User user) {
        return format("{0, user} has disabled TPA requests.", user.displayName());
    }

    static CommandSyntaxException cannotTpaTo(User user){
        return format("Cannot tpa to {0, user}.", user);
    }

    // -----------------------------
    // --- SECTION: CMD NICKNAME ---
    // -----------------------------

    CommandSyntaxException ALREADY_YOUR_NICK = create("This is already your nickname");

    CommandSyntaxException NICK_UNAVAILABLE = create("That nickname is currently unavailable");

    CommandSyntaxException ALREADY_THEIR_NICK = create("This is already their nickname");

    static CommandSyntaxException nickTooLong(int length) {
        return format("Nickname is too long: {0, number} characters out of {1}",
                length, Vars.maxNickLength
        );
    }

    // --------------------------
    // --- SECTION: USER MAIL ---
    // --------------------------

    CommandSyntaxException MAIL_NOTHING_CLAIMABLE = create("No items, rhines or gems to claim");

    CommandSyntaxException MAIL_ALREADY_CLAIMED = create("You already claimed this");

    CommandSyntaxException MAIL_NO_ITEM_GIVEN = create("No item was given, cannot send item.");

    CommandSyntaxException MAIL_PRIVATE = create("Cannot look at another user's mail.");

    // ---------------------------
    // --- SECTION: CMD IGNORE ---
    // ---------------------------

    CommandSyntaxException CANNOT_IGNORE_SELF = create("You cannot ignore yourself... lol");

    // ----------------------------
    // --- SECTION:  CMD PAYING ---
    // ----------------------------

    CommandSyntaxException SENDER_PAY_DISABLED = create("You have disabled paying." +
            "\nUse /paytoggle to enable it."
    );

    CommandSyntaxException CANNOT_PAY_SELF = create("You cannot pay yourself.");

    static CommandSyntaxException targetPayDisabled(User user){
        return format("{0, user} has disabled paying.", user);
    }

    // ----------------------------------
    // --- SECTION: USER INTERACTIONS ---
    // ----------------------------------

    CommandSyntaxException NO_REPLY_TARGETS = create("No one to reply to.");

    CommandSyntaxException NOT_MARRIED = create("You are not married.");

    CommandSyntaxException ALREADY_MARRIED = create("You are already married.");

    CommandSyntaxException MARRY_SELF = create("Cannot marry yourself");

    CommandSyntaxException MARRY_DISABLED_SENDER = create("You have disabled marriage proposals");

    CommandSyntaxException NO_PROPOSALS = create("You haven't received any proposals");

    CommandSyntaxException PRIEST_ALREADY_ACCEPTED = create("You have already accepted.");

    CommandSyntaxException PRIEST_NO_ONE_WAITING = create("You have no one awaiting marriage.");

    static CommandSyntaxException marriageStatusSender(User user) {
        return format("Cannot change marriage status for {0, time}",
                Time.timeUntil(
                        user.getTime(TimeField.MARRIAGE_CHANGE) + Vars.marriageCooldown
                )
        );
    }

    static CommandSyntaxException marriageStatusTarget(User user){
        return format("{0, user} cannot currently change marriage status.", user);
    }

    static CommandSyntaxException targetAlreadyMarried(User user){
        return format("{0, user} is already married.", user);
    }

    static CommandSyntaxException marriageDisabledTarget(User target) {
        return format("{0, user} has disabled marriage requests.", target);
    }

    // ---------------------------------
    // --- SECTION: CMD BECOME BARON ---
    // ---------------------------------

    CommandSyntaxException ALREADY_BARON = create("You are already a baron.");


    // ----------------------------
    // --- SECTION: CMD PROFILE ---
    // ----------------------------

    static CommandSyntaxException profilePrivate(User user) {
        return format("{0, user}'s profile is not public.", user);
    }

    // -----------------------------
    // --- SECTION: CMD WITHDRAW ---
    // -----------------------------

    CommandSyntaxException HOLD_COINS = create("You must be holding the coins you wish to deposit.");

    // -------------------------
    // --- SECTION: CMD NEAR ---
    // -------------------------

    CommandSyntaxException NO_NEARBY_PLAYERS = create("No nearby players.");

    // ---------------------------------------
    // --- SECTION: GENERAL ADMIN COMMANDS ---
    // ---------------------------------------

    CommandSyntaxException CANNOT_USE_RELATIVE_CORD = create("Cannot use relative (~ or ^) coordinates");

    CommandSyntaxException ENCH_MUST_BE_BETTER = create(
            "Enchantment must be higher level than already existing one"
    );

    CommandSyntaxException ITEM_CANNOT_HAVE_META = create(
            "Item cannot have name/enchantments/lore"
    );

    CommandSyntaxException NO_LORE = create("Item has no lore");

    CommandSyntaxException REMOVED_NO_DATA = create("Found no data to remove");

    CommandSyntaxException NO_ATTR_MODS = create("No attribute modifiers to remove");

    CommandSyntaxException HOLIDAY_NO_REWARDS = create(
            "Holiday has no rewards to give, no gems, rhines or items"
    );

    CommandSyntaxException NOT_HOLDING_ROYAL_SWORD = create(
            "You must be holding a royal sword"
    );

    CommandSyntaxException NO_SIGN_COPY = create("No sign copied");

    CommandSyntaxException IGNORING_STAFF_CHAT = create(
            "You are ingoring staff chat, do '/sct_visible' to use it again"
    );

    static CommandSyntaxException structureExists(NamespacedKey key) {
        return format("Structure named '{0, key}' already exists", key);
    }

    static CommandSyntaxException enchantNotFound(Enchantment enchantment) {
        return format("Held item does not have '{0}' enchantment",
                enchantment.displayName(1)
        );
    }

    static CommandSyntaxException invalidBounds(int min, int max) {
        return format("Invalid bounds! Min ({0}) was larger than Max ({1})",
                min, max
        );
    }

    static CommandSyntaxException holidayNoItem(Holiday holiday) {
        return format("{0} has no reward item to give",
                holiday.name()
        );
    }

    static CommandSyntaxException invalidGate(String name) {
        return format("Invalid gate ID: '{0}'",
                name
        );
    }

    static CommandSyntaxException notSign(Location l) {
        return format("{0, location, -c -w} is not a sign", l);
    }

    static CommandSyntaxException defaultTitle(RankTitle title) {
        return format("Cannot use default title: '{0}'", title);
    }

    static CommandSyntaxException notSellable(Material material) {
        return format("'{0}' is not a sellable material", material.name().toLowerCase());
    }

    // ------------------------
    // --- SECTION: USABLES ---
    // ------------------------

    CommandSyntaxException PLAYER_USABLE = create("Players cannot be usable");

    CommandSyntaxException USABLE_INVALID_BLOCK = create("Block is not a valid Tile entity");

    CommandSyntaxException ALREADY_USABLE_ENTITY = create("Entity is already usable");

    CommandSyntaxException ALREADY_USABLE_BLOCK = create("Block is already usable");

    CommandSyntaxException BLOCK_NOT_USABLE = create("Given block is not a usable");

    CommandSyntaxException ENTITY_NOT_USABLE = create("Given entity is not usable");

    CommandSyntaxException REQUIRES_INPUT = create("This type requires input to parse");

    static CommandSyntaxException requiresInput(String thing) {
        return format("{0} requires input", thing);
    }

    // ---------------------------
    // --- SECTION: CMD EMOTES ---
    // ---------------------------

    CommandSyntaxException EMOTE_DISABLE_SELF = create(
            "You have emotes disabled\nUse /emotetoggle to enable them."
    );

    static CommandSyntaxException emoteDisabledTarget(User target) {
        return format("{0, user} has disabled emotes", target);
    }

    // -----------------------
    // --- SECTION: BOSSES ---
    // -----------------------

    CommandSyntaxException BOSS_NOT_ALIVE = create("Boss is not alive");

    CommandSyntaxException DIEGO_ERROR = INSTANCE.create(Messages.DIEGO_ERROR);
}