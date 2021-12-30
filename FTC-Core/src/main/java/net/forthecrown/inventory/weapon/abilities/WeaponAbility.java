package net.forthecrown.inventory.weapon.abilities;

import net.forthecrown.core.Keys;
import net.forthecrown.inventory.weapon.AltAttackContext;
import net.forthecrown.inventory.weapon.WeaponUseContext;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public abstract class WeaponAbility implements Keyed {
    private final Key key;
    private final String name;

    WeaponAbility(String name) {
        this.name = name;
        this.key = Keys.forthecrown(
                name.toLowerCase().replaceAll(" ", "_")
        );
    }

    public abstract void onWeaponUse(WeaponUseContext context);
    public void onAltAttack(AltAttackContext context) {}
    public abstract void onBlockAltAttack(AltAttackContext.c_Block context);
    public abstract void onEntityAltAttack(AltAttackContext.c_Entity context);

    public Component loreDisplay() {
        return Component.text(name);
    }

    @Override
    public @NotNull Key key() {
        return key;
    }
}
