package net.forthecrown.economy;

import net.forthecrown.core.CrownCore;
import net.forthecrown.events.dynamic.SellShopEvents;
import net.forthecrown.inventory.CrownItems;
import net.forthecrown.inventory.CustomInventoryHolder;
import net.forthecrown.core.nbt.NBT;
import net.forthecrown.core.nbt.NbtHandler;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.enums.SellAmount;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * The sellshop, where players can sell their items
 * @see SellAmount
 */
public class SellShop {

    private final CrownUser user;
    private Menu current;

    public SellShop(Player base){
        this(UserManager.getUser(base));
    }

    public SellShop(CrownUser base){
        user = base;

        CrownCore.inst().getServer().getPluginManager().registerEvents(new SellShopEvents(base.getPlayer(), this), CrownCore.inst());
    }

    public Inventory open(Menu menu){
        return switch (menu) {
            case CROPS -> cropsMenu();
            case DROPS -> dropsMenu();
            case MINING -> miningMenu();
            case MINING_BLOCKS -> miningBlocksMenu();
            case MAIN -> mainMenu();
            case DECIDING -> decidingMenu();
        };
    }

    public Inventory dropsMenu(){
        current = Menu.DROPS;
        Inventory inv = getBaseInventory("Mob Drops Shop Menu");
        inv.setItem(4, CrownItems.makeItem(Material.ROTTEN_FLESH, 1, true, "&bDrops"));

        inv.setItem(20, makeSellItem(Material.ROTTEN_FLESH));
        inv.setItem(21, makeSellItem(Material.BONE));
        inv.setItem(22, makeSellItem(Material.ARROW));
        inv.setItem(23, makeSellItem(Material.STRING));
        inv.setItem(24, makeSellItem(Material.SPIDER_EYE));
        inv.setItem(29, makeSellItem(Material.LEATHER));
        inv.setItem(30, makeSellItem(Material.GUNPOWDER));
        inv.setItem(31, makeSellItem(Material.BLAZE_ROD));
        inv.setItem(32, makeSellItem(Material.SLIME_BALL));
        inv.setItem(33, makeSellItem(Material.COD));
        inv.setItem(40, makeSellItem(Material.INK_SAC));

        return inv;
    }

    public Inventory cropsMenu(){
        current = Menu.CROPS;
        Inventory inv = getBaseInventory("Farming Items Shop Menu");
        inv.setItem(4, CrownItems.makeItem(Material.OAK_SAPLING, 1, true, "&bFarming"));

        inv.setItem(20, makeSellItem(Material.BAMBOO));
        inv.setItem(21, makeSellItem(Material.KELP));
        inv.setItem(22, makeSellItem(Material.CACTUS));
        inv.setItem(23, makeSellItem(Material.MELON));
        inv.setItem(24, makeSellItem(Material.VINE));
        inv.setItem(29, makeSellItem(Material.SUGAR_CANE));
        inv.setItem(30, makeSellItem(Material.POTATO));
        inv.setItem(31, makeSellItem(Material.WHEAT));
        inv.setItem(32, makeSellItem(Material.CARROT));
        inv.setItem(33, makeSellItem(Material.PUMPKIN));
        inv.setItem(38, makeSellItem(Material.BEETROOT_SEEDS));
        inv.setItem(39, makeSellItem(Material.BEETROOT));
        inv.setItem(40, makeSellItem(Material.SWEET_BERRIES));
        inv.setItem(41, makeSellItem(Material.CHORUS_FRUIT));
        inv.setItem(42, makeSellItem(Material.WHEAT_SEEDS));

        return inv;
    }

    public Inventory miningMenu(){
        current = Menu.MINING;
        Inventory inv = getBaseInventory("Mining Items Shop Menu");
        inv.setItem(4, CrownItems.makeItem(Material.IRON_PICKAXE, 1, true, "&bMining"));

        inv.setItem(11, makeSellItem(Material.LAPIS_LAZULI));
        inv.setItem(12, makeSellItem(Material.QUARTZ));
        inv.setItem(20, makeSellItem(Material.DIAMOND));
        inv.setItem(21, makeSellItem(Material.IRON_INGOT));
        inv.setItem(29, makeSellItem(Material.EMERALD));
        inv.setItem(30, makeSellItem(Material.GOLD_INGOT));
        inv.setItem(38, makeSellItem(Material.COAL));
        inv.setItem(39, makeSellItem(Material.REDSTONE));

        inv.setItem(14, makeSellItem(Material.STONE));
        inv.setItem(15, makeSellItem(Material.ANDESITE));
        inv.setItem(23, makeSellItem(Material.COBBLESTONE));
        inv.setItem(24, makeSellItem(Material.DIORITE));
        inv.setItem(32, makeSellItem(Material.GRAVEL));
        inv.setItem(33, makeSellItem(Material.GRANITE));
        inv.setItem(41, makeSellItem(Material.SAND));
        inv.setItem(42, makeSellItem(Material.DIRT));

        inv.setItem(34, makeSellItem(Material.SANDSTONE));
        inv.setItem(25, makeSellItem(Material.NETHERRACK));

        inv.setItem(8, CrownItems.makeItem(Material.IRON_BLOCK, 1, true, "&bBlocks menu"));
        return inv;
    }

