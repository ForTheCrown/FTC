package net.forthecrown.mayevent.mobs;

import com.google.common.base.Preconditions;
import net.forthecrown.mayevent.arena.EventArena;
import net.forthecrown.mayevent.guns.GunHolder;
import net.forthecrown.mayevent.guns.HitScanWeapon;
import net.forthecrown.mayevent.guns.TwelveGaugeShotgun;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTables;

public class ShotGunner implements GunHolder {

    public final Zombie zombie;
    public TwelveGaugeShotgun shotgun;
    public final EventArena arena;

    public ShotGunner(Zombie zombie, EventArena arena){
        this.zombie = zombie;
        shotgun = new TwelveGaugeShotgun();
        this.arena = arena;

        EntityEquipment eq = zombie.getEquipment();
        eq.setItemInMainHand(shotgun.item());
        eq.setChestplate(new ItemStack(Material.IRON_CHESTPLATE, 1));
        eq.setHelmet(new ItemStack(Material.IRON_HELMET, 1));

        eq.setHelmetDropChance(0f);
        eq.setChestplateDropChance(0f);
        eq.setLeggingsDropChance(0f);
        eq.setItemInOffHandDropChance(0f);
        eq.setItemInMainHandDropChance(0f);

        eq.setBoots(shotgun.ammoPickup());
        eq.setBootsDropChance(1f);

        zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH).addModifier(arena.getWaveModifier());
        zombie.setHealth(zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

        zombie.setLootTable(LootTables.EMPTY.getLootTable());
    }

    public static ShotGunner spawn(Location l, EventArena arena){
        return new ShotGunner(l.getWorld().spawn(l, Zombie.class), arena);
    }

    public boolean hasLineOfSight(){
        return false;
    }

    @Override
    public TwelveGaugeShotgun getHeldGun() {
        return shotgun;
    }

    @Override
    public void setGun(HitScanWeapon gun) {
        Preconditions.checkArgument(gun instanceof TwelveGaugeShotgun, "Gun can only be twelve gauge shotgun");
        Preconditions.checkNotNull(gun, "Gun was null");

        this.shotgun = (TwelveGaugeShotgun) gun;
    }

    @Override
    public Zombie getHoldingEntity() {
        return zombie;
    }

    @Override
    public Location getLocation() {
        return zombie.getLocation();
    }

    @Override
    public int getWave() {
        return arena.wave();
    }
}
