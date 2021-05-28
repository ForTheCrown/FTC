package net.forthecrown.royals.enchantments;

import net.forthecrown.emperor.utils.Cooldown;
import net.forthecrown.royals.Royals;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

public class EnchantEvents implements Listener {

    private final BukkitScheduler scheduler = Bukkit.getScheduler();
    private final Royals main = Royals.inst;

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        if(inv.getFirstItem() == null || inv.getSecondItem() == null) return;
        if(inv.getResult() == null) return;
        ItemStack first = inv.getFirstItem();

        for (Enchantment e: first.getItemMeta().getEnchants().keySet()) {
            //This is so utterly fucking retarded, but nothing else worked
            if (!e.getClass().getSuperclass().getName().equals(CrownEnchant.class.getName())) continue;

            ItemStack result = event.getResult();
            ItemMeta meta = result.getItemMeta();

            meta.addEnchant(e, first.getItemMeta().getEnchantLevel(e), true);
            result.setItemMeta(meta);
            event.setResult(result);
            return;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (event.getCause() == EntityDamageEvent.DamageCause.THORNS || event.isCancelled()) return;
            if (player.getInventory().getItemInMainHand().containsEnchantment(RoyalEnchants.poisonCrit())) {
                if (event.getEntity() instanceof Player) if (((Player) event.getEntity()).isBlocking()) return;

                boolean flag = player.getFallDistance() > 0.0F && !player.isOnGround() && !player.hasPotionEffect(PotionEffectType.BLINDNESS) && event.getEntity() instanceof LivingEntity;
                if (!flag) return;

                LivingEntity hitEntity = (LivingEntity) event.getEntity();

                final boolean monster = hitEntity instanceof Monster;
                hitEntity.addPotionEffect(new PotionEffect(monster ? PotionEffectType.WITHER : PotionEffectType.POISON, monster ? 45 : 35, monster ? 2 : 1, false, false));

                Particle.DustOptions dust = new Particle.DustOptions(
                        Color.fromRGB(31, 135, 62), 1);
                hitEntity.getWorld().spawnParticle(Particle.REDSTONE, hitEntity.getLocation().getX(), hitEntity.getLocation().getY() + (hitEntity.getHeight() / 2), hitEntity.getLocation().getZ(), 10, 0.2D, 0.2D, 0.2D, dust);
                hitEntity.getWorld().playSound(hitEntity.getLocation(), Sound.ENTITY_SPIDER_HURT, 0.2f, 0.7f);
            }
        }
    }

    @EventHandler
    public void onEntityDmangeEntEvent(EntityDamageByEntityEvent event){
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (!player.isBlocking() && event.getFinalDamage() != 0) return;
            if (!(player.getInventory().getItemInOffHand().containsEnchantment(RoyalEnchants.healingBlock()) || player.getInventory().getItemInMainHand().containsEnchantment(RoyalEnchants.healingBlock()))) return;
            if(Cooldown.contains(player, "Enchant_HealingBlock")) return;

            double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            player.setHealth(Math.min(player.getHealth() + 2, maxHealth));

            player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 0.6f, 1f);
            Cooldown.add(player, "Enchant_HealingBlock",40);
        }
    }

    @EventHandler
    public void onActivate(PlayerInteractEvent event) {
        if(event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if(!event.getPlayer().isSwimming()) return;
        if(event.getPlayer().getInventory().getItemInMainHand().getType() != Material.TRIDENT) return;
        if(!event.getPlayer().getInventory().getItemInMainHand().containsEnchantment(RoyalEnchants.dolphinSwimmer())) return;
        if(event.getPlayer().getGameMode() == GameMode.SURVIVAL || event.getPlayer().getGameMode() == GameMode.ADVENTURE){
            Damageable dmg = ((Damageable) event.getPlayer().getInventory().getItemInMainHand().getItemMeta());
            if (dmg.getDamage() >= 249) return;
            dmg.setDamage(dmg.getDamage() + 2);

            event.getPlayer().getInventory().getItemInMainHand().setItemMeta((ItemMeta) dmg);
        }

        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 120, 1), true);
        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, 1f, 1.5f);

        /*if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (!event.getPlayer().isSwimming() && !event.getPlayer().getInventory().getItemInMainHand().containsEnchantment(RoyalEnchants.dolphinSwimmer())) return;
        if(event.getPlayer().getGameMode() == GameMode.SURVIVAL || event.getPlayer().getGameMode() == GameMode.ADVENTURE){
            Damageable dmg = ((Damageable) event.getPlayer().getInventory().getItemInMainHand().getItemMeta());
            if (dmg.getDamage() >= 249) return;
            dmg.setDamage(dmg.getDamage() + 2);

            event.getPlayer().getInventory().getItemInMainHand().setItemMeta((ItemMeta) dmg);
        }

        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 120, 1), true);
        event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, 1f, 1.5f);*/
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityShootBow(EntityShootBowEvent event) {
        if(!event.getBow().containsEnchantment(RoyalEnchants.strongAim())) return;
        if(event.getForce() != 1) return;
        if(!(event.getProjectile() instanceof Arrow)) return;
        new StrongAimScheduler((Arrow) event.getProjectile());
    }

    public class StrongAimScheduler implements Runnable{
        private final Arrow arrow;
        private final int loopID;

        public StrongAimScheduler(Arrow arrow){
            this.arrow = arrow;
            loopID = scheduler.scheduleSyncRepeatingTask(main, this, 3, 3);
        }

        @Override
        public void run() {
            arrow.setVelocity(arrow.getVelocity().add(new Vector(0, 0.075, 0)));
            if(arrow.isOnGround()) scheduler.cancelTask(loopID);
        }
    }
}