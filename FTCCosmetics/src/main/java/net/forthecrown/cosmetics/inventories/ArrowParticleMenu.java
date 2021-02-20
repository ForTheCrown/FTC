package net.forthecrown.cosmetics.inventories;

import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.cosmetics.Cosmetics;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ArrowParticleMenu implements Listener {

    private final Inventory inv;
    private CrownUser user;

    public ArrowParticleMenu(CrownUser user) {
        CustomInventory cinv = new CustomInventory(36, "Arrow Effects", false, true);
        cinv.setHeadItemSlot(0);
        cinv.setReturnItemSlot(4);

        this.inv = cinv.getInventory();
        this.user = user;
    }

    public ArrowParticleMenu(){
        inv = null;
    }


    public Inventory getInv() {
        return makeInventory();
    }


    private Inventory makeInventory() {
        Inventory result = this.inv;

        ItemStack noEffect = CrownUtils.makeItem(Material.BARRIER, 1, true, ChatColor.GOLD + "No effect", ChatColor.GRAY + "Click to go back to default arrows", ChatColor.GRAY + "without any effects.");

        ItemStack flame = getEffectItem(Particle.FLAME, "&eFlame", ChatColor.GRAY + "Works perfectly with flame arrows.", "", ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "1000" + ChatColor.GRAY + " gems.");
        ItemStack snow = getEffectItem(Particle.SNOWBALL, "&eSnowy", ChatColor.GRAY + "To stay in the Christmas spirit!", "", ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "1000" + ChatColor.GRAY + " gems.");
        ItemStack sneeze = getEffectItem(Particle.SNEEZE, "&eSneeze", ChatColor.GRAY + "Cover the place in that juicy snot.", "", ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "1000" + ChatColor.GRAY + " gems.");
        ItemStack lovetab = getEffectItem(Particle.HEART, "&eCupid's Arrows", ChatColor.GRAY + "Time to do some matchmaking...", "", ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "1000" + ChatColor.GRAY + " gems.");
        ItemStack evillove = getEffectItem(Particle.DAMAGE_INDICATOR, "&eCupid's Evil Twin", ChatColor.GRAY + "Time to undo some matchmaking...", "", ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "1000" + ChatColor.GRAY + " gems.");
        ItemStack honeytrail = getEffectItem(Particle.DRIPPING_HONEY, "&eSticky Trail", ChatColor.GRAY + "For those who enjoy looking at the trail lol", "", ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "1000" + ChatColor.GRAY + " gems.");
        ItemStack smoke = getEffectItem(Particle.CAMPFIRE_COSY_SMOKE, "&eSmoke", "&7Pretend to be a cannon.", "", ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "1000" + ChatColor.GRAY + " gems.");
        ItemStack souls = getEffectItem(Particle.SOUL, "&eSouls", "&7Scary souls escaping from your arrow.", "", ChatColor.GRAY + "Click to purchase for " + ChatColor.GOLD + "1000" + ChatColor.GRAY + " gems.");
        ItemStack firework = getEffectItem(Particle.FIREWORKS_SPARK, "&eFirework", "&7Almost as if you're using a crossbow.", "" , "&7Click to purchase for &61000 &7Gems");

        if(user.getArrowParticle() == null) noEffect.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);

        result.setItem(10, flame);
        result.setItem(11, snow);
        result.setItem(12, sneeze);
        result.setItem(13, lovetab);
        result.setItem(14, evillove);
        result.setItem(15, honeytrail);
        result.setItem(16, smoke);

        result.setItem(19, souls);
        result.setItem(20, firework);

        result.setItem(31, noEffect);

        return result;
    }

    private ItemStack getEffectItem(Particle effect, String name, String... desc){
        ItemStack shit = CrownUtils.makeItem(Material.GRAY_DYE, 1, true, name, desc);
        if(user.getArrowParticle() != null && user.getArrowParticle() == effect) shit.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
        if(user.getParticleArrowAvailable().contains(effect)) shit.setType(Material.ORANGE_DYE);
        return shit;
    }

    @EventHandler
    public void onPlayerShootsBow(EntityShootBowEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER) return;

        Player player = (Player) event.getEntity();
        user = FtcCore.getUser(player.getUniqueId());
        Particle activeArrowParticle = user.getArrowParticle();
        if(activeArrowParticle == null) return;

        double speed = 0;
        if (activeArrowParticle == Particle.FIREWORKS_SPARK) speed = 0.1;
        else if (activeArrowParticle == Particle.CAMPFIRE_COSY_SMOKE) speed = 0.005;
        else if (activeArrowParticle == Particle.SNOWBALL) speed = 0.1;

        addParticleToArrow(event.getProjectile(), activeArrowParticle, speed);
    }

    private void addParticleToArrow(Entity projectile, Particle particle, double speed) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(Cosmetics.plugin, () -> {
            projectile.getWorld().spawnParticle(particle, projectile.getLocation(), 1, 0, 0, 0, speed);
            if (!(projectile.isOnGround() || projectile.isDead())) addParticleToArrow(projectile, particle, speed);
        }, 1);

    }
}
