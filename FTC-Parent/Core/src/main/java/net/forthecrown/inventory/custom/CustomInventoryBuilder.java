package net.forthecrown.inventory.custom;

import net.forthecrown.inventory.custom.borders.Border;
import net.forthecrown.inventory.custom.options.Option;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class CustomInventoryBuilder {
    private CrownUser user;
    private int size = 27;
    private Component title; //Use component pls, means we could also use translatable components if we wanted to
    private Border invBorder;
    private final Map<Integer, Option> invSlots = new HashMap<>();

    public CustomInventoryBuilder setUser(CrownUser user) {
        this.user = user;
        return this;
    }

    public CustomInventoryBuilder setSize(int size) {
        this.size = size;
        return this;
    }

    public CustomInventoryBuilder setTitle(Component title) {
        this.title = title;
        return this;
    }

    public CustomInventoryBuilder setTitle(String title) {
        this.title = Component.text(title);
        return this;
    }

    public CustomInventoryBuilder setInvBorder(Border invBorder) {
        this.invBorder = invBorder;
        return this;
    }

    public CustomInventoryBuilder addOption(int slot, Option option) {
        this.invSlots.put(slot, option);
        return this;
    }

    public CustomInventoryBuilder addOptions(Map<Integer, Option> options) {
        this.invSlots.putAll(options);
        return this;
    }


    public CustomInventory build() {
        CustomInventory result = new CustomInventory();
        Inventory inventory;

        if (this.title == null) inventory = Bukkit.createInventory(result, this.size);
        else inventory = Bukkit.createInventory(result, this.size, this.title);

        for (Map.Entry<Integer, Option> optionSlot : this.invSlots.entrySet())
            inventory.setItem(optionSlot.getKey(), optionSlot.getValue().getItem());

        this.invBorder.applyBorder(inventory);

        result.setInv(inventory);
        result.setInvBorder(this.invBorder);
        result.setInvSlots(this.invSlots);
        result.setUser(user);
        return result;
    }


}
