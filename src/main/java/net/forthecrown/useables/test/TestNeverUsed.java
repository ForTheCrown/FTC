package net.forthecrown.useables.test;

import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.ByteTag;
import net.forthecrown.useables.CheckHolder;
import net.forthecrown.useables.ConstructType;
import net.forthecrown.useables.UsableConstructor;
import net.forthecrown.useables.UsageTest;
import net.forthecrown.useables.UsageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
  public BinaryTag save() {
    return BinaryTags.byteTag(used ? 1 : 0);
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
  public static TestNeverUsed load(BinaryTag tag) {
    return new TestNeverUsed(((ByteTag) tag).byteValue() != 0);
  }
}