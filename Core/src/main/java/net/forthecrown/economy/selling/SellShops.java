package net.forthecrown.economy.selling;

import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.Balances;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.inventory.builder.InventoryBuilder;
import net.forthecrown.inventory.builder.options.InventoryBorder;
import net.forthecrown.inventory.builder.options.InventoryOption;
import net.forthecrown.inventory.builder.options.SimpleCordedOption;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.data.SoldMaterialData;
import net.forthecrown.user.enums.SellAmount;
import net.forthecrown.utils.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import static net.forthecrown.economy.selling.BlockSellOption.four;
import static net.forthecrown.economy.selling.BlockSellOption.nine;
import static net.forthecrown.economy.selling.ItemSellOption.itemSell;
import static org.bukkit.Material.*;

/**
 * Class which contains sell shop constants.
 * <p>This constant class thing is for some reason becoming the norm for inventory things lol</p>
 * <p></p>
 * Other classes shuch as headers should not use the BuiltInventory constants themselves as they might not be initialized
 * when they use it
 */
public class SellShops {
    public static final Component WEB_MESSAGE = Component.text()
            .append(Component.translatable("commands.shop.web", NamedTextColor.GRAY))
            .append(Component.newline())
            .append(Component.text("https://for-the-crown.tebex.io/").color(NamedTextColor.AQUA)
                    .clickEvent(ClickEvent.openUrl("https://for-the-crown.tebex.io/"))
                    .hoverEvent(Component.translatable("commands.shop.web.hover")))
            .build();

    public static InventoryOption WEB_SHOP = new SimpleCordedOption(
            4, 1,
            new ItemStackBuilder(EMERALD_BLOCK, 1)
                    .setName(Component.text("-Web Shop-").style(FtcFormatter.nonItalic(NamedTextColor.GREEN)))
                    .addLore(Component.text("Show's the server's webstore link").style(FtcFormatter.nonItalic(NamedTextColor.GRAY)))
                    .build(),

            (user, context) -> {
                user.getPlayer().closeInventory();
                user.sendMessage(WEB_MESSAGE);
            }
    );

    public static final BuiltInventory MINERALS = baseInventory("Minerals", 54, true)
            .add(SellShopHeader.MINERALS)

            .add(itemSell(20, COAL))
            .add(itemSell(21, EMERALD))
            .add(itemSell(22, DIAMOND))
            .add(itemSell(23, LAPIS_LAZULI))
            .add(itemSell(24, REDSTONE))

            .add(itemSell(29, QUARTZ))
            .add(itemSell(30, COPPER_INGOT))
            .add(itemSell(31, IRON_INGOT))
            .add(itemSell(32, GOLD_INGOT))
            .add(itemSell(33, NETHERITE_INGOT))

            .add(itemSell(40, AMETHYST_SHARD))

            .build();

    public static final BuiltInventory CRAFTABLE_BLOCKS = baseInventory("Craftable Blocks", 54, true)
            .add(SellShopHeader.CRAFTABLE_BLOCKS)

            //First row
            .add(four(20,     AMETHYST_BLOCK,     AMETHYST_SHARD))
            .add(nine(21,     IRON_BLOCK,         IRON_INGOT))
            .add(nine(22,     DIAMOND_BLOCK,      DIAMOND))
            .add(nine(23,     GOLD_BLOCK,         GOLD_INGOT))
            .add(nine(24,     NETHERITE_BLOCK,    NETHERITE_INGOT))

            //Second row
            .add(nine(29,     SLIME_BLOCK,        SLIME_BALL))
            .add(nine(30,     EMERALD_BLOCK,      EMERALD))
            .add(nine(31,     REDSTONE_BLOCK,     REDSTONE))
            .add(nine(32,     LAPIS_BLOCK,        LAPIS_LAZULI))
            .add(nine(33,     COAL_BLOCK,         COAL))

            .build();

    public static final BuiltInventory MINING = baseInventory("Mining", 54, true)
            .add(SellShopHeader.MINING)

            .add(itemSell(4, 1, DIRT))

            .add(itemSell(2, 2, STONE))
            .add(itemSell(3, 2, GRANITE))
            .add(itemSell(4, 2, DIORITE))
            .add(itemSell(5, 2, ANDESITE))
            .add(itemSell(6, 2, COBBLESTONE))

            .add(itemSell(2, 3, GRAVEL))
            .add(itemSell(3, 3, SAND))
            .add(itemSell(4, 3, CALCITE))
            .add(itemSell(5, 3, DEEPSLATE))
            .add(itemSell(6, 3, COBBLED_DEEPSLATE))

            .add(itemSell(2, 4, SMOOTH_BASALT))
            .add(itemSell(3, 4, BASALT))
            .add(itemSell(4, 4, NETHERRACK))
            .add(itemSell(5, 4, BLACKSTONE))
            .add(itemSell(6, 4, END_STONE))

            .build();

