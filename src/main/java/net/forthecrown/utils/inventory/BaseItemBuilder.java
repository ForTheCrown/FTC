package net.forthecrown.utils.inventory;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.sk89q.worldedit.bukkit.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import net.forthecrown.dungeons.enchantments.FtcEnchant;
import net.forthecrown.dungeons.enchantments.FtcEnchants;
import net.forthecrown.text.Text;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.Util;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v1_19_R1.persistence.CraftPersistentDataContainer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * The very base of an item builder implementing any and all methods
 * that all item types allow
 * @see ItemStacks For the item builder static constructors
 * @see PotionItemBuilder For an implementation of this class focused around
 *                        potion items
 * @see SkullItemBuilder For no implementation of this class focused around
 *                       player skull items
 * @see DefaultItemBuilder For the default implementation of this class
 */
@Getter
public abstract class BaseItemBuilder<T extends BaseItemBuilder<T>> implements Cloneable {

    private final Material material;
    private Component name = null;

    private int amount = 1;

    private ItemFlag[] flags = new ItemFlag[0];

    private boolean ignoreEnchantRestrictions = true;
    private boolean unbreakable = false;

    private Collection<Component> lores = new ObjectArrayList<>();

    private final Object2IntMap<Enchantment> enchants = new Object2IntOpenHashMap<>();
    private final CraftPersistentDataContainer persistentData = TagUtil.newContainer();
    private final Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create();

    private Integer customModelData = null;

    public BaseItemBuilder(Material material, int amount) {
        this.material = material;
        setAmount(amount);
    }

    /**
     * Returns this class. Because I couldn't be arsed writing <code>(T) this</code> a lot
     * @return This
     */
    protected abstract T getThis();

    public <X, Z> T addData(@NotNull NamespacedKey key, @NotNull PersistentDataType<X, Z> type, @NotNull Z value) {
        persistentData.set(key, type, value);
        return getThis();
    }

    public T setAmount(int amount) {
        Validate.isTrue(
                amount >= 0 && amount <= material.getMaxStackSize(),
                "Invalid stack size: %s", amount
        );

        this.amount = amount;
        return getThis();
    }

    public T addModifier(Attribute attribute,
                         String name,
                         double amount,
                         AttributeModifier.Operation operation,
                         EquipmentSlot slot
    ) {
        return addModifier(attribute, new AttributeModifier(UUID.randomUUID(), name, amount, operation, slot));
    }

    public T addModifier(Attribute attribute, AttributeModifier modifier) {
        this.modifiers.put(attribute, modifier);
        return getThis();
    }

    public T addLore(String lore) {
        return addLore(Text.stringToItemText(lore));
    }

    public T addLore(Component lore) {
        lores.add(lore);
        return getThis();
    }

    public T addLore(Iterable<Component> lore) {
        for (Component c: lore) {
            lores.add(c);
        }

        return getThis();
    }

    public T setLore(Iterable<Component> lores) {
        this.lores = Lists.newArrayList(lores);
        return getThis();
    }

    public T setName(Component name) {
        this.name = name;
        return getThis();
    }

    public T setName(String name) {
        this.name = Text.stringToItemText(name);
        return getThis();
    }

    public T setFlags(ItemFlag... flags) {
        this.flags = flags;
        return getThis();
    }

    public T addEnchant(Enchantment enchantment, int level){
        this.enchants.put(enchantment, level);
        return getThis();
    }

    public T ignoreEnchantRestrictions(boolean ignoreEnchantRestrictions) {
        this.ignoreEnchantRestrictions = ignoreEnchantRestrictions;
        return getThis();
    }

    public T setUnbreakable(boolean unbreakable){
        this.unbreakable = unbreakable;
        return getThis();
    }

    public T setModelData(Integer customModelData) {
        this.customModelData = customModelData;
        return getThis();
    }

    public ItemStack build() {
        ItemStack result = new ItemStack(material, amount);
        ItemMeta meta = result.getItemMeta();

        meta.setCustomModelData(customModelData);
        meta.addItemFlags(flags);

        if (name != null) {
            meta.displayName(name);
        }

        if (!Util.isNullOrEmpty(lores)) {
            meta.lore(new ArrayList<>(lores));
        }

        if (!enchants.isEmpty()) {
            for (var e: enchants.object2IntEntrySet()) {
                var ench = e.getKey();
                var level = e.getIntValue();

                if (ench instanceof FtcEnchant ftcEnchant) {
                    FtcEnchants.addEnchant(meta, ftcEnchant, level);
                    continue;
                }

                meta.addEnchant(ench, level, ignoreEnchantRestrictions);
            }
        }

        if (!modifiers.isEmpty()) {
            meta.setAttributeModifiers(modifiers);
        }

        if (!persistentData.isEmpty()) {
           var container = (CraftPersistentDataContainer) meta.getPersistentDataContainer();
           container.putAll(persistentData.getRaw());
        }

        meta.setUnbreakable(unbreakable);

        onBuild(result, meta);

        result.setItemMeta(meta);
        return result;
    }

    protected abstract void onBuild(ItemStack item, ItemMeta meta);

    @Override
    protected T clone() {
        try {
            return (T) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}