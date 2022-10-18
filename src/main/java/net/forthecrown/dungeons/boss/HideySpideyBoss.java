package net.forthecrown.dungeons.boss;

import com.destroystokyo.paper.entity.Pathfinder;
import net.forthecrown.text.Messages;
import net.forthecrown.core.Worlds;
import net.forthecrown.dungeons.BossItems;
import net.forthecrown.dungeons.DungeonAreas;
import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.dungeons.boss.components.BossComponent;
import net.forthecrown.dungeons.boss.components.MinionSpawnerComponent;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.data.RankTier;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftVector;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.loot.LootTables;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import java.util.Set;

public class HideySpideyBoss extends SimpleBoss {
    public static Vec3 SPAWN_VEC = new Vec3(-78.5, 55, 284.5);

    public HideySpideyBoss() {
        super("Hidey Spidey", new Location(Worlds.voidWorld(), SPAWN_VEC.x, SPAWN_VEC.y, SPAWN_VEC.z), DungeonAreas.SPIDEY_ROOM,

                DungeonUtils.makeDungeonItem(Material.SPIDER_EYE, 45, (Component) null),
                DungeonUtils.makeDungeonItem(Material.FERMENTED_SPIDER_EYE, 20, (Component) null),
                DungeonUtils.makeDungeonItem(Material.STRING, 30, (Component) null),

                ItemStacks.potionBuilder(Material.TIPPED_ARROW, 5)
                        .setBaseEffect(new PotionData(PotionType.POISON))
                        .addLore(Messages.DUNGEON_LORE)
                        .build()
        );
    }

    @Override
    protected void createComponents(Set<BossComponent> c) {
        super.createComponents(c);

        c.add(
                MinionSpawnerComponent.create(
                        (pos, world, context) -> {
                            Vec3 dif = SPAWN_VEC.subtract(pos).normalize();
                            Vector velocity = CraftVector.toBukkit(dif).multiply(1.5f);

                            return world.spawn(
                                    new Location(world, pos.x, pos.y, pos.z),
                                    CaveSpider.class,
                                    caveSpider -> {
                                        caveSpider.setVelocity(velocity);

                                        double health = context.modifier() + 12;
                                        caveSpider.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
                                        caveSpider.setHealth(health);
                                        caveSpider.setLootTable(LootTables.EMPTY.getLootTable());
                                    }
                            );
                        },
                        200, 10,
                        new double[][] {
                                {-68.5, 57, 284.5},
                                {-88.5, 57, 284.5}
                        }
                )
        );
    }

    @Override
    protected Mob onSpawn(BossContext context) {
        return getWorld().spawn(getSpawn(), Spider.class, spidey -> {
            spidey.setCustomName("Hidey Spidey");
            spidey.setCustomNameVisible(false);
            spidey.setRemoveWhenFarAway(false);
            spidey.setPersistent(true);

            double health = context.health(300);
            spidey.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
            spidey.setHealth(health);

            // God, I love modifiers
            Util.clearModifiers(spidey.getAttribute(Attribute.GENERIC_MAX_HEALTH));

            spidey.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(25);
            spidey.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1);
            spidey.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(context.damage(11));
            spidey.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.28 + (context.modifier()/20));

            Pathfinder pathfinder = spidey.getPathfinder();
            pathfinder.setCanFloat(false);

            spidey.setInvisible(true);
        });
    }

    @Override
    protected void giveRewards(Player p) {
        Util.giveOrDropItem(p.getInventory(), entity.getLocation(), BossItems.HIDEY_SPIDEY.item());

        // Final boss of the first 3 levels,
        // Rewards the free rank tier
        User user = Users.get(p);

        // Don't give the tier if you already
        // have the tier lol
        if (user.getTitles().hasTier(RankTier.FREE)) {
            return;
        }

        user.getTitles().addTier(RankTier.FREE);
        user.sendMessage(Messages.GOT_KNIGHT_RANK);
    }
}