    public static final BuiltInventory DROPS = baseInventory("Drops", 54, true)
            .add(SellShopHeader.DROPS)

            .add(itemSell(2, 2, ROTTEN_FLESH))
            .add(itemSell(3, 2, BONE))
            .add(itemSell(4, 2, ARROW))
            .add(itemSell(5, 2, STRING))
            .add(itemSell(6, 2, SPIDER_EYE))

            .add(itemSell(2, 3, LEATHER))
            .add(itemSell(3, 3, GUNPOWDER))
            .add(itemSell(4, 3, BLAZE_ROD))
            .add(itemSell(5, 3, SLIME_BALL))
            .add(itemSell(6, 3, COD))

            .add(itemSell(3, 4, INK_SAC))
            .add(itemSell(5, 4, GLOW_INK_SAC))

            .build();

    public static final BuiltInventory CROPS = baseInventory("Crops", 54, true)
            .add(SellShopHeader.CROPS)

            .add(itemSell(2, 2, BAMBOO))
            .add(itemSell(3, 2, KELP))
            .add(itemSell(4, 2, CACTUS))
            .add(itemSell(5, 2, MELON))
            .add(itemSell(6, 2, VINE))

            .add(itemSell(2, 3, SUGAR_CANE))
            .add(itemSell(3, 3, POTATO))
            .add(itemSell(4, 3, WHEAT))
            .add(itemSell(5, 3, CARROT))
            .add(itemSell(6, 3, PUMPKIN))

            .add(itemSell(2, 4, BEETROOT_SEEDS))
            .add(itemSell(3, 4, BEETROOT))
            .add(itemSell(4, 4, CHORUS_FRUIT))
            .add(itemSell(5, 4, SWEET_BERRIES))
            .add(itemSell(6, 4, WHEAT_SEEDS))

            .build();

    public static final BuiltInventory MAIN = baseInventory("FTC", 36, false)
            .add(WEB_SHOP)

            .add(SellShopHeader.DROPS.slotClone(2, 2))
            .add(SellShopHeader.MINERALS.slotClone(3, 2))
            .add(SellShopHeader.MINING.slotClone(4, 2))
            .add(SellShopHeader.CRAFTABLE_BLOCKS.slotClone(5, 2))
            .add(SellShopHeader.CROPS.slotClone(6, 2))

            .build();

    private static InventoryBuilder baseInventory(String title, int size, boolean sellMenu) {
        InventoryBuilder builder = new InventoryBuilder(size, Component.text(title + " Shop"))
                .add(new InventoryBorder());

        if(sellMenu) {
            builder.add(PreviousPageOption.OPTION);

            for (SellAmount a: SellAmount.values()) {
                builder.add(a.invOption);
            }
        }

        return builder;
    }

    /**
     * Sells the given material to server shop
     * @param user The user that'll sell
     * @param toRemove The material they'll be selling
     * @param priceScalar The price's scalar. Used for scaling prices in-case they're selling a craftable block.
     * @param data The data to add earnings to and change the price of
     * @return The amount of items sold.
     */
    public static int sell(CrownUser user, Material toRemove, int priceScalar, SoldMaterialData data) {
        SellAmount sellAmount = user.getSellAmount();
        Balances balances = Crown.getBalances();

        ItemStack sellItem = new ItemStack(toRemove, sellAmount.value);
        UserSellResult result = user.sellMaterial(toRemove);

        if(!result.foundAnything()) {
            user.sendMessage(Component.translatable("economy.sellshop.noItems", NamedTextColor.GRAY));
            return 0;
        }

        int totalEarned = result.getFoundAmount() * data.getPrice() * priceScalar;

        balances.add(user.getUniqueId(), totalEarned, true);
        user.addTotalEarnings(totalEarned);

        user.sendMessage(
                Component.translatable("economy.sellshop.sold",
                        NamedTextColor.GRAY,
                        Component.text()
                                .color(NamedTextColor.YELLOW)
                                .append(FtcFormatter.itemAndAmount(sellItem, result.getFoundAmount()))
                                .build(),
                        FtcFormatter.rhines(totalEarned).color(NamedTextColor.GOLD)
                )
        );
        Crown.logger().info(user.getName() + " sold " + result.getFoundAmount() + " " + FtcFormatter.normalEnum(toRemove) + " for " + FtcFormatter.getRhines(totalEarned));

        int initPrice = data.getPrice();

        data.addEarned(totalEarned);
        data.recalculate();
        user.setMatData(data);

        int comparison = data.getPrice();

        if(comparison < initPrice) {
            user.sendMessage(Component.translatable("economy.sellshop.priceDrop",
                    NamedTextColor.GRAY,
                    Component.translatable(toRemove.getTranslationKey()).color(NamedTextColor.YELLOW),
                    FtcFormatter.rhines(comparison * priceScalar).color(NamedTextColor.GOLD)
            ));
        }

        return result.getFoundAmount();
    }
}
