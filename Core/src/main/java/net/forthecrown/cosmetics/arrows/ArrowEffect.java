package net.forthecrown.cosmetics.arrows;

import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.cosmetics.CosmeticConstants;
import net.forthecrown.cosmetics.CosmeticEffect;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.user.CosmeticData;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.ItemStackBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.builder.EqualsBuilder;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ArrowEffect implements CosmeticEffect {

    private final Particle particle;
    private final String name;
    private final Component[] description;
    private final InventoryPos slot;
    private final Key key;

    ArrowEffect(int slot, Particle particle, String name, Component... description) {
        this.particle = particle;
        this.name = name;
        this.description = description;
        this.slot = InventoryPos.fromSlot(slot);

        this.key = Crown.coreKey(name.toLowerCase().replaceAll(" ", "_").replaceAll("'", ""));
    }

    ArrowEffect(int slot, Particle particle, String name, String desc){
        this(slot, particle, name, ChatUtils.convertString(desc, true));
    }

    public Component[] getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public Particle getParticle() {
        return particle;
    }

    @Override
    public InventoryPos getPos() {
        return slot;
    }

    @Override
    public void place(Inventory inventory, CrownUser user) {
        inventory.setItem(getSlot(), makeItem(user));
    }

    public ItemStack makeItem(CrownUser user){
        boolean owned = user.getCosmeticData().hasArrow(this);

        ItemStackBuilder builder = new ItemStackBuilder(owned ? Material.ORANGE_DYE : Material.GRAY_DYE)
                .setName(name().style(FtcFormatter.nonItalic(NamedTextColor.YELLOW)));

        for (Component c: description){
            builder.addLore(c.style(FtcFormatter.nonItalic(NamedTextColor.GRAY)));
        }

        builder.addLore(Component.empty());

        if(!owned){
            builder.addLore(
                    Component.text("Click to purchase for ")
                            .style(FtcFormatter.nonItalic(NamedTextColor.GRAY))
                            .append(Component.text(FtcFormatter.decimalizeNumber(CosmeticConstants.ARROW_PRICE) + " Gems").style(FtcFormatter.nonItalic(NamedTextColor.GOLD)))
            );
        }

        ArrowEffect userEffect = user.getCosmeticData().getActiveArrow();
        if(userEffect != null && userEffect.key.equals(key)){
            builder
                    .addEnchant(Enchantment.CHANNELING, 1)
                    .setFlags(ItemFlag.HIDE_ENCHANTS);
        }

        return builder.build();
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws RoyalCommandException {
        CosmeticData data = user.getCosmeticData();
        boolean owned = data.hasArrow(this);

        if(owned){
            data.setActiveArrow(this);
            user.sendMessage(Component.translatable("user.arrowParticle.set", NamedTextColor.YELLOW, name()));
        } else {
            if(user.getGems() < CosmeticConstants.ARROW_PRICE){
                user.sendMessage(Component.translatable("commands.cannotAfford", NamedTextColor.RED, FtcFormatter.queryGems(CosmeticConstants.ARROW_PRICE)));
                return;
            }

            user.setGems(user.getGems() - CosmeticConstants.ARROW_PRICE);
            data.addArrow(this);
            data.setActiveArrow(this);

            user.sendMessage(
                    Component.translatable("user.particle.bought", NamedTextColor.GRAY, name())
            );
        }

        context.setReloadInventory(true);
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    public String toString(){
        return key.asString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ArrowEffect effect = (ArrowEffect) o;

        return new EqualsBuilder()
                .append(getParticle(), effect.getParticle())
                .append(getName(), effect.getName())
                .append(getDescription(), effect.getDescription())
                .append(getSlot(), effect.getSlot())
                .append(key, effect.key)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getParticle())
                .append(getName())
                .append(getDescription())
                .append(getSlot())
                .append(key)
                .toHashCode();
    }
}
