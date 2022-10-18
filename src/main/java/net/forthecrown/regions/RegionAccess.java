package net.forthecrown.regions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.math.vector.Vector2i;

import java.util.function.UnaryOperator;

/**
 * An interface for accessing Region data. Currently, there are only 2
 * implementations of this interface, {@link PopulationRegion} and
 * {@link Empty}, the latter is simply an empty region which holds no
 * data
 */
public interface RegionAccess extends HoverEventSource<Component> {
    /**
     * Checks if the region has the given property
     * @param property The property to check for
     * @return True, if the region has it, false otherwise
     */
    boolean hasProperty(RegionProperty property);

    /**
     * Gets the region's region position within all
     * regions, relative to other regions and their
     * regions' region poles.
     *
     * hahaha
     * @return The region's position
     */
    RegionPos getPos();

    /**
     * Gets the Region's pole position
     * @return The region's pole position, if none is set, returns {@link RegionPos#toCenter()} for this region's pos
     */
    Vector2i getPolePosition();

    /**
     * Saves the region into the given tag,
     * Empty regions do not serialize anything
     * @param tag The tag to save into
     */
    void save(CompoundTag tag);

    /**
     * Loads the region from the given tag
     * Empty regions do not load anything
     * @param tag The tag to load from
     */
    void load(CompoundTag tag);

    /**
     * Gets the region's display name
     * @return The region's display name with a hover event and a click event, if the region has a name.
     */
    default Component displayName() {
        return Component.text('[' + (hasName() ? getName() : getPos().toString()) + ']')
                .hoverEvent(this.asHoverEvent())
                .clickEvent(hasName() ? ClickEvent.suggestCommand("/visit " + getName()) : null);
    }

    default Component signName() {
        return Component.text(hasName() ? getName() : "Wilderness");
    }

    default boolean hasName() {
        return !Util.isNullOrBlank(getName());
    }

    String getName();

    @RequiredArgsConstructor
    class Empty implements RegionAccess {
        @Getter
        private final RegionPos pos;

        @Override
        public boolean hasProperty(RegionProperty property) {
            return false;
        }

        @Override
        public Vector2i getPolePosition() {
            return pos.toCenter();
        }

        @Override
        public void save(CompoundTag tag) {

        }

        @Override
        public void load(CompoundTag tag) {

        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public @NotNull HoverEvent<Component> asHoverEvent(@NotNull UnaryOperator<Component> op) {
            return Component.empty().asHoverEvent(op);
        }
    }
}