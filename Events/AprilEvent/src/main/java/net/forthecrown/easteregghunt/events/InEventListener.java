package net.forthecrown.easteregghunt.events;

import net.forthecrown.core.utils.ComponentUtils;
import net.forthecrown.core.utils.Cooldown;
import net.forthecrown.easteregghunt.CrazyBunny;
import net.forthecrown.easteregghunt.EasterEntry;
import net.forthecrown.easteregghunt.EasterEvent;
import net.forthecrown.easteregghunt.EasterMain;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Score;

public class InEventListener implements Listener {

    public EasterEntry entry;
    public EasterEvent event;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if(!event.getPlayer().equals(entry.player())) return;
        if(!event.getRightClicked().getPersistentDataContainer().has(EasterMain.spawner.key, PersistentDataType.BYTE)) return;
        if(Cooldown.contains(entry.player())) return;
        Cooldown.add(entry.player(), 10);

        Player player = entry.player();
        Entity entity = event.getRightClicked();
        entry.inc();
        EasterMain.spawner.removeEgg((Slime) entity);
        player.getWorld().playSound(entity.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1);
        player.getWorld().spawnParticle(Particle.END_ROD, entity.getLocation(), 50, 0.1, 0, 0.1, 0.1);

        if(entry.score() <= this.event.initialAmount()/2) return;

        placeThreeEggs(entity.getLocation());
        CrazyBunny bunny = EasterMain.bunny;
        if(bunny.isAlive()) return;

        bunny.spawn(player.getLocation());
        player.sendMessage(ChatColor.GRAY + "The easter bunny has spawned!");
    }

    private void placeThreeEggs(Location location){
        for (int i = 0; i < 3; i++){
            EasterMain.spawner.placeRandomEgg(location);
        }
    }

    private void checkEntity(Entity entity){
        if(!entity.equals(entry.player())) return;
        event.end(entry);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        checkEntity(event.getEntity());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        checkEntity(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if(!event.getEntity().equals(EasterMain.bunny.getEntity())) return;
        event.getDrops().clear();
        if(event.getEntity().getKiller() == null) return;
        entry.player().sendMessage(ComponentUtils.convertString("&7You received &e100 points&7 for killing the Bunny!"));
        Score score = EasterEvent.CROWN.getScore(entry.player().getName());
        score.setScore((score.isScoreSet() ? score.getScore() : 0) + 100);
    }
}
