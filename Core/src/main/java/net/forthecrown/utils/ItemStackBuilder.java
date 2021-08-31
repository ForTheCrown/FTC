package net.forthecrown.utils;

import com.sk89q.worldedit.bukkit.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.utils.math.MathUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;

import java.util.*;

/**
 * A class for building item stacks
 */
public class ItemStackBuilder implements Cloneable {

    private final Material material;
    private Component name = null;
    private int amount = 1;
    private ItemFlag[] flags;

    private boolean ignoreEnchantRestrictions = true;
    private boolean unbreakable = false;

    private List<Component> lores = new ObjectArrayList<>();
    private List<PotionEffect> effects = new ObjectArrayList<>();

    private PotionData baseEffect = null;
    private boolean ambientEffects = true;

    private Map<Attribute, AttributeModifier> modifiers = new Object2ObjectOpenHashMap<>();
    private Map<NamespacedKey, Byte> persistentData = new Object2ObjectOpenHashMap<>();
    private Map<Enchantment, Integer> enchants = new Object2ObjectOpenHashMap<>();
    private CompoundTag tags = new CompoundTag();

    public ItemStackBuilder(Material material){
        this(material, 1);
    }

    public ItemStackBuilder(Material material, int amount){
        this.material = material;
        setAmount(amount);
    }

    public ItemStackBuilder addEffect(PotionEffect effect){
        effects.add(effect);
        return this;
    }

    public ItemStackBuilder setBaseEffect(PotionData effect){
        this.baseEffect = effect;
        return this;
    }

    public PotionData getBaseEffect() {
        return baseEffect;
    }

    public List<PotionEffect> getEffects(){
        return effects;
    }

    public ItemStackBuilder addData(NamespacedKey key, byte b){
        persistentData.put(key, b);
        return this;
    }

