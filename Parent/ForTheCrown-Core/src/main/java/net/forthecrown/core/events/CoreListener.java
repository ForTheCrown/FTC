package net.forthecrown.core.events;

import net.forthecrown.core.CrownWeapons;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.inventories.SellShop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class CoreListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event){
        UserManager.getUser(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeave(PlayerQuitEvent event){
        UserManager.getUser(event.getPlayer()).onLeave();
    }

    @EventHandler
    public void onServerShopNpcUse(PlayerInteractEntityEvent event){
        if(event.getHand() != EquipmentSlot.HAND) return;
        if(event.getRightClicked().getType() != EntityType.WANDERING_TRADER) return;
        LivingEntity trader = (LivingEntity) event.getRightClicked();

        if(trader.hasAI() && trader.getCustomName() == null || !trader.getCustomName().contains("Server Shop")) return;

        event.getPlayer().openInventory(new SellShop(event.getPlayer()).mainMenu());
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if(event.getBlock().getType() != Material.HOPPER) return;
        if(FtcCore.getHoppersInOneChunk() == -1) return;
        int hopperAmount = event.getBlock().getChunk().getTileEntities(block -> block.getType() == Material.HOPPER, true).size();
        if(hopperAmount <= FtcCore.getHoppersInOneChunk()) return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(Component.text("Too many hoppers (Max " + FtcCore.getHoppersInOneChunk() + ")").color(NamedTextColor.RED));
    }

    //Entity death by crown weapon
    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if(event.getEntity().getKiller() == null) return;
        Player player = event.getEntity().getKiller();
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item == null) return;

        if(!CrownWeapons.isLegacyWeapon(item) && !CrownWeapons.isCrownWeapon(item)) return;
        EntityDamageEvent event2 = event.getEntity().getLastDamageCause();
        if (!(event2.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK) || event2.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK))) return;

        CrownWeapons.CrownWeapon weapon = CrownWeapons.fromItem(item);

        if(weapon.getTarget() == EntityType.AREA_EFFECT_CLOUD){
            if(!(event.getEntity() instanceof Creeper)) return;
            if(!((Creeper) event.getEntity()).isPowered()) return;
        } else if(event.getEntity().getType() != weapon.getTarget()) return;

        short pog = (short) (weapon.getProgress() + 1);
        if(pog >= weapon.getGoal()) CrownWeapons.upgradeLevel(weapon, player);
        else {
            weapon.setProgress(pog);
            weapon.update();
        }
    }
}
