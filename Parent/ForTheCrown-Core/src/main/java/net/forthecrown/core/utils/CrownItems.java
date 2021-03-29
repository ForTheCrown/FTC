package net.forthecrown.core.utils;

import net.forthecrown.core.CrownWeapons;
import net.forthecrown.core.FtcCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
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
import java.util.Arrays;
import java.util.UUID;

public final class CrownItems {
    private CrownItems() {}

    public static final NamespacedKey ITEM_KEY = new NamespacedKey(FtcCore.getInstance(), "crownitem");
    public static final Style NON_ITALIC_WHITE = Style.style(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);

    public static final ItemStack BASE_ROYAL_SWORD = makeRoyalWeapon(Material.GOLDEN_SWORD, Component.text("-")
                    .color(NamedTextColor.GOLD)
                    .append(Component.text("Royal Sword")
                            .color(NamedTextColor.YELLOW)
                            .decorate(TextDecoration.BOLD)
                    )
                    .append(Component.text("-")),
            Component.text("The bearer of this weapon has proven themselves,").color(NamedTextColor.GOLD),
            Component.text("not only to the Crown, but also to the Gods...").color(NamedTextColor.GOLD));

    public static final ItemStack BASE_CUTLASS = makeRoyalWeapon(Material.NETHERITE_SWORD,
            ComponentUtils.convertString("&#917558-&#D1C8BA&lCaptain's Cutlass&#917558-"),
            ComponentUtils.convertString("&#917558The bearer of this cutlass bows to no laws, to no king,"),
            ComponentUtils.convertString("&#917558its wielder leads their crew towards everlasting riches.")
    );

    public static final ItemStack BASE_VIKING_AXE = makeRoyalWeapon(Material.IRON_AXE,
            ComponentUtils.convertString("Viking axe"),
            ComponentUtils.convertString("Vikings axe, do big damage"),
            ComponentUtils.convertString(":DDDDD")
    );

    public static final ItemStack VOTE_TICKET;
    public static final ItemStack ELITE_VOTE_TICKET;

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

    private static ItemStack makeRoyalWeapon(Material material, Component name, Component lore2, Component lore3){
        final Component border = Component.text("------------------------------").color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
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
        if(item.getItemMeta().getPersistentDataContainer().has(ITEM_KEY, PersistentDataType.BYTE)) return true;
        if(CrownWeapons.isCrownWeapon(item) || CrownWeapons.isLegacyWeapon(item)) return true;
        return isLegacyItem(item);
    }

    private static boolean isLegacyItem(ItemStack itemStack){
        ItemMeta meta = itemStack.getItemMeta();
        boolean result = meta.lore().size() > 1 && ChatColor.stripColor(meta.getDisplayName()).contains("-Crown-");
        if(result){
            meta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.BYTE, (byte) 1);
            itemStack.setItemMeta(meta);
        }
        return result;
    }

    public static ItemStack getCoins(int amount){
        return makeItem(Material.SUNFLOWER, 1, true, "&eRhines",
                "&6Worth " + amount + " Rhines", "&8Do /deposit to add this to your balance",
                "&8Minted in the year " + CrownUtils.arabicToRoman(CrownUtils.worldTimeToYears(Bukkit.getWorld("world"))) + ".",
                s());
    }

    private static String s(){
        try {
            return "&8During the reign of " + Bukkit.getOfflinePlayer(FtcCore.getKing()).getName() + ".";
        } catch (Exception e){
            return "&8During the Interregnum";
        }
    }

    public static ItemStack getCrown(int level, String owner ){
        String levelS = CrownUtils.arabicToRoman(level);
        ItemStack crown = makeItem(Material.GOLDEN_HELMET, 1, false, "&6-&e&lCrown&6-",
                "&7Rank " + levelS,
                "&8--------------------------------",
                "&6Only the worthy shall wear this ",
                "&6symbol of strength and power.",
                "&8Crafted for " + owner);

        ItemMeta meta = crown.getItemMeta();
        meta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.BYTE, (byte) 1);
        AttributeModifier attributeModifier;

        meta.setUnbreakable(true);

        int eLevel = 5;

        if(level > 1){
            attributeModifier = new AttributeModifier(UUID.randomUUID(), "generic.movement_speed", 0.25,
                    AttributeModifier.Operation.ADD_SCALAR, EquipmentSlot.HEAD);

            meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, attributeModifier);
        }
        if(level > 2){
            attributeModifier = new AttributeModifier(UUID.randomUUID(), "generic.max_health", 20,
                    AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD);

            meta.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, attributeModifier);
        }
        if(level > 3) eLevel = 6;

        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, eLevel, true);
        crown.setItemMeta(meta);

        return crown;
    }

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

    public static ItemStack makeItem(@NotNull Material material, @Nonnegative int amount, boolean hideFlags, String name, String... lores){
        Validate.notNull(material, "Material cannot be null");

        Component[] lore = null;
        if(lores != null){
            lore = new Component[lores.length];
            for (int i = 0; i < lores.length; i++){
                if(CrownUtils.isNullOrBlank(lores[i])) lore[i] = Component.text("");
                else lore[i] = ComponentUtils.convertString(lores[i]);
            }
        }

        return makeItem(material, amount, hideFlags, name == null ? null : ComponentUtils.convertString(name), lore);
    }
}
