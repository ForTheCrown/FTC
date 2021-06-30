package net.forthecrown.inventory.custom;

import net.forthecrown.inventory.custom.borders.Border;
import net.forthecrown.inventory.custom.options.Option;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import java.util.Map;

public class CustomInvBuilder {
    private CrownUser user;
    private int size = 27;
    private TextComponent title;
    private Border invBorder;
    private Map<Integer, Option> invSlots = Map.of();

    public CustomInvBuilder setUser(CrownUser user) {
        this.user = user;
        return this;
    }

    public CustomInvBuilder setSize(int size) {
        this.size = size;
        return this;
    }

    public CustomInvBuilder setTitle(TextComponent title) {
        this.title = title;
        return this;
    }

    public CustomInvBuilder setTitle(String title) {
        this.title = Component.text(title);
        return this;
    }

    public CustomInvBuilder setInvBorder(Border invBorder) {
        this.invBorder = invBorder;
        return this;
    }

    public CustomInvBuilder addOption(int slot, Option option) {
        this.invSlots.put(slot, option);
        return this;
    }

    public CustomInvBuilder addOptions(Map<Integer, Option> options) {
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
