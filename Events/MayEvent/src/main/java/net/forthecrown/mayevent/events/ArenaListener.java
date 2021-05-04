package net.forthecrown.mayevent.events;

import net.forthecrown.core.crownevents.InEventListener;
import net.forthecrown.mayevent.ArenaEntry;
import net.forthecrown.mayevent.MayMain;
import net.forthecrown.mayevent.MayUtils;
import net.forthecrown.mayevent.arena.EventArena;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ArenaListener extends InEventListener<ArenaEntry> {

    public EventArena arena;

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if(!arena.box.contains(event.getEntity())) return;

        if(event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent){
            EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();
            if(damageEvent.getDamager().equals(entry.player())){
                entry.player().sendMessage(
                        Component.text("Earned ")
                                .color(NamedTextColor.GRAY)
                                .append(Component.text("25 Gems").color(NamedTextColor.YELLOW))
                                .append(Component.text(" for melee kill"))
                );
                entry.user().setGems(entry.user().getGems() + 25);
            } else if(damageEvent.getDamager() instanceof Arrow && ((Arrow) damageEvent.getDamager()).getShooter() instanceof Player){
                entry.player().sendMessage(
                        Component.text("Earned ")
                                .color(NamedTextColor.GRAY)
                                .append(Component.text("10 Gems").color(NamedTextColor.YELLOW))
                                .append(Component.text(" for arrow kill"))
                );
                entry.user().setGems(entry.user().getGems() + 10);
            }
        }

        event.getDrops().clear();
        MayUtils.spawn(event.getEntity().getLocation(), ExperienceOrb.class, orb -> orb.setExperience(10));

        arena.currentMobAmount--;
        arena.updateBossbar();

        arena.checkBossbar();
        arena.checkHighlighting();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(!entry.player().equals(event.getEntity())) return;

        event.setCancelled(true);
        if(event.deathMessage() != null) Bukkit.getServer().sendMessage(event.deathMessage());
        MayMain.event.complete(entry);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if(!entry.player().equals(event.getPlayer())) return;

        MayMain.event.end(entry);
    }
}
