package net.forthecrown.useables.test;

import net.forthecrown.useables.CheckHolder;
import net.forthecrown.useables.ConstructType;
import net.forthecrown.useables.UsableConstructor;
import net.forthecrown.useables.UsageTest;
import net.forthecrown.useables.UsageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.Tag;
import org.bukkit.entity.Player;

public class TestNoPassangers extends UsageTest {
    // --- TYPE ---
    public static final UsageType<TestNoPassangers> TYPE = UsageType.of(TestNoPassangers.class);

    @UsableConstructor(ConstructType.EMPTY)
    public TestNoPassangers() {
        super(TYPE);
    }

    @Override
    public Component displayInfo() {
        return null;
    }

    @Override
    public Tag save() {
        return null;
    }

    @Override
    public boolean test(Player player, CheckHolder holder) {
        return player.getPassengers().isEmpty();
    }

    @Override
    public Component getFailMessage(Player player, CheckHolder holder) {
        return Component.text("Cannot have anyone riding you", NamedTextColor.GRAY);
    }
}