package net.forthecrown.core.inventory;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.kingship.Kingship;
import net.forthecrown.economy.Balances;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.utils.CrownUtils;
import net.forthecrown.utils.ItemStackBuilder;
import net.forthecrown.utils.Worlds;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
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
import java.util.Arrays;
import java.util.UUID;

/**
 * Class for server items, such as Royal Swords, Crowns and home of the great makeItem method
 */
public final class CrownItems {
    private CrownItems() {}

    public static final NamespacedKey ITEM_KEY = new NamespacedKey(CrownCore.inst(), "crownitem");

    private static final ItemStack BASE_ROYAL_SWORD = makeRoyalWeapon(Material.GOLDEN_SWORD, Component.text("-")
                    .color(NamedTextColor.GOLD)
                    .append(Component.text("Royal Sword")
                            .color(NamedTextColor.YELLOW)
                            .decorate(TextDecoration.BOLD)
                    )
                    .append(Component.text("-")),
            Component.text("The bearer of this weapon has proven themselves,").color(NamedTextColor.GOLD),
            Component.text("to the Crown...").color(NamedTextColor.GOLD));

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
        if(meta == null) return false;
        if(meta.lore() == null) return false;
        boolean result =  meta.lore().size() > 1 && ChatColor.stripColor(meta.getDisplayName()).contains("-Crown-");
        if(result){
            meta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.BYTE, (byte) 1);
            itemStack.setItemMeta(meta);
        }
        return result;
    }

    public static final Style NON_ITALIC_DARK_GRAY = Style.style(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false);
    public static final Style NON_ITALIC_WHITE = Style.style(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false);
    public static ItemStack getCoins(int amount, int itemAmount){
        return new ItemStackBuilder(Material.SUNFLOWER, itemAmount)
                .setName(Component.text("Rhines").color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                .addLore(Component.text("Worth ").append(Component.text(Balances.getFormatted(amount))).color(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false))
                .addLore(Component.text("Minted in the year " + CrownUtils.arabicToRoman(CrownUtils.worldTimeToYears(Worlds.NORMAL)) + ".").style(NON_ITALIC_DARK_GRAY))
                .addLore(s())
                .build();
    }

    private static Component s(){
        Kingship kingship = CrownCore.getKingship();
        if(!kingship.hasKing()) return Component.text("During the Interregnum").color(NamedTextColor.DARK_GRAY);

        return Component.text("During the reign of ")
                .style(NON_ITALIC_DARK_GRAY)
                .append(Component.text(kingship.isFemale() ? "Queen " : "King "))
                .append(kingship.name());
    }

    public static ItemStack makeCrown(int level, String owner){
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

    //String to component conversion ¬_¬
    public static ItemStack makeItem(@NotNull Material material, @Nonnegative int amount, boolean hideFlags, String name, String... lores){
        Validate.notNull(material, "Material cannot be null");

        Component name_c = null;
        if(name != null){
            name_c = Component.text()
                    .append(ChatUtils.convertString(name))
                    .color(NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                    .build();
        }

        Component[] lore = null;
        if(lores != null){
            lore = new Component[lores.length];
            for (int i = 0; i < lores.length; i++){
                if(CrownUtils.isNullOrBlank(lores[i])) lore[i] = Component.text("");
                else lore[i] = Component.text()
                        .append(ChatUtils.convertString(lores[i]))
                        .color(NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .build();
            }
        }

        return makeItem(material, amount, hideFlags, name_c, lore);
    }
}
