package net.forthecrown.usables.actions;

import static net.forthecrown.usables.UsableCodecs.ITEM_LIST_OR_SINGLE;

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
import lombok.Getter;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.arguments.ItemListResult;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.usables.Action;
import net.forthecrown.usables.Interaction;
import net.forthecrown.usables.ObjectType;
import net.forthecrown.usables.UsableComponent;
import net.forthecrown.usables.Usables;
import net.forthecrown.usables.objects.Kit;
import net.forthecrown.usables.objects.UsableObject;
import net.forthecrown.utils.inventory.ItemList;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class ItemAction implements Action {

  public static final ObjectType<ItemAction> GIVE_TYPE = new ItemActionType(true);
  public static final ObjectType<ItemAction> REMOVE_TYPE = new ItemActionType(false);

  private final boolean give;
  private final ItemList list;

  public ItemAction(boolean give, ItemList list) {
    this.give = give;
    this.list = list;
  }

  @Override
  public void onUse(Interaction interaction) {
    var inventory = interaction.player().getInventory();

    for (ItemStack item : list) {
      var cloned = item.clone();

      if (give) {
        inventory.addItem(cloned);
      } else {
        inventory.removeItemAnySlot(cloned);
      }
    }
  }

  @Override
  public ObjectType<? extends UsableComponent> getType() {
    return give ? GIVE_TYPE : REMOVE_TYPE;
  }

  @Override
  public @Nullable Component displayInfo() {
    return Usables.hoverableItemList(list);
  }
}

class ItemActionType implements ObjectType<ItemAction> {

  private final boolean give;

  public ItemActionType(boolean give) {
    this.give = give;
  }

  @Override
  public ItemAction parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
    if (!reader.canRead()) {
      throw Exceptions.format(
          "Expected input! '-held-item' to use the item your holding, '-inventory' to include "
              + "all items in your inventory, or <amount>;<item> to read an item",
          "-held_item"
      );
    }

    ItemListResult res = Arguments.ITEM_LIST.parse(reader);
    ItemList list = res.get(source);

    return new ItemAction(give, list);
  }

  @Override
  public CompletableFuture<Suggestions> getSuggestions(
      CommandContext<CommandSource> context,
      SuggestionsBuilder builder
  ) {
    return Arguments.ITEM_LIST.listSuggestions(context, builder);
  }

  @Override
  public <S> DataResult<ItemAction> load(Dynamic<S> dynamic) {
    return ITEM_LIST_OR_SINGLE.decode(dynamic)
        .map(Pair::getFirst)
        .map(itemStacks -> new ItemAction(give, itemStacks));
  }

  @Override
  public <S> DataResult<S> save(@NotNull ItemAction value, @NotNull DynamicOps<S> ops) {
    return ITEM_LIST_OR_SINGLE.encodeStart(ops, value.getList());
  }

  @Override
  public boolean canApplyTo(UsableObject object) {
    return object.as(Kit.class).isEmpty();
  }
}