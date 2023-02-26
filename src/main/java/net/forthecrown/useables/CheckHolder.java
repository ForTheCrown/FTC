package net.forthecrown.useables;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.nbt.CompoundTag;
import org.bukkit.entity.Player;

/**
 * Provides a simple base for implementing the {@link CheckHolder} interface
 */
@Getter
@Setter
public abstract class CheckHolder implements UsageTypeHolder {

  /**
   * The NBT tag this holder uses
   */
  public static final String CHECKS_TAG = "checks";

  protected final UsageTypeList<UsageTest> checks
      = UsageTypeList.newTestList();

  /** Determines if fail messages will be sent to players or not */
  protected boolean silent = false;

  /**
   * Gets the first {@link UsageTest} that fails for the given player
   *
   * @param player The player to check
   * @return The failed check instance, null, if all passed
   */
  public UsageTest getFail(Player player) {
    for (var v : getChecks()) {
      // Test if we fail this instance's check
      // If we do, return it
      if (!v.test(player, this)) {
        return v;
      }
    }

    return null;
  }

  public boolean test(Player player) {
    return getFail(player) == null;
  }

  public boolean testInteraction(Player player) {
    // Get the test they failed on
    UsageTest failed;

    try {
       failed = getFail(player);
    } catch (Throwable t) {
      Loggers.getLogger().error("Couldn't run checkable tests!", t);
      return false;
    }

    // Null means they passed all
    if (failed == null) {
      // Call the onPass function for all checks
      for (var v : getChecks()) {
        v.postTests(player, this);
      }

      return true;
    }

    var message = failed.getFailMessage(player, this);

    // Silent -> Don't send message
    // Or message is null lol
    if (isSilent() || message == null) {
      return false;
    }

    player.sendMessage(message);
    return false;
  }

  /**
   * Saves checks into the given tag. This will add the {@link #CHECKS_TAG} entry to the given tag
   * and save all checks to that entry.
   * <p>
   * Also saves the {@link #isSilent()} into the given tag.
   *
   * @param tag The tag to save to
   */
  public void saveChecks(CompoundTag tag) {
    tag.putBoolean("silent", isSilent());

    // If the checks are empty, don't save
    if (getChecks().isEmpty()) {
      return;
    }

    tag.put(CHECKS_TAG, getChecks().save());
  }

  /**
   * Loads all checks from the given tag.
   * <p>
   * Loads {@link #isSilent()} from the given tag as well.
   *
   * @param tag The tag to load from
   * @throws CommandSyntaxException idk lol
   */
  public void loadChecks(CompoundTag tag) throws CommandSyntaxException {
    setSilent(tag.getBoolean("silent"));

    // Clear checks list, because we're re-reading
    // its contents
    getChecks().clear();

    // If there's no checks tag, just clear
    // the checks list
    if (!tag.contains(CHECKS_TAG)) {
      return;
    }

    // Load checks
    getChecks().load(tag.get(CHECKS_TAG));
  }
}