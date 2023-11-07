package net.forthecrown.usables;

import javax.annotation.Nullable;
import net.forthecrown.grenadier.types.IntRangeArgument.IntRange;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.usables.objects.UsableBlock;
import net.forthecrown.usables.objects.UsableEntity;
import net.forthecrown.usables.objects.UsableItem;
import net.forthecrown.utils.inventory.ItemList;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

public final class Usables {
  private Usables() {}

  public static final NamespacedKey BLOCK_KEY  = new NamespacedKey("forthecrown", "usable_block");
  public static final NamespacedKey ENTITY_KEY = new NamespacedKey("forthecrown", "usable_entity");
  public static final NamespacedKey ITEM_KEY   = new NamespacedKey("forthecrown", "usable_item");

  public static boolean isUsable(Block block) {
    if (!(block.getState() instanceof TileState tile)) {
      return false;
    }

    return hasTag(tile, BLOCK_KEY);
  }

  public static boolean isUsable(Entity entity) {
    return hasTag(entity, ENTITY_KEY);
  }

  public static boolean isUsable(@Nullable ItemStack itemStack) {
    if (ItemStacks.isEmpty(itemStack)) {
      return false;
    }
    return hasTag(itemStack.getItemMeta(), ITEM_KEY);
  }

  private static boolean hasTag(PersistentDataHolder holder, NamespacedKey key) {
    return holder.getPersistentDataContainer().has(key, PersistentDataType.TAG_CONTAINER);
  }

  public static UsableEntity entity(Entity entity) {
    return new UsableEntity(entity);
  }

  public static UsableBlock block(Block block) {
    return new UsableBlock(block);
  }

  public static UsableItem item(ItemStack itemStack) {
    return new UsableItem(itemStack);
  }

  public static Condition testConditions(
      Iterable<Condition> conditions,
      Interaction interaction
  ) {
    for (Condition condition : conditions) {
      if (condition.test(interaction)) {
        continue;
      }

      return condition;
    }

    return null;
  }

  public static boolean test(Iterable<Condition> conditions, Interaction interaction) {
    return testConditions(conditions, interaction) == null;
  }

  public static boolean runConditions(
      Iterable<Condition> conditions,
      Interaction interaction
  ) {
    Condition failed = testConditions(conditions, interaction);

    if (failed == null) {
      for (Condition condition : conditions) {
        condition.afterTests(interaction);
      }

      return true;
    }

    Component message = failed.failMessage(interaction);
    boolean silent = interaction.getBoolean("silent").orElse(false);

    if (message != null && !silent) {
      interaction.player().sendMessage(message);
    }

    return false;
  }

  public static void runActions(Iterable<Action> actions, Interaction interaction) {
    for (Action action : actions) {
      action.onUse(interaction);
    }
  }

  public static String boundsDisplay(IntRange ints) {
    if (ints == null || ints.isUnlimited()) {
      return "Any";
    }

    if (ints.isExact()) {
      return String.valueOf(ints.min().getAsInt());
    }

    if (ints.min().isEmpty()) {
      return "at most " + ints.max().getAsInt();
    }

    if (ints.max().isEmpty()) {
      return "at least " + ints.min().getAsInt();
    }

    return String.format("%s to %s", ints.min().getAsInt(), ints.max().getAsInt());
  }

  public static Component hoverableItemList(ItemList list) {
    Component itemList = TextJoiner.newJoiner()
        .setDelimiter(Component.text("\n- "))
        .setPrefix(Component.text("Items:\n"))
        .add(list.stream().map(Text::itemAndAmount))
        .asComponent();

    return Component.text("[Hover to see items]", NamedTextColor.AQUA).hoverEvent(itemList);
  }
}
