package net.forthecrown.economy.shops;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.economy.Economy;

/**
 * And interaction type between a player and a shop.
 * <p></p>
 * Tells a shop's type how handle an interaction with a player, what it needs to complete that interaction, and
 * what will happen if the checks are passed.
 */
public interface ShopInteraction {
    /**
     * Tests if a session can interact with this shop.
     * <p></p>
     * This checks stuff like if the interacting player has enough money or if the shop even has stock
     * <p></p>
     * Note: This method is only considered "failed" when a CommandSyntaxException is thrown in the method,
     * otherwise it is a success
     *
     * @param session The session to check
     * @param economy Economy
     * @throws CommandSyntaxException If the check failed.
     */
    void test(SignShopSession session, Economy economy) throws CommandSyntaxException;

    /**
     * Interacts with the shop
     * <p></p>
     * Does the part about giving or taking items from inventories, changing balances,
     * all the good stuff about a shop
     *
     * @param session The session that's interacting
     * @param economy Balances :)
     */
    void interact(SignShopSession session, Economy economy);

    /**
     * Gets the shop type this interaction type belongs to
     * @return The type's shop type
     */
    ShopType getType();
}
