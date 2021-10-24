package net.forthecrown.economy.shops;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.FtcFlags;
import net.forthecrown.economy.Economy;
import net.forthecrown.user.data.Faction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

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
     * Tests the session's WorldGuard flags.
     * <p></p>
     * Note: This method will send a message to the user and then return false if a check does not pass
     * <p></p>
     * Left this in this class in case there might be a need to override the method in the future for different types
     *
     * @param session The session to check
     * @return True if the flag checks passed, false if otherwise
     */
    default boolean testFlags(SignShopSession session) {
        Faction allowedOwner = FtcFlags.query(session.getShop().getLocation(), FtcFlags.SHOP_OWNERSHIP_FLAG);
        Faction allowedUser = FtcFlags.query(session.getShop().getLocation(), FtcFlags.SHOP_USAGE_FLAG);

        //If the owner's branch doesn't match up with the WG flag
        if(allowedOwner != null && session.getOwner().getFaction() != Faction.DEFAULT && !session.getShop().getType().isAdmin() && allowedOwner != session.getOwner().getFaction()){
            session.getCustomer().sendMessage(
                    Component.translatable(
                            "shops.error.flags.owner",
                            NamedTextColor.GRAY,
                            Component.text(allowedOwner.getName())
                    )
            );
            return false;
        }

        //If the user's branch doesn't match up with the WG flag
        if(allowedUser != null && session.getCustomer().getFaction() != Faction.DEFAULT && allowedUser != session.getCustomer().getFaction()){
            session.getCustomer().sendMessage(
                    Component.translatable(
                            "shops.error.flags.user",
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
