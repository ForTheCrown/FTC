package net.forthecrown.economy.shops;

import net.forthecrown.economy.Economy;

/**
 * A class which handles interactions between a signshop and a player
 * <p></p>
 * Implementation: {@link FtcShopInteractionHandler}
 */
public interface ShopInteractionHandler {

    /**
     * Handles a simple interaction between a given player and a given shop
     * @param shop The shop being interacted with
     * @param player The interacting player
     * @param economy The balances, used for economy stuff
     */
    void handleInteraction(SignShop shop, ShopCustomer player, Economy economy);

    /**
     * Gets a session linked a user
     * @param user The user to get the session of
     * @return The session, or null, if the user has no current session
     */
    SignShopSession getSession(ShopCustomer user);

    /**
     * Gets or creates a session for the given user and the sign shop
     * @param user The user to create the session for
     * @param shop The shop the user is interacting with
     * @return The shop session
     */
    SignShopSession getOrCreateSession(ShopCustomer user, SignShop shop);

    /**
     * Does the session expiry cooldown thing
     * @param session The session to put on the cooldown
     */
    void doSessionExpiryCooldown(SignShopSession session);

    /**
     * Destroys the session and, if needed, logs data about the session.
     * <p></p>
     * Interaction types can also specify any piece of code they want executed when the session expires
     * with {@link SignShopSession#onSessionExpire(Runnable)}
     * @param session The session to remove and log
     */
    void removeAndLog(SignShopSession session);

    /**
     * Checks whether the given session is on the expiry cooldown or not
     * @param session The session to check for
     * @return Whether the session has been placed on the expiry cooldown.
     */
    boolean isOnExpiryCooldown(SignShopSession session);
}