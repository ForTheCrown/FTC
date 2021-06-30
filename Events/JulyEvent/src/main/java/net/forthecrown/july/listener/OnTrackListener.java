package net.forthecrown.july.listener;

import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.core.crownevents.InEventListener;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.core.user.UserManager;
import net.forthecrown.core.utils.Cooldown;
import net.forthecrown.core.utils.CrownBoundingBox;
import net.forthecrown.core.utils.ListUtils;
import net.forthecrown.july.EventConstants;
import net.forthecrown.july.JulyMain;
import net.forthecrown.july.ParkourEntry;
import net.forthecrown.july.effects.BlockEffects;
import net.forthecrown.july.items.GemItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.List;

import static net.forthecrown.july.EventConstants.*;

public class OnTrackListener extends InEventListener<ParkourEntry> implements Listener {

    private final Player player;
    private final boolean practise;

    public List<CrownBoundingBox> loopTriggers;
    public Location elytraFallbackLoc;
    public Location start;

    public CrownBoundingBox elytraFailZone;
    public CrownBoundingBox elytraLoops;
    public CrownBoundingBox elytraFallbackAllowTrigger;

    public CrownBoundingBox finish;
    public CrownBoundingBox track;

    public CrownBoundingBox checkPoint2;
    public Location checkPoint2Fallback;
    public boolean reachedCheckpoint;

    public boolean elytraFallbackAllowed;

    public boolean[] finishedLoops;
    public List<Item> items;

    public OnTrackListener(Player player, boolean practise) {
        this.player = player;
        this.practise = practise;
    }

    public void calculateOffsets(Location minLoc){
        loopTriggers = ListUtils.convertToList(ELYTRA_TRIGGERS, offset -> offset.apply(minLoc));

        elytraFallbackLoc = ELYTRA_FALLBACK_OFFSET.apply(minLoc).toCenterLocation();
        elytraFailZone = ELYTRA_FAIL.apply(minLoc);
        elytraLoops = ELYTRA_LOOPS.apply(minLoc);
        elytraFallbackAllowTrigger = ELYTRA_ALLOW.apply(minLoc);

        elytraFallbackAllowed = false;
        finishedLoops = new boolean[ELYTRA_TRIGGERS.size()];

        finish = END_REGION.apply(minLoc);
        track = REL_REGION.apply(minLoc);
        start = START_OFFSET.apply(minLoc).toCenterLocation();

        checkPoint2 = CHECKPOINT_2.apply(minLoc);
        checkPoint2Fallback = CHECK_2_FALLBACK.apply(minLoc);
        reachedCheckpoint = false;

        resetTrack();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if(isNotPlayer(event.getPlayer())) return;

        if(player.getLocation().getBlockY() <= 0){
            end();
            return;
        }

        if(elytraFallbackAllowed && elytraFailZone.contains(player.getLocation().toVector())) player.teleport(elytraFallbackLoc);
        if(elytraFallbackAllowTrigger.contains(player)) elytraFallbackAllowed = true;
        if(checkPoint2.contains(player)) reachedCheckpoint = true;
        if(loopTriggers != null) checkElytraCollision();

        checkOnTrack();
        checkInEnd();

        BlockEffects.attemptExecution(player, standingOn());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if(isNotPlayer(event.getEntity())) return;
        if(event.getCause() == EntityDamageEvent.DamageCause.VOID) return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if(isNotPlayer(event.getEntity())) return;

        Item itemEntity = event.getItem();
        ItemStack item = itemEntity.getItemStack();
        if(!GemItems.isGem(item)) return;

        if(!GemItems.mayPickUp(itemEntity, player)){
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        event.getItem().remove();

        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);

        CrownUser user = UserManager.getUser(player);
        int worth = GemItems.getWorth(item);

        if(worth == EventConstants.GEM_SECRET_VALUE && !Cooldown.contains(player, "Event_GemCooldown_Secret")){
            Cooldown.add(player, "Event_GemCooldown_Secret", 72000);
        }

        if(!Cooldown.contains(player, "Event_GemCooldown")){
            Cooldown.add(player, "Event_GemCooldown", 72000);
        }

        user.getPlayer().sendMessage(
                Component.text("You got ")
                        .color(NamedTextColor.YELLOW)
                        .append(ChatFormatter.queryGems(worth).color(NamedTextColor.GOLD))
        );
        user.addGems(worth);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(isNotPlayer(event.getEntity())) return;

        event.setCancelled(true);
        Bukkit.getScheduler().runTaskLater(JulyMain.inst, this::end, 1);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if(isNotPlayer(event.getPlayer())) return;

        end();
    }

    public boolean isNotPlayer(Entity plr){
        return !player.equals(plr);
    }

    public Material standingOn(){
        return player.getLocation().subtract(0, 1, 0).getBlock().getType();
    }

    public void checkElytraCollision(){
        if(practise) return;
        int index = -1;

        for (CrownBoundingBox b: loopTriggers){
            index++;
            if(finishedLoops[index]) continue;
            if(!b.contains(player)) continue;

            World world = b.getWorld();

            world.spawn(b.getCenterLocation(), Firework.class, firework -> {
                FireworkMeta meta = firework.getFireworkMeta();
                meta.addEffect(COOL_EFFECT);
                firework.setFireworkMeta(meta);

                firework.detonate();
            });

            b.getBlocks(block -> block.getType() != Material.AIR)
                    .forEach(b1 -> {
                        if(b1.getType() == Material.YELLOW_WOOL) b1.setType(Material.LIME_WOOL);
                        else b1.setType(Material.GREEN_WOOL);
                    });
            finishedLoops[index] = true;
        }
    }

    public boolean canComplete(){
        for (boolean b: finishedLoops) if(!b) return false;
        return true;
    }

    public void checkOnTrack(){
        if(track.contains(player)) return;

        if(elytraFallbackAllowed) player.teleport(elytraFallbackLoc);
        else if(reachedCheckpoint) player.teleport(checkPoint2Fallback);
        else player.teleport(start);
    }

    public void checkInEnd(){
        if(!finish.contains(player)) return;

        if(practise){
            end();
            return;
        }

        if(!canComplete()){
            if(Cooldown.contains(player, "Event_EndMessage")) return;

            player.sendMessage(Component.text("Cannot complete yet (Didn't fly through all rings)").color(NamedTextColor.GRAY));
            Cooldown.add(player, "Event_EndMessage", 20*10);

            return;
        }

        JulyMain.event.complete(entry);
    }

    public void loopsToDefault(){
        if(practise) return;

        for (Block b: elytraLoops){
            if(b.getType() == Material.AIR) continue;

            if(b.getType() == Material.LIME_WOOL) b.setType(Material.YELLOW_WOOL);
            else if(b.getType() == Material.GREEN_WOOL) b.setType(Material.MAGENTA_WOOL);
        }
    }

    public void killGems(){
        track.getEntitiesByType(Item.class).forEach(Entity::remove);

        if(items == null || items.isEmpty()) return;
        items.forEach(Entity::remove);
    }

    public void resetTrack(){
        loopsToDefault();
        killGems();
    }

    public void end(){
        if(practise) JulyMain.event.endPractise(player);
        else JulyMain.event.end(entry);
    }
}
