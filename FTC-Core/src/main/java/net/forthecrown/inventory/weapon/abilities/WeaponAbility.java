package net.forthecrown.inventory.weapon.abilities;

import net.forthecrown.inventory.weapon.AltAttackContext;
import net.forthecrown.inventory.weapon.WeaponUseContext;
import net.forthecrown.utils.ItemLoreBuilder;
import net.kyori.adventure.key.Keyed;
import net.minecraft.nbt.CompoundTag;

public abstract class WeaponAbility {
    private final Type type;
    protected int level;

    public WeaponAbility(Type type, int level) {
        this.type = type;
        this.level = level;
    }

    public WeaponAbility(Type type, CompoundTag tag) {
        this.type = type;
        setLevel(tag.getInt("level"));
    }

    public void onAltAttack(AltAttackContext context) {}

    public abstract void onAttack(WeaponUseContext context);
    public abstract void onBlockAltAttack(AltAttackContext.c_Block context);
    public abstract void onEntityAltAttack(AltAttackContext.c_Entity context);

    public void save(CompoundTag tag) {
        tag.putInt("level", level);
    }

    public Type getType() {
        return type;
    }

    public abstract void addInfo(ItemLoreBuilder builder);

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public interface Type extends Keyed {
        WeaponAbility create();
        WeaponAbility load(CompoundTag data);
    }
}
