package net.forthecrown.inventory.weapon;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.inventory.FtcItems;
import net.forthecrown.inventory.RoyalItem;
import net.forthecrown.registry.Registries;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;

public final class RoyalWeapons {
    private RoyalWeapons() {}

    private static final Int2ObjectMap<WeaponUpgrade> UPGRADES = new Int2ObjectOpenHashMap<>();

    public static void init() {
        //Upgrade creation
        UPGRADES.put(5, WeaponUpgrade.enchantment(Enchantment.LOOT_BONUS_MOBS, 4));
        UPGRADES.put(10, WeaponUpgrade.enchantment(Enchantment.LOOT_BONUS_MOBS, 5));

        //Goal registration
        register(WeaponGoal.simple(EntityType.ZOMBIE, 1000, 1));
        register(WeaponGoal.simple(EntityType.SKELETON, 1000, 2));
        register(WeaponGoal.simple(EntityType.SNOWMAN, 100, 3));
        register(WeaponGoal.simple(EntityType.CREEPER, 1000, 4));
        register(WeaponGoal.simple(EntityType.BLAZE, 1000, 5));
        register(WeaponGoal.simple(EntityType.ENDERMAN, 1000, 6));
        register(WeaponGoal.simple(EntityType.GHAST, 200, 7));
        register(new WeaponGoal.ChargedCreeperGoal(25, 8));
        register(WeaponGoal.simple(EntityType.WITHER, 10, 9));

        Registries.WEAPON_GOALS.close();
    }

    private static void register(WeaponGoal goal) {
        Registries.WEAPON_GOALS.register(goal.key(), goal);
    }

    /**
     * Gets all the goals the given rank needs to beat to rank up
     * @param rank The rank
     * @return All goals that rank has to beat
     */
    public static List<WeaponGoal> getGoalsAtRank(int rank) {
        List<WeaponGoal> result = new ObjectArrayList<>();

        for (WeaponGoal g: Registries.WEAPON_GOALS) {
            if(g.getGoalRank() == rank) result.add(g);
        }

        return result;
    }

    /**
     * Gets the upgrade the given level will recieve
     * @param rank The rank
     * @return The upgrade recieved when getting to that level, null, if no reward for the given level.
     */
    public static WeaponUpgrade getUpgrade(int rank) {
        return UPGRADES.get(rank);
    }

    /**
     * Makes a royal sword
     * @param owner The owner of the sword
     * @return The Sword itself
     */
    public static ItemStack make(UUID owner) {
        ItemStackBuilder builder = new ItemStackBuilder(Material.GOLDEN_SWORD, 1)
                .setName(
                        Component.text("-")
                                .style(nonItalic(NamedTextColor.GOLD))
                                .append(Component.text("Royal Sword")
                                        .style(nonItalic(NamedTextColor.YELLOW))
                                        .decorate(TextDecoration.BOLD)
                                )
                                .append(Component.text("-"))
                )
                .setFlags(ItemFlag.HIDE_ATTRIBUTES)

                .setUnbreakable(true)

                .addModifier(Attribute.GENERIC_ATTACK_DAMAGE, "generic.attackDamage", 7, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND)
                .addModifier(Attribute.GENERIC_ATTACK_SPEED, "generic.attackSpeed", -2.4, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND)

                .addEnchant(Enchantment.DAMAGE_ALL, 5)
                .addEnchant(Enchantment.LOOT_BONUS_MOBS, 3)
                .addEnchant(Enchantment.SWEEPING_EDGE, 3);

        ItemStack result = builder.build();
        RoyalSword sword = new RoyalSword(owner, result);
        sword.update();

        return result;
    }

    /**
     * Checks if the given item is a royal sword
     * @param item The item to check
     * @return True if the item is not null or empty and has the {@link RoyalItem#NBT_KEY} tag
     */
    public static boolean isRoyalSword(ItemStack item) {
        if(FtcUtils.isItemEmpty(item)) return false;
        return FtcItems.hasTagElement(item.getItemMeta(), RoyalItem.NBT_KEY);
    }
}
