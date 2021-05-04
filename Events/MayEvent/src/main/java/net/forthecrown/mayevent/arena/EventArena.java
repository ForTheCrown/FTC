package net.forthecrown.mayevent.arena;

import net.forthecrown.core.CrownBoundingBox;
import net.forthecrown.core.nbt.NBT;
import net.forthecrown.core.nbt.NbtGetter;
import net.forthecrown.core.utils.Cooldown;
import net.forthecrown.core.utils.CrownRandom;
import net.forthecrown.core.utils.ItemStackBuilder;
import net.forthecrown.mayevent.*;
import net.forthecrown.mayevent.guns.HitScanWeapon;
import net.forthecrown.mayevent.guns.RocketLauncher;
import net.forthecrown.mayevent.guns.StandardRifle;
import net.forthecrown.mayevent.guns.TwelveGaugeShotgun;
import net.kyori.adventure.text.Component;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.EntityTypes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTables;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EventArena {

    ArenaUpdater updater;

    public final List<Location> mobSpawns;
    public final Map<Location, EntityTypes<? extends EntityLiving>> specialSpawns;
    public final List<Location> itemDrops;
    public final List<Location> gems;
    private final Location[] wallSpawns;
    public final List<TriggerableEvent> triggerableEvents;
    public final Location startLocation;
    public final CrownRandom random;
    public final ArenaEntry entry;
    public final BossBar bossBar;
    public final CrownBoundingBox box;
    private final Location minLoc;

    private int wave = 0;
    public short initialMobAmount = 0;
    public short currentMobAmount = 0;

    public final Location firstGun;
    public final Location secondGun;
    public final Location rocket;

    public final StandardRifle playerRifle;
    public final TwelveGaugeShotgun playerShotgun;
    public final RocketLauncher playerRocket;
    public final ArmorStand waveDisplay;

    public EventArena(
            ArenaEntry entry,
            List<Location> mobSpawns,
            List<Location> itemDrops,
            List<Location> gems,
            Location[] wallSpawns,
            Map<Location, EntityTypes<? extends EntityLiving>> specialSpawns,
            List<TriggerableEvent> triggerableEvents,
            Location firstGun,
            Location secondGun,
            Location rocket,
            Location minLoc,
            Location startLocation,
            CrownBoundingBox box
    ) {

        this.entry = entry;
        this.mobSpawns = mobSpawns;
        this.specialSpawns = specialSpawns;
        this.itemDrops = itemDrops;
        this.gems = gems;
        this.wallSpawns = wallSpawns;
        this.triggerableEvents = triggerableEvents;

        this.firstGun = firstGun;
        this.secondGun = secondGun;
        this.rocket = rocket;

        this.minLoc = minLoc;
        this.startLocation = startLocation;
        this.box = box;
        this.random = new CrownRandom();
        this.bossBar = makeBossBar();

        this.playerRifle = new StandardRifle();
        this.playerShotgun = new TwelveGaugeShotgun();
        this.playerRocket = new RocketLauncher();

        waveDisplay = startLocation.getWorld().spawn(startLocation.clone().add(0, 3, 0), ArmorStand.class, stand -> {
            stand.setInvulnerable(true);
            stand.setGravity(false);

            stand.setInvisible(true);
            stand.setCustomNameVisible(true);

            stand.setPersistent(false);
            stand.setRemoveWhenFarAway(true);

            stand.customName(Component.text(bossBar.getTitle()));
        });
    }

    public void start(){
        entry.player().teleport(startLocation);
        entry.player().setHealth(20);
        entry.player().setFoodLevel(20);

        StartingKit.give(entry.player());

        MayUtils.dropItem(MayUtils.validateIsAir(firstGun), playerRifle.item(), false);
        MayUtils.dropItem(MayUtils.validateIsAir(rocket), playerRocket.item(), false);

        for (Location l: itemDrops) spawnPickup(MayUtils.validateIsAir(l));

        ItemStack gemItem = new ItemStackBuilder(Material.DIAMOND)
                .addEnchant(Enchantment.LUCK, 5)
                .build();

        NBT nbt = NbtGetter.ofItemTags(gemItem);
        nbt.put("gems", true);
        gemItem = NbtGetter.applyTags(gemItem, nbt);

        if(!Cooldown.contains(entry.player(), "event_arena")){
            for (Location l: gems){
                MayUtils.dropItem(MayUtils.validateIsAir(l), gemItem, false);
            }
            Cooldown.add(entry.player(), "event_arena", 72000);
        }

        updater = new ArenaUpdater(this);
        updater.runTaskTimer(MayMain.inst, 1, 1);
    }

    private int currentItemIndex = 0;
    public void spawnNextPickup(){
        currentItemIndex++;
        if(currentItemIndex == itemDrops.size()) currentItemIndex = 0;

        spawnPickup(itemDrops.get(currentItemIndex));
    }

    public void spawnPickup(Location loc){
        ItemStack item = getRandomDrop();
        MayUtils.dropItem(MayUtils.validateIsAir(loc), item, false);
    }

    private byte spawnWhat = 5;
    private ItemStack getRandomDrop(){

        spawnWhat--;
        switch (spawnWhat) {
            case 4: return new ItemStack(Material.ARROW, 8);
            case 3: return new ItemStack(Material.GOLDEN_CARROT, 8);
            case 2: return new ItemStackBuilder(Material.SPLASH_POTION, 1)
                    .setBaseEffect(new PotionData(random.nextBoolean() ? PotionType.INSTANT_HEAL : PotionType.REGEN))
                    .build();

            case 1:
            default:
                HitScanWeapon gun = entry.getLowestAmmoGun();
                if(gun == null) return playerRifle.ammoPickup();

                spawnWhat = 5;
                return gun.ammoPickup();
        }
    }

    public void checkIfOnWalls(){
        if(!(entry.player().getLocation().getBlockY() >= 42)) return;

        for (Location l: wallSpawns){
            Class<? extends Mob> mob = random.nextBoolean() ? Zombie.class : Skeleton.class;
            MayUtils.spawnAndEffect(l, mob, this::onMobSpawn);
        }
    }

    private void spawnMobsForWave(){

        for (Location l: mobSpawns){
            MayUtils.validateIsAir(l);

            Class<? extends Mob> clazz = random.nextBoolean() ? Zombie.class : Skeleton.class;

            MayUtils.spawnAndEffect(l, clazz, this::onMobSpawn);
        }

        for (Map.Entry<Location, EntityTypes<? extends EntityLiving>> e: specialSpawns.entrySet()){
            Location l = e.getKey();
            MayUtils.validateIsAir(l);

            CraftWorld craftWorld = ((CraftWorld) l.getWorld());
            Entity ent = e.getValue().a(craftWorld.getHandle());
            ent.setPosition(l.getX(), l.getY(), l.getZ());

            craftWorld.addEntity(ent, CreatureSpawnEvent.SpawnReason.CUSTOM);
            initialMobAmount++;
            currentMobAmount++;
        }

        triggerableEvents.forEach(e -> e.poll(this));

        if(toRemove.size() > 0){
            triggerableEvents.removeAll(toRemove);
            toRemove.clear();
        }
    }

    public Consumer<? extends Mob> onMobSpawn(Mob mob){
        return mob1 -> {
            AttributeModifier waveMod = getWaveModifier();

            mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).addModifier(waveMod);
            mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).addModifier(waveMod);

            try {
                mob.getAttribute(Attribute.GENERIC_ATTACK_SPEED).addModifier(waveMod);
            } catch (Exception ignored) {}

            mob.setHealth(mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            mob.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.3);

            mob.setLootTable(LootTables.EMPTY.getLootTable());

            mob.setTarget(entry.player());

            EntityEquipment eq = mob.getEquipment();
            eq.setLeggingsDropChance(0f);
            eq.setChestplateDropChance(0f);
            eq.setHelmetDropChance(0f);
            eq.setBootsDropChance(0f);

            eq.setItemInMainHandDropChance(0f);
            eq.setItemInOffHandDropChance(0f);

            DoomEvent.MOB_TEAM.addEntry(mob.getUniqueId().toString());
            initialMobAmount++;
            currentMobAmount++;
        };
    }

    List<TriggerableEvent> toRemove = new ArrayList<>();

    public AttributeModifier getWaveModifier(){
        return new AttributeModifier("wave_modifier", wave, AttributeModifier.Operation.ADD_NUMBER);
    }

    public Location minLoc() {
        return minLoc.clone();
    }

    public void checkBossbar(){
        if(bossBar.getProgress() <= 0) nextWave();
    }

    public void nextWave() {
        updater.startWaveDelay();
    }

    public void nextWaveActual(){
        wave++;
        initialMobAmount = 0;
        currentMobAmount = 0;

        bossBar.setProgress(1);
        bossBar.setColor(BarColor.RED);
        bossBar.setTitle("Wave " + wave);

        waveDisplay.customName(Component.text(bossBar.getTitle()));
        spawnMobsForWave();
    }

    public int wave() {
        return wave;
    }

    public void shutdown(){
        updater.cancel();

        bossBar.removeAll();
        bossBar.setVisible(false);

        waveDisplay.remove();

        box.getEntitiesByType(Item.class).forEach(Item::remove);
        box.getEntitiesByType(Mob.class).forEach(Mob::remove);
    }

    public void checkHighlighting(){
        if(currentMobAmount < 4){
            box.getEntitiesByType(Mob.class).forEach(m -> m.setGlowing(true));
        }
    }

    public void updateBossbar(){
        try {
            bossBar.setProgress((double) currentMobAmount / (double) initialMobAmount);
        } catch (Exception e) { bossBar.setProgress(1); }
    }

    private BossBar makeBossBar(){
        BossBar bar = Bukkit.createBossBar(wave > 0 ? "Wave " + wave : "Get ready!", BarColor.YELLOW, BarStyle.SOLID, BarFlag.CREATE_FOG, BarFlag.DARKEN_SKY, BarFlag.PLAY_BOSS_MUSIC);
        bar.setProgress(1);
        bar.setVisible(true);
        bar.addPlayer(entry.player());
        return bar;
    }
}
