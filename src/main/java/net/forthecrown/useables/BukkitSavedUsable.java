package net.forthecrown.useables;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.utils.io.TagUtil;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * A usable which is saved into a {@link PersistentDataContainer}
 * and typically exists within the MC world itself.
 */
public abstract class BukkitSavedUsable extends AbstractUsable {
    /**
     * Determines if the vanilla interaction that occurs
     * when you interact with the underlying MC entity
     * should be cancelled
     */
    @Getter @Setter
    protected boolean cancelVanilla;

    public abstract PersistentDataContainer getDataContainer();
    protected abstract NamespacedKey getDataKey();

    public void save() {
        var container = getDataContainer();
        save(container);
    }

    public void save(PersistentDataContainer container) {
        CompoundTag tag = new CompoundTag();
        save(tag);

        container.set(
                getDataKey(),
                PersistentDataType.TAG_CONTAINER,
                TagUtil.ofCompound(tag)
        );
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putBoolean("cancelVanilla", cancelVanilla);
        super.save(tag);
    }

    public void load() {
        var container = getDataContainer();
        load(container);
    }

    public void load(PersistentDataContainer container) {
        var tag = container.getOrDefault(
                getDataKey(),
                PersistentDataType.TAG_CONTAINER,
                TagUtil.newContainer()
        );

        load(TagUtil.ofContainer(tag));
    }

    @Override
    public void load(CompoundTag tag) {
        setCancelVanilla(tag.getBoolean("cancelVanilla"));
        super.load(tag);
    }
}