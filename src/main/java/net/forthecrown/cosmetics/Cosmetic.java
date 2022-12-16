package net.forthecrown.cosmetics;

import lombok.Getter;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Messages;
import net.forthecrown.user.data.CosmeticData;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Slot;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import static net.forthecrown.utils.text.Text.nonItalic;

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

        this.displayData = new CosmeticMeta(metaBuilder);

        type.add(this);
    }

    public Cosmetic(String name, CosmeticType type, Slot slot, Component... desc) {
        this(name, type, slot,
                CosmeticMeta.builder()
                        .setName(name)
                        .addDescription(desc)
        );
    }

    public MenuNode createNode() {
        return MenuNode.builder()
                .setItem(user -> {
                    var data = user.getCosmeticData();
                    boolean owned = data.contains(this);
                    boolean active = this.equals(data.get(getType()));

                    var builder = ItemStacks.builder(displayData.getMaterial(owned))
                            .setNameRaw(displayData.getItemDisplayName());

                    for (Component c: displayData.getDescription()) {
                        builder.addLoreRaw(c.style(nonItalic(NamedTextColor.GRAY)));
                    }

                    builder.addLoreRaw(Component.empty());

                    if (!owned) {
                        builder.addLoreRaw(
                                Text.format("Click to purchase for &6{0, gems}",
                                        nonItalic(NamedTextColor.GRAY),
                                        type.getPrice()
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
                    boolean owned = data.contains(this);

                    if (owned) {
                        if (equals(data.get(getType()))) {
                            throw Exceptions.alreadySetCosmetic(
                                    displayData.getItemDisplayName(),
                                    getType().getDisplayName()
                            );
                        }

                        data.set(getType(), this);
                        user.sendMessage(Messages.setCosmetic(this));
                    } else {
                        if(user.getGems() < type.getPrice()) {
                            user.sendMessage(
                                    Text.format("Cannot afford {0, gems}",
                                            NamedTextColor.RED,
                                            type.getPrice()
                                    )
                            );

                            return;
                        }

                        user.setGems(user.getGems() - type.getPrice());

                        data.add(this);
                        data.set(getType(), this);

                        user.sendMessage(
                                Text.format(
                                        "Bought {0} for {1, gems}",
                                        NamedTextColor.GRAY,
                                        displayData.getItemDisplayName(),
                                        type.getPrice()
                                )
                        );
                    }

                    click.shouldReloadMenu(true);
                })

                .build();
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