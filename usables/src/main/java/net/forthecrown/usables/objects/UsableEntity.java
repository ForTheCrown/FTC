package net.forthecrown.usables.objects;

import static net.forthecrown.usables.Usables.ENTITY_KEY;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

@Getter
public class UsableEntity extends InWorldUsable {

  private final Entity entity;

  public UsableEntity(Entity entity) {
    Objects.requireNonNull(entity);
    this.entity = entity;
  }

  @Override
  public void fillContext(Map<String, Object> context) {
    super.fillContext(context);
    context.put("location", entity.getLocation());
    context.put("entity", entity);
  }

  @Override
  public CommandSender getCommandSender() {
    return entity;
  }

  @Override
  public Component name() {
    return entity.teamDisplayName();
  }

  @Override
  public String getCommandPrefix() {
    return "/usable_entity " + entity.getUniqueId();
  }

  @Override
  protected void executeOnContainer(
      boolean saveIntent,
      Consumer<PersistentDataContainer> consumer
  ) {
    PersistentDataContainer pdc = entity.getPersistentDataContainer();
    PersistentDataContainer dataPdc;

    if (saveIntent) {
      dataPdc = pdc.getAdapterContext().newPersistentDataContainer();
    } else {
      if (!pdc.has(ENTITY_KEY, PersistentDataType.TAG_CONTAINER)) {
        LOGGER.warn("Cannot load from non-usable entity {}", entity);
        return;
      }

      dataPdc = pdc.get(ENTITY_KEY, PersistentDataType.TAG_CONTAINER);
    }

    consumer.accept(dataPdc);

    if (saveIntent) {
      pdc.set(ENTITY_KEY, PersistentDataType.TAG_CONTAINER, dataPdc);
    }
  }
}
