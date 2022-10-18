package net.forthecrown.text;

import net.forthecrown.commands.ToggleCommand;
import net.forthecrown.core.Vars;
import net.forthecrown.core.Worlds;
import net.forthecrown.core.admin.Mute;
import net.forthecrown.economy.market.MarketEviction;
import net.forthecrown.economy.sell.SellResult;
import net.forthecrown.economy.shops.HistoryEntry;
import net.forthecrown.economy.shops.ShopType;
import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.economy.shops.SignShopSession;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.text.format.ComponentFormat;
import net.forthecrown.text.format.UnitFormat;
import net.forthecrown.user.*;
import net.forthecrown.user.data.MailMessage;
import net.forthecrown.user.data.RankTier;
import net.forthecrown.user.data.RankTitle;
import net.forthecrown.user.data.UserHomes;
import net.forthecrown.user.property.Properties;
import net.forthecrown.user.property.UserProperty;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.chat.Keybinds;
import org.apache.commons.lang3.Range;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static net.forthecrown.text.Text.format;
import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.event.ClickEvent.openUrl;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;

/**
 * Factory class for messages that should in theory serve as the
 * place where most, if not all, of FTC's message are stored as
 * either constants or factory methods.
 * <p>
 * With that being said, there will be instances where messages
 * are stored as field variables outside this class or just
 * declared on-the-go somewhere else.
 * <p>
 * Also forgive me, but for this class I'm breaking my rule of
 * having all fields at the top of the class, this class will be
 * broken up into sections and each section will have fields related
 * to itself there.
 * <p>
 * This class will be broken up into sections, section's will begin
 * with a header in the following style: <pre>
 * // ---------------------
 * // SECTION: SECTION_NAME
 * // ---------------------
 * </pre>
 * Sections for specific commands will be prefixed with "CMD", for
 * example, the /pay command section's name will be "CMD PAY". The
 * same goes for sections related to {@link User}s, they will be
 * prefixed with "USER", eg: "USER INTERACTIONS".
 * <p>
 * This class makes heavy use of the {@link Text} utility
 * class, mostly for text formatting.
 * @see Text
 * @see Text#format(Component, Object...)
 * @see ComponentFormat
 */
public interface Messages {

    // ---------------------------------------
    // --- SECTION: COMMON / UNCATEGORIZED ---
    // ---------------------------------------

    Style CHAT_URL = Style.style(TextDecoration.UNDERLINED)
            .hoverEvent(Component.text("Click to open link!"));

    /**
     * Common text which simply states "Click to allow"
     */
    TextComponent CLICK_TO_ALLOW = text("Click to Allow");

    /**
     * Common text which simply states "Click to deny"
     */
    TextComponent CLICK_TO_DENY = text("Click to Deny");

    /**
     * Common text which simply states "Click to confirm"
     * Used mostly in hover events
     */
    TextComponent CLICK_TO_CONFIRM = text("Click to confirm");

    /**
     * A tick encased by square brackets with {@link #CLICK_TO_ALLOW} hover event
     */
    TextComponent BUTTON_ACCEPT_TICK = text("[✔]").hoverEvent(CLICK_TO_ALLOW);

    /**
     * A special unicode cross encased by square brackets with {@link #CLICK_TO_DENY}
     * hover event.
     */
    TextComponent BUTTON_DENY_CROSS = text("[✖]").hoverEvent(CLICK_TO_DENY);

    /**
     * Standard " < " previous page button with hover text, and bold and yellow styling
     */
    TextComponent PREVIOUS_PAGE = text(" < ", NamedTextColor.YELLOW, TextDecoration.BOLD)
            .hoverEvent(Component.translatable("spectatorMenu.previous_page"));

    /**
     * Standard " > " next page button with hover text, and bold and yellow styling
     */
    TextComponent NEXT_PAGE = text(" > ", NamedTextColor.YELLOW, TextDecoration.BOLD)
            .hoverEvent(Component.translatable("spectatorMenu.next_page"));

    /**
     * A standard page border made up of spaces with a {@link TextDecoration#STRIKETHROUGH}
     * style applied to them as well as gray coloring.
     */
    TextComponent PAGE_BORDER = text("                  ",
            NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH
    );

    /**
     * Standard aqua colored button which states "[Confirm]"
     * and has {@link #CLICK_TO_CONFIRM} as the hover text
     */
    TextComponent BUTTON_CONFIRM = text("[Confirm]", NamedTextColor.AQUA)
            .hoverEvent(CLICK_TO_CONFIRM);

    /**
     * Red button which states "[Deny]" and has {@link #CLICK_TO_DENY} as
     * the hover text
     */
    TextComponent BUTTON_DENY = text("[Deny]", NamedTextColor.RED)
            .hoverEvent(CLICK_TO_DENY);

    /**
     * Green button which states "[Acccept]" and has "Click to accept"
     * as the hover text
     */
    TextComponent BUTTON_ACCEPT = text("[Accept]", NamedTextColor.GREEN)
            .hoverEvent(text("Click to accept"));

    /** Common text which says "Click me!" lol */
    TextComponent CLICK_ME = text("Click me!");

    /**
     * Uncategorized text which says that special items cannot be dropped
     */
    TextComponent CANNOT_DROP_SPECIAL = text("Cannot drop special item!", NamedTextColor.RED);

    /**
     * Uncategorized message which states that all-caps messages cannot be sent
     */
    TextComponent ALL_CAPS = text("Please do not send all caps messages.", NamedTextColor.GRAY);

    /** Uncategorized display name of the "Hit me!" dummy entities */
    TextComponent DUMMY_NAME = text("Hit me!");

    /** Red ❤  */
    TextComponent HEART = text("❤", NamedTextColor.DARK_RED);

    /** Cyan claim button for mail */
    TextComponent CLAIM = text("[Claim]", NamedTextColor.AQUA);

    /** Message shown when denying an incoming request of any kind. */
    TextComponent REQUEST_DENIED = text("Request denied.", NamedTextColor.GRAY);

    /** Message shown when accepting an incoming request of any kind. */
    TextComponent REQUEST_ACCEPTED = text("Accepted request.", NamedTextColor.GRAY);

    /** Message shown when cancelling an outgoing request of any kind */
    TextComponent REQUEST_CANCELLED = text("Cancelling request.", NamedTextColor.YELLOW);

    /** Message stating something was claimed */
    TextComponent CLAIMED = text("Claimed ", NamedTextColor.YELLOW);

    /**
     * This is for a weird thing to get around an issue,
     * there are some commands that accept '-clear' as a valid
     * input for a message to clear some text or line somewhere,
     * this allows us to still use a message argument while testing
     * if the input is meant to be a clear command or not
     */
    TextComponent DASH_CLEAR = text("-clear");

    /** The "[Claim the items!]" button holiday mail messages show */
    TextComponent HOLIDAYS_GO_CLAIM = text("[Claim the items right now!]", NamedTextColor.AQUA)
            .hoverEvent(CLICK_ME)
            .clickEvent(runCommand("/mail claim 1"));

    /** Text informing the viewer of lacking permissions */
    TextComponent NO_PERMISSION = text("You do not have permission to use this!", NamedTextColor.RED);

    /** Text which simply says null */
    TextComponent NULL = text("null");

    /** Unknown command message that's used as a permission denied message */
    TextComponent UNKNOWN_COMMAND = text("Unknown command. Type\"/help\" for help", NamedTextColor.WHITE);

    static Component createButton(Component text, String cmd, Object... args) {
        return text.clickEvent(runCommand(String.format(cmd, args)));
    }

    /**
     * Creates a message which states that the given user
     * is not online
     * @param user The user whose display name to use
     * @return The formatted message
     */
    static Component notOnline(User user) {
        return format("{0, user} is not online", user);
    }

    /**
     * Creates a next page button by applying the
     * given click event to the {@link #NEXT_PAGE}
     * constant.
     *
     * @param event The click event to apply, may be null
     * @return The created text
     */
    static Component nextPage(@Nullable ClickEvent event) {
        return NEXT_PAGE.clickEvent(event);
    }

    /**
     * Creates a previous page button by applying the
     * given click event to the {@link #PREVIOUS_PAGE}
     * constant.
     *
     * @param event The click event to apply, may be null
     * @return The created text
     */
    static Component previousPage(@Nullable ClickEvent event) {
        return PREVIOUS_PAGE.clickEvent(event);
    }

    /**
     * Returns {@link #BUTTON_ACCEPT_TICK} with the given <code>cmd</code>
     * as the <code>run_command</code> click event
     * @param cmd The command to use
     * @return The created button component
     */
    static Component tickButton(String cmd, Object... args) {
        return createButton(BUTTON_ACCEPT_TICK, cmd, args);
    }

    /**
     * Returns {@link #BUTTON_DENY_CROSS} with the given <code>cmd</code>
     * as the <code>run_command</code> click event
     * @param cmd The command to use
     * @return The created button component
     */
    static Component crossButton(String cmd, Object... args) {
        return createButton(BUTTON_DENY_CROSS, cmd, args);
    }

    /**
     * Returns {@link #BUTTON_CONFIRM} with the given
     * <code>cmd</code> as the <code>run_command</code> click event
     * @param cmd The command to use
     * @return The created button
     */
    static Component confirmButton(String cmd, Object... args) {
        return createButton(BUTTON_CONFIRM, cmd, args);
    }

    /**
     * Returns {@link #BUTTON_DENY} with the given
     * <code>cmd</code> as the <code>run_command</code> click event
     * @param cmd The command to use
     * @return The created button
     */
    static Component denyButton(String cmd, Object... args) {
        return createButton(BUTTON_DENY, cmd, args);
    }

    /**
     * Returns {@link #BUTTON_ACCEPT} with the given
     * <code>cmd</code> as the <code>run_command</code> click event
     * @param cmd The command to use
     * @return The created button
     */
    static Component acceptButton(String cmd, Object... args) {
        return createButton(BUTTON_ACCEPT, cmd, args);
    }

    /**
     * Creates a message which states there are too many
     * hoppers in one chunk and gives the {@link Vars#hoppersInOneChunk}
     * variable as the max amount
     * @return The formatted message
     */
    static Component tooManyHoppers() {
        return format("&cToo many hoppers in one chunk! &7(Max {0, number})",
                Vars.hoppersInOneChunk
        );
    }

