package net.forthecrown.dungeons;

import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.dungeons.bosses.DungeonBoss;
import net.forthecrown.squire.Squire;
import net.forthecrown.utils.ItemStackBuilder;
import net.forthecrown.utils.transformation.FtcBoundingBox;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Husk;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public final class DungeonUtils {
    private DungeonUtils() {}

    public static final NamespacedKey PUNCHING_BAG_KEY = Squire.createRoyalKey("dummy");
    public static final Component DUNGEON_LORE = Component.text("Dungeon Item");

    public static ItemStack makeDungeonItem(Material material, int amount, @Nullable String name) {
        return makeDungeonItem(material, amount, name == null ? null : Component.text(name).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
    }

    public static ItemStack makeDungeonItem(Material material, int amount, @Nullable Component name){
        return new ItemStackBuilder(material, amount)
                .setName(name)
                .addLore(DUNGEON_LORE)
                .build();
    }

    public static TextComponent itemRequiredMessage(DungeonBoss<?> boss){
        TextComponent text = Component.text()
                .color(NamedTextColor.AQUA)
                .append(Component.translatable("dungeons.neededItems"))
                .build();
        for (ItemStack i: boss.getSpawningItems()){
            text = text.append(Component.newline());
            text = text.append(
                    Component.text()
                            .hoverEvent(i.asHoverEvent())
                            .append(Component.text("- " + i.getAmount() + " "))
                            .append(FtcFormatter.itemDisplayName(i).color(NamedTextColor.DARK_AQUA))
            );
        }
        return text;
    }

    public static @Nullable Player getNearestVisiblePlayer(Location location, FtcBoundingBox inBox){
        double lastDistance = Double.MAX_VALUE;
        Player result = null;
        for (Player p: inBox.getPlayers()){
            if(p.equals(result)) continue;
            if(p.getGameMode() == GameMode.SPECTATOR || p.getGameMode() == GameMode.CREATIVE) continue;

            Vector pos = location.toVector();
            Vector target = p.getLocation().toVector();
            Vector trace = target.subtract(pos);
            RayTraceResult resultRay = location.getWorld().rayTraceBlocks(location.clone(), trace, 25, FluidCollisionMode.NEVER, false);
            if(resultRay == null) continue;

            double distance = p.getLocation().distance(location);
            if(distance < lastDistance){
                lastDistance = distance;
                result = p;
            }
        }
        return result;
    }

    public static @Nullable Player getOptimalTarget(LivingEntity e, FtcBoundingBox inBox){
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
        location.getWorld().spawn(location, Husk.class, zomzom -> {
            zomzom.getEquipment().setHelmet(new ItemStack(Material.HAY_BLOCK));
            zomzom.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
            zomzom.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
            zomzom.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));

            zomzom.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(200);
            zomzom.setAI(false);
            zomzom.stopSound(SoundStop.all());
            zomzom.setGravity(false);

            zomzom.setCustomNameVisible(true);
            zomzom.customName(Component.text("Hit Me!").color(NamedTextColor.GOLD));

            zomzom.setRemoveWhenFarAway(false);
            zomzom.setPersistent(true);
            zomzom.setCanPickupItems(false);
            zomzom.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
            zomzom.getPersistentDataContainer().set(PUNCHING_BAG_KEY, PersistentDataType.BYTE, (byte) 1);
        });
    }
}
