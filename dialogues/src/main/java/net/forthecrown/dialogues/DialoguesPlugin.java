package net.forthecrown.dialogues;

import lombok.Getter;
import net.forthecrown.dialogues.commands.CommandDialogue;
import net.forthecrown.dialogues.commands.DialogueArgument;
import net.forthecrown.usables.UsablesPlugin;
import net.forthecrown.utils.PluginUtil;
import org.bukkit.plugin.java.JavaPlugin;

public class DialoguesPlugin extends JavaPlugin {

  @Getter
  private DialogueManager manager;

  @Override
  public void onEnable() {
    manager = new DialogueManager();
    manager.load();

    DialogueArgument.setInstance(new DialogueArgument(this));

    new CommandDialogue(this);

    if (PluginUtil.isEnabled("FTC-Usables")) {
      var actions = UsablesPlugin.get().getActions();
      actions.register("dialogue", DialogueAction.TYPE);
    }
  }
}
