package net.forthecrown.mayevent.guns;

import net.forthecrown.core.nbt.NBT;
import net.forthecrown.core.nbt.NbtGetter;
import net.forthecrown.core.utils.Cooldown;
import net.forthecrown.core.utils.ItemStackBuilder;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public abstract class HitScanWeapon {

    protected final int maxAmmo;
    protected final int tickFiringDelay;
    protected final double damage;
    protected final String name;
    protected final ItemStack item;
    protected final ItemStack ammoPickup;

    protected int remainingAmmo;
    protected IChatBaseComponent message;
    protected MessageMaker makeMessage;

    protected HitScanWeapon(int maxAmmo, int tickFiringDelay,
                            double damage, String name,
                            MessageMaker makeMessage,                                                //Actionbar message, first and second int are ammo and max ammo
                            ItemBuilderSupplier weaponItem,                                   //Weapon item
                            ItemBuilderSupplier ammoPickup                                    //Ammo pickup
    ) {
        this.maxAmmo = maxAmmo;
        this.remainingAmmo = maxAmmo;
        this.name = name;
        this.damage = damage;
        this.tickFiringDelay = tickFiringDelay;
        this.makeMessage = makeMessage;

        ItemStack item = weaponItem.get().build();
        NBT nbt = NbtGetter.ofItemTags(item);
        nbt.put("eventItem", (byte) 1);
        nbt.put("gun", name);
        nbt.put("gunPickup", name);

        ItemStack ammoItemPickup = ammoPickup.get().build();
        NBT nbtPickup = NbtGetter.ofItemTags(ammoItemPickup);
        nbtPickup.put("eventItem", (byte) 1);
        nbtPickup.put("gunPickup", name);

        this.ammoPickup = NbtGetter.applyTags(ammoItemPickup, nbtPickup);
        this.item = NbtGetter.applyTags(item, nbt);
        makeMessage();
    }

    protected abstract boolean onUse(HitScanShot shot);

    public boolean attemptUse(GunHolder holder){
        return attemptUse(holder.getLocation(), holder.getAimingDirection(), holder.ignoreAmmo(), holder.getHoldingEntity(), holder.getWave());
    }

    //Return true if, and only if, item should be added to firing cooldown
    public boolean attemptUse(Location eyeLoc, Vector direction, boolean ignoreAmmo, LivingEntity source, int wave){
        if(testCooldownAndAdd(source)) return false;
        if(remainingAmmo <= 0 && !ignoreAmmo) return false;
        if(!ignoreAmmo) remainingAmmo--;

        Entity ent = source.getTargetEntity(100, false);
        RayTraceResult hitScan = eyeLoc.getWorld().rayTrace(eyeLoc, direction, 100, FluidCollisionMode.ALWAYS, false, 50,
                e -> (!(e instanceof Player)) || !(e instanceof Arrow));

        boolean result = onUse(new HitScanShot(hitScan, eyeLoc, direction, source, ent, wave));
        makeMessage();
        return result;
    }

    public void pickupAmmo(int amount){
        remainingAmmo += amount;
        if(remainingAmmo > maxAmmo) remainingAmmo = maxAmmo;
        makeMessage();
    }

    public void makeMessage(){
        message = makeMessage.apply(remainingAmmo, maxAmmo);
    }

    public boolean testCooldownAndAdd(Entity source){
        if(Cooldown.contains(source, "MayEvent_Guns_" + name)) return true;

        Cooldown.add(source, "MayEvent_Guns_" + name, tickFiringDelay);
        return false;
    }

    public int maxAmmo() {
        return maxAmmo;
    }

    public int remainingAmmo() {
        return remainingAmmo;
    }

    public ItemStack item() {
        return item.clone();
    }

    public int tickFiringDelay() {
        return tickFiringDelay;
    }

    public String name() {
        return name;
    }

    public IChatBaseComponent message() {
        return message;
    }

    public ItemStack ammoPickup() {
        return ammoPickup.clone();
    }

    public double damage() {
        return damage;
    }

    public interface MessageMaker extends BiFunction<Integer, Integer, IChatBaseComponent>{}
    public interface ItemBuilderSupplier extends Supplier<ItemStackBuilder> {}

    public static class HitScanShot {
        public final boolean hasHitEntity;
        public final RayTraceResult hitScan;
        public final Location eyeLoc;
        public final Location endLoc;
        public final Vector direction;
        public final Entity source;
        public final Entity target;
        public final int wave;

        public HitScanShot(RayTraceResult scan, Location shooterEyeLoc, Vector direction, Entity source, Entity target, int wave) {
            hasHitEntity = target != null;
            hitScan = scan;
            this.target = target;
            this.eyeLoc = shooterEyeLoc;
            this.endLoc = scan.getHitPosition().toLocation(shooterEyeLoc.getWorld());
            this.direction = direction;
            this.source = source;
            this.wave = wave;
        }
    }
}
