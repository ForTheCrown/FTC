package net.forthecrown.inventory.weapon.abilities;

import net.forthecrown.inventory.weapon.WeaponUseContext;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public interface WeaponAbility extends Keyed {
    void onWeaponUse(WeaponUseContext context);

    void onAltAttack(PlayerInteractEvent event);
    void onEntityAltAttack(PlayerInteractEntityEvent event);

    Component loreDisplay();
}