    /**
     * Creates a message stating the viewer died
     * at the given location
     * @param l The location the viewer died at
     * @return The formatted message
     */
    static Component diedAt(Location l) {
        return format("You died at &e{0, location}&r{1}",
                NamedTextColor.GRAY,
                l,

                // Optionally add world name, only if
                // not in overworld
                l.getWorld().equals(Worlds.overworld()) ?
                        "!" :
                        "world: " + Text.formatWorldName(l.getWorld()) + "!"
        );
    }

    /**
     * Formats a chat message
     * @param sender The sender of the message
     * @param message The contents of the message the viewer
     *                sent, this should already be formatted
     *                according to the user's permissions
     * @return The formatted message
     */
    static Component chatMessage(User sender, Component message) {
        return format("&#e6e6e6{0, user} &7&l> &r{1}",
                sender, message
        );
    }

    /**
     * Creates a message sent to the sender of a request.
     * @param target The target of the request
     * @return The formatted message
     */
    static Component requestSent(User target, Component cancelButton) {
        return format("Sent request to &e{0, user}&r. &7{1}",
                NamedTextColor.GOLD,
                target,
                cancelButton
        );
    }


    /**
     * Creates an accept message for the request's sender
     * telling them that the request's target has accepted the
     * request.
     * @param target The Target that accepted the request
     * @return The formatted message
     */
    static Component requestAccepted(User target) {
        return format("&e{0, user}&r accepted your request.",
                NamedTextColor.GOLD, target
        );
    }

    /**
     * Creates a denied message for the request's sender informing
     * them that the request's target has denied the request.
     * @param target The user that denied the request
     * @return The formatted message
     */
    static Component requestDenied(User target) {
        return format("&6{0, user}&r denied your request.",
                NamedTextColor.GRAY, target
        );
    }

    /**
     * Creates a cancellation message to tell the request's
     * target that the sender cancelled the request.
     * @param sender The user that cancelled the request
     * @return The formatted message
     */
    static Component requestCancelled(User sender) {
        return format("&6{0, user}&r cancelled their request",
                NamedTextColor.GRAY, sender
        );
    }

    // ----------------------------------
    // --- SECTION: JOINING / LEAVING ---
    // ----------------------------------
    // Note:
    // Joining and leaving messages still
    // use vanilla translatable messages,
    // why? Because it works, and it's not
    // an FTC message, so why not lol
    //   -- Jules

    /** Golden "Welcome back!" message */
    TextComponent WELCOME_BACK = text("Welcome back!", NamedTextColor.GOLD);

    /**
     * Formats a normal join message
     * @param user The joining user
     * @return The join message
     */
    static TranslatableComponent joinMessage(User user){
        return Component.translatable("multiplayer.player.joined",
                NamedTextColor.YELLOW,
                user.displayName().color(getJoinColor(user))
        );
    }

    /**
     * Formats a join message with a new name
     * @param user1 The joining user
     * @return The formatted message
     */
    static TranslatableComponent newNameJoinMessage(User user1){
        return Component.translatable("multiplayer.player.joined.renamed",
                NamedTextColor.YELLOW,

                user1.displayName().color(getJoinColor(user1)),
                text(user1.getPreviousNames().get(user1.getPreviousNames().size()-1))
        );
    }

    /**
     * Formats a simple leave message
     * @param user The leaving user
     * @return the formatted message
     */
    static TranslatableComponent leaveMessage(User user){
        return Component.translatable("multiplayer.player.left",
                NamedTextColor.YELLOW,
                user.displayName(false).color(getJoinColor(user))
        );
    }

    /**
     * Gets a color to use on the user's display name when they join
     * @param user The user to get the color for
     * @return The join color.
     */
    static TextColor getJoinColor(User user) {
        RankTier r = user.getTitles().getTier();

        if (r == RankTier.TIER_2) {
            return NamedTextColor.GOLD;
        }

        return NamedTextColor.YELLOW;
    }

    /**
     * Creates a message welcoming the
     * given user to the server
     * @param user The user joining for the first time
     * @return The formatted message
     */
    static Component firstJoin(User user) {
        return format("Welcome &6{0, user}&r to the server!",
                NamedTextColor.YELLOW, user
        );
    }


    // ----------------------------------
    // --- SECTION: USER INTERACTIONS ---
    // ----------------------------------

    // Constants used in blocked player / separated player testing
    /**
     * Format used by {@link Users#testBlocked(User, User, String, String)}
     * for separated players.
     */
    String SEPARATED_FORMAT = "You are forcefully separated from {0, user}!";

    /**
     * Format used by {@link net.forthecrown.user.DirectMessage} if
     * the blocked player check fails with the sender of the message
     * having blocked the target
     */
    String DM_BLOCKED_SENDER = "You have blocked {0, user}!";

    /**
     * Format used by {@link net.forthecrown.user.DirectMessage} if
     * the blocked player check fails with the target of the message
     * having blocked the sender
     */
    String DM_BLOCKED_TARGET = "{0, user} has blocked you!";

    /**
     * Format used by {@link net.forthecrown.user.MarriageMessage}
     * if the sender has blocked their spouse lol
     */
    String MC_BLOCKED_SENDER = "You blocked your spouse... lol";

    /**
     * Format used by {@link net.forthecrown.user.MarriageMessage}
     * if the spouse of the sender has blocked them lol
     */
    String MC_BLOCKED_TARGET = "Your spouse blocked you... lol";

    /**
     * Format used by {@link net.forthecrown.commands.CommandMail} when testing
     * if a user can send another user mail. Will be shown to sender if
     * the sender has blocked the target
     */
    String MAIL_BLOCKED_SENDER = "Cannot send mail, you have blocked {0, user}";

    /**
     * Format used by {@link net.forthecrown.commands.CommandMail} when testing
     * if a user can send another user mail. Will be shown to sender if
     * the target has blocked the sender
     */
    String MAIL_BLOCKED_TARGET = "Cannot send mail, {0, user} has blocked you";

    /**
     * Format used by {@link net.forthecrown.commands.economy.CommandPay} when
     * a user attempts to pay another user, this will be shown regardless of if
     * the sender has blocked the target or vice versa
     */
    String PAY_BLOCKED = "Cannot pay blocked user: {0, user}";

    /**
     * Format used by {@link net.forthecrown.commands.markets.CommandMergeShop}
     * stating you cannot merge with a user the sender has blocked
     */
    String MARKET_MERGE_BLOCKED_SENDER = "Cannot merge shops with a user you've blocked";

    /**
     * Format used by {@link net.forthecrown.commands.markets.CommandMergeShop}
     * stating you cannot merge with a user that has blocked the sender
     */
    String MARKET_MERGE_BLOCKED_TARGET = "Cannot merge shops with a user that's blocked you";

    /**
     * Format used by {@link net.forthecrown.commands.markets.CommandShopTrust}
     * when the sender has blocked the user they wish to trust
     */
    String STRUST_BLOCKED_SENDER = "Cannot trust {0, user}: You've blocked them";

    /**
     * Format used by {@link net.forthecrown.commands.economy.CommandShop}
     * when the sender has been blocked by the user they wish to trust
     */
    String STRUST_BLOCKED_TARGET = "Cannot trust {0, user}: They've blocked you";

    /**
     * The "[Marriage]" prefix that is prepended onto th marriage message
     * before the sender's display name
     * <p>
     * Used by {@link net.forthecrown.user.MarriageMessage}
     */
    TextComponent MARRIAGE_PREFIX = text()
            .color(TextColor.color(255, 158, 208))
            .append(
                    text("["),
                    text("Marriage", TextColor.color(255, 204, 230)),
                    text("] ")
            )
            .build();

    /**
     * The pink header/message delimiter pointer: " > "
     * <p>
     * Used by {@link net.forthecrown.user.MarriageMessage}
     */
    TextComponent MARRIAGE_POINTER = text(" > ",
            TextColor.color(255, 158, 208), TextDecoration.BOLD
    );

    /**
     * The "me" in {@link net.forthecrown.user.DirectMessage} messages
     */
    TextComponent DM_ME_HEADER = text("me");

    /** Message stating the viewer cannot send a marriage chat message */
    TextComponent CANNOT_SEND_MCHAT = text("Cannot send Marriage Chat message", NamedTextColor.GRAY);

    TextComponent BOTH_ALLOW_RIDING = text("You must both allow riding", NamedTextColor.GRAY);

    TextComponent CANNOT_RIDE_HERE = text("Cannot ride here!", NamedTextColor.GRAY);

    /**
     * Creates a message that says you are now married to
     * the given user
     * @param to The user who the viewer of this
     *           message is married to
     * @return The formatted message
     */
    static Component nowMarried(User to) {
        return format("You are now married to &6{0, user}&r!",
                NamedTextColor.GOLD, to
        );
    }

    /**
     * Creates a message stating the viewer divorced
     * the given user
     * @param spouse The viewer's now ex-spouse
     * @return The formatted message
     */
    static Component senderDivorced(User spouse) {
        return format("Divorced &e{0, user}&r.",
                NamedTextColor.GOLD, spouse
        );
    }

    /**
     * Creates a message saying the given user has
     * divorced the viewer of the message
     * @param user The user that divorced the viewer
     * @return The formatted message
     */
    static Component targetDivorced(User user) {
        return format("&6{0, user}&e divorced you.", user);
    }

    /**
     * Creates a message saying that the given user
     * is AFK and may not see the {@link net.forthecrown.user.DirectMessage}
     * they were sent.
     *
     * @param afkUser The user that's AFK
     * @return The formatted message
     */
    static Component afkDirectMessage(User afkUser) {
        return format("{0, user} is AFK and may not see your message",
                NamedTextColor.GRAY, afkUser
        );
    }

    /**
     * Creates a message saying the given player was ignored
     * @param target The player being ignored
     * @return The formatted message
     */
    static Component ignorePlayer(User target) {
        return format("Ignored &6{0, user}", NamedTextColor.YELLOW, target);
    }

    /**
     * Creates a message saying the given player was unignored
     * @param target The player being unignored
     * @return The formatted message
     */
    static Component unignorePlayer(User target) {
        return format("Unignored &e{0, user}", NamedTextColor.GRAY, target);
    }

    // -------------------------
    // --- SECTION: USER AFK ---
    // -------------------------

    /**
     * AFK tab suffix
     */
    TextComponent AFK_SUFFIX = text(" [AFK]", NamedTextColor.GRAY);

