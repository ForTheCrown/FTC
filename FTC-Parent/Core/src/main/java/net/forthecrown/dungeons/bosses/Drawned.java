package net.forthecrown.dungeons.bosses;

import net.forthecrown.core.ForTheCrown;
import net.forthecrown.dungeons.DungeonAreas;
import net.forthecrown.dungeons.BossFightContext;
import net.forthecrown.dungeons.BossItems;
import net.forthecrown.utils.ItemStackBuilder;
import net.forthecrown.dungeons.DungeonUtils;
import net.forthecrown.utils.Worlds;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.Guardian;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTables;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Drawned extends DungeonBoss<Drowned> {

    private final Set<Guardian> guardians = new HashSet<>();

    public Drawned() {
        super("Drawned", new Location(Worlds.VOID, -123.5, 25.5, 38.5), (short) 300, DungeonAreas.DRAWNED_ROOM,
                Arrays.asList(
                        Artifacts.ELDER.item(),
                        Artifacts.HIDDEN.item(),
                        Artifacts.IRON.item(),
                        Artifacts.NAUTILUS.item(),
                        Artifacts.TURTLE.item()
                )
        );
    }

    @Override
    protected Drowned onSummon(BossFightContext context) {
        Drowned drowned = spawnLocation.getWorld().spawn(spawnLocation, Drowned.class, drawned ->{
            drawned.getEquipment().setItemInMainHand(
                    new ItemStackBuilder(Material.TRIDENT, 1)
                            .build()
            );
            drawned.getEquipment().setItemInMainHandDropChance(0);
            drawned.setAdult();
            drawned.customName(Component.text("Drawned").color(NamedTextColor.YELLOW));
            drawned.setCustomNameVisible(true);
            drawned.setRemoveWhenFarAway(false);
            drawned.setPersistent(true);

            final double health = context.getBossHealth(350);
            drawned.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
            drawned.getAttribute(Attribute.GENERIC_MAX_HEALTH).getModifiers().clear();
            drawned.setHealth(health);

            drawned.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(50.0);
            drawned.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0F);
            drawned.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.32F);
        });

        for (int i = 0; i < 3; i++){
            spawnGuardian(drowned.getLocation(), false);
        }

        return drowned;
    }

    @Override
    protected void onUpdate() {
        // Sounds
        for(int i = 1; i < 6; i++) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(ForTheCrown.inst(), () -> {
                bossEntity.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, bossEntity.getLocation().getX(), bossEntity.getLocation().getY() + 1.5, bossEntity.getLocation().getZ(), 30, 0.2d, 0.1d, 0.2d, 1);
                bossEntity.getWorld().playSound(bossEntity.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, SoundCategory.MASTER, 2f, 0.8f);
            }, i * 8L);
        }

        if(guardians.size() > 2) return;

        //Spawn the minions lol
        bossEntity.setInvulnerable(true);
        bossEntity.setGlowing(true);

        for (int i = 0; i < 3; i++){
            spawnGuardian(bossEntity.getLocation(), true);
        }

        bossEntity.getWorld().strikeLightningEffect(bossEntity.getLocation());

        Bukkit.getScheduler().runTaskLater(ForTheCrown.inst(), () -> {
            bossEntity.setInvulnerable(false);
            bossEntity.setGlowing(false);
            for (Guardian g: guardians){
                g.setInvulnerable(false);
            }
        }, 20);

    }

    private void spawnGuardian(Location location, boolean invulnerable){
        location.getWorld().spawn(location, Guardian.class, guardian -> {
            guardian.setInvulnerable(invulnerable);
            guardian.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(context.getBossHealth(10));
            guardian.setLootTable(LootTables.EMPTY.getLootTable());
            guardians.add(guardian);
        });
    }

    @Override
    protected void onDeath(BossFightContext context) {
        giveRewards(null, BossItems.DRAWNED.item(), context);
        for (Guardian s: guardians){
            s.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 99999, 254, false, false));
        }
        guardians.clear();
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if(!guardians.contains(event.getEntity())) return;
        guardians.remove(event.getEntity());
    }

    public enum Artifacts{
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
