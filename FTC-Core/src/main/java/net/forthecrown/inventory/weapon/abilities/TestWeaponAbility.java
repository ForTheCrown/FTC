package net.forthecrown.inventory.weapon.abilities;

import net.forthecrown.core.Keys;
import net.forthecrown.inventory.weapon.AltAttackContext;
import net.forthecrown.inventory.weapon.WeaponUseContext;
import net.forthecrown.utils.ItemLoreBuilder;
import net.kyori.adventure.key.Key;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class TestWeaponAbility extends WeaponAbility {
    public static final Key KEY = Keys.forthecrown("test_weapon_ability");

    public static final Type TYPE = new Type() {
        @Override
        public WeaponAbility create() {
            return new TestWeaponAbility(1, true);
        }

        @Override
        public WeaponAbility load(CompoundTag data) {
            return new TestWeaponAbility(data);
        }

        @Override
        public @NotNull Key key() {
            return KEY;
        }
    };

    private final boolean extraData;

    public TestWeaponAbility(int level, boolean extraData) {
        super(TYPE, level);
        this.extraData = extraData;

    }

    public TestWeaponAbility(CompoundTag tag) {
        super(TYPE, tag);
        this.extraData = tag.getBoolean("extraData");
    }

    @Override
    public void onAttack(WeaponUseContext context) {

    }

    @Override
    public void onBlockAltAttack(AltAttackContext.c_Block context) {

    }

    @Override
    public void onEntityAltAttack(AltAttackContext.c_Entity context) {
    }

    @Override
    public void addInfo(ItemLoreBuilder builder) {
        builder.add("Test weapon info, rank: " + level);
    }
}