    /**
     * Message which states the viewer is no longer AFK
     */
    TextComponent UN_AFK_SELF = text("You are no longer AFK.", NamedTextColor.GRAY);

    /**
     * Message which states the viewer is now AFK with no
     * given reason
     */
    TextComponent AFK_SELF = text("You are now AFK", NamedTextColor.GRAY);

    /**
     * Message displayed to users that were kicked for AFK-ing
     * for too long
     */
    TextComponent AFK_KICK = text("Kicked for being AFK");

    /**
     * Creates a message stating the user has been set to afk
     * automatically and for how long they were AFK
     * @return The formatted message
     */
    static Component autoAfkReason() {
        return format("Automatic, been afk for longer than: {0, time}",
                NamedTextColor.GRAY,
                Vars.autoAfkDelay
        );
    }

    /**
     * Creates a message saying the given user is no longer
     * AFK
     * @param user The user that un-AFK'ed
     * @return The formatted component
     */
    static Component unafk(User user) {
        return format("{0, user} is no longer AFK.", NamedTextColor.GRAY, user);
    }

    /**
     * Creates an AFK message stating the the viewer
     * went AFK.
     * <p>
     * if <code>reason == null</code> {@link #AFK_SELF}
     * is returned instead.
     * @param reason The reason for going AFK
     * @return The formatted component
     */
    static Component afkSelf(Component reason) {
        if (reason == null) {
            return AFK_SELF;
        }

        return format("You are now AFK: '{0}'", NamedTextColor.GRAY, reason);
    }

    /**
     * Creates a message stating the given reason went AFK for the given
     * reason.
     * <p>
     * If the reason is null, a formatted message without the reason
     * field is returned.
     * @param user The user that went AFK
     * @param reason The reason they went AFK
     * @return The formatted component
     */
    static Component afkOthers(User user, Component reason) {
        if (reason == null) {
            return format("{0, user} is now AFK.", NamedTextColor.GRAY, user);
        }

        return format("{0, user} is now AFK: '{1}'", NamedTextColor.GRAY, user, reason);
    }

    // -----------------------------------------
    // --- SECTION: USER TELEPORTATION / TPA ---
    // -----------------------------------------

    /**
     * Message used by {@link UserTeleport} to tell users
     * that the delayed teleport was cancelled, most likely
     * because they moved
     */
    TextComponent TELEPORT_CANCELLED = text("Teleport cancelled!", NamedTextColor.GRAY);

    /**
     * Message shown to a user when the {@link UserTeleport#getDestination()}
     * supplier throws an error
     */
    TextComponent TELEPORT_ERROR = text("Cannot teleport, error getting destination",
            NamedTextColor.GRAY
    );

    /** Message stating the viewer is already teleporting */
    TextComponent ALREADY_TELEPORTING = text("You are already teleporting!", NamedTextColor.GRAY);

    /** Message stating the viewer denied all incoming TPA requests */
    TextComponent TPA_DENIED_ALL = text("Denied all TPA requests", NamedTextColor.YELLOW);

    /**
     * Message format for {@link #tpaTargetMessage(String, User)} for a <code>/tpahere</code> command
     */
    String TPA_FORMAT_HERE = "&e{0, user}&r has requested that you teleport to them. &e{1} &7{2}";

    /**
     * Message format for {@link #tpaTargetMessage(String, User)} for a <code>/tpa</code> command
     */
    String TPA_FORMAT_NORMAL = "&e{0, user}&r has requested to teleport to you. &e{1} &7{2}";

    /**
     * Creates a message that tells the viewer that they
     * will teleport in a given amount of time
     *
     * @param delay The teleportation delay, in milliseconds
     * @param type Teleportation type
     * @return The formatted message
     */
    static Component teleportStart(long delay, UserTeleport.Type type) {
        return format("{0} in &e{1, time}&r\nDon't move!",
                NamedTextColor.GRAY,
                type.getAction(), delay
        );
    }

    /**
     * Message that tells the viewer they are
     * teleporting, or performing {@link UserTeleport.Type#getAction()}
     * @param type The teleportation type
     * @return The formatted type
     */
    static Component teleportComplete(UserTeleport.Type type) {
        return format("{0}...", NamedTextColor.GRAY, type.getAction());
    }

    /**
     * Creates a tpa message that's sent to the target of the
     * tpa request. This method supplies, 3 arguments to the
     * given <code>format</code> parameter, they are:
     * <pre>
     * 0: The sender
     * 1: The TPA accept button
     * 2: the TPA deny button
     * </pre>
     *
     * @param format The message format to use, should be one of
     *               {@link #TPA_FORMAT_NORMAL} or {@link #TPA_FORMAT_HERE}
     * @param sender The sender of the message
     * @return The formatted message
     */
    static Component tpaTargetMessage(String format, User sender) {
        return format(format,
                NamedTextColor.GOLD,
                sender,

                tpaAcceptButton(sender),
                tpaDenyButton(sender)
        );
    }

    /**
     * Creates a tpa cancel button
     * @param target The target of the tpa request
     * @return The formatted button component
     */
    static Component tpaCancelButton(User target) {
        return crossButton("/tpacancel %s", target.getName());
    }

    /**
     * Creates a tpa accept button
     * @param sender The sender of the tpa request
     * @return The formatted button component
     */
    static Component tpaAcceptButton(User sender) {
        return tickButton("/tpaccept %s", sender.getName());
    }

    /**
     * Creates a tpa deny button
     * @param sender The sender of the tpa request
     * @return The formatted button component
     */
    static Component tpaDenyButton(User sender) {
        return crossButton("/tpdeny %s", sender.getName());
    }

    /**
     * Creates a message stating the viewer can
     * teleport again in x amount of time
     * @param nextTpTime The next allowed timestamp the user can teleport at
     * @return The formatted component
     */
    static Component canTeleportIn(long nextTpTime) {
        return format("You can teleport again in &6{0, time}",
                NamedTextColor.GRAY,
                Time.timeUntil(nextTpTime)
        );
    }

    // ---------------------------
    // --- SECTION: USER HOMES ---
    // ---------------------------

    /**
     * Message stating that a default home was set
     */
    TextComponent HOMES_DEF_SET = text("Default home set", NamedTextColor.GOLD);

    /**
     * Message stating that you're teleporting home
     */
    TextComponent TELEPORTING_HOME = text("Teleporting home", NamedTextColor.GRAY);

    /**
     * Home list header for when you're viewing your own homes.
     * Used by {@link net.forthecrown.commands.home.CommandHomeList}
     */
    TextComponent HOMES_LIST_HEADER_SELF = text("Your homes: ", NamedTextColor.GOLD);

    /**
     * Creates a message stating that a home by the given
     * name was set to the viewer's current location
     * @param name The name of the home that was set
     * @return The formatted message
     */
    static Component homeSet(String name) {
        return format("Set home &6{0}&r to current location",
                NamedTextColor.YELLOW, name
        );
    }

    /**
     * Creates a message stating the user is teleporting to a
     * home by the given name.
     * @param homeName The home's name
     * @return The formatted message
     */
    static Component teleportingHome(String homeName) {
        return format("Teleporting to {0}.", NamedTextColor.GRAY, homeName);
    }

    /**
     * Lists the homes in the given home map. This only lists
     * the entries, this does not include any header
     * @param homes The home map to list
     * @param cmd The command prefix to use for the entry's {@link ClickEvent}s
     * @return The formatted message
     */
    static Component listHomes(UserHomes homes, String cmd) {
        return TextJoiner.onComma()
                .add(
                        homes.getHomes().entrySet().stream()
                                .map(entry -> {
                                    return text("[" + entry.getKey() + "]", NamedTextColor.GOLD)
                                            .hoverEvent(
                                                    Text.prettyLocation(entry.getValue(), false)
                                            )
                                            .clickEvent(
                                                    runCommand(cmd + entry.getKey())
                                            );
                                })
                )
                .asComponent();
    }

    /**
     * Creates a home list header with the given user's display name
     * @param user The homes' owner
     * @return The formatted message
     */
    static Component homeListHeader(User user) {
        return format("{0, user}'s homes: ", NamedTextColor.GOLD, user);
    }

    /**
     * Creates a message saying the given user's home
     * with the given name was deleted
     * @param user The user whose home was deleted
     * @param home The name of the deleted home
     * @return The formatted message
     */
    static Component deletedHomeOther(User user, String home) {
        return format("Deleted &6{0, user}&r's home: '&6{1}&r'",
                NamedTextColor.YELLOW,
                user, home
        );
    }

    /**
     * Creates a message saying that a
     * home by the given name was deleted
     * @param home The name of the deleted home
     * @return The formatted message
     */
    static Component deletedHomeSelf(String home) {
        return format("Deleted home '&6{0}&r'",
                NamedTextColor.YELLOW, home
        );
    }

    // --------------------------
    // --- SECTION: USER MAIL ---
    // --------------------------

    /**
     * The mail received message to display for received mail that
     * has no sender
     */
    TextComponent MAIL_RECEIVED = text("You've got mail!", NamedTextColor.GOLD);

    /** Header used for the mail list, if the mail being viewed is the user's own */
    TextComponent MAIL_HEADER_SELF = text("Your mail", NamedTextColor.GOLD);

    /** Gray "[Unread]" button, with "Mark as unread" as the hover text */
    TextComponent MAIL_UNREAD = text("[Unread]", NamedTextColor.GRAY)
            .hoverEvent(text("Mark as unread"));

    /** Yellow "[Read]" button, with "Mark as read" as the hover text */
    TextComponent MAIL_READ = text("[Read]", NamedTextColor.YELLOW)
            .hoverEvent(text("Mark as read"));

    /**
     * Attachment button, not for actually claiming the attachment, but for displaying it
     * if anyone other than the owner of the mail is viewing the mail list
     */
    TextComponent MAIL_ATTACHMENT = text("[Attachment]", NamedTextColor.GRAY);

    /** Yellow message stating a message was marked as read */
    TextComponent MARKED_READ = text("Marked as read", NamedTextColor.YELLOW);

    /** Gray message stating a message was marked as unread */
    TextComponent MARKED_UNREAD = text("Marked as unread", NamedTextColor.GRAY);

    TextComponent MAIL_CLEARED = text("Mail cleared", NamedTextColor.GRAY);

