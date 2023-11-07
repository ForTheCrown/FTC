package net.forthecrown.economy.market;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.Instant;
import lombok.experimental.UtilityClass;
import net.forthecrown.Worlds;
import net.forthecrown.command.Exceptions;
import net.forthecrown.economy.ShopsPlugin;
import net.forthecrown.user.TimeField;
import net.forthecrown.user.User;
import org.bukkit.World;

/**
 * Utility class for market related matters
 */
public @UtilityClass class Markets {

  /**
   * Checks if the given ownership can change status and throws an exception if they can't
   *
   * @param ownership The market ownership to check
   * @throws CommandSyntaxException If the given owner can't change status
   */
  public static void checkStatusChange(User user) throws CommandSyntaxException {
    checkStatusChange(user, "You cannot currently do this, next allowed: {0, time, -timestamp}.");
  }

  public static void checkCanPurchase(User user) throws CommandSyntaxException {
    checkStatusChange(user, "Cannot purchase shop right now, allowed in: {0, time, -timestamp}.");
  }

  public static void checkStatusChange(User user, String format) throws CommandSyntaxException {
    long lastAction = user.getTime(TimeField.MARKET_LAST_ACTION);
    var config = ShopsPlugin.getPlugin().getShopConfig();

    Instant lastActionInst = Instant.ofEpochMilli(lastAction);
    Instant nextAllowed    = lastActionInst.plus(config.getMarketActionCooldown());

    if (Instant.now().isAfter(nextAllowed)) {
      return;
    }

    throw Exceptions.format(format, nextAllowed);
  }

  public static boolean canChangeStatus(User user) {
    long lastAction = user.getTime(TimeField.MARKET_LAST_ACTION);
    var config = ShopsPlugin.getPlugin().getShopConfig();

    Instant lastActionInst = Instant.ofEpochMilli(lastAction);
    Instant nextAllowed    = lastActionInst.plus(config.getMarketActionCooldown());

    return Instant.now().isAfter(nextAllowed);
  }

  /**
   * Tests if this user currently owns a shop
   *
   * @param user The user to test
   * @return True if the user directly owns a market shop
   */
  public static boolean ownsShop(User user) {
    return ShopsPlugin.getPlugin().getMarkets().get(user.getUniqueId()) != null;
  }

  /**
   * Gets the world the markets are located in
   *
   * @return The market world
   */
  public static World getWorld() {
    return Worlds.overworld();
  }
}