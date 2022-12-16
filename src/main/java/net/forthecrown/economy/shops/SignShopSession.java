package net.forthecrown.economy.shops;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Messages;
import net.forthecrown.core.challenge.Challenges;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.economy.Economy;
import net.forthecrown.economy.TransactionType;
import net.forthecrown.economy.Transactions;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.log.DataLogs;
import net.forthecrown.log.LogQuery;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.text.Text;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 *
 */
public class SignShopSession {
    /**
     * The amount of time, in ticks, a {@link SignShopSession}
     * will stay alive until it `expires` and is removed
     */
    public static final int SESSION_TIME_OUT = 2 * 20;

    /**
     * The material the session's shop sells
     */
    @Getter private final Material material;

    /**
     * The session's shop
     */
    @Getter private final SignShop shop;

    /**
     * The shop's type
     */
    @Getter private final ShopType type;

    /**
     * The shop's inventory
     */
    @Getter
    private final Inventory shopInventory;

    /**
     * The customer interacting with the shop,
     * in almost all cases, this will be a
     * {@link User} object.
     */
    @Getter
    private final User customer;

    /**
     * The amount of items exchanged during
     * this session's life span
     */
    @Getter @Setter
    private int amount = 0;

    /**
     * The task that holds the mighty responsibility
     * of killing this session by making it expired.
     * <p>
     * Rather dramatic text, but you get the point
     */
    BukkitTask expireTask;

    SignShopSession(SignShop shop, User customer) {
        this.shop = shop;
        this.customer = customer;

        this.material = shop.getExampleItem().getType();
        this.type = shop.getType();
        this.shopInventory = shop.getInventory();
    }

    /**
     * Runs the session's logic.
     * <p>
     * Calls {@link ShopInteraction#interact(SignShopSession)} for
     * the session's type and passes itself in as the parameter.
     * After that it informs the user they've successfully interacted
     * with the shop.
     * <p>
     * It will also call {@link #growAmount(int)} with the shop's
     * {@link #getExampleItem()} item amount
     */
    public void run() {
        type.getInteraction().interact(this);
        customer.sendMessage(Messages.sessionInteraction(this));

        growAmount(getExampleItem().getAmount());

        MarketShop market = Economy.get()
                .getMarkets()
                .get(shop.getPosition());

        // Trigger challenge for using shops
        if (market != null) {
            triggerChallenge(market);
        }

        // Log the transaction occurring
        Transactions.builder()
                .type(TransactionType.SHOP)
                .extra(
                        "shop=%s item=%s type=%s market=%s",
                        getShop().getName(),
                        ItemStacks.toNbtString(getExampleItem()),
                        getType(),
                        market == null ? null : market.getName()
                )
                .sender(
                        type.isBuyType()
                                ? customer.getUniqueId()
                                : shop.getOwner()
                )
                .target(
                        type.isBuyType()
                                ? shop.getOwner()
                                : customer.getUniqueId()
                )
                .amount(getPrice())
                .log();
    }

    /**
     * Attempts to trigger the daily shop purchase challenge.
     * @param market The market shop this session's shop is located in
     */
    private void triggerChallenge(@NotNull MarketShop market) {
        Challenges.apply("daily/buy", challenge -> {
            // Create initial query to find if they've bought
            // from this shop before
            var query = LogQuery.builder(Transactions.TRANSACTION_SCHEMA)
                    .maxResults(1)

                    .field(Transactions.T_EXTRA)
                    .add(Objects::nonNull)
                    .add(s -> s.contains("market=" + market.getName()));

            // Set the correct field to be the active field
            //
            // If user is buying from shop, they're sending
            // money, if they're selling to the shop, they're
            // getting the money, aka being the target
            if (shop.getType().isBuyType()) {
                query.field(Transactions.T_SENDER);
            } else {
                query.field(Transactions.T_TARGET);
            }

            // Add predicates to the new active field
            query.add(Objects::nonNull)
                    .add(s -> s.contains(customer.getUniqueId().toString()));

            // Great variable name
            boolean hasBoughtFromMarketBefore = !DataLogs.query(query.build())
                    .isEmpty();

            if (!hasBoughtFromMarketBefore) {
                challenge.trigger(customer);
            }
        });
    }

