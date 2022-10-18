package net.forthecrown.cosmetics;

import lombok.Getter;
import net.forthecrown.utils.inventory.menu.Slot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.builder.HashCodeBuilder;

import static net.forthecrown.text.Text.nonItalic;

public abstract class Cosmetic {
    @Getter
    protected final String name;
    @Getter
    protected final Slot slot;

    @Getter
    protected final CosmeticType type;

    @Getter
    protected final CosmeticMeta displayData;

    public Cosmetic(String name, CosmeticType type, Slot slot, CosmeticMeta.Builder metaBuilder) {
        this.name = name;
        this.slot = slot;
        this.type = type;

        this.displayData = new CosmeticMeta(this, metaBuilder);

        type.add(this);
    }

    public Cosmetic(String name, CosmeticType type, Slot slot, Component... desc) {
        this(name, type, slot,
                CosmeticMeta.builder()
                        .setName(name)
                        .addDescription(desc)
        );
    }

    public Component displayName() {
        return name().style(nonItalic(NamedTextColor.YELLOW));
    }

    public Component name() {
        return Component.text(getName());
    }

    @Override
    public String toString() {
        return getSerialId();
    }

    public String getSerialId() {
        return getName()
                .replaceAll(" ", "_")
                .replaceAll("'", "")
                .toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Cosmetic effect = (Cosmetic) o;

        return effect.getName().equals(getName())
                && effect.getType().equals(getType());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(this.getName())
                .append(this.getSlot())
                .append(this.getType())
                .toHashCode();
    }
}