package net.forthecrown.dungeons;

import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public enum BossItems {

    ZHAMBIE ("Zhambie Defeated!", "He only wanted to hug you..."),
    SKALATAN ("Skalatan Defeated!", "Matching outfits for extra style points..."),
    HIDEY_SPIDEY ("Hidey Spidey Defeated!", "He could run, but not hide..."),
    DRAWNED ("Drawned Defeated!", "Never too late to learn how to swim..."),
    EVOKER ("Emo Defeated!", "It was just a phase...");

    private final ItemStack itemCreator;

    BossItems(String name, String tagLine) {
        this.itemCreator = ItemStacks.builder(Material.GOLDEN_APPLE, 1)
                .setName(Component.text(name).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .addLore(Component.text(tagLine))
                .addData(Bosses.KEY, PersistentDataType.BYTE, (byte) 1)
                .build();
    }

    @NotNull
    public ItemStack item() {
        return itemCreator.clone();
    }
}