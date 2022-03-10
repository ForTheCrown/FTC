package net.forthecrown.dungeons.boss.evoker;

import com.destroystokyo.paper.ParticleBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Crown;
import net.forthecrown.dungeons.BossItems;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.inventory.ItemStacks;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.VanillaAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftSlime;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.entity.Evoker;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class EvokerRewards {
    private static final Logger LOGGER = Crown.logger();

    public static final double
        MAX_VEL = 1.5D;

    public static void create(EvokerBoss boss, BossContext context) {
        PackageController controller = new PackageController(boss, context);

        for (Player p: context.players()) {
            controller.add(Package.of(BossItems.EVOKER));
            controller.add(Package.of(EvokerVars.RHINE_REWARD.get()));
        }

        controller.start();
    }

    static class PackageController {
        private final List<PackageData> packages = new ObjectArrayList<>();
        private final BossContext context;
        private final Evoker entity;
        private final Vec3 startPos;
        private final ServerLevel level;

        private BukkitTask task;

        public PackageController(EvokerBoss boss, BossContext context) {
            this.context = context;
            this.entity = boss.getBossEntity();

            Location l = entity.getLocation();
            startPos = new Vec3(l.getX(), l.getY() + entity.getHeight() * 0.75, l.getZ());

            level = VanillaAccess.getLevel(boss.getWorld());
        }

        void start() {
            task = Bukkit.getScheduler().runTaskTimer(Crown.inst(), this::tick, 1, 1);
            LOGGER.info("Started reward controller");
        }

        void add(Package reward) {
            Slime s = new Slime(EntityType.SLIME, level);
            s.setInvulnerable(true);
            s.setPos(startPos);

            s.aware = false;

            CraftSlime bukkit = (CraftSlime) s.getBukkitEntity();
            bukkit.setInvisible(true);

            double xVel = FtcUtils.RANDOM.nextDouble(0.5, 2D);
            double yVel = FtcUtils.RANDOM.nextDouble(0.5, 2D);

            Vector velocity = new Vector(
                    FtcUtils.RANDOM.nextBoolean() ? xVel : -xVel,
                    FtcUtils.RANDOM.nextDouble(),
                    FtcUtils.RANDOM.nextBoolean() ? yVel : -yVel
            );

            bukkit.setVelocity(velocity);
            level.addFreshEntity(s);

            FtcUtils.getNoClipTeam().addEntity(bukkit);

            LOGGER.info("Created reward slime and added it to world and created data");
            PackageData data = new PackageData(s, reward);
            packages.add(data);
        }

        void tick() {
            if(packages.isEmpty()) {
                LOGGER.info("Packages empty, stopping");
                task.cancel();
                task = null;
            }

            Iterator<PackageData> iterator = packages.iterator();

            while (iterator.hasNext()) {
                PackageData d = iterator.next();
                Location l = d.slime.getBukkitEntity().getLocation();

                if(d.slime.isOnGround()) {
                    Collection<ItemStack> items = d.reward.getItems(l, context, entity);
                    LOGGER.info("Dropping package at: ({}, {}, {})", l.getX(), l.getY(), l.getZ());

                    for (ItemStack i: items) {
                        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(i);

                        ItemEntity entity = new ItemEntity(level, l.getX(), l.getY(), l.getZ(), nmsItem);
                        level.addFreshEntity(entity);
                    }

                    d.slime.remove(Entity.RemovalReason.DISCARDED);
                    iterator.remove();
                    continue;
                }

                new ParticleBuilder(Particle.CAMPFIRE_SIGNAL_SMOKE)
                        .location(l)
                        .count(3)
                        .extra(0.0D)
                        .allPlayers()
                        .spawn();
            }
        }
    }

    record PackageData(Slime slime, Package reward) {}

    interface Package {
        Collection<ItemStack> getItems(Location l, BossContext context, Evoker entity);

        static Package of(BossItems items) {
            return (l, context, evoker) -> Collections.singleton(items.item());
        }

        static Package of(LootTable lootTable) {
            return (l, context, evoker) -> {
                LootContext context1 = new LootContext.Builder(l)
                        .luck(context.modifier())
                        .lootedEntity(evoker)
                        .build();

                return lootTable.populateLoot(FtcUtils.RANDOM, context1);
            };
        }

        static Package of(int rhinesInitial) {
            return (l, context, evoker) -> {
                int rhines = (int) Math.ceil(context.modifier() * rhinesInitial);

                boolean divisible = rhines % 4 == 0 && rhines >= 4;
                int rhineAmount = divisible ? rhines / 4 : rhines;
                int itemAmount =  divisible ? 4 : 1;

                return Collections.singleton(
                        ItemStacks.makeCoins(rhineAmount, itemAmount)
                );
            };
        }
    }
}
