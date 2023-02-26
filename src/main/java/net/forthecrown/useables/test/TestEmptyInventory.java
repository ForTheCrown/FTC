package net.forthecrown.useables.test;

import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.useables.CheckHolder;
import net.forthecrown.useables.ConstructType;
import net.forthecrown.useables.UsableConstructor;
import net.forthecrown.useables.UsageTest;
import net.forthecrown.useables.UsageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class TestEmptyInventory extends UsageTest {

  // --- TYPE ---
  public static final UsageType<TestEmptyInventory> TYPE = UsageType.of(TestEmptyInventory.class)
      .requiresInput(false);

  @UsableConstructor(ConstructType.EMPTY)
  public TestEmptyInventory() {
    super(TYPE);
  }

  @Override
  public @Nullable Component displayInfo() {
    return null;
  }

  @Override
  public @Nullable BinaryTag save() {
    return null;
  }

  @Override
  public boolean test(Player player, CheckHolder holder) {
    return player.getInventory().isEmpty();
  }

  @Override
  public @Nullable Component getFailMessage(Player player, CheckHolder holder) {
    return Component.text("Your inventory must be empty", NamedTextColor.GRAY);
  }
}