    /**
     * Creates a "You have mail" message for a user to
     * inform them that they've received mail while they
     * were offline.
     * @param unreadAmount The amount of unread messages
     *                     the user has received
     * @return The formatted message
     */
    static Component mailJoinMessage(int unreadAmount) {
        return format(
                "You have mail! &7{0} unread messages.",
                NamedTextColor.YELLOW,
                unreadAmount
        )
                .clickEvent(runCommand("/mail"))
                .hoverEvent(text("Click to read mail!"));
    }

    /**
     * Creates a message that says the given user sent the viewer
     * a mail message. It adds a hover event to the message that allows
     * you to see the message content and also tells you to click the
     * message to mark it as read. If the message also has an
     * attachment, the message will have "Message has items!" appended
     * onto it on the next line
     *
     * @param text The message being sent
     * @param sender The sender of the text,
     *               null, if there's no user sender
     * @param hasAttachment True, if the message has an
     *                      attachment, false otherwise
     * @return The formatted component
     */
    static Component mailReceived(Component text, @Nullable User sender, boolean hasAttachment) {
        Component initalMessage;

        // If there's a sender, format the message to include
        // the sender's display name, otherwise, just
        // set the initial message to be the default
        // mail received message
        if (sender == null) {
            initalMessage = MAIL_RECEIVED;
        } else {
            initalMessage = format("&e{0, user}&r sent you mail!",
                    NamedTextColor.GOLD, sender
            );
        }

        // Add a hover event and click event and return
        initalMessage =  initalMessage
                .hoverEvent( // Hover displays message
                        format("Message: '{0}'\nClick to mark as read",
                                text
                        )
                )
                // Click event marks mail as read
                .clickEvent(runCommand("/mail mark_read 1"));

        if (hasAttachment) {
            return initalMessage
                    .append(Component.newline())
                    .append(
                            text("Message has items!", NamedTextColor.YELLOW)
                                    .hoverEvent(text("Click to claim!"))
                                    .clickEvent(runCommand("/mail claim 1"))
                    );
        }

        return initalMessage;
    }

    /**
     * Creates a message to tell a user they sent a
     * mail message to the given user
     * @param target The target of the mail
     * @param text The mail's content
     * @return The formatted message
     */
    static Component mailSent(User target, Component text) {
        return format("Sent mail to &6{0, user}&r: '{1}'",
                NamedTextColor.YELLOW,
                target, text.color(NamedTextColor.WHITE)
        );
    }

    /**
     * Creates a mail list header with the
     * given user's display name
     * @param user The user whose display name to use
     * @return The formatted message
     */
    static Component mailHeader(User user) {
        return format("{0, user}'s mail", user);
    }

    /**
     * Formats the given message's meta info,
     * such as the date it was sent and by
     * whom
     * @param message The message to format the metadata of
     * @return The formatted message
     */
    static Component messageMetaInfo(MailMessage message) {
        return format("Sent {0, date}.{1}",
                message.getSent(),
                message.getSender() != null ?
                        format("\nSent by {0, user}.", message.getSender())
                        : ""
        );
    }

    /**
     * Creates a message stating the given user's
     * mail was cleared
     * @param target The user whose mail was cleared
     * @return The formatted message
     */
    static Component clearedMail(User target) {
        return format("Cleared &e{0, user}&r's mail",
                NamedTextColor.GRAY, target
        );
    }

    /**
     * Creates a message stating a message was marked as
     * read/unread for the given user
     * @param target The user the message's status was changed for
     * @param read True, if the message was marked as read, false for unread
     * @return The formatted message
     */
    static Component markedReadOther(User target, boolean read) {
        return format("Marked a message as {0} for {1, user}",
                read ? NamedTextColor.YELLOW : NamedTextColor.GRAY,
                read ? "read" : "unread",
                target
        );
    }

    // ----------------------
    // --- SECTION: STAFF ---
    // ----------------------

    /**
     * Text used by {@link net.forthecrown.user.UserVanishTicker}
     * to remind vanished staff members they are vanished, so they
     * don't reveal themselves
     */
    TextComponent YOU_ARE_IN_VANISH = text("YOU ARE IN VANISH", NamedTextColor.RED);

    /**
     * Message stating the viewer was healed
     */
    TextComponent HEALED = text("You were healed!", NamedTextColor.YELLOW);

    /**
     * Message stating the viewer had their appetite satiated.
     */
    TextComponent FED = text("You were fed!", NamedTextColor.YELLOW);

    /**
     * The worst Message I've ever typed up,
     * shown to vanished staff who try to talk in normal chat
     */
    TextComponent CHAT_NO_SPEAK_VANISH = text("You are in vanish, to talky talk, cutie pie",
            NamedTextColor.GRAY, TextDecoration.BOLD
    );

    /**
     * Creates a message saying the item held by the given user was repaired
     * @param user The user holding the item
     * @return The formatted message
     */
    static Component repairedItem(User user) {
        return format("Repaired item held by &6{0, user}&r.", NamedTextColor.YELLOW, user);
    }

    /**
     * Creates a message saying the given user is being healed
     * @param target The user being healed
     * @return The formatted message
     */
    static Component healing(User target) {
        return format("Healing &6{0}&r!", NamedTextColor.YELLOW, target);
    }

    /**
     * Creates a message saying the given user was fed
     * @param target The user that was fed
     * @return The formatted message
     */
    static Component feeding(User target) {
        return format("Satiated the appetite of &6{0, user}&r!", NamedTextColor.YELLOW, target);
    }

    /**
     * Creates a message using {@link #toggleMessage(String, boolean)}
     * saying that the view is now/no longer in staff chat only mode
     * @param toggled The staff chat toggle state
     * @return The formatted message
     */
    static Component toggleStaffChatSelf(boolean toggled) {
        return toggleMessage("All chat messages will n{1} be sent to Staff Chat",
                toggled
        );
    }

    /**
     * Creates a message saying the given user will now/no longer
     * be in staff chat only mode
     * @param user The user whose state was changed
     * @param state The staff chat toggle state
     * @return The formatted message
     */
    static Component toggleStaffChatOther(User user, boolean state) {
        return format("{0, user}'s messages will all be sent to staff chat: {1}",
                NamedTextColor.GRAY,
                user, state
        );
    }

    // ------------------------------
    // --- SECTION: EAVES DROPPER ---
    // ------------------------------

    /** "[ED] " prefix for EavesDropper messages */
    Component EAVESDROPPER_PREFIX = format("&7[&8ED&7] ");

    /**
     * Formats a {@link DirectMessage} message for
     * eaves dropper view
     *
     * @param message The message to format
     * @param mute The mute status of the message's sender, used for
     *             prepending the mute prefix onto the message
     * @return The formatted message
     */
    static Component edDirectMessage(DirectMessage message, Mute mute) {
        return format("{0}&7[&r{1} &7-> &r{2}&7] &r{3}",
                mute.getPrefix(),

                message.senderDisplayName(),
                message.targetDisplayName(),

                message.getFormattedText()
        );
    }

    /**
     * Formats a {@link MarriageMessage} for eaves dropper
     * viewing
     *
     * @param message The message to format
     * @param mute The mute status of the message's sender, used for
     *             prepending the mute prefix onto the message
     * @return The formatted message
     */
    static Component edMarriageChat(MarriageMessage message, Mute mute) {
        return format("{0} {1}{2, user} > {2}",
                MARRIAGE_PREFIX,
                mute.getPrefix(),
                message.getSender(),
                message.getFormattedText()
        );
    }

    /**
     * Formats the lines of a sign for Eaves dropper viewing
     * @param placer The player that placed the sign
     * @param signPos The position of the sign
     * @param lines The lines of the sign
     * @return The formatted message
     */
    static Component edSign(Player placer, WorldVec3i signPos, List<Component> lines) {
        return format(
                """
                {0, user} placed a sign at {1, location, -world -clickable}:
                1) {2}
                2) {3}
                3) {4}
                4) {5}
                """,
                placer,
                signPos,
                lines.get(0),
                lines.get(1),
                lines.get(2),
                lines.get(3)
        );
    }

    /**
     * Formats a chat message for eaves dropper viewing
     * @param msg The fully formatted chat message, must include
     *            the player's display name as well as their
     *            message contents
     * @param mute The mute status of the sender of the message
     * @return The formatted message
     */
    static Component edChat(Component msg, Mute mute) {
        return format("{0}{1}", msg, mute);
    }

    static Component edOreMining(Player player, Block block, int veinCount) {
        return format("&e{0, user}&r found &e{1, number} {2}&r at &e{3, location, -clickable -world}&r.",
                NamedTextColor.GRAY,
                player,
                veinCount,
                block.getType(),
                block.getLocation()
        );
    }

    /**
     * Prepends the {@link #EAVESDROPPER_PREFIX} onto
     * the given message.
     * @param msg The message to prepend the prefix onto
     * @return The formatted message
     */
    static Component edPrependPrefix(Component msg) {
        return EAVESDROPPER_PREFIX
                .append(msg);
    }

    // ----------------------------
    // --- SECTION: PUNISHMENTS ---
    // ----------------------------

    /** Message shown to hard muted users when they attempt speak */
    Component YOU_ARE_MUTED = text("You are muted!", NamedTextColor.RED);

    /** Message saying that commands cannot be used while jailed */
    Component JAIL_NO_COMMANDS = text("Cannot use commands while jailed",
            NamedTextColor.RED
    );

    /**
     * Message used to warn users when they fail
     * {@link net.forthecrown.core.admin.BannedWords#checkAndWarn(CommandSender, Component)}
     */
    Component BAD_LANGUAGE = text("Mind your tongue, you wench", NamedTextColor.RED);

    /**
     * Formats a message stating the users are
     * being separated
     * @param first The first user
     * @param second The second user
     * @return the formatted message
     */
    static Component separating(User first, User second) {
        return format("Separating {0, user} and {1, user}",
                first, second
        );
    }

    /**
     * Formats a message stating the two given
     * users are being unseparated
     * @param first The first user
     * @param second The second user
     * @return The formatted message
     */
    static Component unseparating(User first, User second) {
        return format("Unseparating {0, user} and {1, user}",
                first, second
        );
    }

    // --------------------------------
    // --- SECTION: USER PROPERTIES ---
    // --------------------------------

