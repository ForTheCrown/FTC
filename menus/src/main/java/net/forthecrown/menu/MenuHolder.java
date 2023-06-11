package net.forthecrown.menu;

import lombok.Getter;
import net.forthecrown.utils.context.Context;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

@Getter
public class MenuHolder implements InventoryHolder {

  private final Inventory inventory;
  private final Menu menu;
  private final Context context;

  public MenuHolder(Menu menu, Context context) {
    this.menu = menu;
    this.context = context;

    Component title = menu.getTitle();

    if (title == null) {
      this.inventory = Bukkit.createInventory(this, menu.getSize());
    } else {
      this.inventory = Bukkit.createInventory(this, menu.getSize(), title);
    }
  }
}