package net.forthecrown.economy.market;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.experimental.UtilityClass;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Worlds;
import net.forthecrown.economy.Economy;
import net.forthecrown.user.User;
import net.forthecrown.user.data.TimeField;
import net.forthecrown.user.data.UserMarketData;
import net.forthecrown.utils.Time;
import org.bukkit.World;

/** Utility class for market related matters */
public @UtilityClass class Markets {
    /**
     * Checks if the given ownership can change status and throws an exception
     * if they can't
     * @param ownership The market ownership to check
     * @throws CommandSyntaxException If the given owner can't change status
     */
    public static void checkStatusChange(UserMarketData ownership) throws CommandSyntaxException {
        checkStatusChange(
                ownership,
                "You cannot currently do this, next allowed: {0, time, -timestamp}."
        );
    }

    public static void checkCanPurchase(UserMarketData ownership) throws CommandSyntaxException {
        checkStatusChange(
                ownership,
                "Cannot purchase shop right now, allowed in: {0, time, -timestamp}."
        );
    }

    public static void checkStatusChange(UserMarketData ownership, String transKey) throws CommandSyntaxException {
        if (canChangeStatus(ownership.getUser())) {
            return;
        }

        long nextAllowed = ownership.getUser()
                .getTime(TimeField.MARKET_LAST_ACTION) + MarketConfig.statusCooldown;

        throw Exceptions.format(transKey, nextAllowed);
    }

    /**
     * Tests if this user currently owns a shop
     * @param user The user to test
     * @return True if the user directly owns a market shop
     */
    public static boolean ownsShop(User user) {
        return Economy.get().getMarkets().get(user.getUniqueId()) != null;
    }

    /**
     * Tests if this user's {@link MarketConfig#statusCooldown} has
     * ended or not
     * @param user The user to test
     * @return True, if the market cooldown has ended for this user
     */
    public static boolean canChangeStatus(User user) {
        return Time.isPast(
                MarketConfig.statusCooldown + user.getTime(TimeField.MARKET_LAST_ACTION)
        );
    }

    /**
     * Gets the world the markets are located in
     * @return The market world
     */
    public static World getWorld() {
        return Worlds.overworld();
    }
}