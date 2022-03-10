package net.forthecrown.inventory;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Kingship;
import net.forthecrown.core.Worlds;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.inventory.crown.Crowns;
import net.forthecrown.inventory.weapon.RoyalWeapons;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Map;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;

/**
 * Class for server items, such as Royal Swords, Crowns and home of the great makeItem method
 */
public final class ItemStacks {
    private ItemStacks() {}

    /**
     * Key used by persistent data container to store stuff
     */
    public static final NamespacedKey GENERIC_ITEM_KEY = new NamespacedKey(Crown.inst(), "crownitem");

    private static final ItemStack VOTE_TICKET;
    private static final ItemStack ELITE_VOTE_TICKET;

    public static final Style NON_ITALIC_DARK_GRAY = Style.style(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false);

    static {
        final Component border = Component.text("-----------------------------").color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
        final Component line1 = Component.text("These can be used to go inside");
        final Component line3 = Component.text("Try to get as much stuff as you");

        VOTE_TICKET = new ItemStackBuilder(Material.PAPER, 1)
                .setName(
                        Component.text("Bank Ticket")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.AQUA)
                )

                .addLore(border)
                .addLore(line1)
                .addLore(Component.text("the bank vault in Hazelguard."))
                .addLore(line3)
                .addLore(Component.text("can from chests. You get 30 sec!"))
                .addLore(border)

                .addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 1)
                .build();


        ELITE_VOTE_TICKET = new ItemStackBuilder(Material.PAPER, 1)
                .setName(
                        Component.text("Elite Bank Ticket")
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                .color(NamedTextColor.AQUA)
                )

                .addLore(border)
                .addLore(line1)
                .addLore(Component.text("the elite bank vault in Hazelguard."))
                .addLore(line3)
                .addLore(Component.text("can from chests. You get 50 sec!"))
                .addLore(border)

                .addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 2)
                .build();
    }

    public static ItemStack voteTicket(){
        return VOTE_TICKET.clone();
    }

    public static ItemStack eliteVoteTicket(){
        return ELITE_VOTE_TICKET.clone();
    }

    /**
     * Make some coins
     * @param amount The amount the coin(s) will be worth
     * @param itemAmount The amount of seperate coins to make
     * @return The created coin(s)
     */
    public static ItemStack makeCoins(int amount, int itemAmount){
        ItemStackBuilder builder = new ItemStackBuilder(Material.SUNFLOWER, itemAmount)
                .setName(coinName());

        for (Component c: coinLore(amount)) {
            builder.addLore(c);
        }

        return builder.build();
    }

    public static Component coinName() {
        return Component.text("Rhines").style(nonItalic(NamedTextColor.YELLOW));
    }

    public static Component[] coinLore(int worth) {
        return new Component[] {
                Component.text("Worth ")
                        .append(FtcFormatter.rhinesNonTrans(worth))
                        .style(nonItalic(NamedTextColor.GOLD)),

                Component.text("Minted in the year " + FtcUtils.arabicToRoman(FtcUtils.worldTimeToYears(Worlds.OVERWORLD)) + ".")
                        .style(NON_ITALIC_DARK_GRAY),

                Kingship.coinDisplay()
        };
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

    public static void setTags(ItemMeta meta, CompoundTag tag) {
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

    public static @NotNull CompoundTag getTags(ItemMeta meta) {
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
        return getTags(meta).getCompound(key);
    }

    public static void setTagElement(ItemMeta meta, String key, CompoundTag tag) {
        CompoundTag internalTag = getTags(meta);
        internalTag.put(key, tag);

        setTags(meta, internalTag);
    }

    public static boolean hasTagElement(ItemMeta meta, String key) {
        return getTags(meta).contains(key);
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
            // This will have to be updated with each MC update,
            // because the package name will change
            return Class.forName("org.bukkit.craftbukkit.v1_18_R2.inventory.CraftMetaItem");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Couldn't find class for item meta??????", e);
        }
    }

    /**
     * Checks if the item is 'empty'
     * <p></p>
     * An empty item is an item that's either null,
     * has a type == air or an amount that's less
     * than 1.
     *
     * @param itemStack The item to check
     * @return Whether the item is 'empty'
     */
    public static boolean isEmpty(ItemStack itemStack) {
        return itemStack == null || itemStack.getType().isAir() || itemStack.getAmount() < 1;
    }

    /**
     * A special item is something like a sword or crown,
     * that doesn't leave your inventory when you die.
     *
     * @param item The item to check
     * @return Whether it's a sword or crown
     */
    public static boolean isSpecial(ItemStack item) {
        if(isEmpty(item)) return false;
        return RoyalWeapons.isRoyalSword(item) || Crowns.isCrown(item);
    }
}
