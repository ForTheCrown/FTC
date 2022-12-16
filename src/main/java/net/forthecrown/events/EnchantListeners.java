package net.forthecrown.events;

import com.destroystokyo.paper.ParticleBuilder;
import net.forthecrown.dungeons.enchantments.FtcEnchant;
import net.forthecrown.dungeons.enchantments.FtcEnchants;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.function.Consumer;

public class EnchantListeners implements Listener {
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (ItemStacks.isEmpty(event.getResult())) {
            return;
        }

        AnvilInventory anvil = event.getInventory();

        ItemStack first = anvil.getFirstItem();
        ItemStack second = anvil.getSecondItem();
        ItemStack result = event.getResult();

        if (ItemStacks.isEmpty(first) || ItemStacks.isEmpty(second)) {
            return;
        }

        var enchants = first.getEnchantments();

        for (var e: enchants.entrySet()) {
            if (!(e.getKey() instanceof FtcEnchant enchant)) {
                continue;
            }

            int level = e.getValue();

            if (!enchant.canEnchantItem(second)) {
                result.editMeta(meta -> {
                    if (!meta.hasLore()) {
                        return;
                    }

                    var lore = meta.lore();
                    lore.removeIf(c -> c.contains(enchant.displayName(level)));

                    meta.lore(lore);
                });

                continue;
            }

            result.addUnsafeEnchantment(enchant, level);
        }

        event.setResult(result);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)
                || event.getCause() == EntityDamageEvent.DamageCause.THORNS
        ) {
            return;
        }

        if (!player.getInventory()
                .getItemInMainHand()
                .containsEnchantment(FtcEnchants.POISON_CRIT)
        ) {
            return;
        }

        if (event.getEntity() instanceof Player dmgPlayer
                && dmgPlayer.isBlocking()
        ) {
            return;
        }

        if (!(player.getFallDistance() > 0.0F
                && !player.isOnGround()
                && !player.hasPotionEffect(PotionEffectType.BLINDNESS)
                && event.getEntity() instanceof LivingEntity
        )) {
            return;
        }

        LivingEntity hitEntity = (LivingEntity) event.getEntity();

        final boolean monster = hitEntity instanceof Monster;

        hitEntity.addPotionEffect(new PotionEffect(
                monster ? PotionEffectType.WITHER : PotionEffectType.POISON,
                monster ? 45 : 35,
                monster ? 2 : 1,
                false,
                false
        ));

        new ParticleBuilder(Particle.REDSTONE)
                .location(
                        hitEntity.getLocation()
                                .add(0, hitEntity.getHeight() / 2, 0)
                )
                .data(new Particle.DustOptions(
                        Color.fromRGB(31, 135, 62),
                        1
                ))
                .count(10)
                .offset(0.2D, 0.2D, 0.2D)
                .spawn();

        hitEntity.getWorld().playSound(hitEntity.getLocation(), Sound.ENTITY_SPIDER_HURT, 0.2f, 0.7f);
    }

    @EventHandler
    public void onEntityDamageEntEvent(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        if (!player.isBlocking() && event.getFinalDamage() != 0) {
            return;
        }

        var inv = player.getInventory();
        if (!(inv.getItemInOffHand().containsEnchantment(FtcEnchants.HEALING_BLOCK)
                || inv.getItemInMainHand().containsEnchantment(FtcEnchants.HEALING_BLOCK))
        ) {
            return;
        }

        if (Cooldown.containsOrAdd(player, "Enchant_HealingBlock", 40)) {
            return;
        }

        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

        player.setHealth(Math.min(player.getHealth() + 2, maxHealth));
        player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 0.6f, 1f);
    }

    @EventHandler
    public void onActivate(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        var player = event.getPlayer();

        if (!player.isSwimming()) {
            return;
        }

        var item = player.getInventory().getItemInMainHand();

        if (item.getType() != Material.TRIDENT) {
            return;
        }

        if (!item.containsEnchantment(FtcEnchants.DOLPHIN_SWIMMER)) {
            return;
        }

        if (player.getGameMode() == GameMode.SURVIVAL
                || player.getGameMode() == GameMode.ADVENTURE
        ) {
            Damageable dmg = ((Damageable) item.getItemMeta());

            // If we're just below the amount that
            // would destroy the item, then stop
            if (dmg.getDamage() >= 249) {
                return;
            }

            dmg.setDamage(dmg.getDamage() + 2);
            item.setItemMeta(dmg);
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 120, 1));
        player.playSound(player.getLocation(), Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, 1f, 1.5f);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!event.getBow().containsEnchantment(FtcEnchants.STRONG_AIM)) {
            return;
        }

        if (event.getForce() != 1) {
            return;
        }

        if (!(event.getProjectile() instanceof Arrow)) {
            return;
        }

        Tasks.runTimer(new StrongAimScheduler((Arrow) event.getProjectile()), 3, 3);
    }

    private record StrongAimScheduler(Arrow arrow) implements Consumer<BukkitTask> {
        @Override
        public void accept(BukkitTask task) {
            arrow.setVelocity(arrow.getVelocity().add(new Vector(0, 0.075, 0)));

            if (arrow.isDead() || arrow.isOnGround()) {
                Tasks.cancel(task);
            }
        }
    }
}