    /**
     * Message format for {@link net.forthecrown.commands.ToggleCommand}
     * for toggling the player's profile between public and private
     * @see Properties#PROFILE_PRIVATE
     */
    String TOGGLE_PROFILE_PRIVATE = "Others can n{1} see your profile.";

    /**
     * Message format for {@link net.forthecrown.commands.ToggleCommand}
     * for toggling if the user should ignore or see automated broadcasts.
     * @see Properties#IGNORING_ANNOUNCEMENTS
     */
    String TOGGLE_BROADCASTS = "N{1} ignoring broadcast.";

    /**
     * Message format for {@link net.forthecrown.commands.ToggleCommand}
     * for toggling weather the user can pay or be paid by others
     * @see Properties#PAY
     */
    String TOGGLE_PAYING = "You can n{1} pay others.";

    /**
     * Message format for {@link net.forthecrown.commands.ToggleCommand}
     * for toggling the player's ability to ride and be ridden by other
     * players.
     * @see Properties#PLAYER_RIDING
     */
    String TOGGLE_RIDING = "Player riding {0}";

    /**
     * Message format for {@link net.forthecrown.commands.ToggleCommand}
     * for toggling the player's ability to TPA.
     * to
     * @see Properties#TPA
     */
    String TOGGLE_TPA = "N{1} allowing TPAs";

    /**
     * Message format for {@link net.forthecrown.commands.ToggleCommand}
     * for toggling if all their messages will be sent to marriage chat
     * instead of normal chat.
     * @see Properties#MARRIAGE_CHAT
     */
    String TOGGLE_MCHAT = "Your messages will n{1} be sent to your spouse.";

    /**
     * Message format for {@link net.forthecrown.commands.ToggleCommand}
     * for toggling if the user can send and receive emotes.
     * @see Properties#EMOTES
     */
    String TOGGLE_EMOTES = "You can n{1} send or receive emotes.";

    /**
     * Message format for {@link net.forthecrown.commands.ToggleCommand}
     * for toggling if the user can propose or be proposed to by others.
     * @see Properties#ACCEPTING_PROPOSALS
     */
    String TOGGLE_MARRIAGE = "N{1} accepting marriage proposals.";

    /**
     * Message format for {@link net.forthecrown.commands.ToggleCommand}
     * for toggling if the player can send and receive region invites.
     * @see Properties#REGION_INVITING
     */
    String TOGGLE_INVITE = "Others can n{1} send you region invites.";

    /**
     * Message format for {@link net.forthecrown.commands.ToggleCommand}
     * for toggling if the player hulk smashes when visiting regions
     * @see Properties#HULK_SMASHING
     */
    String TOGGLE_HULK = "{0} hulk smashing.";

    /**
     * Message format for {@link net.forthecrown.commands.ToggleCommand}
     * for toggling god mode
     * @see Properties#GOD
     */
    String TOGGLE_GODMODE = "God mode {0}.";

    /**
     * Message format for {@link net.forthecrown.commands.ToggleCommand}
     * for toggling if the user can fly lol
     * @see Properties#FLYING
     */
    String TOGGLE_FLY = "Flying {0}.";

    /**
     * Message format for {@link ToggleCommand} for logging if the user
     * sees durability warnings
     * @see Properties#DURABILITY_ALERTS
     */
    String TOGGLE_DURABILITY_WARN = "N{1} showing item durability warnings";

    /**
     * Message format for {@link ToggleCommand} for toggling if
     * a user sees {@link DirectMessage} eaves drop reports.
     * @see Properties#EAVES_DROP_DM
     */
    String TOGGLE_EAVESDROP_DM = "N{1} spying on people's DMs.";

    /**
     * Message format for {@link ToggleCommand} for toggling if
     * a user sees {@link MarriageMessage} eaves drop reports.
     * @see Properties#EAVES_DROP_MCHAT
     */
    String TOGGLE_EAVESDROP_MCHAT = "N{1} spying on people's marriage DMs.";

    /**
     * Message format for {@link ToggleCommand} for toggling if
     * a user sees muted players' chat messages
     * @see Properties#EAVES_DROP_MUTED
     */
    String TOGGLE_EAVESDROP_MUTED = "N{1} spying on people's muted messages.";

    /**
     * Message format for {@link ToggleCommand} for toggling if
     * a user sees what people write on signs
     * @see Properties#EAVES_DROP_SIGN
     */
    String TOGGLE_EAVESDROP_SIGN = "N{1} showing what people write on signs.";

    /**
     * Formats the message to tell users they have enabled/disabled a boolean
     * {@link UserProperty}
     * <p>
     * The format is given 2 of the following arguments, which changes depending
     * on the <code>state</code> parameter:<pre>
     * Argument 0: "Enabled" or "Disabled"
     * Argument 1: "ow" or "o longer"
     * </pre>
     * The second argument doesn't have a starting 'n', this is so you can
     * decide if that letter should be capitalized yourself.
     * <p>
     * The color of the returned text also depends on the <code>state</code>
     * parameter, if it's true, the color will be yellow, otherwise it'll be
     * gray
     * @param format The message format to use.
     * @param state The new state of the property
     * @return The formatted component
     */
    static Component toggleMessage(String format, boolean state) {
        return format(format,
                state ? NamedTextColor.YELLOW : NamedTextColor.GRAY,
                /* Arg 0 */ state ? "Enabled" : "Disabled",
                /* Arg 1 */ state ? "ow" : "o longer"
        );
    }

    /**
     * Creates a message for staff when they change a boolean
     * {@link UserProperty} for another
     * user.
     * @param display The {@link ToggleCommand#getDisplayName()}
     * @param user The user the value was changed for
     * @param state The new state of the property
     * @return The formatted component
     */
    static Component toggleOther(String display, User user, boolean state) {
        return format("{0} {1} for &e{2, user}",
                NamedTextColor.GRAY,

                state ? "Disabled" : "Enabled",
                display, user
        );
    }

    // -------------------------------------
    // --- SECTION: CMD GENERAL / COMMON ---
    // -------------------------------------

    /**
     * Message shown by {@link net.forthecrown.commands.CommandSuicide}
     */
    Component CMD_SUICIDE = text("Committing suicide :(", NamedTextColor.GRAY);

    /**
     * Inventory title used by {@link net.forthecrown.commands.CommandSelfOrUser} for
     * it's <code>/bin</code> command
     */
    Component DISPOSAL = text("Disposal");

    // ------------------------------
    // --- SECTION: CMD SET SPAWN ---
    // ------------------------------

    /**
     * Format used by {@link #worldSpawnSet(Location)} and {@link #serverSpawnSet(Location)}
     * to show that a spawn location was changed.
     */
    String SET_SPAWN_FORMAT = "Set &e{0}&r spawn to &6{1, location, -w -c}&r.";

    /**
     * Creates a message stating a world's spawn location
     * was changed
     * @param loc The location the spawn was moved to
     * @return The formatted message
     */
    static Component worldSpawnSet(Location loc) {
        return format(SET_SPAWN_FORMAT, "world", loc);
    }

    /**
     * Creates a message stating a server's spawn location
     * was changed
     * @param loc The location the spawn was moved to
     * @return The formatted message
     */
    static Component serverSpawnSet(Location loc) {
        return format(SET_SPAWN_FORMAT, "server", loc);
    }

    // ----------------------------
    // --- SECTION: CMD MAP TOP ---
    // ----------------------------

    /**
     * Creates a message stating the user has the given text
     * @param unitDisplay The unit display of the user
     * @return The formatted message
     */
    static Component unitQuerySelf(Component unitDisplay) {
        return format("You have &e{0}&r.",
                NamedTextColor.GRAY, unitDisplay
        );
    }

    /**
     * Creates a message stating the given user has the given
     * text.
     * @param unitDisplay The unit display of the user
     * @param target The user
     * @return The formatted message
     */
    static Component unitQueryOther(Component unitDisplay, User target) {
        return format("&e{0, user} &rhas &6{1}&r.",
                NamedTextColor.GRAY,
                target, unitDisplay
        );
    }

    // -------------------------
    // --- SECTION: CMD WILD ---
    // -------------------------

    /**
     * Message shown to users who fail the {@link net.forthecrown.commands.CommandWild}
     * hazelguard/resource world test.
     */
    Component WILD_TEST_FAIL_TEXT = text(
            "You can only do this in the resource world or at Hazelguard." +
                    "\nThe portal to get there is in Hazelguard.",
            NamedTextColor.GRAY
    );

    /**
     * Message shown to users who use the {@link net.forthecrown.commands.CommandWild}
     * command in the Resource World.
     */
    Component WILD_RW_TEXT = text("You've been teleported, do ")
            .color(NamedTextColor.GRAY)
            .append(text("[warp portal]")
                    .color(NamedTextColor.YELLOW)
                    .clickEvent(runCommand("/warp portal"))
                    .hoverEvent(text("Warps you to the portal"))
            )
            .append(text(" to get back"));

    // -----------------------------
    // --- SECTION: CMD NICKNAME ---
    // -----------------------------

    /**
     * Message stating the viewer's nickname was cleared
     */
    Component NICK_CLEARED = text("Cleared nickname", NamedTextColor.GRAY);

    /**
     * Creates a message stating the viewer set their nickname to
     * the given nick
     * @param nick The set nickname
     * @return The formatted message
     */
    static Component nickSetSelf(Component nick) {
        return format("Nickname set to '&f{0}&r'", NamedTextColor.GRAY, nick);
    }

    /**
     * Creates a message stating the given user's nickname was
     * set to the given value
     * @param user The user whose nickname was changed
     * @param nick The value their nick was set to
     * @return The formatted message
     */
    static Component nickSetOther(User user, Component nick) {
        return format("Set &e{0}&r's nickname to '&f{1}&r'",
                NamedTextColor.GRAY,
                user.displayName(), nick
        );
    }

    /**
     * Creates a message stating the user's given
     * nickname was cleared
     * @param user The user whose nickname was cleared
     * @return The formatted message
     */
    static Component nickClearOther(User user) {
        return format("Cleared &e{0, user}&r's nickname.",
                NamedTextColor.GRAY, user
        );
    }

    // ---------------------------
    // --- SECTION: CMD NEARBY ---
    // ---------------------------

    /**
     * Header for the <code>/near</code> command
     */
    Component NEARBY_HEADER = text("Nearby players: ", NamedTextColor.GOLD);