    public Inventory miningBlocksMenu(){
        current = Menu.MINING_BLOCKS;
        Inventory inv = getBaseInventory("Mining Blocks Shop Menu");

        inv.setItem(20, makeSellBlock(Material.DIAMOND_BLOCK, Material.DIAMOND));
        inv.setItem(21, makeSellBlock(Material.GOLD_BLOCK, Material.GOLD_INGOT));
        inv.setItem(22, makeSellBlock(Material.EMERALD_BLOCK, Material.EMERALD));
        inv.setItem(23, makeSellBlock(Material.IRON_BLOCK, Material.IRON_INGOT));
        inv.setItem(24, makeSellBlock(Material.REDSTONE_BLOCK, Material.REDSTONE));

        inv.setItem(32, makeSellBlock(Material.COAL_BLOCK, Material.COAL));
        inv.setItem(31, makeSellBlock(Material.SLIME_BLOCK, Material.SLIME_BALL));
        inv.setItem(30, makeSellBlock(Material.LAPIS_BLOCK, Material.LAPIS_LAZULI));

        return inv;
    }

    public Inventory mainMenu(){
        current = Menu.MAIN;
        Inventory inv = new CustomInventoryHolder("FTC Shop", 27).getInventory();

        inv.setItem(11, CrownItems.makeItem(Material.GOLD_BLOCK, 1, true, "&e-Item Shop-", "&7Sell vanilla items."));
        inv.setItem(15, CrownItems.makeItem(Material.EMERALD_BLOCK, 1, true, "&e-Web store-", "&7Online server shop."));

        for(int i = 0; i < 10; i++){
            inv.setItem(i, CrownItems.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, "&7-"));
            inv.setItem(i+17, CrownItems.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, "&7-"));
        }

        return inv;
    }

    public Inventory decidingMenu(){
        current = Menu.DECIDING;
        Inventory inv = new CustomInventoryHolder("FTC Shop", 27).getInventory();

        inv.setItem(11, CrownItems.makeItem(Material.OAK_SAPLING, 1, true, "&bFarming", "&7Crops and other farmable items."));
        inv.setItem(13, CrownItems.makeItem(Material.IRON_PICKAXE, 1, true, "&bMining", "&7Ores and common blocks."));
        inv.setItem(15, CrownItems.makeItem(Material.ROTTEN_FLESH, 1, true, "&bDrops", "&7Common mobdrops."));

        for(int i = 0; i < 10; i++){
            inv.setItem(i, CrownItems.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, "&7-"));
            inv.setItem(i+17, CrownItems.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, "&7-"));
        }

        return inv;
    }

    private ItemStack makeSellBlock(Material material, Material ingot){
        int price = getItemPrice(ingot) * 9;

        ItemStack result = createSellItem(material, price, CrownCore.getItemPrice(ingot) * 9, user.getSellAmount());
        NBT nbt = NbtHandler.ofItemTags(result);
        nbt.put("ingot", ingot.toString());

        return NbtHandler.applyTags(result, nbt);
    }

    private ItemStack makeSellItem(Material material){
        return createSellItem(material, getItemPrice(material), CrownCore.getItemPrice(material), user.getSellAmount());
    }

    private short getItemPrice(Material material){
        return user.getMatData(material).getPrice();
    }

    private ItemStack createSellItem(Material material, int price, int origPrice, SellAmount sellAmount){
        final Style style = Style.style(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
        boolean thing = sellAmount == SellAmount.ALL;

        ItemStackBuilder builder = new ItemStackBuilder(material, sellAmount.value)
                .addLore(Component.text("Value: " + Balances.getFormatted(price) + " per item").style(style));

        if(price < origPrice) builder.addLore(Component.text("Original price: " + Balances.getFormatted(origPrice)).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).color(NamedTextColor.GRAY));

        builder
                .addLore(Component.text((price * 64) + " Rhines per stack.").style(style.color(NamedTextColor.GOLD)))
                .addLore(Component.text("Amount will sell: " + (thing ? "all" : sellAmount.getValue()) + ".").style(style.color(NamedTextColor.GRAY)))
                .addLore(Component.text("Change the amount setting on the right.").style(style.color(NamedTextColor.GRAY)));

        return builder.build();
    }

    private Inventory getBaseInventory(String menuName){
        CustomInventoryHolder holder = new CustomInventoryHolder(menuName, 54);
        Inventory inv = holder.getInventory();
        inv.setItem(0, CrownItems.makeItem(Material.PAPER, 1, true, "&e< Previous page"));

        //add glass panes
        final ItemStack border = CrownItems.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, "&7-");
        for (int i = 1; i < 54; i++){
            if(i == 4) i++;
            if(i == 10 || i == 19 || i == 28 || i == 37) i += 8;

            inv.setItem(i, border);
        }

        //black sell amount panes
        inv.setItem(17, getSellAmountPane(SellAmount.PER_1));
        inv.setItem(26, getSellAmountPane(SellAmount.PER_16));
        inv.setItem(35, getSellAmountPane(SellAmount.PER_64));
        inv.setItem(44, getSellAmountPane(SellAmount.ALL));

        return inv;
    }

    private ItemStack getSellAmountPane(SellAmount paneToGet){
        String[] asd = {"&7Set the amount of items you", "&7will sell per click"};
        ItemStack toReturn = CrownItems.makeItem(Material.BLACK_STAINED_GLASS_PANE, paneToGet.value, true, paneToGet.text, asd);

        if(user.getSellAmount() == paneToGet) toReturn.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
        return toReturn;
    }

    public Menu getCurrentMenu() {
        return current;
    }

    public enum Menu {
        MAIN,
        DECIDING,
        CROPS,
        DROPS,
        MINING,
        MINING_BLOCKS
    }
}
