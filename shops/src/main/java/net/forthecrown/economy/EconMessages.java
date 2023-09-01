package net.forthecrown.economy;

import static net.forthecrown.text.Messages.CLICK_ME;
import static net.forthecrown.text.Messages.acceptButton;
import static net.forthecrown.text.Messages.confirmButton;
import static net.forthecrown.text.Messages.denyButton;
import static net.forthecrown.text.Text.format;
import static net.kyori.adventure.text.Component.keybind;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;

import net.forthecrown.economy.market.MarketEviction;
import net.forthecrown.economy.signshops.HistoryEntry;
import net.forthecrown.economy.signshops.ShopType;
import net.forthecrown.economy.signshops.SignShop;
import net.forthecrown.economy.signshops.SignShopSession;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.ItemStack;

public interface EconMessages {

  String MEMBER_EDIT_FORMAT = "Players trusted in your shop can n{1} edit sign shops.";

  TextComponent EVICTION_CANCELLED = text("You shop eviction was cancelled :D",
      NamedTextColor.YELLOW
  );

  TextComponent APPEAL_BUTTON = text("[Appeal]", NamedTextColor.AQUA)
      .hoverEvent(CLICK_ME)
      .clickEvent(runCommand("/marketappeal"));

  Component UNCLAIM_CONFIRM = format(
      "Are you sure you wish to unclaim your shop? {0}" +
          "\n&cEverything inside will be removed, and you won't be able to get it back",
      NamedTextColor.GRAY,

      confirmButton("/unclaimshop confirm")
  );

  TextComponent MARKET_UNCLAIMED = text("You have unclaimed your shop and it has been reset",
      NamedTextColor.GRAY
  );

  TextComponent MARKET_EVICT_INACTIVE = text("You have been too inactive!", NamedTextColor.GRAY);

  TextComponent MARKET_EVICT_STOCK = text("Too many shops out of stock for too long!",
      NamedTextColor.RED);

  TextComponent MARKET_APPEALED_EVICTION = text("Eviction appealed!", NamedTextColor.YELLOW);

  String MARKET_MERGE_BLOCKED_SENDER = "Cannot merge shops with a user you've blocked";
  String MARKET_MERGE_BLOCKED_TARGET = "Cannot merge shops with a user that's blocked you";

  String STRUST_BLOCKED_SENDER = "Cannot trust {0, user}: You've blocked them";
  String STRUST_BLOCKED_TARGET = "Cannot trust {0, user}: They've blocked you";

  static Component marketBought(int price) {
    return format("You bought this shop for &6{0, rhines}&r.",
        NamedTextColor.YELLOW,
        price
    );
  }

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
    var plugin = ShopsPlugin.getPlugin();

    return format("Too few shops! &7Need at least {0, number}.",
        NamedTextColor.RED,
        plugin.getShopConfig().getMinimumShopAmount()
    );
  }


  String BOUGHT = "bought";

  String SOLD = "sold";

  Component SHOP_CANNOT_DESTROY = text("You cannot destroy a shop you do not own.",
      NamedTextColor.GRAY);

  Component SHOP_HISTORY_TITLE = text("Shop history", NamedTextColor.YELLOW);

  Component WG_CANNOT_MAKE_SHOP
      = format("&c&lHey! &rShop creation is disabled here!", NamedTextColor.GRAY);

  Component SHOP_CREATE_FAILED = text("Shop creation failed! ", NamedTextColor.DARK_RED)
      .append(text("No item in inventory!", NamedTextColor.RED));

  Component SHOP_NO_EXAMPLE = text("This shop has no item set!", NamedTextColor.RED);

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

        keybind("key.sneak"),
        keybind("key.use")
    );
  }

  static Component formatShopHistory(HistoryEntry entry, ItemStack exampleItem) {
    return format(
        "&e{0, user} &r{1} &6{2, number} {3, item, -!amount}&r for &6{4, rhines}&r, "
            + "date: &e{5, date}",

        NamedTextColor.GRAY,
        entry.customer(),
        entry.wasBuy() ? BOUGHT : SOLD,
        entry.amount(),
        exampleItem,
        entry.earned(),
        entry.date()
    );
  }
}