    // -------------------------
    // --- SECTION: CMD LIST ---
    // -------------------------

    /**
     * Suffix to display for players on the <code>/list</code> command that
     * are hidden, aka, vanished.
     */
    Component VANISHED_LIST_SUFFIX = text(" [Hidden]", NamedTextColor.GRAY);

    /**
     * Creates a header for the <code>/list</code> command
     * @param userCount The amount of online users
     * @return The formatted message
     */
    static Component listHeader(int userCount) {
        return format("There are &6{0, number}&r out of &6{1, number}&r players online.",
                NamedTextColor.YELLOW,
                userCount, Bukkit.getMaxPlayers()
        );
    }

    /**
     * Lists all players in the given collection. This will
     * also prepend 'Players: ' onto the front of that list.
     * @param users The users to list, vanished or afk players
     *              will not be ignored
     * @return The formatted component
     */
    static Component listPlayers(Collection<User> users) {
        return TextJoiner.onComma()
                .setColor(NamedTextColor.YELLOW)
                .setPrefix(text("Players: "))

                // Add users
                .add(users.stream()
                        .map(user -> {
                            var text = text()
                                    .color(NamedTextColor.WHITE);

                            text.append(user.displayName());

                            if (user.get(Properties.VANISHED)) {
                                text.append(VANISHED_LIST_SUFFIX);
                            }

                            if (user.isAfk()) {
                                text.append(AFK_SUFFIX);
                            }

                            return text.build();
                        })
                )

                // Return result
                .asComponent();
    }

    // --------------------------------
    // --- SECTION: CMD IGNORE LIST ---
    // --------------------------------

    /**
     * The title header of the ignored players list
     */
    Component IGNORE_LIST_HEADER = text("Ignored players: ", NamedTextColor.GOLD);

    /**
     * Lists all blocked users
     * @param users The users to list
     * @return The formatted component
     */
    static Component listBlocked(Collection<UUID> users) {
        return joinIds(users, IGNORE_LIST_HEADER);
    }

    static Component joinIds(Collection<UUID> uuids, Component header) {
        return TextJoiner.onComma()
                .setColor(NamedTextColor.GOLD)
                .setPrefix(header)
                .add(
                        uuids.stream()
                                .map(uuid -> {
                                    var user = Users.get(uuid);
                                    user.unloadIfOffline();

                                    return user.displayName()
                                            .color(NamedTextColor.YELLOW);
                                })
                )
                .asComponent();
    }

    // -------------------------
    // --- SECTION: CMD HELP ---
    // -------------------------

    Component BANK_HELP_MESSAGE = text()
            .append(text("Bank info:").color(NamedTextColor.YELLOW))
            .append(Component.newline())

            .append(text("The Bank ").color(NamedTextColor.YELLOW))
            .append(text("can provide you with extra items in your adventure on "))
            .append(text("FTC").color(NamedTextColor.GOLD)).append(text("."))
            .append(Component.newline())
            .append(text("To enter the bank, you need a "))
            .append(text("[Bank Ticket]")
                    .color(NamedTextColor.AQUA))
            .append(text(" earned by voting for the server with"))
            .append(text(" /vote.")
                    .color(NamedTextColor.GOLD)
                    .hoverEvent(HoverEvent.showText(text("Click here to vote for the server")))
                    .clickEvent(runCommand("/vote")))
            .append(Component.newline())
            .append(text("Entering "))
            .append(text("the vault ").color(NamedTextColor.YELLOW))
            .append(text("will consume the ticket, allowing you to "))
            .append(text("loot the chests ").color(NamedTextColor.YELLOW))
            .append(text("for a short period of time."))

            .build();

    Component IP_HELP_MESSAGE = text("Server's IP:")
            .color(NamedTextColor.YELLOW)
            .append(Component.newline())
            .append(
                    text("mc.forthecrown.net (Click to copy)")
                            .color(NamedTextColor.AQUA)
                            .hoverEvent(text("Click to copy"))
                            .clickEvent(ClickEvent.copyToClipboard("mc.forthecrown.net"))
            );

    Component DYNMAP_HELP_MESSAGE = Component.text("The server's dynmap:", NamedTextColor.GRAY)
            .append(Component.newline())
            .append(Component.text("mc.forthecrown.net:3140/")
                    .color(NamedTextColor.AQUA)
                    .clickEvent(ClickEvent.openUrl("http://mc.forthecrown.net:3140/"))
                    .hoverEvent(Messages.CLICK_ME)
            );

    Component POLEHELP_MESSAGE = Component.text()
            .append(Component.text("Info about poles: ").color(NamedTextColor.YELLOW))

            .append(line("Use", "findpole", "to find the closest pole"))
            .append(line("Use", "visit", "to travel between them"))
            .append(line("Use", "movein", "to make a pole your home"))
            .append(line("Then use", "home", "to go there"))
            .build();

    private static Component line(String pre, String cmd, String post) {
        return Component.text()
                .append(Component.newline())
                .append(Component.text(pre))
                .append(Component.text(" [" + cmd + "] ")
                        .color(NamedTextColor.YELLOW)
                        .hoverEvent(Component.text("Click me :D"))
                        .clickEvent(ClickEvent.suggestCommand("/" + cmd))
                )
                .append(Component.text(post + "."))
                .build();
    }

    // ---------------------------------------
    // --- SECTION: CMD WITHDRAW / DEPOSIT ---
    // ---------------------------------------

    String WITHDRAW_FORMAT_SINGLE = "You got &e{0}&r that's worth &6{1}&r.";

    String WITHDRAW_FORMAT_MULTIPLE = "You got &e{0}&r that are worth &6{1}&r.";

    static Component withdrew(int items, int earned) {
        String format = items == 1 ? WITHDRAW_FORMAT_SINGLE : WITHDRAW_FORMAT_MULTIPLE;

        return format(format,
                NamedTextColor.GRAY,
                UnitFormat.coins(items), earned
        );
    }

    static Component deposit(int coins, int earned) {
        return format("You deposited &e{0}&r and received &6{1, rhines}&r.",
                NamedTextColor.GRAY,
                UnitFormat.coins(coins), earned
        );
    }

    // ---------------------------------
    // --- SECTION: CMD BECOME BARON ---
    // ---------------------------------

    static Component becomeBaronConfirm(String cmd) {
        return format("Are you sure you wish to become a &e{0}&r?" +
                        "It will cost &6{1, rhines}&r.\n{2}",
                NamedTextColor.GRAY,
                RankTitle.BARON.getTruncatedPrefix(),
                Vars.baronPrice,
                confirmButton(cmd)
        );
    }

    static Component becomeBaron() {
        return format("Congratulations! &7You are now a {0}!",
                NamedTextColor.GOLD,
                RankTitle.BARON.getTruncatedPrefix()
        );
    }

    // --------------------------
    // --- SECTION: CMD SIGNS ---
    // --------------------------

    static Component setLine(int line, Component value) {
        if (value == empty()) {
            return format("Cleared line &e{0}&r.",
                    NamedTextColor.GRAY,
                    line
            );
        }

        return format("Line &e{0}&r set to '&f{1}&r'",
                NamedTextColor.GRAY,
                line, value
        );
    }

    // -------------------------
    // --- SECTION: CMD SUDO ---
    // -------------------------

    static Component sudoCommand(User target, String cmd) {
        return format("Forcing &e{0, user}&r to run '&f{1}&r'",
                NamedTextColor.GRAY,
                target, cmd
        );
    }

    static Component sudoChat(User target, String chat) {
        return format("Forcing &e{0, user}&r to say '&f{1}&r'",
                NamedTextColor.GRAY,
                target, chat
        );
    }

    // ------------------------------
    // --- SECTION: CMD GAMEMODES ---
    // ------------------------------

    Component GAME_MODE_SELF = text("own", NamedTextColor.GOLD);

    static Component gameModeChangedSelf(GameMode mode) {
        return format("Set {0} gamemode to &e{1}&r.",
                NamedTextColor.GRAY,
                GAME_MODE_SELF,
                Component.translatable(mode)
        );
    }

    static Component gameModeChangedOther(User target, GameMode mode) {
        return format("Set {0, user}'s gamemode to &e{1}&r.",
                NamedTextColor.GRAY,
                target,
                Component.translatable(mode)
        );
    }

    static Component gameModeChangedTarget(CommandSource changer, GameMode mode) {
        return format("&6{0}&r changed your gamemode to &e{1}&r.",
                NamedTextColor.GRAY,
                Text.sourceDisplayName(changer),
                Component.translatable(mode)
        );
    }

    // ------------------------
    // --- SECTION: CMD PAY ---
    // ------------------------

    static Component paidMultiple(int paid, int amount) {
        return format("Paid {0, number} players {1, rhines}, total: {2, rhines}",
                paid, amount,
                paid * amount
        );
    }

    static Component payTarget(User sender, int amount, @Nullable Component message) {
        if (message == null) {
            return format("You received &6{1, rhines}&r from &e{0, user}&r.",
                    NamedTextColor.GRAY,
                    sender, amount
            );
        }

        return format("You received &6{1, rhines}&r from &e{0, user}&r: &f{2}",
                NamedTextColor.GRAY,
                sender, amount, message
        );
    }

    static Component paySender(User target, int amount, @Nullable Component message) {
        if (message == null) {
            return format("Paid &6{0, rhines}&r to &e{1, user}&r.",
                    NamedTextColor.GRAY,
                    amount, target
            );
        }

        return format("Paid &6{0, rhines}&r to &e{1, user}&r: &f{2}",
                NamedTextColor.GRAY,
                amount, target, message
        );
    }

    // ---------------------------------------
    // --- SECTION: ITEM MODIFIER COMMANDS ---
    // ---------------------------------------

    Component CLEARED_ENCHANTMENTS = text("Cleared all enchantments", NamedTextColor.GRAY);

    Component CLEARED_LORE = text("Cleared lore", NamedTextColor.GRAY);

    Component CLEARED_ITEM_NAME = text("Cleared item name!", NamedTextColor.GRAY);

    Component MERGED_ITEM_DATA = text("Merged item data", NamedTextColor.GRAY);

    Component REMOVED_ITEM_DATA = text("Removed item data", NamedTextColor.GRAY);

    Component CLEARED_ATTRIBUTE_MODS = text("Cleared attribute modifiers", NamedTextColor.GRAY);

