package net.forthecrown.economy.shops;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.WgFlags;
import net.forthecrown.economy.Balances;
import net.forthecrown.user.enums.Branch;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * And interaction type between a player and a shop.
 * <p></p>
 * <p>
 * Tells a shop's type how handle an interaction with a player, what it needs to complete that interaction, and
 * what will happen if the checks are passed.
 * </p>
 */
public interface ShopInteraction {
    /**
     * Tests if a session is can interact with this shop.
     * <p>This checks stuff like if the interacting player has enough money or if shop even has stock</p>
     * <p>
     * Note: This method is only considered "failed" when a CommandSyntaxException is thrown in the method,
     * otherwise it is a success
     * </p>
     * @param session The session to check
     * @param balances Economy
     * @throws CommandSyntaxException If the check failed.
     */
    void test(SignShopSession session, Balances balances) throws CommandSyntaxException;

    /**
     * Interacts with the shop
     * <p>
     * Does the part about giving or taking items from inventories, changing balances,
     * all the good stuff about a shop
     * </p>
     * @param session The session that's interacting
     * @param balances Balances :)
     */
    void interact(SignShopSession session, Balances balances);

    /**
     * Tests the session's WorldGuard flags.
     * <p>
     * Note: This method will send a message to the user and then return false if a check does not pass
     * </p> <p>
     * Left this in this class in case there might be a need to override the method in the future for different types
     * </p>
     * @param session The session to check
     * @return True if the flag checks passed, false if otherwise
     */
    default boolean testFlags(SignShopSession session) {
        Branch allowedOwner = WgFlags.query(session.getShop().getLocation(), WgFlags.SHOP_OWNERSHIP_FLAG);
        Branch allowedUser = WgFlags.query(session.getShop().getLocation(), WgFlags.SHOP_USAGE_FLAG);

        //If the owner's branch doesn't match up with the WG flag
        if(allowedOwner != null && session.getOwner().getBranch() != Branch.DEFAULT && !session.getShop().getType().isAdmin() && allowedOwner != session.getOwner().getBranch()){
            session.getUser().sendMessage(
                    Component.translatable("shops.wrongOwner",
                            NamedTextColor.GRAY,
                            Component.text(allowedOwner.getName())
                    )
            );
            return false;
        }

        //If the user's branch doesn't match up with the WG flag
        if(allowedUser != null && session.getUser().getBranch() != Branch.DEFAULT && allowedUser != session.getUser().getBranch()){
            session.getUser().sendMessage(
                    Component.translatable("shops.wrongUser",
                            NamedTextColor.GRAY,
                            Component.text(allowedUser.getName())
                    )
            );
            return false;
        }

        //Both checks passed :D
        return true;
    }

    /**
     * Gets the shop type this interaction type belongs to
     * @return The type's shop type
     */
    ShopType getType();
}
