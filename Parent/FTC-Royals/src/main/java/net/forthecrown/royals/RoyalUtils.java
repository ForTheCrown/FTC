package net.forthecrown.royals;

import net.forthecrown.core.CrownBoundingBox;
import net.forthecrown.core.utils.ComponentUtils;
import net.forthecrown.core.utils.CrownItems;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.royals.dungeons.bosses.mobs.DungeonBoss;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

public class RoyalUtils {

    public static final NamespacedKey PUNCHING_BAG_KEY = new NamespacedKey(Royals.inst, "dummy");
    public static final Component DUNGEON_LORE = Component.text("Dungeon Item");

    public static ItemStack makeDungeonItem(Material material, int amount, @Nullable String name){
        return CrownItems.makeItem(material, amount, false,
                CrownUtils.isNullOrBlank(name) ? null : ComponentUtils.convertString(name).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE),
                DUNGEON_LORE
        );
    }

    public static TextComponent itemRequiredMessage(DungeonBoss<?> boss){
        TextComponent text = Component.text()
                .color(NamedTextColor.AQUA)
                .append(Component.text("To spawn the boss in this level you need:"))
                .build();
        for (ItemStack i: boss.getSpawningItems()){
            text = text.append(Component.newline());
            text = text.append(
                    Component.text()
                            .hoverEvent(i.asHoverEvent())
                            .append(Component.text("- " + i.getAmount() + " "))
                            .append(i.getItemMeta().hasDisplayName() ? i.getItemMeta().displayName().color(NamedTextColor.DARK_AQUA) : Component.text(CrownUtils.normalEnum(i.getType())).color(NamedTextColor.DARK_AQUA))
            );
        }
        return text;
    }

    public static @Nullable Player getNearestVisiblePlayer(Location location, CrownBoundingBox inBox){
        double lastDistance = Double.MAX_VALUE;
        Player result = null;
        for (Player p: inBox.getPlayers()){
            if(p.equals(result)) continue;
            if(p.getGameMode() == GameMode.SPECTATOR || p.getGameMode() == GameMode.CREATIVE) continue;

            double distance = p.getLocation().distance(location);
            if(distance < lastDistance){
                lastDistance = distance;
                result = p;
            }
        }
        return result;
    }

    public static @Nullable Player getOptimalTarget(LivingEntity e, CrownBoundingBox inBox){
        Player result = getNearestVisiblePlayer(e.getEyeLocation(), inBox);

        if(e.getLastDamageCause() == null) return result;
        if(!(e.getLastDamageCause() instanceof EntityDamageByEntityEvent)) return result;
        EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e.getLastDamageCause();

        if(event.getDamager() instanceof Player) {
            if (inBox.contains(event.getDamager())) result = (Player) event.getDamager();
        }
        return result;
    }

    public static void spawnDummy(Location location){
        location.getWorld().spawn(location, Zombie.class, zomzom -> {
            zomzom.getEquipment().setHelmet(new ItemStack(Material.HAY_BLOCK));
            zomzom.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
            zomzom.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
            zomzom.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));

            zomzom.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(200);
            zomzom.setAI(false);
            zomzom.setGravity(false);
            zomzom.setCanPickupItems(false);
            zomzom.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
            zomzom.getPersistentDataContainer().set(PUNCHING_BAG_KEY, PersistentDataType.BYTE, (byte) 1);
        });
    }
}