package net.forthecrown.dungeons.boss;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Worlds;
import net.forthecrown.dungeons.BossItems;
import net.forthecrown.dungeons.DungeonAreas;
import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTables;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class DrawnedBoss extends SimpleBoss {

    private final Set<Guardian> guardians = new ObjectOpenHashSet<>();

    public DrawnedBoss() {
        super("Drawned", new Location(Worlds.voidWorld(), -123.5, 25.5, 38.5), DungeonAreas.DRAWNED_ROOM,
                Artifacts.ELDER.item(),
                Artifacts.HIDDEN.item(),
                Artifacts.IRON.item(),
                Artifacts.NAUTILUS.item(),
                Artifacts.TURTLE.item()
        );
    }

    @Override
    protected Mob onSpawn(BossContext context) {
        tick = 0;

        return getWorld().spawn(getSpawn(), Drowned.class, drawned ->{
            drawned.getEquipment().setItemInMainHand(new ItemStack(Material.TRIDENT, 1));
            drawned.getEquipment().setItemInMainHandDropChance(0);
            drawned.setAdult();
            drawned.customName(Component.text("Drawned").color(NamedTextColor.YELLOW));
            drawned.setCustomNameVisible(true);
            drawned.setRemoveWhenFarAway(false);
            drawned.setPersistent(true);

            final double health = context.health(350);
            drawned.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
            drawned.setHealth(health);

            Util.clearModifiers(drawned.getAttribute(Attribute.GENERIC_MAX_HEALTH));

            drawned.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(50.0);
            drawned.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0F);
            drawned.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.32F);
        });
    }

    @Override
    protected void giveRewards(Player player) {
        Util.giveOrDropItem(player.getInventory(), entity.getLocation(), BossItems.DRAWNED.item());
    }

    public static final int MINION_SPAWN_INTERVAL = 300;
    private int tick;

    @Override
    protected void tick() {
        tick++;

        if (tick <= MINION_SPAWN_INTERVAL) {
            return;
        }
        tick = 0;

        if (guardians.size() > 2) {
            return;
        }

        // Sounds
        for(int i = 1; i < 6; i++) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(FTC.getPlugin(), () -> {
                entity.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, entity.getLocation().getX(), entity.getLocation().getY() + 1.5, entity.getLocation().getZ(), 30, 0.2d, 0.1d, 0.2d, 1);
                entity.getWorld().playSound(entity.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, SoundCategory.MASTER, 2f, 0.8f);
            }, i * 8L);
        }

        // Give boss godmode
        entity.setInvulnerable(true);
        entity.setGlowing(true);

        for (int i = 0; i < 3; i++) {
            // Spawn minions with invulnerability
            spawnGuardian(true);
        }

        entity.getWorld().strikeLightningEffect(entity.getLocation());

        Bukkit.getScheduler().runTaskLater(FTC.getPlugin(), () -> {
            // Remove boss' godmode
            entity.setInvulnerable(false);
            entity.setGlowing(false);

            // Remove the minions' invulnerability
            for (Guardian g: guardians) {
                if (g.isDead()) {
                    continue;
                }

                g.setInvulnerable(false);
            }
        }, 20);
    }

    @Override
    protected void onDeath(BossContext context) {
        // Since we're not using the MinionSpawnerComponent, cuz this is a
        // bit complex for that, we've gotta apply wither to the minions
        // manually (._. )
        for (Guardian s: guardians) {
            if (s.isDead()) {
                continue;
            }

            s.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 99999, 254, false, false));
        }
        
        guardians.clear();
    }

    private void spawnGuardian(boolean invulnerable) {
        guardians.add(
                getWorld().spawn(entity.getLocation(), Guardian.class, guardian -> {
                    guardian.setInvulnerable(invulnerable);
                    guardian.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(currentContext.health(10));
                    guardian.setLootTable(LootTables.EMPTY.getLootTable());
                })
        );
    }

    public enum Artifacts {
        IRON (DungeonUtils.makeDungeonItem(Material.IRON_NUGGET, 1, "Iron Artifact")),
        ELDER (DungeonUtils.makeDungeonItem(Material.PRISMARINE_CRYSTALS, 1, "Elder Artifact")),
        TURTLE (DungeonUtils.makeDungeonItem(Material.SCUTE, 1, "Turtle Artifact")),
        NAUTILUS (DungeonUtils.makeDungeonItem(Material.NAUTILUS_SHELL, 1, "Nautilus Artifact")),
        HIDDEN (DungeonUtils.makeDungeonItem(Material.QUARTZ, 1, "Hidden Artifact"));

        private final ItemStack item;
        Artifacts(ItemStack itemStack){
            this.item = itemStack;
        }

        public ItemStack item() {
            return item.clone();
        }
    }
}