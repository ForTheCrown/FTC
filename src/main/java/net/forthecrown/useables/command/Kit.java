package net.forthecrown.useables.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import lombok.Getter;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.TagTypes;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public class Kit extends CommandUsable {
  public static final String TAG_ITEMS = "items";

  @Getter
  private final List<ItemStack> items = new ArrayList<>();

  public Kit(String name, List<ItemStack> items) {
    super(name);

    items.forEach(itemStack -> {
      if (ItemStacks.isEmpty(itemStack)) {
        return;
      }

      this.items.add(itemStack.clone());
    });
  }

  public Kit(String name, CompoundTag tag) throws CommandSyntaxException {
    super(name, tag);

    var itemArray = tag.getList(TAG_ITEMS, TagTypes.compoundType());
    for (var e : itemArray) {
      items.add(TagUtil.readItem(e));
    }
  }

  @Override
  protected void save(CompoundTag tag) {
    var list = BinaryTags.listTag();

    for (var i : items) {
      list.add(TagUtil.writeItem(i));
    }

    tag.put(TAG_ITEMS, list);
  }

  @Override
  public boolean onInteract(Player player, boolean adminInteraction) {
    if (!testSpace(player)) {
      return false;
    }

    PlayerInventory inv = player.getInventory();

    for (ItemStack i : items) {
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

    for (ItemStack i : inventory) {
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

    for (ItemStack i : items) {
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