package net.forthecrown.economy;

import static net.forthecrown.command.Exceptions.create;
import static net.forthecrown.command.Exceptions.format;
import static net.forthecrown.command.Exceptions.unknown;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.economy.market.MarketDisplay;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.user.User;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.ItemStack;

public interface EconExceptions {


  /**
   * Exception which states that the viewer does not own a shop
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

  CommandSyntaxException NON_AUTO_APPEAL = create(
      "Can only appeal automatic evictions, this is a staff-created eviction");

  CommandSyntaxException ALTS_CANNOT_OWN
      = create("Alt-accounts cannot own shops");

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

  CommandSyntaxException SHOP_NO_DESC = create(
      "You must provide a description of the shop's items!");

  /**
   * Exception which states the user must be looking at a shop they own
   */
  CommandSyntaxException LOOK_AT_SHOP = create("You must be looking at a shop you own");

  /**
   * Exception which simply says 'Invalid shop'
   * <p>
   * Used by {@link net.forthecrown.commands.economy.CommandShopHistory} to state a given shop's
   * name is invalid
   */
  CommandSyntaxException INVALID_SHOP = create("Invalid shop");

  CommandSyntaxException TRANSFER_SELF = create("Cannot transfer a shop to yourself");

  static CommandSyntaxException unknownShop(StringReader reader, int cursor, String name) {
    return unknown("Shop", Readers.copy(reader, cursor), name);
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
    var config = ShopsPlugin.getPlugin().getShopConfig();
    return format("Shop price exceeded max price of {0, rhines}.", config.getMaxPrice());
  }


}