    /**
     * Delays this session expiring by {@link #SESSION_TIME_OUT}
     * ticks.
     * <p>
     * Will call the {@link #expire()} method once expiry
     * timeout has ended
     */
    public void delayExpiry() {
        Tasks.cancel(expireTask);
        expireTask = Tasks.runLater(this::expire, SESSION_TIME_OUT);
    }

    /**
     * Kills this session, removes it from the session map, logs this session,
     * if {@link #shouldLog()} returns true.
     * <p>
     * This will also send the customer a message about the session, if
     * they interacted with the shop for more than 5 times and, if the
     * session's shop is a non-admin shop, it will tell the owner that
     * someone used their shop and tell them if the shop went out of stock
     */
    public void expire() {
        // Remove this session from the manager, as it has expired
        Economy.get().getShops().removeSession(this);

        // Nothing happened lol
        if (getAmount() <= 0) {
            return;
        }

        // Add history entry
        shop.getHistory().addEntry(this);

        // If we should log the session
        if (shouldLog()) {
            // Brilliant logger statement, I can't wait until we have
            // an economy logger to throw this into
            FTC.getLogger().info(
                    "{} {} {} {} at a{} shop, location: {} for {}, shop price: {}",

                    customer.getName(),
                    (type.isBuyType() ? "bought" : "sold"),
                    getAmount(),
                    Text.prettyEnumName(material),
                    (type.isAdmin() ? "n admin" : " "),
                    shop.getName(),
                    getTotalEarned(),
                    getPrice()
            );
        }

        // If used shop more than 5 times
        if (getAmount() > (getExampleItem().getAmount() * 5)) {
            customer.sendMessage(Messages.sessionEndCustomer(this));
        }

        // Admin shops don't have owners and
        // also no one cares lol
        if (type.isAdmin()) {
            return;
        }

        // Tell owner that someone used their shop
        var owner = getOwnerUser();
        owner.sendMessage(Messages.sessionEndOwner(this));

        if (!shop.inStock()) {
            owner.sendMessage(Messages.stockIssueMessage(type, shop.getPosition()));
        }
    }

    /**
     * Checks if this session should be logged by two checking the value of
     * one of two variables, {@link GeneralConfig#logAdminShop} or
     * {@link GeneralConfig#logNormalShop}
     * @return True, if this session should be logged to console, false otherwise
     */
    private boolean shouldLog() {
        return type.isAdmin() ? GeneralConfig.logAdminShop : GeneralConfig.logNormalShop;
    }

    /**
     * Gets the current shop's exampleItem
     * @return Current shop's exampleItem
     */
    public ItemStack getExampleItem() {
        return getShop().getExampleItem();
    }

    /**
     * Gets the price of the current shop
     * @return The current shop's price
     */
    public int getPrice() {
        return getShop().getPrice();
    }

    /**
     * Gets whether the use has space for more items in their inventory.
     * @return Whether the user has inventory room.
     */
    public boolean customerIsFull() {
        return getCustomerInventory().firstEmpty() == -1;
    }

    /**
     * Gets whether the shop has space for more items.
     * @return Whether the shop has inventory room.
     */
    public boolean shopIsFull() {
        return getShop().isFull();
    }

    /**
     * Gets the inventory of the session's customer
     * @return The customer's inventory
     */
    public Inventory getCustomerInventory() {
        return customer.getInventory();
    }

    /**
     * Adds to the traded item amount
     * @param amount The amount to add
     */
    public void growAmount(int amount) {
        setAmount(getAmount() + amount);
    }

    /**
     * Gets the total amount of rhines earned
     * from this session thus far
     * @return The total rhines earned from this session
     */
    public int getTotalEarned() {
        return getPrice() * (getAmount() / getExampleItem().getAmount());
    }

    /**
     * Gets the user that owns this session's shop
     * @return Shop owner
     */
    public User getOwnerUser() {
        return Users.get(getShop().getOwner());
    }
}