    Component REMOVED_ATTRIBUTE_MOD = text("Removed attribute modifier", NamedTextColor.GRAY);

    static Component addedEnchant(Enchantment enchantment, int level) {
        return format("Added enchantment '&e{0}&r' to held item.",
                NamedTextColor.GRAY,
                enchantment.displayName(level)
        );
    }

    static Component removedEnchant(Enchantment enchantment) {
        return format("Removed enchantment '&e{0}&r' from held item.",
                NamedTextColor.GRAY,
                enchantment.displayName(1)
        );
    }

    static Component removedLoreIndex(int index) {
        return format("Removed lore at line &e{0, number}&r.",
                NamedTextColor.GRAY,
                index
        );
    }

    static Component removedLoreRange(Range<Integer> range) {
        return format("Removed lore from &e{0, number}&r to &e{1, number}&r.",
                NamedTextColor.GRAY,
                range.getMinimum(),
                range.getMaximum()
        );
    }

    static Component addedLore(Component text) {
        return format("Added '&f{0}&r' to lore",
                NamedTextColor.GRAY,
                text
        );
    }

    static Component setItemName(Component name) {
        return format("Set item name to '&f{0}&r'",
                NamedTextColor.GRAY,
                name
        );
    }

    static Component addedAttributeModifier(Attribute attr, AttributeModifier mod) {
        var hover = text(
                mod.toString()
                        .replaceAll(", ", "\n")
                        .replaceAll("AttributeModier\\{", "Modifier data:{\n")
                        .replaceAll("}", "\n}")
        );

        return format("Added modifier for attribute '&e{0}&r'",
                NamedTextColor.GRAY,
                Component.text(attr.key().asString())
                        .hoverEvent(hover)
        );
    }

    // ---------------------------
    // --- SECTION: CMD EMOTES ---
    // ---------------------------

    // --- BONK ---

    Component EMOTE_BONK_COOLDOWN = text("Calm down there buckaroo, don't go 'round bonkin so much");

    Component EMOTE_BONK_SELF = format("Don't hurt yourself {0}", HEART);

    static Component bonkSender(User target) {
        return format("You bonked &e{0, user}&r!", target);
    }

    static Component bonkTarget(User sender) {
        return format("{0, user} bonked you!", sender)
                .hoverEvent(text("Bonk them back!"))
                .clickEvent(runCommand("/bonk " + sender.getName()));
    }

    // --- HUG ---

    Component EMOTE_HUG_COOLDOWN = format("{0} You're too nice of a person! {0}", HEART);

    Component EMOTE_HUG_SELF = format(
            "It's alright to love yourself {0}" +
                    "\nWe've all got to love ourselves {0}",
            HEART
    );

    static Component hugReceived(User target) {
        return format("&e{0, user}&e has already received some love lol", target);
    }

    static Component hugSender(User target) {
        return format("{0} You hugged &e{1, user}&r {0}",
                HEART, target
        );
    }

    static Component hugTarget(User sender, boolean sendBack) {
        var initial = format("{0} &e{1, user}&r hugged you! {0}", HEART, sendBack);

        if (sendBack) {
            initial = initial
                    .hoverEvent(text("Click to hug them back!"))
                    .clickEvent(runCommand("/hug " + sender.getName()));
        }

        return initial;
    }

    // --- JINGLE ---

    Component EMOTE_JINGLE_COOLDOWN = text("You jingle people too often lol");

    static Component jingleSender(User target) {
        return format("You sent &e{0, user}&r a sick Christmas beat!", target);
    }

    static Component jingleTarget(User sender, boolean sendBack) {
        var initial = format("&e{0, user}&r sent you a sick Christmas beat!", sender);

        if (sendBack) {
            initial = initial
                    .hoverEvent(text("Click to send some jingles back!"))
                    .clickEvent(runCommand("/jingle " + sender.getName()));
        }

        return initial;
    }

    // --- POKE ---

    Component[] EMOTE_POKE_PARTS = {
            text("Stomach"),
            text("Back"),
            text("Arm"),
            text("Butt"),
            text("Cheek"),
            text("Neck"),
            text("Belly")
    };

    Component EMOTE_POKE_SELF = text("You poked yourself... weirdo");

    Component EMOTE_POKE_COOLDOWN = text("You poke people too often lol");

    static Component pokeSender(User target, Component bodyPart) {
        return format("You poked &e{0}&r's {1}.",
                target, bodyPart
        );
    }

    static Component pokeTarget(User sender, Component bodyPart) {
        return format("&e{0}&r poke your {1}.",
                sender, bodyPart
        );
    }

    // --- SCARE ---

    Component EMOTE_SCARE_COOLDOWN = text("D: Too scary! Take a lil break!");

    static Component scareSender(User target) {
        return format("You scared &e{0, user}&r!", target);
    }

    static Component scareTarget(User sender, boolean scareBack) {
        var initial = format("&e{0, user}&r scared you!", sender);

        if (scareBack) {
            initial = initial
                    .hoverEvent(text("Click to scare them back! >:)"))
                    .clickEvent(runCommand("/scare " + sender.getName()));
        }

        return initial;
    }

    // --- SMOOCH ---

    Component EMOTE_SMOOCH_SELF = format("&eLove yourself!&r ( ^ 3^) ❤")
            .hoverEvent(text("You're amazing! ʕっ•ᴥ•ʔっ"));

    Component EMOTE_SMOOCH_COOLDOWN = text("You kiss too much lol");

    static Component smoochSender(User target) {
        return format("{0} You smooched &e{1, user}&r! {0}",
                HEART, target
        );
    }

    static Component smoochTarget(User sender) {
        return format("{0} &e{1, user}&r smooched you! {0}",
                HEART, sender
        )
                .hoverEvent(text("Click to smooch back!"))
                .clickEvent(runCommand("/smooch " + sender.getName()));
    }


    // ---------------------------
    // --- SECTION: SIGN SHOPS ---
    // ---------------------------

    String BOUGHT = "bought";

    String SOLD = "sold";

    Component SHOP_CANNOT_DESTROY = text("You cannot destroy a shop you do not own.", NamedTextColor.GRAY);

    Component SHOP_HISTORY_TITLE = text("Shop history", NamedTextColor.YELLOW);

    Component WG_CANNOT_MAKE_SHOP = format("&c&lHey! &rShop creation is disabled here!", NamedTextColor.GRAY);

    Component SHOP_CREATE_FAILED = text("Shop creation failed! ", NamedTextColor.DARK_RED)
            .append(text("No item in inventory!", NamedTextColor.RED));

    static Component formatShopHistory(HistoryEntry entry, ItemStack exampleItem) {
        return format("&e{0, user} &r{1} &6{2, number} {3, item}&r for &6{4, rhines}&r, date: &e{5, date}",
                NamedTextColor.GRAY,
                entry.customer(),
                entry.wasBuy() ? BOUGHT : SOLD,
                entry.amount(),
                exampleItem,
                entry.earned(),
                entry.date()
        );
    }

    static Component setShopType(ShopType type) {
        return format("This shop is now a {0} shop.",
                NamedTextColor.GRAY, type.getStockedLabel()
        );
    }

    static Component shopTransferSender(User target) {
        return format("Transferred shop to &e{0, user}&r.",
                NamedTextColor.GRAY,
                target
        );
    }

    static Component shopTransferTarget(User sender, SignShop shop) {
        return format("{0, user} has transferred a shop to you.\nLocated at: {1, location}.",
                NamedTextColor.GRAY,
                sender, shop.getPosition()
        );
    }

    static Component shopEditAmount(int amount) {
        return format("Set shop item amount to &e{0, number}&r.",
                NamedTextColor.GRAY, amount
        );
    }

    static Component shopEditPrice(int price) {
        return format("Set shop price to &e{0, rhines}&r.",
                NamedTextColor.GRAY, price
        );
    }

    static Component sessionEndOwner(SignShopSession session) {
        String format = session.getType().isBuyType() ?
                "&e{0, user}&r bought &6{1}&r from your shop at &e{2, location}&r for &e{3, rhines}&r."
                : "&e{0, user}&r sold &6{1}&r to your shop at &e{2, location}&r for &e{3, rhines}&r.";

        return format(format, NamedTextColor.GRAY,
                session.getCustomer(),
                Text.itemAndAmount(session.getExampleItem(), session.getAmount()),
                session.getShop().getPosition(),
                session.getTotalEarned()
        );
    }

    static Component sessionEndCustomer(SignShopSession session) {
        String format = session.getType().isBuyType() ?
                "Bought a total of &6{0}&r for &e{1, rhines}&r."
                : "Sold a total of &6{0}&r for &e{1, rhines}&r.";

        return format(format, NamedTextColor.GRAY,
                Text.itemAndAmount(session.getExampleItem(), session.getAmount()),
                session.getTotalEarned()
        );
    }

    static Component stockIssueMessage(ShopType type, WorldVec3i pos) {
        return format("Your shop at &e{0, location}&r is {1}.",
                NamedTextColor.GRAY,
                pos, type.isBuyType() ? "out of stock" : "full"
        );
    }

    static Component sessionInteraction(SignShopSession session) {
        String format = session.getType().isBuyType() ?
                "Bought &e{0, item}&r for &6{1, rhines}&r."
                : "Sold &e{0, item}&r for &6{1, rhines}&r.";

        return format(format, NamedTextColor.GRAY,
                session.getExampleItem(),
                session.getPrice()
        );
    }

    static Component createdShop(SignShop shop) {
        return format("&aSign shop created!&r It'll {0} {1, item, -amount} for {2, rhines}." +
                "\n&7Use {3}+{4} to edit the shop inventory.",

                !shop.getType().isBuyType() ? "buy" : "sell",
                shop.getExampleItem(),
                shop.getPrice(),

                keybind(Keybinds.SNEAK),
                keybind(Keybinds.USE)
        );
    }

    // ------------------------
    // --- SECTION: REGIONS ---
    // ------------------------

    Component HOME_REGION_SET = text(
            "Set home region." +
            "\nUse /home or /homepost to come to this region." +
            "\nUse /invite <player> to invite others there",
            NamedTextColor.YELLOW
    );

    static Component inviteCancelled(User target) {
        return format("Cancelled invite sent to &e{0, user}&r.",
                NamedTextColor.GRAY, target
        );
    }

