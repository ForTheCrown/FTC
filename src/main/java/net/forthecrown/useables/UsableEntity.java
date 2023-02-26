package net.forthecrown.useables;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.utils.EntityIdentifier;
import net.forthecrown.utils.text.writer.TextWriter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;

@RequiredArgsConstructor
public class UsableEntity extends BukkitSavedUsable {

  private static final String TAG_IDENTIFIER = "identifier";

  @Getter
  private final UUID id;
  @Getter
  @Setter
  private EntityIdentifier identifier;

  public Entity getEntity() {
    return identifier.get();
  }

  @Override
  public void adminInfo(TextWriter writer) {
    writer.field("Entity", id);
    super.adminInfo(writer);
  }

  @Override
  public PersistentDataContainer getDataContainer() {
    return getEntity().getPersistentDataContainer();
  }

  @Override
  protected NamespacedKey getDataKey() {
    return Usables.ENTITY_KEY;
  }

  @Override
  public void save(CompoundTag tag) {
    tag.put(TAG_IDENTIFIER, identifier.save());
    super.save(tag);
  }

  @Override
  public void load(CompoundTag tag) {
    identifier = EntityIdentifier.load(tag.get(TAG_IDENTIFIER));
    super.load(tag);
  }
}