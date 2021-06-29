package net.forthecrown.cosmetics.custominvs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.Map;

public class CustomInvBuilder {
    private int size = 27;
    private TextComponent title;
    private Border invBorder;
    private Map<Integer, Option> invSlots = Map.of();

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

    public CustomInv build() {
        return new CustomInv(this.size, this.title, this.invBorder, this.invSlots);
    }
}
