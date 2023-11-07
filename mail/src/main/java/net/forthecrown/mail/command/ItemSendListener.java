package net.forthecrown.mail.command;

import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.EnumSet;
import lombok.Getter;
import net.forthecrown.command.Exceptions;
import net.forthecrown.events.Events;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.text.PlayerMessage;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

@Getter
class ItemSendListener implements Listener, InventoryHolder {

  static final int ITEM_SLOT = 2;

  private final CommandSource source;
  private final PlayerMessage message;
  private final MailTargets targets;
  private final EnumSet<MailSendOption> options;

  private final Inventory inventory;

  public ItemSendListener(
      CommandSource source,
      PlayerMessage message,
      MailTargets targets,
      EnumSet<MailSendOption> options
  ) {
    this.source = source;
    this.message = message;
    this.targets = targets;
    this.options = options;

    this.inventory = Bukkit.createInventory(this, InventoryType.HOPPER, text("Mail an item"));

    ItemStack item = new ItemStack(Material.BARRIER, 1);
    inventory.setItem(0, item);
    inventory.setItem(1, item);
    inventory.setItem(3, item);
    inventory.setItem(4, item);
  }

  @EventHandler(ignoreCancelled = true)
  public void onInventoryClick(InventoryClickEvent event) {
    if (event.getClickedInventory() == null) {
      return;
    }

    if (!event.getClickedInventory().equals(inventory)) {
      return;
    }

    if (event.getSlot() == ITEM_SLOT) {
      return;
    }

    event.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void onInventoryClose(InventoryCloseEvent event) {
    if (!event.getInventory().equals(inventory)) {
      return;
    }

    ItemStack item = event.getInventory().getItem(ITEM_SLOT);
    Events.unregister(this);

    if (ItemStacks.isEmpty(item)) {
      event.getPlayer().sendMessage(
          text("No item set, message cancelled", NamedTextColor.RED)
      );

      return;
    }

    try {
      MailCommands.sendMail(source, targets, user -> {
        var builder = MailCommands.createBuilder(source)
            .target(user)
            .message(message)
            .attachment(attach -> attach.addItem(item));

        MailSendOption.apply(options, builder);
        builder.send();
      });
    } catch (CommandSyntaxException exc) {
      Exceptions.handleSyntaxException(source, exc);
    }
  }
}
