package net.forthecrown.inventory;

import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.core.kingship.Kingship;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.ItemStackBuilder;
import net.forthecrown.utils.Worlds;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnegative;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;

/**
 * Class for server items, such as Royal Swords, Crowns and home of the great makeItem method
 */
public final class FtcItems {
    private FtcItems() {}

    /**
     * Key used by persistent data container to store stuff
     */
    public static final NamespacedKey ITEM_KEY = new NamespacedKey(Crown.inst(), "crownitem");

    private static final ItemStack BASE_ROYAL_SWORD = makeRoyalWeapon(Material.GOLDEN_SWORD, Component.text("-")
                    .color(NamedTextColor.GOLD)
                    .append(Component.text("Royal Sword")
                            .color(NamedTextColor.YELLOW)
                            .decorate(TextDecoration.BOLD)
                    )
                    .append(Component.text("-")),
            Component.text("The bearer of this weapon has proven themselves,").color(NamedTextColor.GOLD),
            Component.text("to the Crown...").color(NamedTextColor.GOLD)
    );

    private static final ItemStack BASE_CUTLASS = makeRoyalWeapon(Material.NETHERITE_SWORD,
            ChatUtils.convertString("&#917558-&#D1C8BA&lCaptain's Cutlass&#917558-"),
            ChatUtils.convertString("&#917558The bearer of this cutlass bows to no laws, to no king,"),
            ChatUtils.convertString("&#917558its wielder leads their crew towards everlasting riches.")
    );

    private static final ItemStack BASE_VIKING_AXE = makeRoyalWeapon(Material.IRON_AXE,
            ChatUtils.convertString("Viking axe"),
            ChatUtils.convertString("Vikings axe, do big damage"),
            ChatUtils.convertString(":DDDDD")
    );

    private static final ItemStack VOTE_TICKET;
    private static final ItemStack ELITE_VOTE_TICKET;

    public static final Style NON_ITALIC_DARK_GRAY = Style.style(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false);
    public static final Style NON_ITALIC_WHITE = Style.style(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false);

