package net.forthecrown.mayevent;

import com.destroystokyo.paper.ParticleBuilder;
import net.forthecrown.core.CrownBoundingBox;
import net.forthecrown.core.nbt.NBT;
import net.forthecrown.core.nbt.NbtGetter;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

public class MayUtils {
    public static void drawLine(Location point1, Location point2, double space, ParticleBuilder builder) {
        World world = point1.getWorld();
        Validate.isTrue(point2.getWorld().equals(world), "Lines cannot be in different worlds!");
        Vector p1 = point1.toVector();
        Vector p2 = point2.toVector();
        Vector vector = p2.clone().subtract(p1).normalize().multiply(space);
        double length = 0;
        for (; length < 20; p1.add(vector)) {
            builder.location(world, p1.getX(), p1.getY(), p1.getZ())
                    .spawn();
            length += space;
        }
    }

    public static final ItemStack noDropper = new ItemStack(Material.AIR, 1);
    public static void attemptDestruction(Location location, int radius){
        CrownBoundingBox box = CrownBoundingBox.of(location, radius);
        for (Block b: box){
            if(isNonDestructable(b.getType())) continue;
            breakBlock(b);
        }
    }

    public static void damageInRadius(Location at, int radius, double damage){
        CrownBoundingBox box = CrownBoundingBox.of(at, radius);
        box.getEntitiesByType(LivingEntity.class).forEach(l -> l.damage(damage));
    }

    public static void breakBlock(Block block){
        block.breakNaturally(noDropper, true);
    }

    public static ItemStack addEventTag(ItemStack item){
        NBT nbt = NbtGetter.ofItemTags(item);
        nbt.put("eventItem", (byte) 1);
        return NbtGetter.applyTags(item, nbt);
    }

    public static boolean isNonDestructable(Material mat){
        return mat != Material.CRACKED_STONE_BRICKS && mat != Material.ANDESITE_SLAB && mat != Material.ANDESITE_STAIRS
                && mat != Material.COBBLESTONE_WALL && mat != Material.ANDESITE && mat != Material.DIORITE
                && mat != Material.DIORITE_SLAB && mat != Material.DIORITE_STAIRS;
    }

    public static <T extends Entity> T spawnAndEffect(Location l, Class<T> clazz, Consumer<T> consumer){
        l.getWorld().playSound(l, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        new ParticleBuilder(Particle.REDSTONE).color(Color.YELLOW).extra(25).count(25).location(l).spawn();

        return spawn(l, clazz, consumer);
    }

    public static <T extends Entity> T spawn(Location location, Class<T> clazz, Consumer<T> consumer){
        validateIsAir(location);

        return location.getWorld().spawn(location, clazz, consumer);
    }

    public static void dropItem(Location location, ItemStack itemStack, boolean willAge){
        validateIsAir(location);

        doItemEffects(location.getWorld().dropItem(location, itemStack, item -> {
            item.setCanMobPickup(false);
            item.setCanPlayerPickup(true);
            item.setPersistent(true);

            item.setItemStack(itemStack);
            item.setWillAge(willAge);
        }));
    }

    private static void doItemEffects(Item item){
        item.setGlowing(true);
        item.getWorld().playSound(item.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

        new BukkitRunnable() {
            @Override
            public void run() {
                if(item.isDead()) return;
                item.setGlowing(false);
            }
        }.runTaskLater(MayMain.inst, 20);
    }

    public static Location validateIsAir(Location location){
        while (location.getBlock().getType() != Material.AIR){
            location.add(0, 1, 0);
        }
        return location;
    }
}