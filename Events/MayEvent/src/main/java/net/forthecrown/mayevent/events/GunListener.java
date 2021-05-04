package net.forthecrown.mayevent.events;

import com.destroystokyo.paper.ParticleBuilder;
import net.forthecrown.core.nbt.NBT;
import net.forthecrown.core.nbt.NbtGetter;
import net.forthecrown.mayevent.ArenaEntry;
import net.forthecrown.mayevent.MayMain;
import net.forthecrown.mayevent.MayUtils;
import net.forthecrown.mayevent.guns.*;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.ChatMessageType;
import net.minecraft.server.v1_16_R3.EnumChatFormat;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class GunListener implements Listener {

    private final Player player;
    private final ArenaEntry entry;
    public GunListener(Player player, ArenaEntry entry){
        this.player = player;
        this.entry = entry;
    }

    @EventHandler
    public void onItemPickup(PlayerAttemptPickupItemEvent event) {
        if(!event.getPlayer().equals(player)) return;

        ItemStack item = event.getItem().getItemStack();
        NBT nbt = NbtGetter.ofItemTags(item);

        //Picked up gun
        if(nbt.has("gun")){
            Class<? extends HitScanWeapon> gun = classFromName(nbt.getString("gun"));
            if(gun == null) return;

            HitScanWeapon weapon;
            try {
                weapon = gun.getDeclaredConstructor().newInstance();
            } catch (Exception e){
                return;
            }

            entry.pickUp(weapon);
            return;
        }

        //Picked up ammo
        if(nbt.has("gunPickup")) {
            event.setCancelled(true);

            HitScanWeapon weapon = entry.guns.get(nbt.getString("gunPickup"));
            if(weapon == null) return;
            if(weapon.remainingAmmo() == weapon.maxAmmo()) return;

            weapon.pickupAmmo(item.getAmount());
            event.getItem().remove();
            entry.user().sendMessage(new ChatComponentText("Picked up ammo for " + weapon.name()).a(EnumChatFormat.BOLD), ChatMessageType.GAME_INFO);
            entry.player().playSound(entry.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
            return;
        }

        //Picked up gems
        if(nbt.has("gems")){
            event.setCancelled(true);
            event.getItem().remove();

            MayMain.eLogger.logAction(player, "Collected 500 gems");
            entry.user().setGems(entry.user().getGems() + 500);
            entry.user().sendMessage(new ChatComponentText("Picked up 500 gems").a(EnumChatFormat.GOLD));
            player.playSound(entry.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
        }
    }

    @EventHandler
    public void onWeaponUse(PlayerInteractEvent event) {
        if(!event.getPlayer().equals(player)) return;

        if(player.getInventory().getItemInMainHand() != null && player.getInventory().getItemInMainHand().getType() == Material.FIRE_CHARGE){
            event.setCancelled(true);
            Location loc = player.getEyeLocation().add(player.getEyeLocation().getDirection()).add(player.getVelocity());
            Fireball ball = MayUtils.spawn(loc, Fireball.class, fireball -> fireball.setDirection(player.getEyeLocation().getDirection()));
            new ListenerRocketTracker(ball);

            player.getInventory().getItemInMainHand().subtract(1);
        }

        if(entry.fireGun()) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        if(!event.getPlayer().equals(player)) return;
        entry.sendGunMessage();
    }

    public Class<? extends HitScanWeapon> classFromName(String name){
        switch (name){
            case "Rocket Launcher": return RocketLauncher.class;
            case "Assault Rifle": return StandardRifle.class;
            case "Shotgun": return TwelveGaugeShotgun.class;
            default: return null;
        }
    }

    public static class ListenerRocketTracker extends BukkitRunnable{

        private final Fireball fireball;

        public ListenerRocketTracker(Fireball fireball){
            this.fireball = fireball;
            runTaskTimer(MayMain.inst, 2, 2);
        }

        @Override
        public void run() {
            if(!fireball.isDead()) return;

            Location loc = fireball.getLocation();
            new ParticleBuilder(Particle.EXPLOSION_HUGE).count(5).extra(5).location(loc).allPlayers().spawn();
            loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);

            MayUtils.attemptDestruction(fireball.getLocation(), 7);
            MayUtils.damageInRadius(fireball.getLocation(), 6, 6);
            cancel();
        }
    }
}
