package net.forthecrown.dungeons.boss.evoker.phases;

import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.dungeons.boss.evoker.BossMessage;
import net.forthecrown.dungeons.boss.evoker.EvokerConfig;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootTables;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Map;

public final class AttackPhases {
    private AttackPhases() {}

    // Copied from the zombie class in NMS... this has to be a rounding error during compilation lol
    public static final double ZOMBIE_BASE_MOVE_SPEED = 0.23000000417232513D;

    public static final AttackPhase
            SHIELD          = new ShieldPhase(),
            POTION          = new PotionPhase(),
            SHULKER         = new ShulkerPhase(),
            NORMAL_ATTACK   = new NormalAttackPhase(),
            GHAST           = new GhastPhase();

    public static final AttackPhase ZOMBIES = new SummonPhase(
            (pos, world, context) -> {
                Class<? extends Monster> spawnClass = Util.RANDOM.nextInt(100) < EvokerConfig.zombies_skeletonChance ?
                        Skeleton.class : Zombie.class;

                return world.spawn(new Location(world, pos.x(), pos.y(), pos.z()), spawnClass, zombie -> {
                   modifyHealth(zombie, 20.0D, context);
                   clearAllDrops(zombie);
                   getMobTeam().addEntity(zombie);

                   AttributeInstance speed = zombie.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                   Util.clearModifiers(speed);

                   speed.setBaseValue(
                           Math.min(context.modifier() * (ZOMBIE_BASE_MOVE_SPEED * 1.33D), ZOMBIE_BASE_MOVE_SPEED * 2)
                   );
                });
            },
            BossMessage.random("phase_zombie_start", 2),
            BossMessage.random("phase_zombie_end", 2)
    );

    public static final AttackPhase ILLAGERS = new SummonPhase(
            (pos, world, context) -> {
                boolean ravager = Util.RANDOM.nextInt(100) < EvokerConfig.illager_ravagerChance + Math.ceil(context.modifier());
                Class<? extends Raider> clazz = ravager ? Ravager.class : (Util.RANDOM.nextBoolean() ? Pillager.class : Vindicator.class);

                return world.spawn(
                        new Location(world, pos.x(), pos.y(), pos.z()),
                        clazz,
                        raider -> {
                            modifyHealth(raider, ravager ? 100 : 24.0D, context);
                            clearAllDrops(raider);
                            getMobTeam().addEntity(raider);

                            ItemStack mainHand = raider.getEquipment().getItemInMainHand();

                            if (ItemStacks.isEmpty(mainHand)) {
                                return;
                            }

                            ItemMeta meta = mainHand.getItemMeta();

                            if (mainHand.getType() == Material.IRON_AXE) {
                                meta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
                            }

                            for (Map.Entry<Enchantment, Integer> e: meta.getEnchants().entrySet()) {
                                int level = (int) Math.max(e.getValue(), Math.ceil(context.modifier()));
                                meta.addEnchant(e.getKey(), level, true);
                            }

                            mainHand.setItemMeta(meta);
                        }
                );
            },
            BossMessage.partySize("phase_illager_start"),
            BossMessage.partySize("phase_illager_end")
    );

    // Phases that can be selected during round phase shuffle
    public static final AttackPhase[] SELECTABLE_PHASES = {
            POTION, ZOMBIES, ILLAGERS, NORMAL_ATTACK, GHAST
    };

    // Phases that get appended onto the randomized phase order
    public static final AttackPhase[] NON_SELECTABLE_PHASES = {
            SHULKER, SHIELD
    };

    static void clearAllDrops(Mob mob) {
        EntityEquipment equipment = mob.getEquipment();

        for (EquipmentSlot s: EquipmentSlot.values()) {
            equipment.setDropChance(s, 0f);
        }

        mob.setLootTable(LootTables.EMPTY.getLootTable());
    }

    static Team getMobTeam() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team t = scoreboard.getTeam(EvokerConfig.mobTeam);

        if (t == null) {
            t = scoreboard.registerNewTeam(EvokerConfig.mobTeam);
            t.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            t.setAllowFriendlyFire(false);
        }

        return t;
    }

    public static void modifyHealth(org.bukkit.entity.LivingEntity entity, double base, BossContext context) {
        double health = context.health(base);

        AttributeInstance maxHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        Util.clearModifiers(maxHealth);

        maxHealth.setBaseValue(health);
        entity.setHealth(health);
    }
}