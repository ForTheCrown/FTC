package net.forthecrown.royals.dungeons.bosses.mobs;

import com.destroystokyo.paper.entity.Pathfinder;
import net.forthecrown.core.utils.ItemStackBuilder;
import net.forthecrown.royals.RoyalUtils;
import net.forthecrown.royals.Royals;
import net.forthecrown.royals.dungeons.DungeonAreas;
import net.forthecrown.royals.dungeons.bosses.BossFightContext;
import net.forthecrown.royals.dungeons.bosses.BossItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.loot.LootTables;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class HideySpidey extends DungeonBoss<Spider> {

    private Set<CaveSpider> helpers = new HashSet<>();

    public HideySpidey(Royals plugin) {
        super(plugin, "Hidey Spidey", new Location(DungeonAreas.WORLD, -78.5, 55, 284.5), (short) 20, DungeonAreas.SPIDEY_ROOM,
                Arrays.asList(
                        RoyalUtils.makeDungeonItem(Material.SPIDER_EYE, 45, (Component) null),
                        RoyalUtils.makeDungeonItem(Material.FERMENTED_SPIDER_EYE, 20, (Component) null),
                        RoyalUtils.makeDungeonItem(Material.STRING, 30, (Component) null),
                        new ItemStackBuilder(Material.TIPPED_ARROW, 5)
                                .setBaseEffect(new PotionData(PotionType.POISON))
                                .addLore(RoyalUtils.DUNGEON_LORE)
                                .build()
                )
        );
    }

    @Override
    protected Spider onSummon(BossFightContext context) {
        Spider spider = spawnLocation.getWorld().spawn(spawnLocation, Spider.class, spidey -> {
            spidey.setCustomName("Hidey Spidey");
            spidey.setCustomNameVisible(false);
            spidey.setRemoveWhenFarAway(false);
            spidey.setPersistent(true);

            double health = context.getBossHealth(300);
            spidey.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
            spidey.setHealth(health);

            spidey.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(25);
            spidey.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1);
            spidey.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(context.getBossHealth(11));
            spidey.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.28 + (context.getModifier()/20));

            Pathfinder pathfinder = spidey.getPathfinder();
            pathfinder.setCanFloat(false);

            new PotionEffect(PotionEffectType.INVISIBILITY, 9999, 0, false, false).apply(spidey);
        });

        tillSpawn = 10;
        return spider;
    }

    byte tillSpawn = 7;
    @Override
    protected void onUpdate() {
        for (CaveSpider s: helpers){
            Player nearest = RoyalUtils.getNearestVisiblePlayer(s.getLocation(), getBossRoom());
            if(s.getTarget() != null && s.getTarget().equals(nearest)) continue;
            s.setTarget(nearest);
        }
        tillSpawn--;
        if(tillSpawn == 0){
            if(helpers.size() < 11) spawnHelper(random.nextInt(100) < 50 ? SpawnPart.WEST : SpawnPart.EAST);
            tillSpawn = 5;
        }

        if(!bossEntity.isOnGround()){
            Vector pos = bossEntity.getLocation().clone().toVector();
            Vector target = spawnLocation.clone().add(0, 2, 0).toVector();
            Vector velocity = target.subtract(pos);
            velocity = velocity.normalize().multiply(0.25);
            bossEntity.setVelocity(velocity);
        }
    }

    @Override
    public void onHit(EntityDamageEvent event) {
        bossEntity.setFireTicks(0);
    }

    @Override
    protected void onDeath(BossFightContext context) {
        giveRewards("adventure:hideyspidey", BossItems.HIDEY_SPIDEY.item(), context);
        for (CaveSpider s: helpers){
            s.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 99999, 254, false, false));
        }
        helpers.clear();
    }

    public void spawnHelper(SpawnPart part){
        CaveSpider spider = part.spawnLocation.getWorld().spawn(part.spawnLocation, CaveSpider.class, caveSpider -> {

            Vector pos = part.spawnLocation.clone().toVector();
            Vector target = HideySpidey.SpawnPart.EAST.trackLocation.clone().add(0, 2, 0).toVector();
            Vector velocity = target.subtract(pos);
            velocity = velocity.normalize().multiply(1.5);
            caveSpider.setVelocity(velocity);

            double health = context.getModifier() + 12;
            caveSpider.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
            caveSpider.setHealth(health);
            caveSpider.setLootTable(LootTables.EMPTY.getLootTable());
            caveSpider.getPathfinder().findPath(part.trackLocation);
            caveSpider.getPathfinder().moveTo(part.trackLocation);
            caveSpider.setLootTable(LootTables.EMPTY.getLootTable());
        });
        helpers.add(spider);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if(!helpers.contains(event.getEntity())) return;
        helpers.remove(event.getEntity());
    }

    public enum SpawnPart{
        WEST (
                new Location(DungeonAreas.WORLD, -71.5, 55, 284, 0, -90),
                new Location(DungeonAreas.WORLD, -89.5, 57, 284.5)
        ),
        EAST (
                new Location(DungeonAreas.WORLD, -85.5, 55, 284.5, 0, 90),
                new Location(DungeonAreas.WORLD, -67.5, 57, 284.5)
        );

        public final Location trackLocation;
        public final Location spawnLocation;
        SpawnPart(Location location, Location spawnLocation){
            this.trackLocation = location;
            this.spawnLocation = spawnLocation;
        }
    }
}
