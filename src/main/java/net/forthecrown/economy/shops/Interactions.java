package net.forthecrown.economy.shops;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Class for storing shop interactions as
 * constants.
 * @see #SELL
 * @see #BUY
 * @see #ADMIN_BUY
 * @see #ADMIN_SELL
 */
final class Interactions {
    private Interactions() {}

    /**
     * Interaction type for admin buy shops
     * <h2>Testing</h2>
     * Tests if the customer's inventory is full and that
     * the customer can afford the shop's price
     * <h2>Interaction</h2>
     * Subtracts the appropriate amount of rhines from customer
     * and gives them the shop's item
     */
    public static final ShopInteraction ADMIN_BUY = new ShopInteraction() {
        @Override
        public void test(@NotNull SignShopSession session) throws CommandSyntaxException {
            // Check that the customer has space for items
            if (session.customerIsFull()) {
                throw Exceptions.INVENTORY_FULL;
            }

            // Check that the user isn't poor :)
            if (!session.getCustomer().hasBalance(session.getPrice())) {
                throw Exceptions.cannotAfford(session.getPrice());
            }
        }

        @Override
        public void interact(@NotNull SignShopSession session) {
            ItemStack exampleItem = session.getExampleItem();
            var customer = session.getCustomer();

            //Remove money
            if (session.getPrice() > 0) {
                customer.removeBalance(session.getPrice());
            }

            //Give item
            session.getCustomerInventory().addItem(exampleItem);
        }
    };

    public static final ShopInteraction ADMIN_SELL = new ShopInteraction() {
        @Override
        public void test(@NotNull SignShopSession session) throws CommandSyntaxException {
            ItemStack example = session.getExampleItem();

            //User does not have item to sell
            if (!session.getCustomerInventory()
                    .containsAtLeast(example, example.getAmount())
            ) {
                throw Exceptions.dontHaveItemForShop(example);
            }
        }

        @Override
        public void interact(@NotNull SignShopSession session) {
            ItemStack example = session.getExampleItem();
            var customer = session.getCustomer();

            //Add money and remove item
            if (session.getPrice() > 0) {
                customer.addBalance(session.getPrice());
            }

            session.getCustomerInventory().removeItemAnySlot(example.clone());
        }
    };

    public static final ShopInteraction SELL = new ShopInteraction() {
        @Override
        public void test(@NotNull SignShopSession session) throws CommandSyntaxException {
            // Overlap with admin sell, run its checks first
            ADMIN_SELL.test(session);

            //Check the shop's owner can afford the shop
            if (!session.getOwnerUser().hasBalance(session.getPrice())) {
                throw Exceptions.shopOwnerCannotAfford(session.getPrice());
            }

            //Check shop has space for any more items
            if (session.shopIsFull()) {
                session.getShop().update();
                throw Exceptions.SHOP_NO_SPACE;
            }
        }

        @Override
        public void interact(@NotNull SignShopSession session) {
            ADMIN_SELL.interact(session);

            var owner = session.getOwnerUser();
            ItemStack example = session.getExampleItem();

            //Add item to shop, give user mulaa
            session.getShopInventory().addItem(example.clone());

            if (session.getPrice() > 0) {
                owner.removeBalance(session.getPrice());
            }
        }
    };

    public static final ShopInteraction BUY = new ShopInteraction() {
        @Override
        public void test(@NotNull SignShopSession session) throws CommandSyntaxException {
            // Overlap with admin function, call its test method
            ADMIN_BUY.test(session);

            //Shop stock check
            if (!session.getShop().inStock()) {
                session.getShop().update();
                throw Exceptions.OUT_OF_STOCK;
            }
        }

        @Override
        public void interact(@NotNull SignShopSession session) {
            // Overlap with admin function, call its interact method
            ADMIN_BUY.interact(session);

            var owner = session.getOwnerUser();

            //Add money to owner, remove item from shop
            if (session.getPrice() > 0) {
                owner.addBalance(session.getPrice());
            }

            // Remove the item from the shop's inventory
            session.getShopInventory().removeItem(session.getExampleItem());
        }
    };
}