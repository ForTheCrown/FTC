package net.forthecrown.inventory.weapon.abilities;

import net.forthecrown.core.Keys;
import net.forthecrown.inventory.weapon.WeaponUseContext;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class PoofWeaponAbility implements WeaponAbility {
    public static final Key KEY = Keys.ftc("ability_poof");

    @Override
    public void onWeaponUse(WeaponUseContext context) {

    }

    @Override
    public void onAltAttack(PlayerInteractEvent event) {
    }

    @Override
    public void onEntityAltAttack(PlayerInteractEntityEvent event) {

    }

    @Override
    public Component loreDisplay() {
        return Component.text("Poof");
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }
}
