package net.forthecrown.regions.visit.handlers;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Crown;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.regions.visit.RegionVisit;
import net.forthecrown.regions.visit.VisitHandler;
import net.forthecrown.utils.VanillaAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.*;

public class PassengerHandler implements VisitHandler {
    PassengerInfo info = new PassengerInfo();

    @Override
    public void onStart(RegionVisit visit) {
        Player player = visit.getUser().getPlayer();
        visit.setHulkSmashSafe(player.getPassengers().isEmpty());

        info.fillFrom(visit.getUser().getPlayer(), visit);
    }

    @Override
    public void onTeleport(RegionVisit visit) {
        info.movePassengers();
    }

    static class PassengerInfo extends Object2ObjectLinkedOpenHashMap<Entity, List<Entity>> {
        public void movePassengers() {
            if(isEmpty()) return;

            int iteration = 3;

            for (Map.Entry<Entity, List<Entity>> e : entrySet()) {
                Bukkit.getScheduler().runTaskLater(
                        Crown.inst(),
                        () -> {
                            Entity entity = e.getKey();
                            List<Entity> entities = e.getValue();

                            for (Entity ent : entities) {
                                addPassenger(entity, ent);
                            }
                        },
                        ++iteration
                );
            }
        }

        public void fillFrom(Entity entity, RegionVisit v) {
            Validate.notNull(entity);
            Validate.notNull(v);

            fillRecursive(entity, entity.getPassengers(), v);
        }

        private void fillRecursive(Entity entity, List<Entity> passengers, RegionVisit v) {
            if(passengers.isEmpty()) return;
            ListIterator<Entity> iterator = passengers.listIterator();

            while (iterator.hasNext()) {
                Entity e = iterator.next();
                List<Entity> ePassangers = new ObjectArrayList<>(e.getPassengers());
                entity.removePassenger(entity);

                // Entity e could be a player rider slime
                // which is killed when it dismounts
                if(e.isDead()) {
                    iterator.remove();
                } else {
                    // ensure the entity doesn't get moved by the
                    // owned entity handler
                    OwnedEntityHandler handler = v.getHandler(OwnedEntityHandler.class);
                    if(handler != null) {
                        handler.ignored.add(e.getUniqueId());
                    }

                    e = moveEntity(e, v.getTeleportLocation());
                    iterator.set(e);
                    fillRecursive(e, ePassangers, v);
                }
            }

            put(moveEntity(entity, v.getTeleportLocation()), passengers);
        }

        private void addPassenger(Entity entity, Entity passenger) {
            // If it's two players, make sure they're properly riding each other
            if(entity instanceof Player && passenger instanceof Player) {
                Cosmetics.getRideManager().startRiding((Player) entity, (Player) passenger);
                return;
            }

            // Otherwise, just do addPassenger
            entity.addPassenger(passenger);
        }

        private Entity moveEntity(Entity entity, Location dest) {
            //handle boats and minecarts differently
            if(entity instanceof Vehicle) {
                return moveVehicle(entity, dest);
            }

            entity.teleport(dest);
            return entity;
        }

        // kill the original entity and create a copy of it
        // at the destination, cuz teleporting it is way too
        // slow. Aka, the boat moves slowly
        private Entity moveVehicle(Entity entity, Location dest) {
            Level level = VanillaAccess.getLevel(dest.getWorld());
            net.minecraft.world.entity.Entity nms = VanillaAccess.getEntity(entity);

            CompoundTag saved = new CompoundTag();
            nms.saveWithoutId(saved);

            saved.putString("id", nms.getEncodeId());
            saved.put("Pos", newDoubleList(dest.getX(), dest.getY(), dest.getZ()));

            nms.discard();

            Optional<net.minecraft.world.entity.Entity> e = EntityType.create(saved, level);
            net.minecraft.world.entity.Entity e1 = e.get();

            level.addFreshEntity(e1, CreatureSpawnEvent.SpawnReason.DEFAULT);
            return e1.getBukkitEntity();
        }

        protected ListTag newDoubleList(double... values) {
            ListTag nbttaglist = new ListTag();
            double[] adouble1 = values;
            int i = values.length;

            for(int j = 0; j < i; ++j) {
                double d0 = adouble1[j];
                nbttaglist.add(DoubleTag.valueOf(d0));
            }

            return nbttaglist;
        }
    }
}