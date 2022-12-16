package net.forthecrown.economy.shops;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jetbrains.annotations.NotNull;

/**
 * And interaction type between a player and a shop.
 * <p>
 * Tells a shop's type how to handle an interaction with a player,
 * what it needs to complete that interaction, and what will happen
 * if the checks are passed.
 * <p>
 * These interactions are available either through the {@link ShopType}
 * enum with {@link ShopType#getInteraction()} or by accessing the
 * interaction constants directly in {@link Interactions}.
 * <p>
 * Although an interaction will test some fundamental parts about a
 * shop's interaction like inventory content and if the owner/customer
 * has a valid balance, the job of telling the customer/user about
 * the purchase occurring is left to the {@link SignShopSession}.
 */
public interface ShopInteraction {
    /**
     * Tests if a session can interact with this shop.
     * <p>
     * This checks stuff like if the interacting player has enough money or if the shop even has stock
     * <p>
     * Note: This method is only considered "failed" when a CommandSyntaxException is thrown in the method,
     * otherwise it is a success
     *
     * @param session The session to check
     * @throws CommandSyntaxException If the check failed.
     */
    void test(@NotNull SignShopSession session) throws CommandSyntaxException;

    /**
     * Interacts with the shop
     * <p>
     * Does the part about giving or taking items from inventories, changing balances,
     * all the good stuff about a shop
     *
     * @param session The session that's interacting
     */
    void interact(@NotNull SignShopSession session);
}