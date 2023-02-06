package net.forthecrown.useables;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.forthecrown.utils.text.writer.TextWriter;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.Player;

/**
 * Provides a simple way of implementing the {@link Usable} interface
 */
public abstract class Usable extends CheckHolder {

  /**
   * The NBT tag actions are saved to
   */
  public static final String ACTIONS_TAG = "actions";

  @Getter
  protected final UsageTypeList<UsageAction> actions
      = UsageTypeList.newActionList();

  public void save(CompoundTag tag) {
    saveActions(tag);
    saveChecks(tag);
  }

  public void load(CompoundTag tag) {
    try {
      loadActions(tag);
      loadChecks(tag);
    } catch (CommandSyntaxException e) {
      e.printStackTrace();
    }
  }

  /**
   * Writes admin info about this usable into the given writer
   *
   * @param writer The writer to write to
   */
  public void adminInfo(TextWriter writer) {
    writer.field("Silent", isSilent());

    writer.field("Actions", "");
    getActions().write(writer);

    writer.field("Tests", "");
    getChecks().write(writer);
  }

  /**
   * Runs the interaction check and, if those pass, runs the usage actions of this usable
   *
   * @param player The player interacting with this usable
   * @return True, if all checks passed, false otherwise
   */
  public boolean interact(Player player) {
    if (!testInteraction(player)) {
      return false;
    }

    onInteract(player);
    return true;
  }

  /**
   * Runs all usage actions on the given player
   *
   * @param player The player to actions on
   */
  public void onInteract(Player player) {
    for (var a : getActions()) {
      a.onUse(player, this);
    }
  }

  /**
   * Saves all actions into the given tag
   *
   * @param tag The tag to save actions to
   */
  public void saveActions(CompoundTag tag) {
    // Don't serialize an empty list
    if (getActions().isEmpty()) {
      return;
    }

    tag.put(ACTIONS_TAG, getActions().save());
  }

  /**
   * Loads all actions from the given tag
   *
   * @param tag The tag to laod from
   * @throws CommandSyntaxException idk lmao
   */
  public void loadActions(CompoundTag tag) throws CommandSyntaxException {
    // Clear checks list, because we're re-reading
    // its contents
    getActions().clear();

    // No actions tag? Don't load the list
    if (!tag.contains(ACTIONS_TAG)) {
      return;
    }

    // Load list with usage action registry
    getActions().load(tag.get(ACTIONS_TAG));
  }
}