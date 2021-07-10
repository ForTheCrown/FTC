package net.forthecrown.cosmetics.deaths;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.cosmetics.CosmeticConstants;
import net.forthecrown.cosmetics.CosmeticEffect;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.user.CosmeticData;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.ItemStackBuilder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractDeathEffect implements CosmeticEffect {

    protected final int slot;
    protected final Component[] description;
    protected final String name;
    protected final Key key;

    AbstractDeathEffect(int slot, String name, Component... description) {
        this.slot = slot;
        this.description = description;
        this.name = name;

        this.key = CrownCore.coreKey(name.toLowerCase().replaceAll(" ", "_").replaceAll("'", ""));
    }

    AbstractDeathEffect(int slot, String name, String desc){
        this(slot, name, ChatUtils.convertString(desc, true));
    }

    public abstract void activate(Location loc);

    @Override
    public @NotNull Key key() {
        return key;
    }

    public String getName() {
        return name;
    }

    public Component[] getDescription() {
        return description;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void place(Inventory inventory, CrownUser user) {
        inventory.setItem(getSlot(), makeItem(user));
    }

    public ItemStack makeItem(CrownUser user){
        CosmeticData data = user.getCosmeticData();
        boolean owned = data.hasDeath(this);

        ItemStackBuilder builder = new ItemStackBuilder(owned ? Material.ORANGE_DYE : Material.GRAY_DYE)
                .setName(name().style(ChatFormatter.nonItalic(NamedTextColor.YELLOW)));

        for (Component c: description){
            builder.addLore(c.style(ChatFormatter.nonItalic(NamedTextColor.GRAY)));
        }

        builder.addLore(Component.empty());

        if(!owned){
            builder.addLore(
                    Component.text("Click to purchase for ")
                            .style(ChatFormatter.nonItalic(NamedTextColor.GRAY))
                            .append(Component.text(CosmeticConstants.DEATH_PRICE + " Gems").style(ChatFormatter.nonItalic(NamedTextColor.GOLD)))
            );
        }

        AbstractDeathEffect deathParticle = data.getActiveDeath();
        if(deathParticle != null && deathParticle.key.equals(key)){
            builder
                    .addEnchant(Enchantment.CHANNELING, 1)
                    .setFlags(ItemFlag.HIDE_ENCHANTS);
        }

        return builder.build();
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws RoyalCommandException {
        CosmeticData data = user.getCosmeticData();
        boolean owned = data.hasDeath(this);

        if(owned){
            data.setActiveDeath(this);
            user.sendMessage(Component.translatable("user.deathParticle.set", NamedTextColor.YELLOW, name()));
        } else {
            if(user.getGems() < CosmeticConstants.DEATH_PRICE){
                user.sendMessage(Component.translatable("commands.cannotAfford", NamedTextColor.RED, ChatFormatter.queryGems(CosmeticConstants.DEATH_PRICE)));
                return;
            }

            user.setGems(user.getGems() - CosmeticConstants.DEATH_PRICE);
            data.addDeath(this);
            data.setActiveDeath(this);

            user.sendMessage(
                    Component.translatable("user.particle.bought", NamedTextColor.GRAY, name())
            );
        }

        context.setReloadInventory(true);
    }

    public String toString(){
        return key.asString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        AbstractDeathEffect effect = (AbstractDeathEffect) o;

        return new EqualsBuilder()
                .append(getSlot(), effect.getSlot())
                .append(getDescription(), effect.getDescription())
                .append(getName(), effect.getName())
                .append(key, effect.key)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getSlot())
                .append(getDescription())
                .append(getName())
                .append(key)
                .toHashCode();
    }
}
