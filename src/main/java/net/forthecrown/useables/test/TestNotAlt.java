package net.forthecrown.useables.test;

import net.forthecrown.useables.*;
import net.forthecrown.user.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.Tag;
import org.bukkit.entity.Player;

public class TestNotAlt extends UsageTest {
    // --- TYPE ---
    public static final UsageType<TestNotAlt> TYPE = UsageType.of(TestNotAlt.class);

    @UsableConstructor(ConstructType.EMPTY)
    public TestNotAlt() {
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
        return !UserManager.get().getAlts().isAlt(player.getUniqueId());
    }

    @Override
    public Component getFailMessage(Player player, CheckHolder holder) {
        return Component.text("Alt accounts may not use this", NamedTextColor.GRAY);
    }
}