package net.forthecrown.economy.sell;

import com.google.common.collect.ImmutableMap;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.Messages;
import net.forthecrown.utils.text.writer.LoreWriter;
import net.forthecrown.utils.text.writer.TextWriters;
import net.forthecrown.user.data.SellAmount;
import net.forthecrown.user.data.UserShopData;
import net.forthecrown.user.property.BoolProperty;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.inventory.DefaultItemBuilder;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Slot;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class SellShopNodes {
    private SellShopNodes() {}

    /* ----------------------------- CONSTANTS ------------------------------ */

    private static final ItemStack PREVIOUS_PAGE_ITEM = ItemStacks.builder(Material.PAPER)
            .setName("&e< Previous Page")
            .addLore("&7Return to the main menu")
            .build();

    /** Node to toggle selling items with names */
    static final MenuNode SELLING_NAMED = toggleProperty("Named Items", Properties.SELLING_NAMED_ITEMS);

    /** Node to toggle selling items with lore */
    static final MenuNode SELLING_LORE = toggleProperty("Items With Lore", Properties.SELLING_LORE_ITEMS);

    /** Slot 2 node map of sell amount options */
    static final Map<Slot, MenuNode> SELL_AMOUNT_NODES = ImmutableMap.<Slot, MenuNode>builder()
            .put(Slot.of(8, 1), sellAmountOption(SellAmount.PER_1))
            .put(Slot.of(8, 2), sellAmountOption(SellAmount.PER_16))
            .put(Slot.of(8, 3), sellAmountOption(SellAmount.PER_64))
            .put(Slot.of(8, 4), sellAmountOption(SellAmount.ALL))
            .build();

    /** Node which shows the web-store link */
    static final MenuNode WEBSTORE = MenuNode.builder()
            .setItem(
                    ItemStacks.builder(Material.EMERALD_BLOCK)
                            .setName("&bWebstore")
                            .build()
            )

            .setRunnable((user, context) -> {
                user.sendMessage(Messages.SHOP_WEB_MESSAGE);
                context.shouldClose(true);
            })

            .build();

    /** Node to toggle selling compacted items */
    static final MenuNode COMPACT_TOGGLE = MenuNode.builder()
            .setItem((user, context) -> {
                boolean compacted = user.get(Properties.SELLING_COMPACTED);

                var builder = ItemStacks.builder(
                        compacted ? Material.IRON_BLOCK : Material.IRON_INGOT
                ) ;

                builder.setName("Toggle compact selling")
                        .addLore("&7Compact items are the block forms of items")
                        .addLore("&7Example: Diamonds and DiamondBlocks")
                        .addLore("&8Click to toggle");

                if (compacted) {
                    builder.addLore("Currently selling compact items");
                } else {
                    builder.addLore("Not currently selling compact items");
                }

                return builder.build();
            })

            .setRunnable((user, context) -> {
                user.flip(Properties.SELLING_COMPACTED);
                context.shouldReloadMenu(true);
            })

            .build();

    /* ----------------------------- STATICS ------------------------------ */

    public static MenuNode previousPage(SellShop shop) {
        return MenuNode.builder()
                .setItem((user, context) -> PREVIOUS_PAGE_ITEM)
                .setRunnable((user, context) -> {
                    shop.getMainMenu().open(user);
                })
                .build();
    }

    private static MenuNode toggleProperty(String name, BoolProperty property) {
        return MenuNode.builder()
                .setItem(user -> {
                    var builder = ItemStacks.builder(Material.BLACK_STAINED_GLASS_PANE)
                            .setName((user.get(property) ? "Selling " : "Ignoring ") + name )
                            .addLore("Click to switch");

                    if (user.get(property)) {
                        builder
                                .addEnchant(Enchantment.BINDING_CURSE, 1)
                                .setFlags(ItemFlag.HIDE_ENCHANTS);
                    }

                    return builder.build();
                })

                .setRunnable((user, context) -> {
                    user.flip(property);
                    context.shouldReloadMenu(true);
                })

                .build();
    }

    private static MenuNode sellAmountOption(SellAmount amount) {
        return MenuNode.builder()
                .setItem(user -> {
                    var builder = ItemStacks.builder(Material.BLACK_STAINED_GLASS_PANE)
                            .setAmount(amount.getItemAmount())
                            .setName(amount.getSellPerText())
                            .addLore("&7Set the amount of items you")
                            .addLore("&7will sell per click");

                    if (user.get(Properties.SELL_AMOUNT) == amount) {
                        builder.addEnchant(Enchantment.BINDING_CURSE, 1)
                                .setFlags(ItemFlag.HIDE_ENCHANTS);
                    }

                    return builder.build();
                })

                .setRunnable((user, context) -> {
                    var current = user.get(Properties.SELL_AMOUNT);

                    if (current == amount) {
                        return;
                    }

                    user.set(Properties.SELL_AMOUNT, amount);
                    context.shouldReloadMenu(true);
                })

                .build();
    }

    /**
     * Creates a menu node to sell the given data's items
     * @param data The data to create the node for
     * @return The created node
     */
    static MenuNode sellNode(ItemSellData data) {
        return MenuNode.builder()
                .setItem(user -> {
                    boolean compacted = user.get(Properties.SELLING_COMPACTED)
                            && data.canBeCompacted();

                    UserShopData earnings = user.getComponent(UserShopData.class);
                    Material material = compacted ? data.getCompactMaterial() : data.getMaterial();
                    int amount = user.get(Properties.SELL_AMOUNT).getItemAmount();
                    int mod = compacted ? data.getCompactMultiplier() : 1;
                    int originalPrice = mod * data.getPrice();

                    int price = ItemSell.calculateValue(material, data, earnings, 1).getEarned();

                    SellResult stackResult = ItemSell.calculateValue(
                            material,
                            data,
                            earnings,
                            material.getMaxStackSize()
                    );

                    LoreWriter writer = TextWriters.loreWriter();

                    writer.formattedLine("Value: {0, rhines} per item.",
                            NamedTextColor.YELLOW, price
                    );

                    if (originalPrice < price) {
                        writer.formattedLine("Original value: {0, rhines}",
                                NamedTextColor.GRAY,
                                originalPrice
                        );
                    }

                    writer.formattedLine("Value per stack ({0}): {1, rhines}",
                            NamedTextColor.GOLD,
                            material.getMaxStackSize(),
                            stackResult.getEarned()
                    );

                    if (stackResult.getSold() < material.getMaxStackSize()) {
                        writer.formattedLine("Can only sell {0} until price drops to {1, rhines}",
                                NamedTextColor.GRAY,
                                stackResult.getSold(), 0
                        );
                    }

                    writer.formattedLine("Amount you will sell: {0}",
                            NamedTextColor.GRAY,
                            user.get(Properties.SELL_AMOUNT).amountText()
                    );

                    writer.line("Change the amount you sell on the right", NamedTextColor.GRAY);

                    DefaultItemBuilder builder = ItemStacks.builder(material)
                            .setAmount(amount);

                    if (user.hasPermission(Permissions.AUTO_SELL)) {
                        if (earnings.getAutoSelling().contains(material)) {
                            builder
                                    .addEnchant(Enchantment.BINDING_CURSE, 1)
                                    .setFlags(ItemFlag.HIDE_ENCHANTS);

                            writer.line("&7Shift-Click to stop auto selling this item");
                        } else {
                            writer.line("&7Shift-Click to start auto selling this item");
                        }
                    }

                    builder.setLore(writer.getLore());
                    return builder.build();
                })

                .setRunnable((user, context) -> {
                    boolean compacted = user.get(Properties.SELLING_COMPACTED)
                            && data.canBeCompacted();

                    var material = compacted ? data.getCompactMaterial() : data.getMaterial();

                    // If toggling auto sell
                    if (context.getClickType() == ClickType.SHIFT_LEFT
                            || context.getClickType() == ClickType.SHIFT_RIGHT
                            && user.hasPermission(Permissions.AUTO_SELL)
                    ) {
                        var autoSelling = user.getComponent(UserShopData.class)
                                .getAutoSelling();

                        if (autoSelling.contains(material)) {
                            autoSelling.remove(material);
                        } else {
                            autoSelling.add(material);
                        }

                        context.shouldReloadMenu(true);
                        return;
                    }

                    ItemSeller handler = ItemSeller.inventorySell(user, material, data);
                    handler.run(true);

                    context.shouldReloadMenu(true);
                })

                .setPlaySound(false)
                .build();
    }
}