package net.forthecrown.economy.shops;

import net.forthecrown.core.FTC;
import net.forthecrown.economy.Economy;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

/**
 * Sign shop constants and utility methods
 */
public final class SignShops {
    private SignShops() {}

    /* ----------------------------- CONSTANTS ------------------------------ */

    /**
     * The current key used to tell the shop manager which blocks are and
     * aren't shops.
     * <p>
     * Also, the key the sign's data is saved under in the {@link org.bukkit.block.Sign}'s
     * {@link org.bukkit.persistence.PersistentDataContainer}
     */
    public static final NamespacedKey SHOP_KEY = new NamespacedKey(FTC.getPlugin(), "signshop");

    /**
     * The free item slot in the initial item
     * selection screen
     */
    public static final int EXAMPLE_ITEM_SLOT = 2;

    /**
     * The default inventory size of shops
     */
    public static final int DEFAULT_INV_SIZE  = 27;

    /** The sign line that has the shop's type */
    public static final int LINE_TYPE = 0;

    /** The sign line that has the shop's price */
    public static final int LINE_PRICE = 3;

    /**
     * Buy type label
     */
    public static final String BUY_LABEL  = "=[Buy]=";

    /**
     * Sell type label
     */
    public static final String SELL_LABEL = "=[Sell]=";

    /**
     * The style to use for labels to display a shop being out of stock
     */
    public static final Style OUT_OF_STOCK_STYLE  = Style.style(NamedTextColor.RED, TextDecoration.BOLD);

    /**
     * The style normal shop labels use
     */
    public static final Style NORMAL_STYLE        = Style.style(NamedTextColor.GREEN, TextDecoration.BOLD);

    /**
     * The style admin shop labels use
     */
    public static final Style ADMIN_STYLE         = Style.style(NamedTextColor.AQUA, TextDecoration.BOLD);

    /**
     * The price line prefix
     */
    public static final Component PRICE_LINE      = Component.text("Price: ").color(NamedTextColor.DARK_GRAY);

    /**
     * The barrier item displayed in the Example Inventory
     */
    static final ItemStack EXAMPLE_BARRIER = ItemStacks.builder(Material.BARRIER, 1)
            .setNameRaw(Component.text(""))
            .build();

    /* ----------------------------- UTILITY METHODS ------------------------------ */

    /**
     * Checks whether a block is a preexisting signshop.
     * A null check is also performed in the statement
     * @param block The block to check
     * @return Whether the block is a shop or not
     */
    public static boolean isShop(Block block) {
        if (block == null) {
            return false;
        }

        if (block.getState() instanceof Sign sign) {
            PersistentDataContainer container = sign.getPersistentDataContainer();
            return container.has(SHOP_KEY, PersistentDataType.TAG_CONTAINER);
        } else {
            return false;
        }
    }

    /**
     * Tests if the player with the given ID can edit
     * the given shop.
     * <p>
     * What mayEdit means in this function's context is that
     * the player is either the owner of the shop, or, if
     * the shop is located within a {@link MarketShop}, then
     * it must allow member editing and the given player must
     * be a co-owner of that market
     *
     * @param shop The shop to test the player against
     * @param uuid The player to test
     * @return True, if the player is allowed to edit
     *         the shop.
     */
    public static boolean mayEdit(SignShop shop, UUID uuid) {
        // Can't edit a shop that doesn't exist lol
        if (shop == null) {
            return false;
        }

        // If the UUID is that of the owner
        if (uuid.equals(shop.getOwner())) {
            return true;
        }

        // Get the shop's position and try to
        // find a market that overlaps that area
        WorldVec3i vec = shop.getPosition();
        MarketShop s = Economy.get()
                .getMarkets()
                .get(vec);

        // Shop is null or has no owner, false
        if (s == null || !s.hasOwner()) {
            return false;
        }

        // Members aren't allowed to edit each other's shops
        // in this market, so false
        if (!s.isMemberEditingAllowed()) {
            return false;
        }

        // Must be a co owner to edit
        return s.getMembers().contains(uuid);
    }

    /**
     * Gets the "Price: $12345" line for the shop's sign with the given amount.
     * @param amount The amount to get the text for
     * @return The created text
     */
    public static Component priceLine(int amount) {
        // Take the price line prefix and just append the price
        // onto it lol
        return SignShops.PRICE_LINE
                .append(Component.text("$" + amount)
                        .color(NamedTextColor.BLACK)
                );
    }

    /**
     * Creates an inventory for the given {@link SignShop} instance.
     * <p>
     * Delegate method for {@link Bukkit#createInventory(InventoryHolder, int, Component)}
     * @param shop The shop to create the inventory for
     * @param size The size of the inventory
     * @return The created inventory
     */
    public static Inventory createInventory(SignShop shop, int size) {
        return Bukkit.createInventory(shop, size, Component.text("Shop Contents"));
    }

    /**
     * Gets the hopper inventory with 1 available slot, used for setting the exampleItem of a shop
     * @return the example inventory
     */
    public static Inventory createExampleInventory() {
        // Create example inventory
        Inventory inv = Bukkit.createInventory(null, InventoryType.HOPPER, Component.text("Specify what and how much"));

        // Fill these 4 slots, so they can only place
        // 1 item in the inventory.
        inv.setItem(0, EXAMPLE_BARRIER);
        inv.setItem(1, EXAMPLE_BARRIER);
        inv.setItem(3, EXAMPLE_BARRIER);
        inv.setItem(4, EXAMPLE_BARRIER);

        return inv;
    }
}