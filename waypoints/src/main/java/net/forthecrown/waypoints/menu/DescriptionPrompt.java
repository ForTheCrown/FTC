package net.forthecrown.waypoints.menu;

import static net.forthecrown.waypoints.menu.EditMenu.ensureValid;
import static net.kyori.adventure.text.Component.text;

import com.google.common.base.Strings;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.antigrief.BannedWords;
import net.forthecrown.command.Exceptions;
import net.forthecrown.text.PlayerMessage;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.utils.Tasks;
import net.forthecrown.waypoints.Waypoint;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DescriptionPrompt extends StringPrompt {

  private final User user;
  private final Waypoint waypoint;

  BukkitTask task;

  public DescriptionPrompt(User user, Waypoint waypoint) {
    this.user = user;
    this.waypoint = waypoint;
  }

  @Override
  public @NotNull String getPromptText(@NotNull ConversationContext context) {
    // I hate using legacy systems like this
    return "Type the waypoint's new description into chat "
        + ChatColor.GRAY + "(Use 'clear' to clear the description)";
  }

  @Override
  public @Nullable Prompt acceptInput(
      @NotNull ConversationContext context,
      @Nullable String input
  ) {
    try {
      setDescription(input);
    } catch (CommandSyntaxException exc) {
      Exceptions.handleSyntaxException(user, exc);
    }

    return Prompt.END_OF_CONVERSATION;
  }

  private void setDescription(String desc)
      throws CommandSyntaxException
  {
    ensureValid(waypoint);

    if (Strings.isNullOrEmpty(desc) || desc.equalsIgnoreCase("clear")) {
      waypoint.setDescription(null);
      user.sendMessage(text("Cleared description", NamedTextColor.YELLOW));
      return;
    }

    desc = desc.replace("\\n", "\n");

    if (BannedWords.checkAndWarn(user.getPlayer(), desc)) {
      return;
    }

    PlayerMessage message = PlayerMessage.of(desc, user);
    waypoint.setDescription(message);

    user.sendMessage(
        Text.vformat("Set waypoint description to\n&f{0}",
            NamedTextColor.GRAY,
            message
        )
    );

    WaypointMenus.open(WaypointMenus.EDIT_MENU, user, waypoint);
    Tasks.cancel(task);
  }
}
