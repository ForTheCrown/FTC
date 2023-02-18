package net.forthecrown.economy.sell;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.forthecrown.core.Messages;
import net.forthecrown.user.data.SellAmount;
import net.forthecrown.user.property.BoolProperty;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Slot;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public final class SellShopNodes {

  private SellShopNodes() {
  }

  /* ----------------------------- CONSTANTS ------------------------------ */

  private static final ItemStack PREVIOUS_PAGE_ITEM = ItemStacks.builder(Material.PAPER)
      .setName("&e< Previous Page")
      .addLore("&7Return to the main menu")
      .build();

  /**
   * Node to toggle selling items with names
   */
  static final MenuNode SELLING_NAMED
      = toggleProperty("Named Items", Properties.SELLING_NAMED_ITEMS);

  /**
   * Node to toggle selling items with lore
   */
  static final MenuNode SELLING_LORE
      = toggleProperty("Items With Lore", Properties.SELLING_LORE_ITEMS);

  /**
   * Slot 2 node map of sell amount options
   */
  static final Map<Slot, MenuNode> SELL_AMOUNT_NODES = ImmutableMap.<Slot, MenuNode>builder()
      .put(Slot.of(8, 1), sellAmountOption(SellAmount.PER_1))
      .put(Slot.of(8, 2), sellAmountOption(SellAmount.PER_16))
      .put(Slot.of(8, 3), sellAmountOption(SellAmount.PER_64))
      .put(Slot.of(8, 4), sellAmountOption(SellAmount.ALL))
      .build();

  /**
   * Node which shows the web-store link
   */
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

  static final MenuNode INFO = MenuNode.builder()
      .setItem(
          ItemStacks.builder(Material.BOOK)
              .setName("&eInfo")

              .addLore("&7Here you can sell items to earn &eRhines&7!")
              .addLore("")
              .addLore("&7If you sell a lot of items, their prices will drop.")
              .addLore("&7The bigger the item's initial price, the faster")
              .addLore("&7the price drops")
              .addLore("")
              .addLore("&7Item prices will recover over time")

              .build()
      )
      .build();

  /**
   * Node to toggle selling compacted items
   */
  static final MenuNode COMPACT_TOGGLE = MenuNode.builder()
      .setItem((user, context) -> {
        boolean compacted = user.get(Properties.SELLING_COMPACTED);

        var builder = ItemStacks.builder(
            compacted ? Material.IRON_BLOCK : Material.IRON_INGOT
        );

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
              .setName("&e" + (user.get(property) ? "Selling " : "Ignoring ") + name)
              .addLore("&7Click to switch");

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
   *
   * @param data The data to create the node for
   * @return The created node
   */
  static MenuNode sellNode(ItemSellData data) {
    var node = new SellableItemNode(data);

    return MenuNode.builder()
        .setItem(node)
        .setRunnable(node)

        .setPlaySound(false)
        .build();
  }
}