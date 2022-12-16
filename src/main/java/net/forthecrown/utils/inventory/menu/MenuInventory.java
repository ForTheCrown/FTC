package net.forthecrown.utils.inventory.menu;

import lombok.Getter;
import net.forthecrown.inventory.FtcInventoryImpl;
import net.forthecrown.utils.inventory.menu.context.InventoryContext;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.InventoryHolder;

public class MenuInventory extends FtcInventoryImpl {
    @Getter
    private final InventoryContext context;

    public MenuInventory(InventoryHolder owner, int size, Component title, InventoryContext context) {
        super(owner, size, title);
        this.context = context;
    }

    @Override
    public Menu getHolder() {
        return (Menu) super.getHolder();
    }
}