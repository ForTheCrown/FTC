package net.forthecrown.utils.inventory.menu;

import lombok.Getter;
import net.forthecrown.inventory.FtcInventoryImpl;
import net.forthecrown.utils.context.Context;
import net.kyori.adventure.text.Component;

public class MenuInventory extends FtcInventoryImpl {

  @Getter
  private final Context context;

  public MenuInventory(Menu owner, int size, Component title, Context context) {
    super(owner, size, title);
    this.context = context;
  }

  @Override
  public Menu getHolder() {
    return (Menu) super.getHolder();
  }
}