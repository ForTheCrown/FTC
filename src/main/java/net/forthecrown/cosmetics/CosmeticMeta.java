package net.forthecrown.cosmetics;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import net.forthecrown.text.Text;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.user.data.CosmeticData;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import static net.forthecrown.text.Text.nonItalic;

@Getter
public class CosmeticMeta {
    private final Component displayName;
    private final ImmutableSet<Component> description;
    private final Material availableMaterial;
    private final Material unavailableMaterial;

    private final MenuNode option;

    private final Cosmetic cosmetic;

    CosmeticMeta(Cosmetic cosmetic, Builder dataBuilder) {
        this.cosmetic = cosmetic;

        if (dataBuilder.option != null) {
            this.displayName = dataBuilder.name;
        } else {
            this.displayName = Validate.notNull(dataBuilder.name);
        }

        this.description = dataBuilder.description.build();

        this.availableMaterial = dataBuilder.availableMaterial;
        this.unavailableMaterial = dataBuilder.unavailableMaterial;

        if (dataBuilder.option == null) {
            option = createOption();
        } else {
            option = dataBuilder.option;
        }
    }

    private MenuNode createOption() {
        return MenuNode.builder()
                .setItem(user -> {
                    var data = user.getCosmeticData();
                    boolean owned = data.contains(cosmetic);
                    boolean active = cosmetic.equals(data.get(cosmetic.getType()));

                    var builder = ItemStacks.builder(owned ? Material.ORANGE_DYE : Material.GRAY_DYE)
                            .setName(getItemDisplayName());

                    for (Component c: getDescription()) {
                        builder.addLore(c.style(nonItalic(NamedTextColor.GRAY)));
                    }

                    builder.addLore(Component.empty());

                    if (!owned) {
                        builder.addLore(
                                Text.format("Click to purchase for &6{0, gems}",
                                        nonItalic(NamedTextColor.GRAY),
                                        cosmetic.type.getPrice()
                                )
                        );
                    }

                    if (active) {
                        builder
                                .addEnchant(Enchantment.CHANNELING, 1)
                                .setFlags(ItemFlag.HIDE_ENCHANTS);
                    }

                    return builder.build();
                })

                .setRunnable((user, click) -> {
                    CosmeticData data = user.getCosmeticData();
                    boolean owned = data.contains(cosmetic);

                    if (owned) {
                        if (equals(data.get(cosmetic.getType()))) {
                            throw Exceptions.alreadySetCosmetic(
                                    getItemDisplayName(),
                                    cosmetic.getType().getDisplayName()
                            );
                        }

                        data.set(cosmetic.getType(), cosmetic);

                        user.sendMessage(
                                Text.format(
                                        "Set {0} to be your {1} effect",
                                        getItemDisplayName(),
                                        cosmetic.getType().getDisplayName()
                                )
                        );
                    } else {
                        if(user.getGems() < cosmetic.type.getPrice()){
                            user.sendMessage(
                                    Text.format("Cannot afford {0, gems}",
                                            NamedTextColor.RED,
                                            cosmetic.type.getPrice()
                                    )
                            );

                            return;
                        }

                        user.setGems(user.getGems() - cosmetic.type.getPrice());

                        data.add(cosmetic);
                        data.set(cosmetic.getType(), cosmetic);

                        user.sendMessage(
                                Text.format(
                                        "Bought {0} for {1, gems}",
                                        NamedTextColor.GRAY,
                                        getItemDisplayName(),
                                        cosmetic.type.getPrice()
                                )
                        );
                    }

                    click.shouldReloadMenu(true);
                })

                .build();
    }

    public Component getItemDisplayName() {
        return getDisplayName()
                .style(nonItalic(NamedTextColor.YELLOW));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Component name;
        private ImmutableSet.Builder<Component> description = ImmutableSet.builder();
        private Material availableMaterial = Material.ORANGE_DYE;
        private Material unavailableMaterial = Material.GRAY_DYE;

        private MenuNode option;

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

        public Builder setAvailableMaterial(Material availableMaterial) {
            this.availableMaterial = availableMaterial;
            return this;
        }

        public Builder setUnavailableMaterial(Material unavailableMaterial) {
            this.unavailableMaterial = unavailableMaterial;
            return this;
        }

        public Builder setOption(MenuNode option) {
            this.option = option;
            return this;
        }
    }
}