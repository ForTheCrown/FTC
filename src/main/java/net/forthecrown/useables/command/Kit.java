package net.forthecrown.useables.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.forthecrown.text.Text;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.TagUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class Kit extends CommandUsable {
    @Getter
    private final List<ItemStack> items = new ArrayList<>();

    public Kit(String name, List<ItemStack> items) {
        super(name);

        items.forEach(itemStack -> {
            if (ItemStacks.isEmpty(itemStack)) {
                return;
            }

            items.add(itemStack.clone());
        });
    }

    public Kit(String name, JsonObject json) throws CommandSyntaxException {
        super(name, json);

        JsonArray itemArray = json.getAsJsonArray("items");
        for (JsonElement e: itemArray) {
            items.add(JsonUtils.readItem(e));
        }
    }

    public Kit(String name, CompoundTag tag) throws CommandSyntaxException {
        super(name, tag);

        var itemArray = tag.getList("items", Tag.TAG_COMPOUND);
        for (var e: itemArray) {
            items.add(TagUtil.readItem(e));
        }
    }

    @Override
    protected void save(CompoundTag tag) {
        var list = new ListTag();

        for (var i: items) {
            list.add(TagUtil.writeItem(i));
        }
    }

    @Override
    public boolean onInteract(Player player) {
        if (!testSpace(player)) {
            return false;
        }

        PlayerInventory inv = player.getInventory();

        for (ItemStack i: items) {
            inv.addItem(i.clone());
        }
        return true;
    }

    public boolean testSpace(Player player) {
        if (!hasSpace(player.getInventory())) {
            if (!silent) {
                player.sendMessage(Component.text("No room in inventory"));
            }

            return false;
        }

        return true;
    }

    public boolean hasSpace(PlayerInventory inventory) {
        int freeSlots = 0;

        for (ItemStack i: inventory) {
            if (ItemStacks.isEmpty(i)) {
                freeSlots++;
            }
        }

        return freeSlots >= items.size();
    }

    @Override
    public @NotNull HoverEvent<Component> asHoverEvent(@NotNull UnaryOperator<Component> op) {
        var builder = Component.text()
                .append(Component.text("Items: "));

        for (ItemStack i: items){
            Component name = Text.itemAndAmount(i);

            builder
                    .append(Component.newline())
                    .append(name);
        }

        return builder
                .build()
                .asHoverEvent(op);
    }
}