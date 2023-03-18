package net.forthecrown.useables.util;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.ArrayArgument;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.IntTag;
import net.forthecrown.nbt.ListTag;
import net.forthecrown.utils.inventory.ItemStacks;
import net.minecraft.advancements.critereon.MinMaxBounds;
import org.bukkit.inventory.ItemStack;

public final class UsageUtil {

  private UsageUtil() {
  }

  /* ----------------------------- ITEM UTILITIES ------------------------------ */

  public static final ArgumentType<ItemStack> ITEM_ARGUMENT
      = new ItemParser();

  public static final ArrayArgument<ItemStack> ITEM_ARRAY_PARSER
      = ArgumentTypes.array(ITEM_ARGUMENT);

  public static final String HELD_ITEM_FLAG = "-held_item";
  public static final String INVENTORY_FLAG = "-inventory";

  public static ImmutableList<ItemStack> parseItems(StringReader reader, CommandSource source)
      throws CommandSyntaxException {
    // If it's starting with '-', it's a 'flag'
    if (reader.canRead() && reader.peek() != '-') {
      return ImmutableList.copyOf(ITEM_ARRAY_PARSER.parse(reader));
    }

    String flag = !reader.canRead()
        ? HELD_ITEM_FLAG
        : reader.readUnquotedString().toLowerCase();

    var inventory = source.asPlayer().getInventory();

    if (flag.equals(HELD_ITEM_FLAG)) {
      var item = inventory.getItemInMainHand();
      if (ItemStacks.isEmpty(item)) {
        throw Exceptions.MUST_HOLD_ITEM;
      }

      return ImmutableList.of(item.clone());
    }

    if (flag.equals(INVENTORY_FLAG)) {
      ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
      var it = ItemStacks.nonEmptyIterator(inventory);

      while (it.hasNext()) {
        builder.add(it.next().clone());
      }

      return builder.build();
    }

    throw Exceptions.unknown("Item Parse Flag", reader, flag);
  }

  public static <S> CompletableFuture<Suggestions> suggestItems(CommandContext<S> context,
                                                                SuggestionsBuilder builder
  ) {
    Completions.suggest(builder, HELD_ITEM_FLAG, INVENTORY_FLAG);
    return ITEM_ARRAY_PARSER.listSuggestions(context, builder);
  }

  public static BinaryTag saveItems(ImmutableList<ItemStack> items) {
    if (items.size() == 1) {
      return ItemStacks.save(items.get(0));
    }

    ListTag list = BinaryTags.listTag();

    for (var i : items) {
      if (ItemStacks.isEmpty(i)) {
        continue;
      }

      list.add(ItemStacks.save(i));
    }

    return list;
  }

  public static ImmutableList<ItemStack> loadItems(BinaryTag tag) {
    if (tag instanceof CompoundTag cTag) {
      return ImmutableList.of(ItemStacks.load(cTag));
    }

    ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
    ListTag lTag = (ListTag) tag;

    for (var t : lTag) {
      builder.add(
          ItemStacks.load((CompoundTag) t)
      );
    }

    return builder.build();
  }

  /* ----------------------------- MIN MAX BOUNDS ------------------------------ */

  public static MinMaxBounds.Ints readBounds(BinaryTag tag) {
    if (tag instanceof IntTag integer) {
      return MinMaxBounds.Ints.exactly(integer.intValue());
    }

    CompoundTag cTag = (CompoundTag) tag;
    Integer min = cTag.contains("min") ? cTag.getInt("min") : null;
    Integer max = cTag.contains("max") ? cTag.getInt("max") : null;

    if (min == null && max == null) {
      return MinMaxBounds.Ints.ANY;
    } else if (min == null) {
      return MinMaxBounds.Ints.atMost(max);
    } else if (max == null) {
      return MinMaxBounds.Ints.atLeast(min);
    }

    return MinMaxBounds.Ints.between(min, max);
  }

  public static BinaryTag writeBounds(MinMaxBounds.Ints ints) {
    if (ints.isAny()) {
      return BinaryTags.compoundTag();
    }

    if (isExact(ints)) {
      return BinaryTags.intTag(ints.getMin());
    }

    CompoundTag tag = BinaryTags.compoundTag();

    if (ints.getMax() != null) {
      tag.putInt("max", ints.getMax());
    }

    if (ints.getMin() != null) {
      tag.putInt("min", ints.getMin());
    }

    return tag;
  }

  public static boolean isExact(MinMaxBounds bounds) {
    if (bounds.getMin() == null || bounds.getMax() == null) {
      return false;
    }

    return bounds.getMin().equals(bounds.getMax());
  }

  public static String boundsDisplay(MinMaxBounds.Ints ints) {
    if (ints == null || ints.isAny()) {
      return "Any";
    }

    if (isExact(ints)) {
      return ints.getMin().toString();
    }

    if (ints.getMin() == null) {
      return "at most " + ints.getMax();
    }

    if (ints.getMax() == null) {
      return "at least " + ints.getMin();
    }

    return String.format("%s to %s", ints.getMin(), ints.getMax());
  }
}