    static {
        final Component border = Component.text("-----------------------------").color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
        final Component line1 = Component.text("These can be used to go inside");
        final Component line3 = Component.text("Try to get as much stuff as you");

        VOTE_TICKET = makeItem(
                Material.PAPER, 1, true,
                Component.text("Bank Ticket")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.AQUA)
                ,

                border,
                line1,
                Component.text("the bank vault in Hazelguard."),
                line3,
                Component.text("can from chests. You get 30 sec!"),
                border
                );
        VOTE_TICKET.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 1);
        VOTE_TICKET.getItemMeta().getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.BYTE, (byte) 1);


        ELITE_VOTE_TICKET = makeItem(
                Material.PAPER, 1, true,
                Component.text("Elite Bank Ticket")
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .color(NamedTextColor.AQUA)
                ,

                border,
                line1,
                Component.text("the elite bank vault in Hazelguard."),
                line3,
                Component.text("can from chests. You get 50 sec!"),
                border
                );
        ELITE_VOTE_TICKET.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 2);
        ELITE_VOTE_TICKET.getItemMeta().getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.BYTE, (byte) 1);
    }

    public static ItemStack voteTicket(){
        return VOTE_TICKET.clone();
    }

    public static ItemStack eliteVoteTicket(){
        return ELITE_VOTE_TICKET.clone();
    }

    public static ItemStack royalSword(){
        return BASE_ROYAL_SWORD.clone();
    }

    public static ItemStack cutlass(){
        return BASE_CUTLASS.clone();
    }

    public static ItemStack vikingAxe(){
        return BASE_VIKING_AXE.clone();
    }

    //Makes a royal weapon, like Viking Axe, Royal Sword or Captain's Cutlass
    private static ItemStack makeRoyalWeapon(Material material, Component name, Component lore2, Component lore3){
        final Component border = Component.text("------------------------------").style(nonItalic(NamedTextColor.DARK_GRAY));
        return new ItemStackBuilder(material, 1)
                .setFlags(ItemFlag.HIDE_ATTRIBUTES)
                .setName(name.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .addLore(Component.text("Rank I").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .addLore(border)
                .addLore(lore2.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .addLore(lore3.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .addLore(border)
                .addLore(Component.text("0/1000").color(NamedTextColor.AQUA).append(Component.text(" Zombies to rank up!").color(NamedTextColor.DARK_AQUA)).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .addLore(Component.text("Donators can upgrade Royal tools beyond Rank 5.").color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .setUnbreakable(true)
                .addModifier(Attribute.GENERIC_ATTACK_DAMAGE, "generic.attackDamage", 7, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND)
                .addModifier(Attribute.GENERIC_ATTACK_SPEED, "generic.attackSpeed", -2.4, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND)
                .addEnchant(Enchantment.DAMAGE_ALL, 5)
                .addEnchant(Enchantment.LOOT_BONUS_MOBS, 3)
                .addEnchant(Enchantment.SWEEPING_EDGE, 3)
                .addData(ITEM_KEY, (byte) 1)
                .build();
    }

    public static boolean isCrownItem(@Nullable ItemStack item){
        if(item == null) return false;
        if(!item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(ITEM_KEY, PersistentDataType.BYTE);
    }

    /**
     * Make some coins
     * @param amount The amount the coin(s) will be worth
     * @param itemAmount The amount of seperate coins to make
     * @return The created coin(s)
     */
    public static ItemStack makeCoins(int amount, int itemAmount){
        return new ItemStackBuilder(Material.SUNFLOWER, itemAmount)
                .setName(
                        Component.text("Rhines").style(nonItalic(NamedTextColor.YELLOW))
                )
                .addLore(
                        Component.text("Worth ")
                                .append(FtcFormatter.rhinesNonTrans(amount))
                                .style(nonItalic(NamedTextColor.GOLD))
                )
                .addLore(
                        Component.text("Minted in the year " + FtcUtils.arabicToRoman(FtcUtils.worldTimeToYears(Worlds.OVERWORLD)) + ".")
                                .style(NON_ITALIC_DARK_GRAY)
                )
                .addLore(s())
                .build();
    }

    private static Component s(){
        Kingship kingship = Crown.getKingship();
        if(!kingship.hasKing()) return Component.text("During the Interregnum").color(NamedTextColor.DARK_GRAY);

        return Component.text("During the reign of ")
                .style(NON_ITALIC_DARK_GRAY)
                .append(Component.text(kingship.isFemale() ? "Queen " : "King "))
                .append(kingship.name());
    }

    /**
     * The Crown's title, the -Crown-
     */
    public static final Component CROWN_TITLE = Component.text()
            .style(nonItalic(NamedTextColor.GOLD))
            .append(Component.text("-"))
            .append(Component.text("Crown").style(nonItalic(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD)))
            .append(Component.text("-"))
            .build();

    /**
     * Creates a crown
     * @param level The crown's level
     * @param owner The name of the owner
     * @return The created crown
     */
    public static ItemStack makeCrown(int level, String owner){
        String levelS = FtcUtils.arabicToRoman(level);

        ItemStackBuilder builder = new ItemStackBuilder(Material.GOLDEN_HELMET, 1)
                .setName(CROWN_TITLE)

                .addLore(Component.text("Rank " + levelS).style(nonItalic(NamedTextColor.GRAY)))
                .addLore(Component.text("--------------------------------").style(nonItalic(NamedTextColor.DARK_GRAY)))
                .addLore(Component.text("Only the worthy shall wear this").style(nonItalic(NamedTextColor.GOLD)))
                .addLore(Component.text("symbol of strength and power.").style(nonItalic(NamedTextColor.GOLD)))
                .addLore(Component.text("Crafted for " + owner).style(nonItalic(NamedTextColor.DARK_GRAY)))

                .addData(ITEM_KEY, (byte) 1)
                .setUnbreakable(true);

        int eLevel = 5;

        if(level > 1){
            builder.addModifier(Attribute.GENERIC_MOVEMENT_SPEED, "generic.movement_speed", 0.25,
                    AttributeModifier.Operation.ADD_SCALAR, EquipmentSlot.HEAD
            );
        }

        if(level > 2){
            builder.addModifier(Attribute.GENERIC_MAX_HEALTH, "generic.max_health", 20,
                    AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD
            );
        }

        if(level > 3) eLevel = 6;
        builder.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, eLevel);

        return builder.build();
    }

    /**
     * Makes an item using the given values
     * @param material The material of the item
     * @param amount The item's amount
     * @param hideFlags Whether to hide flags
     * @param name The items' name
     * @param loreStrings Any lore to add to the item
     * @deprecated Method is too limited, use {@link ItemStackBuilder}
     * @return The created item
     */
    @Deprecated
    public static ItemStack makeItem(@NotNull Material material, @Nonnegative int amount, boolean hideFlags, @Nullable Component name, @Nullable Component... loreStrings) {
        Validate.notNull(material, "Material cannot be null");

        ItemStack result = new ItemStack(material, amount);
        ItemMeta meta = result.getItemMeta();

        if(name != null) meta.displayName(name);
        if (loreStrings != null) meta.lore(Arrays.asList(loreStrings));
        if (hideFlags) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        }

        result.setItemMeta(meta);
        return result;
    }

    /**
     * Makes an item using the given values
     * @param material The material of the item
     * @param amount The item's amount
     * @param hideFlags Whether to hide flags
     * @param name The items' name
     * @param lores Any lore to add to the item
     * @deprecated Method is too limited and out of date, use {@link ItemStackBuilder}
     * @return The created item
     */
    @Deprecated
    public static ItemStack makeItem(@NotNull Material material, @Nonnegative int amount, boolean hideFlags, String name, String... lores){
        Validate.notNull(material, "Material cannot be null");

        Component name_c = null;
        if(name != null){
            name_c = Component.text()
                    .append(ChatUtils.convertString(name))
                    .style(NON_ITALIC_WHITE)
                    .build();
        }

        Component[] lore = null;
        if(lores != null){
            lore = new Component[lores.length];
            for (int i = 0; i < lores.length; i++){
                if(FtcUtils.isNullOrBlank(lores[i])) lore[i] = Component.empty();
                else lore[i] = Component.text()
                        .append(ChatUtils.convertString(lores[i]))
                        .style(NON_ITALIC_WHITE)
                        .build();
            }
        }

        return makeItem(material, amount, hideFlags, name_c, lore);
    }

    public static void setCustomTags(ItemMeta meta, CompoundTag tag) {
        try {
            Field f = getTagField();
            f.setAccessible(true);

            Map<String, Tag> metaTags = (Map<String, Tag>) f.get(meta);
            metaTags.putAll(tag.tags);

            f.set(meta, metaTags);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Couldn't set internalTag in ItemMeta", e);
        }
    }

    public static @NotNull CompoundTag getCustomTags(ItemMeta meta) {
        try {
            Field f = getTagField();
            f.setAccessible(true);

            Map<String, Tag> metaTags = (Map<String, Tag>) f.get(meta);

            CompoundTag result = new CompoundTag();
            result.tags.putAll(metaTags);

            return result;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Couldn't get internalTag in ItemMeta", e);
        }
    }

    public static CompoundTag getTagElement(ItemMeta meta, String key) {
        return getCustomTags(meta).getCompound(key);
    }

    public static void setTagElement(ItemMeta meta, String key, CompoundTag tag) {
        CompoundTag internalTag = getCustomTags(meta);
        internalTag.put(key, tag);

        setCustomTags(meta, internalTag);
    }

    public static boolean hasTagElement(ItemMeta meta, String key) {
        return getCustomTags(meta).contains(key);
    }

    private static Field getTagField() {
        Class meta = metaClass();
        try {
            return meta.getDeclaredField("unhandledTags");
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("couldn't find internalTag field", e);
        }
    }

    private static Class metaClass() {
        try {
            return Class.forName("org.bukkit.craftbukkit.v1_17_R1.inventory.CraftMetaItem");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Couldn't find class for item meta??????", e);
        }
    }
}
