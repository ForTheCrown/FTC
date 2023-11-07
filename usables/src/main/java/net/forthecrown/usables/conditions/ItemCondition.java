package net.forthecrown.usables.conditions;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.arguments.ItemListResult;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.usables.Condition;
import net.forthecrown.usables.Interaction;
import net.forthecrown.usables.UsableComponent;
import net.forthecrown.usables.Usables;
import net.forthecrown.usables.ObjectType;
import net.forthecrown.utils.inventory.ItemList;
import net.forthecrown.utils.inventory.ItemLists;
import net.forthecrown.utils.io.FtcCodecs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemCondition implements Condition {

  public static final ObjectType<ItemCondition> HAS_ITEMS = new ItemConditionType(true);
  public static final ObjectType<ItemCondition> MISSING_ITEMS = new ItemConditionType(false);

  private final boolean requires;
  private final ItemList list;

  public ItemCondition(boolean requires, ItemList list) {
    this.requires = requires;
    this.list = list;
  }

  public ItemList getList() {
    return ItemLists.cloneAllItems(list);
  }

  @Override
  public boolean test(Interaction interaction) {
    var inventory = interaction.player().getInventory();

    for (ItemStack stack : list) {
      boolean contained = inventory.containsAtLeast(stack, stack.getAmount());

      if (contained != requires) {
        return false;
      }
    }

    return true;
  }

  @Override
  public Component failMessage(Interaction interaction) {
    var inventory = interaction.player().getInventory();

    return Text.format("You must {0}have these items:\n- {1}",
        NamedTextColor.GRAY,
        requires ? "" : "NOT ",

        TextJoiner.on("\n- ").add(list.stream().map(stack -> {
          boolean contained = inventory.containsAtLeast(stack, stack.getAmount());

          NamedTextColor color = contained == requires
              ? NamedTextColor.YELLOW
              : NamedTextColor.GRAY;

          return Text.itemAndAmount(stack).color(color);
        }))
    );
  }

  @Override
  public ObjectType<? extends UsableComponent> getType() {
    return requires ? HAS_ITEMS : MISSING_ITEMS;
  }

  @Override
  public @Nullable Component displayInfo() {
    return Usables.hoverableItemList(list);
  }
}

class ItemConditionType implements ObjectType<ItemCondition> {

  private final boolean requires;

  public ItemConditionType(boolean requires) {
    this.requires = requires;
  }

  @Override
  public ItemCondition parse(StringReader reader, CommandSource source)
      throws CommandSyntaxException
  {
    if (!reader.canRead()) {
      throw Exceptions.format(
          "Expected input! '-held-item' to use the item your holding, '-inventory' to include "
              + "all items in your inventory, or <amount>;<item> to read an item",
          "-held_item"
      );
    }

    ItemListResult res = Arguments.ITEM_LIST.parse(reader);
    ItemList list = res.get(source);
    return new ItemCondition(requires, list);
  }

  @Override
  public CompletableFuture<Suggestions> getSuggestions(
      CommandContext<CommandSource> context,
      SuggestionsBuilder builder
  ) {
    return Arguments.ITEM_LIST.listSuggestions(context, builder);
  }

  @Override
  public <S> DataResult<ItemCondition> load(Dynamic<S> dynamic) {
    return FtcCodecs.ITEM_LIST_CODEC.decode(dynamic)
        .map(Pair::getFirst)
        .map(itemStacks -> new ItemCondition(requires, itemStacks));
  }

  @Override
  public <S> DataResult<S> save(@NotNull ItemCondition value, @NotNull DynamicOps<S> ops) {
    return FtcCodecs.ITEM_LIST_CODEC.encodeStart(ops, value.getList());
  }
}