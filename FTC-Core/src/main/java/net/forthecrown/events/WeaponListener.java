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
import org.bukkit.inventory.EquipmentSlot;
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
                    history.clicks.add(0, Click.LEFT);

                    if(!ComVars.allowNonOwnerSwords() && !sword.getOwner().equals(damager.getUniqueId())) return;
                    event.setDamage(sword.damage(damager, event, history));
                }
        );
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getHand() == EquipmentSlot.OFF_HAND) return;

        consumeSword(
                event.getItem(),

                sword -> {
                    ClickHistory history = getHistory(event.getPlayer());
                    history.clicks.add(0, Click.RIGHT);

                    WeaponAbility ability = sword.getAbility();
                    if(ability != null) {
                        AltAttackContext.c_Block block = new AltAttackContext.c_Block(event, sword, history);

                        ability.onAltAttack(block);
                        ability.onBlockAltAttack(block);
                    }
                }
        );
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if(event.getHand() == EquipmentSlot.OFF_HAND) return;

        consumeSword(
                event.getPlayer().getInventory().getItemInMainHand(),
                sword -> {
                    ClickHistory history = getHistory(event.getPlayer());
                    history.clicks.add(0, Click.RIGHT);

                    WeaponAbility ability = sword.getAbility();
                    if(ability != null) {
                        AltAttackContext.c_Entity entity = new AltAttackContext.c_Entity(event, sword, history);

                        ability.onAltAttack(entity);
                        ability.onEntityAltAttack(entity);
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