    static Component senderInvited(User target) {
        return format("Invited &e{0, user}&r.",
                NamedTextColor.GOLD, target
        );
    }

    static Component targetInvited(User sender) {
        return format("&e{0, user}&r has invited you to their region.",
                NamedTextColor.GOLD, sender
        );
    }

    static Component invitedTotal(int count) {
        return format("Invited a total of &e{0, number}&r people.",
                NamedTextColor.GOLD, count
        );
    }

    static Component listRegions(Collection<PopulationRegion> namedRegions) {
        return format("&eRegions: &r{0}.",
                TextJoiner.onComma()
                        .add(namedRegions.stream()
                                .map(region -> {
                                    return region.displayName()
                                            .color(NamedTextColor.AQUA);
                                })
                        )
        );
    }

    static Component whichRegionNamed(PopulationRegion region) {
        return format("You're currently in the {0} region.",
                NamedTextColor.GOLD,
                region.displayName()
                        .colorIfAbsent(NamedTextColor.YELLOW)
        );
    }

    // -------------------------
    // --- SECTION: MARRYING ---
    // -------------------------

    Component HAZELGUARD_CHAPEL = text("The chapel in Hazelguard", NamedTextColor.YELLOW);

    Component PRIEST_TEXT_CONFIRM = text("[I do]", NamedTextColor.AQUA)
            .hoverEvent(CLICK_ME);

    Component PRIEST_TEXT_WAITING = text("Now your spouse will have to accept.",
            NamedTextColor.YELLOW
    );

    Component PRIEST_TEXT_MARRY = text("[Marry someone]", NamedTextColor.AQUA)
            .hoverEvent(CLICK_ME);

    Component PRIEST_TEXT = text("Welcome to the church. What can I help you with?",
            NamedTextColor.YELLOW
    );

    static Component proposeSender(User target) {
        return format("Proposed to &e{0, user}&r.", NamedTextColor.GOLD, target);
    }

    static Component proposeTarget(User sender) {
        return format("&e{0, user}&r has proposed to you! {1} {2}",
                NamedTextColor.GOLD,
                sender,
                acceptButton("/marryaccept " + sender.getName()),
                denyButton("/marrydeny")
        );
    }

    static Component confirmDivorce(User spouse) {
        return format("Are you sure you wish to divorce &e{0, user}&r? {1}",
                NamedTextColor.GRAY,
                spouse,
                confirmButton("/divorce confirm")
        );
    }

    static Component proposeDenySender(User target) {
        return format("&e{0, user}&r denied your proposal :(",
                NamedTextColor.GRAY, target
        );
    }

    static Component proposeDenyTarget(User sender) {
        return format("Denied &e{0, user}&r's proposal.",
                NamedTextColor.GRAY, sender
        );
    }

    static Component proposeAcceptSender() {
        return format("Accepted marriage proposal! Hurry to {0} to complete the ritual!",
                NamedTextColor.GRAY, HAZELGUARD_CHAPEL
        );
    }

    static Component proposeAcceptTarget(User sender) {
        return format("&e{0, user}&r accepted your marriage proposal! Hurry to {1} to complete the ritual!",
                NamedTextColor.GRAY, sender, HAZELGUARD_CHAPEL
        );
    }

    static Component priestTextConfirm(User sender, User target) {
        return format("Do you, &6{0, user}&r, take &6{1, user}&r to be your lawfully wed spouse?",
                NamedTextColor.YELLOW,
                sender, target
        );
    }

    // ------------------------
    // --- SECTION: MARKETS ---
    // ------------------------

    String MEMBER_EDIT_FORMAT = "Players trusted in your shop can n{1} edit sign shops.";

    TextComponent EVICTION_CANCELLED = text("You shop eviction was cancelled :D",
            NamedTextColor.YELLOW
    );

    TextComponent APPEAL_BUTTON = text("[Appeal]", NamedTextColor.AQUA)
            .hoverEvent(CLICK_ME)
            .clickEvent(runCommand("/marketappeal"));

    TextComponent MARKET_BOUGHT = text("You bought this shop!", NamedTextColor.YELLOW);

    Component UNCLAIM_CONFIRM = format(
            "Are you sure you wish to unclaim your shop? {0}" +
            "\n&cEverything inside will be removed and you won't be able to get it back",
            NamedTextColor.GRAY,

            confirmButton("/unclaimshop confirm")
    );

    TextComponent MARKET_UNCLAIMED = text("You have unclaimed your shop and it has been reset",
            NamedTextColor.GRAY
    );

    TextComponent MARKET_EVICT_INACTIVE = text("You have been too inactive!", NamedTextColor.GRAY);

    TextComponent MARKET_EVICT_STOCK = text("Too many shops out of stock for too long!", NamedTextColor.RED);

    TextComponent MARKET_APPEALED_EVICTION = text("Eviction appealed!", NamedTextColor.YELLOW);

    static Component shopTrustSender(User target) {
        return format("Trusted &e{0, user}&r.",
                NamedTextColor.GOLD, target
        );
    }

    static Component shopTrustTarget(User sender) {
        return format("&e{0, user}&r has trusted you in their shop.",
                NamedTextColor.GOLD, sender
        );
    }

    static Component shopUntrustSender(User target) {
        return format("Untrusted &e{0, user}&r.",
                NamedTextColor.GOLD, target
        );
    }

    static Component shopUntrustTarget(User sender) {
        return format("&e{0, user}&r has untrusted you in their shop.",
                NamedTextColor.GOLD, sender
        );
    }

    static Component evictionMail(MarketEviction eviction) {
        return format("You shop is being evicted, eviction date: &e{0, date}&r, reason: '&6{1}&r' {2}",
                NamedTextColor.GRAY,
                eviction.getEvictionTime(),
                eviction.getReason(),
                APPEAL_BUTTON
        );
    }

    static Component evictionNotice(MarketEviction eviction) {
        return format(
                "Your shop has been market for eviction, reason: '&e{0}&r'" +
                        "\nEviction date: &6{1, date}&r (in {1, time, -timestamp}) {2}",
                NamedTextColor.GRAY,

                eviction.getReason(),
                eviction.getEvictionTime(),
                APPEAL_BUTTON
        );
    }

    static Component marketTransferredSender(User target) {
        return format("Transferred your shop to &6{0, user}&r.",
                NamedTextColor.YELLOW, target
        );
    }

    static Component marketTransferredTarget(User sender) {
        return format("&6{0, user}&r transferred their shop to you!",
                NamedTextColor.YELLOW, sender
        );
    }

    static Component marketTransferConfirm(User target) {
        return format("Are you sure you wish to transfer your shop to &e{0, user}&r? {1}" +
                        "\nYou will not be able to access the shop after this and all " +
                        "trusted players will be removed.",

                NamedTextColor.GRAY,
                target,
                confirmButton("/transfershop " + target.getName() + " confirm")
        );
    }

    static Component marketUnmergeSender(User target) {
        return format("Unmerged your shop with &e{0, user}&r.",
                NamedTextColor.GRAY, target
        );
    }

    static Component marketUnmergeTarget(User sender) {
        return format("&e{0, user}&r unmerged their shop with yours",
                NamedTextColor.GRAY, sender
        );
    }

    static Component marketMergeTarget(User sender) {
        return format("&e{0, user}&r wants to merge their shop with yours. {1} {2}",
                NamedTextColor.GRAY,
                sender,

                acceptButton("/mergeshop %s accept", sender.getName()),
                denyButton("/mergeshop %s deny", sender.getName())
        );
    }

    static Component marketMerged(User sender) {
        return format("Merged your shop with &e{0, user}&r.",
                NamedTextColor.GRAY, sender
        );
    }

    static Component issuedEviction(User target, long date, Component reason) {
        return format("Issued eviction notice to {0, user}. " +
                "Will be evicted {1, date} (In {1, time, -timestamp})" +
                "\nReason: '{2}'",
                target, date, reason
        );
    }

    static Component cannotAppeal(Component reason) {
        return format("Cannot appeal eviction: '&f{0}&r'", NamedTextColor.RED, reason);
    }

    static Component tooLittleShops() {
        return format("Too few shops! &7Need at least {0, number}.",
                NamedTextColor.RED,
                Vars.markets_minShopAmount
        );
    }

    // --------------------------
    // --- SECTION: SELL SHOP ---
    // --------------------------

    Component SHOP_WEB_MESSAGE = text("Our webstore", NamedTextColor.GRAY)
            .append(newline())
            .append(
                    text("forthecrown.buycraft.net", NamedTextColor.AQUA)
                            .hoverEvent(CLICK_ME)
                            .clickEvent(openUrl("https://forthecrown.buycraft.net/"))
            );

    static Component soldItems(SellResult result, Material material) {
        return format("Sold &e{0}&r for &6{1, rhines}&r.",
                NamedTextColor.GRAY,

                Text.itemAndAmount(new ItemStack(material), result.getSold()),
                result.getEarned()
        );
    }

    static Component priceDropped(Material material, int before, int after) {
        return format("Your price for &e{0}&r dropped from &6{1, rhines}&r to &e{2, rhines}&r.",
                NamedTextColor.GRAY,
                Component.translatable(material),
                before, after
        );
    }

    // -------------------------
    // --- SECTION: DUNGEONS ---
    // -------------------------

    Component LOST_ITEMS = text("You lost a random amount of your Dungeon Items...", NamedTextColor.YELLOW);

    Component DIEGO_ERROR = text("You'll have to beat the dreaded Drawned to get the Dolphin Swimmer trident.");

    Component DIEGO_BUTTON = text("[Claim Trident]", NamedTextColor.AQUA)
            .hoverEvent(CLICK_ME);

    Component DIEGO_TEXT = text("Hello, what can I do for ya?", NamedTextColor.YELLOW);

    Component GOT_KNIGHT_RANK = format("Got {0} rank", NamedTextColor.GOLD, RankTitle.KNIGHT);

    Component DUNGEON_LORE = text("Dungeon Item");

    static Component emptyBossRoomWarning(long emptyRoomTicks) {
        return format("If the boss' room is empty for longer than {0, time}, it will despawn!",
                NamedTextColor.YELLOW,
                Time.ticksToMillis(emptyRoomTicks)
        );
    }

    // ------------------------------------
    // --- SECTION: USER PROFILE FORMAT ---
    // ------------------------------------

    Component YOUR = Component.text("Your", NamedTextColor.YELLOW);
}