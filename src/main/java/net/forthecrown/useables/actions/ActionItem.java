package net.forthecrown.useables.actions;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.Usable;
import net.forthecrown.useables.ConstructType;
import net.forthecrown.useables.UsableConstructor;
import net.forthecrown.useables.UsageAction;
import net.forthecrown.useables.UsageType;
import net.forthecrown.useables.util.UsageUtil;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.TextJoiner;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.Tag;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Getter
public class ActionItem extends UsageAction {

  // --- TYPE ---
  public static final UsageType<ActionItem> TYPE_REM = UsageType.of(ActionItem.class)
      .setSuggests(UsageUtil::suggestItems)
      .requiresInput(false);

  public static final UsageType<ActionItem> TYPE_ADD = UsageType.of(ActionItem.class)
      .setSuggests(UsageUtil::suggestItems)
      .requiresInput(false);

  private final boolean add;
  private final ImmutableList<ItemStack> items;

  public ActionItem(UsageType<ActionItem> type, ImmutableList<ItemStack> items) {
    super(type);

    this.items = items;
    this.add = type == TYPE_ADD;
  }

  @Override
  public void onUse(Player player, Usable holder) {
    var inventory = player.getInventory();

    for (var i : items) {
      if (ItemStacks.isEmpty(i)) {
        continue;
      }

      ItemStack item = i.clone();

      if (add) {
        Util.giveOrDropItem(inventory, player.getLocation(), item);
      } else {
        inventory.removeItemAnySlot(item);
      }
    }
  }

  @Override
  public @Nullable Component displayInfo() {
    return Text.format("items={0}",
        TextJoiner.onComma()
            .add(items.stream()
                .map(Text::itemAndAmount)
            )
    );
  }

  @Override
  public @Nullable Tag save() {
    return UsageUtil.saveItems(items);
  }

  // --- TYPE CONSTRUCTORS ---

  @UsableConstructor(ConstructType.PARSE)
  public static ActionItem parse(UsageType<ActionItem> type, StringReader reader,
                                 CommandSource source
  )
      throws CommandSyntaxException {
    return new ActionItem(
        type,
        UsageUtil.parseItems(reader, source)
    );
  }

  @UsableConstructor(ConstructType.TAG)
  public static ActionItem load(UsageType<ActionItem> type, Tag tag) {
    return new ActionItem(type, UsageUtil.loadItems(tag));
  }
}