package net.forthecrown.events;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.core.ComVars;
import net.forthecrown.inventory.weapon.AltAttackContext;
import net.forthecrown.inventory.weapon.RoyalSword;
import net.forthecrown.inventory.weapon.RoyalWeapons;
import net.forthecrown.inventory.weapon.abilities.WeaponAbility;
import net.forthecrown.inventory.weapon.click.Click;
import net.forthecrown.inventory.weapon.click.ClickHistory;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class WeaponListener implements Listener {
    public static final Map<UUID, ClickHistory> CLICK_HISTORIES = new Object2ObjectOpenHashMap<>();

    public WeaponListener() {

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(!(event.getDamager() instanceof Player damager)) return;
        if(!(event.getEntity() instanceof LivingEntity)) return;
        if(event.getEntity() instanceof ArmorStand) return;

        consumeSword(
                damager.getInventory().getItemInMainHand(),
                sword -> {
                    ClickHistory history = getHistory(damager);
                    history.clicks.add(Click.LEFT);

                    if(!ComVars.allowNonOwnerSwords() && !sword.getOwner().equals(damager.getUniqueId())) return;
                    sword.damage(damager, event, history);
                }
        );
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        consumeSword(
                event.getItem(),

                sword -> {
                    ClickHistory history = getHistory(event.getPlayer());
                    history.clicks.add(Click.RIGHT);

                    WeaponAbility ability = sword.getAbility();
                    if(ability != null) {
                        AltAttackContext.c_Block block = new AltAttackContext.c_Block(event, sword, history);

                        ability.onBlockAltAttack(block);
                        ability.onAltAttack(block);
                    }
                }
        );
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        consumeSword(
                event.getPlayer().getInventory().getItemInMainHand(),
                sword -> {
                    ClickHistory history = getHistory(event.getPlayer());
                    history.clicks.add(Click.RIGHT);

                    WeaponAbility ability = sword.getAbility();
                    if(ability != null) {
                        AltAttackContext.c_Entity entity = new AltAttackContext.c_Entity(event, sword, history);

                        ability.onEntityAltAttack(entity);
                        ability.onAltAttack(entity);
                    }
                }
        );
    }

    void consumeSword(ItemStack item, Consumer<RoyalSword> swordConsumer) {
        if(!RoyalWeapons.isRoyalSword(item)) return;

        RoyalSword sword = new RoyalSword(item);
        swordConsumer.accept(sword);
    }

    ClickHistory getHistory(Player player) {
        ClickHistory history = CLICK_HISTORIES.computeIfAbsent(player.getUniqueId(), ClickHistory::new);
        history.taskLogic();

        return history;
    }
}