    public Map<NamespacedKey, Byte> getPersistentData() {
        return persistentData;
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public ItemStackBuilder setAmount(int amount) {
        Validate.isTrue(MathUtil.isInRange(amount, 0, material.getMaxStackSize()), "Invalid stack size");
        this.amount = amount;
        return this;
    }

    public Map<Attribute, AttributeModifier> getModifiers() {
        return modifiers;
    }

    public ItemStackBuilder addModifier(Attribute attribute, String name, double amount, AttributeModifier.Operation operation, EquipmentSlot slot){
        return addModifier(attribute, new AttributeModifier(UUID.randomUUID(), name, amount, operation, slot));
    }

    public ItemStackBuilder addModifier(Attribute attribute, AttributeModifier modifier) {
        this.modifiers.put(attribute, modifier);
        return this;
    }

    public ItemStackBuilder modifiers(Map<Attribute, AttributeModifier> modifiers) {
        this.modifiers = modifiers;
        return this;
    }

    public List<Component> getLore() {
        return lores;
    }

    public ItemStackBuilder addLore(String lore, boolean translateColors){
        return addLore(ChatUtils.convertString(lore, translateColors));
    }

    public ItemStackBuilder setLore(Collection<String> lores, boolean translateColors){
        return setLore(ListUtils.convert(lores, s -> ChatUtils.stringToNonItalic(s, translateColors)));
    }

    public ItemStackBuilder addLore(Component lore) {
        this.lores.add(ChatUtils.renderIfTranslatable(lore));
        return this;
    }

    public ItemStackBuilder addLore(Iterable<Component> lore) {
        for (Component c: lore) {
            lores.add(ChatUtils.renderIfTranslatable(c));
        }

        return this;
    }

    public ItemStackBuilder setLore(List<Component> lores) {
        this.lores = ListUtils.convert(lores, ChatUtils::renderIfTranslatable);
        return this;
    }

    public Component getName() {
        return name;
    }

    public ItemStackBuilder setName(Component name) {
        this.name = ChatUtils.renderIfTranslatable(name);
        return this;
    }

    public ItemStackBuilder setName(String name, boolean translateColors){
        this.name = ChatUtils.stringToNonItalic(name, translateColors);
        return this;
    }

    public ItemFlag[] getFlags() {
        return flags;
    }

    public ItemStackBuilder setFlags(ItemFlag... flags) {
        if(flags.length == 1){
            this.flags = new ItemFlag[1];
            this.flags[0] = flags[0];
        } else this.flags = flags;
        return this;
    }

    public Map<Enchantment, Integer> getEnchants() {
        return enchants;
    }

    public ItemStackBuilder setEnchants(Map<Enchantment, Integer> enchants) {
        this.enchants = enchants;
        return this;
    }

    public ItemStackBuilder addEnchant(Enchantment enchantment, int level){
        this.enchants.put(enchantment, level);
        return this;
    }

    public boolean ignoreEnchantRestrictions() {
        return ignoreEnchantRestrictions;
    }

    public ItemStackBuilder ignoreEnchantRestrictions(boolean ignoreEnchantRestrictions) {
        this.ignoreEnchantRestrictions = ignoreEnchantRestrictions;
        return this;
    }

    public ItemStackBuilder setUnbreakable(boolean unbreakable){
        this.unbreakable = unbreakable;
        return this;
    }

    public boolean isUnbreakable() {
        return unbreakable;
    }

    public ItemStackBuilder addTag(String name, Tag tag) {
        tags.put(name, tag);
        return this;
    }

    public ItemStackBuilder putByteTag(String key, byte value) {
        tags.putByte(key, value);
        return this;
    }

    public ItemStackBuilder putShortTag(String key, short value) {
        tags.putShort(key, value);
        return this;
    }

    public ItemStackBuilder putIntTag(String key, int value) {
        tags.putInt(key, value);
        return this;
    }

    public ItemStackBuilder putLongTag(String key, long value) {
        tags.putLong(key, value);
        return this;
    }

    public ItemStackBuilder putUUIDTag(String key, UUID value) {
        tags.putUUID(key, value);
        return this;
    }

    public ItemStackBuilder putFloatTag(String key, float value) {
        tags.putFloat(key, value);
        return this;
    }

    public ItemStackBuilder putDoubleTag(String key, double value) {
        tags.putDouble(key, value);
        return this;
    }

    public ItemStackBuilder putStringTag(String key, String value) {
        tags.putString(key, value);
        return this;
    }

    public ItemStackBuilder setTags(CompoundTag tags) {
        this.tags = tags;
        return this;
    }

    public CompoundTag getTags() {
        return tags;
    }

    public boolean ambientEffects() {
        return ambientEffects;
    }

    public ItemStackBuilder setAmbientEffects(boolean ambientEffects) {
        this.ambientEffects = ambientEffects;
        return this;
    }

    public ItemStack build() {
        ItemStack result = new ItemStack(material, amount);
        ItemMeta meta = result.getItemMeta();

        if(!ListUtils.isNullOrEmpty(flags)) meta.addItemFlags(flags);
        if(name != null) meta.displayName(name);
        if(!ListUtils.isNullOrEmpty(lores)) meta.lore(lores);

        if(!MapUtils.isNullOrEmpty(enchants)){
            for (Enchantment e: enchants.keySet()){
                meta.addEnchant(e, enchants.get(e), ignoreEnchantRestrictions);
            }
        }

        if(!MapUtils.isNullOrEmpty(modifiers)){
            for (Map.Entry<Attribute, AttributeModifier> entry: modifiers.entrySet()){
                meta.addAttributeModifier(entry.getKey(), entry.getValue());
            }
        }

        if(!MapUtils.isNullOrEmpty(persistentData)){
            for (Map.Entry<NamespacedKey, Byte> entry: persistentData.entrySet()){
                meta.getPersistentDataContainer().set(entry.getKey(), PersistentDataType.BYTE, entry.getValue());
            }
        }

        if(!ListUtils.isNullOrEmpty(effects)){
            PotionMeta meta1 = (PotionMeta) meta;
            for (PotionEffect e: effects){
                meta1.addCustomEffect(e, ambientEffects);
            }
        }

        if(baseEffect != null){
            PotionMeta meta1 = (PotionMeta) meta;
            meta1.setBasePotionData(baseEffect);
        }

        meta.setUnbreakable(unbreakable);

        result.setItemMeta(meta);

        if(!tags.isEmpty()) {
            net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(result);
            CompoundTag tag = nms.getOrCreateTag();

            tag.merge(tags);
            nms.setTag(tag);

            return CraftItemStack.asBukkitCopy(nms);
        } else return result;
    }

    @Override
    protected ItemStackBuilder clone() {
        try {
            return (ItemStackBuilder) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
