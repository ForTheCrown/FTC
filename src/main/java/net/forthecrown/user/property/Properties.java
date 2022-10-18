package net.forthecrown.user.property;

import net.forthecrown.core.registry.Registries;
import net.forthecrown.user.User;
import net.forthecrown.user.data.SellAmount;
import net.kyori.adventure.text.Component;

/**
 * A class which stores the {@link UserProperty} instances as
 * constants.
 */
public class Properties {
    /**
     * The amount a user will sell in /shop.
     */
    public static final EnumProperty<SellAmount>
    SELL_AMOUNT             = new EnumProperty<>("sellAmount",       SellAmount.PER_1);

    /**
     * Determines whether a user is allowed to send
     * and receive command emotes such as '/kiss'.
     */
    public static final BoolProperty
    EMOTES                  = new BoolProperty("emotes",             true),

    /**
     * Determines whether the given user sees
     * automated announcements or not.
     */
    IGNORING_ANNOUNCEMENTS  = new BoolProperty("ignoringBroadcasts", false),

    /**
     * Determines whether a user will instantly
     * teleport between poles or fly into the sky
     * and then drop down lol.
     */
    HULK_SMASHING           = new BoolProperty("hulkSmashing",       true),

    /**
     * Determines whether others can see a users
     * profile.
     */
    PROFILE_PRIVATE         = new BoolProperty("profilePrivate",     false),

    /**
     * Determines if a player can TPA to other players
     * or have other players TPA to them.
     */
    TPA                     = new BoolProperty("tpa",                true),

    /**
     * Determines if a player can pay other players
     * and be paid by other players.
     */
    PAY                     = new BoolProperty("paying",             true),

    /**
     * Determines whether a user can send and receive
     * region invites.
     */
    REGION_INVITING         = new BoolProperty("regionInvites",      true),

    /**
     * Determines if a user has marriage chat toggled on.
     * <p>
     * If true, all of the player's messages will go to
     * their spouse only, not the general chat.
     */
    MARRIAGE_CHAT           = new BoolProperty("marriageChatToggle", false),

    /**
     * Determines if a user can send and receive marriage
     * proposals.
     */
    ACCEPTING_PROPOSALS     = new BoolProperty("acceptingProposals", false),

    /**
     * Determines if a user sees a muted user's chat messages
     * with {@link net.forthecrown.core.admin.EavesDropper}
     * <p>
     * Only affects users with {@link net.forthecrown.core.Permissions#EAVESDROP}
     */
    EAVES_DROP_MUTED        = new BoolProperty("eavesDrop_muted",    false),

    /**
     * Determines if a user sees other people's direct messages through
     * {@link net.forthecrown.core.admin.EavesDropper}
     * <p>
     * Only affects users with {@link net.forthecrown.core.Permissions#EAVESDROP}
     */
    EAVES_DROP_DM           = new BoolProperty("eavesDrop_dm",       false),

    /**
     * Determines if a user sees what people write on signs
     * with {@link net.forthecrown.core.admin.EavesDropper}
     * <p>
     * Only affects users with {@link net.forthecrown.core.Permissions#EAVESDROP}
     */
    EAVES_DROP_SIGN         = new BoolProperty("eavesDrop_signs",    false),

    /**
     * Determines if a user sees what other married users send to
     * each other through {@link net.forthecrown.user.MarriageMessage}
     * with {@link net.forthecrown.core.admin.EavesDropper}
     * <p>
     * Only affects users with {@link net.forthecrown.core.Permissions#EAVESDROP}
     */
    EAVES_DROP_MCHAT        = new BoolProperty("eavesDrop_mChat",    false),

    /**
     * Determines if a user sees eaves dropper messages when other
     * users mine into veins. Used by {@link net.forthecrown.core.admin.EavesDropper}
     * <p>
     * Only affects uses with {@link net.forthecrown.core.Permissions#EAVESDROP}
     */
    EAVES_DROP_MINING       = new BoolProperty("eavesDrop_mining",   false),

    /**
     * /shop property, determines if the selling system
     * should also sell named items, or ignore them.
     */
    SELLING_NAMED_ITEMS     = new BoolProperty("sellingNamedItems",  false),

    /**
     * /shop property, determines if the selling system
     * should also sell items with lore or ignore them.
     */
    SELLING_LORE_ITEMS      = new BoolProperty("sellingLoreItems",   false),

    /**
     * /shop property, determines whether the user is selling items in
     * their compact form or not
     */
    SELLING_COMPACTED       = new BoolProperty("sellingCompact",    false),

    /**
     * Determines if a user should be shown other users' staff notes when
     * they join.
     * <p>
     * This property only affects uses that have the staff permissions,
     * it will not affect regular users.
     */
    VIEWS_NOTES             = new BoolProperty("viewsNotes",         true),

    /**
     * Determines if a user receives alerts when their gear or items
     * pass a certain durability threshold.
     * @see net.forthecrown.events.player.DurabilityListener
     */
    DURABILITY_ALERTS       = new BoolProperty("durabilityAlerts",   true);

    /**
     * Determines if a player can ride other players
     * or be ridden by other players.
     */
    public static final BoolProperty PLAYER_RIDING = new BoolProperty("playerRiding", true) {
        @Override
        public void onUpdate(User user) {
            user.updateRiding();
        }
    };

    /**
     * Determines a player's TAB prefix.
     */
    public static final TextProperty PREFIX = new TextProperty("prefix", Component.empty()) {
        @Override
        public void onUpdate(User user) {
            user.updateTabName();
        }
    };

    /**
     * Determines a player's TAB suffix
     */
    public static final TextProperty SUFFIX = new TextProperty("suffix", Component.empty()) {
        @Override
        public void onUpdate(User user) {
            user.updateTabName();
        }
    };

    /**
     * Determines the display name shown in the TAB menu, will be
     * {@link Component#empty()} if not set. If not set, the user's
     * TAB display name will simply be their username or nickname
     */
    public static final TextProperty TAB_NAME = new TextProperty("tabName", Component.empty()) {
        @Override
        public void onUpdate(User user) {
            user.updateTabName();
        }
    };

    /**
     * Determines if a player is vanished or not
     */
    public static final BoolProperty VANISHED = new BoolProperty("vanished", false) {
        @Override
        public void onUpdate(User user) {
            user.updateVanished();
        }
    };

    /**
     * Determines if a player is invincible or not
     */
    public static final BoolProperty GOD = new BoolProperty("godMode", false) {
        @Override
        public void onUpdate(User user) {
            user.updateGodMode();
        }
    };

    /**
     * Determines if a player can fly or not
     */
    public static final BoolProperty FLYING = new BoolProperty("flying", false) {
        @Override
        public void onUpdate(User user) {
            user.updateFlying();
        }
    };

    /** Empty initializer method to freeze property registry */
    static void init() {
        Registries.USER_PROPERTIES.freeze();
    }
}