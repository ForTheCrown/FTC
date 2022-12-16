package net.forthecrown.useables.test;

import net.forthecrown.useables.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.Tag;
import org.bukkit.entity.Player;

public class TestNeverUsed extends UsageTest {
    // --- TYPE ---
    public static final UsageType<TestNeverUsed> TYPE = UsageType.of(TestNeverUsed.class);

    private boolean used;

    public TestNeverUsed(boolean used) {
        super(TYPE);
        this.used = used;
    }

    @Override
    public Component displayInfo() {
        return Component.text("used=" + used);
    }

    @Override
    public Tag save() {
        return ByteTag.valueOf(used);
    }

    @Override
    public boolean test(Player player, CheckHolder holder) {
        return !used;
    }

    @Override
    public void postTests(Player player, CheckHolder holder) {
        used = true;
    }

    @Override
    public Component getFailMessage(Player player, CheckHolder holder) {
        return Component.text("Only 1 person may ever use this", NamedTextColor.GRAY);
    }

    // --- TYPE CONSTRUCTORS ---

    @UsableConstructor(ConstructType.EMPTY)
    public static TestNeverUsed create() {
        return new TestNeverUsed(false);
    }

    @UsableConstructor(ConstructType.TAG)
    public static TestNeverUsed load(Tag tag) {
        return new TestNeverUsed(((ByteTag) tag).getAsByte() != 0);
    }
}