package net.forthecrown.usables.objects;

import static net.forthecrown.usables.Usables.ITEM_KEY;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.Getter;
import net.forthecrown.text.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

@Getter
public class UsableItem extends InWorldUsable {

  private final ItemStack item;

  public UsableItem(ItemStack item) {
    Objects.requireNonNull(item);
    this.item = item;
  }

  @Override
  public void fillContext(Map<String, Object> context) {
    super.fillContext(context);
    context.put("item", item);
  }

  @Override
  public Component name() {
    return Text.itemDisplayName(item);
  }

  @Override
  public String getCommandPrefix() {
    return "/usable_item";
  }

  @Override
  protected void executeOnContainer(
      boolean saveIntent,
      Consumer<PersistentDataContainer> consumer
  ) {
    ItemMeta meta = item.getItemMeta();

    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    PersistentDataContainer dataPdc;

    if (saveIntent) {
      dataPdc = pdc.getAdapterContext().newPersistentDataContainer();
    } else {
      if (!pdc.has(ITEM_KEY, PersistentDataType.TAG_CONTAINER)) {
        LOGGER.warn("Cannot load from non-usable item {}", item);
        return;
      }

      dataPdc = pdc.get(ITEM_KEY, PersistentDataType.TAG_CONTAINER);
    }

    consumer.accept(dataPdc);

    if (saveIntent) {
      pdc.set(ITEM_KEY, PersistentDataType.TAG_CONTAINER, dataPdc);
      item.setItemMeta(meta);
    }
  }
}
