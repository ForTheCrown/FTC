package net.forthecrown.cosmetics;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;

import static net.forthecrown.utils.text.Text.nonItalic;

@Getter
public class CosmeticMeta {
    private final Component displayName;
    private final ImmutableList<Component> description;
    private final Material availableMaterial;
    private final Material unavailableMaterial;

    CosmeticMeta(Builder dataBuilder) {
        this.displayName = dataBuilder.name;

        this.description = dataBuilder.description.build();

        this.availableMaterial = dataBuilder.availableMaterial;
        this.unavailableMaterial = dataBuilder.unavailableMaterial;
    }

    public Component getItemDisplayName() {
        return getDisplayName()
                .style(nonItalic(NamedTextColor.YELLOW));
    }

    public Material getMaterial(boolean owned) {
        return owned ? availableMaterial : unavailableMaterial;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Accessors(chain = true)
    public static class Builder {
        private Component name;
        private final ImmutableList.Builder<Component>
                description = ImmutableList.builder();

        @Setter
        private Material availableMaterial = Material.ORANGE_DYE;

        @Setter
        private Material unavailableMaterial = Material.GRAY_DYE;

        public Builder setName(Component name) {
            this.name = name;
            return this;
        }

        public Builder setName(String name) {
            return setName(Text.renderString(name));
        }

        public Builder addDescription(Component component) {
            description.add(component);
            return this;
        }

        public Builder addDescription(Component... components) {
            for (var c: Validate.noNullElements(components)) {
                addDescription(c);
            }

            return this;
        }

        public Builder addDescription(String desc) {
            return addDescription(Text.renderString(desc));
        }
    }
}