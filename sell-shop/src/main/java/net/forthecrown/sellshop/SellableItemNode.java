package net.forthecrown.sellshop;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.command.Exceptions;
import net.forthecrown.menu.ClickContext;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.text.BufferedTextWriter;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextWriters;
import net.forthecrown.user.User;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.DefaultItemBuilder;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@RequiredArgsConstructor
public class SellableItemNode implements MenuNode {

  private final ItemSellData data;

  @Override
  public void onClick(User user, Context ctx, ClickContext context)
      throws CommandSyntaxException
  {
    boolean compacted = user.get(SellProperties.COMPACTED) && data.canBeCompacted();
    var material = compacted ? data.getCompactMaterial() : data.getMaterial();

    // If toggling auto sell
    if (context.getClickType().isShiftClick()) {
      toggleAutoSell(user, material, context);
      return;
    }

    ItemSeller handler = ItemSeller.inventorySell(user, material, data);
    var result = handler.run(true);

    if (result.getFailure() == null) {
      context.shouldReloadMenu(true);
    }
  }

  private void toggleAutoSell(User user, Material material, ClickContext context)
      throws CommandSyntaxException
  {
    if (!user.hasPermission(SellPermissions.AUTO_SELL)) {
      throw Exceptions.format("Cannot toggle auto sell! Donators only");
    }

    var autoSelling = user.getComponent(UserShopData.class)
        .getAutoSelling();

    if (autoSelling.contains(material)) {
      autoSelling.remove(material);
    } else {
      autoSelling.add(material);
    }

    context.shouldReloadMenu(true);
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user, @NotNull Context context) {
    boolean compacted = user.get(SellProperties.COMPACTED)
        && data.canBeCompacted();

    UserShopData earnings = user.getComponent(UserShopData.class);
    Material material = compacted ? data.getCompactMaterial() : data.getMaterial();
    int amount = user.get(SellProperties.SELL_AMOUNT).getItemAmount();
    int mod = compacted ? data.getCompactMultiplier() : 1;
    int originalPrice = mod * data.getPrice();

    int price = ItemSell.calculateValue(material, data, earnings, 1).getEarned();

    BufferedTextWriter writer = TextWriters.buffered();

    writer.formattedLine("Value: {0, rhines} per item.",
        NamedTextColor.YELLOW, price
    );

    if (originalPrice < price) {
      writer.formattedLine("Original value: {0, rhines}",
          NamedTextColor.GRAY,
          originalPrice
      );
    }

    addStackSellInfo(writer, material, earnings);
    addPriceChangeInfo(writer, material, earnings);

    writer.newLine();
    writer.newLine();

    writer.formattedLine("Amount you will sell: {0}",
        NamedTextColor.GRAY,
        user.get(SellProperties.SELL_AMOUNT).amountText()
    );

    writer.line("Change the amount you sell on the right", NamedTextColor.GRAY);

    DefaultItemBuilder builder = ItemStacks.builder(material)
        .setAmount(amount);

    if (user.hasPermission(SellPermissions.AUTO_SELL)) {
      if (earnings.getAutoSelling().contains(material)) {
        builder
            .addEnchant(Enchantment.BINDING_CURSE, 1)
            .setFlags(ItemFlag.HIDE_ENCHANTS);

        writer.line("&7Shift-Click to stop auto selling this item");
      } else {
        writer.line("&7Shift-Click to start auto selling this item");
      }
    }

    builder.setLore(writer.getBuffer());
    return builder.build();
  }

  private void addStackSellInfo(
      BufferedTextWriter writer,
      Material material,
      UserShopData earnings
  ) {
    SellResult stackResult = ItemSell.calculateValue(
        material,
        data,
        earnings,
        material.getMaxStackSize()
    );

    writer.formattedLine("Value per stack ({0}): {1, rhines}",
        NamedTextColor.GOLD,
        material.getMaxStackSize(),
        stackResult.getEarned()
    );

    if (stackResult.getSold() < material.getMaxStackSize()) {
      writer.formattedLine("Can only sell {0} until price drops to {1, rhines}",
          NamedTextColor.GRAY,
          stackResult.getSold(),
          0
      );
    }
  }

  private void addPriceChangeInfo(
      BufferedTextWriter writer,
      Material material,
      UserShopData earnings
  ) {
    int earned = earnings.get(material);
    int rhinesUntilDrop = data.calcPriceDrop(earned);
    int price = data.calculatePrice(earned);

    if (price <= 0) {
      return;
    }

    int itemsUntilPriceDrop = rhinesUntilDrop / price;

    itemsUntilPriceDrop = Math.max(1, itemsUntilPriceDrop);

    writer.formattedLine(
        "Price will drop after selling {0, number} item{1}",
        NamedTextColor.GRAY,
        itemsUntilPriceDrop,
        Text.conditionalPlural(itemsUntilPriceDrop)
    );
  }
}