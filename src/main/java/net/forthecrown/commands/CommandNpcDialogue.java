package net.forthecrown.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.utils.dialogue.DialogueManager;

public class CommandNpcDialogue extends FtcCommand {

  public CommandNpcDialogue() {
    super(DialogueManager.COMMAND_NAME);

    setDescription("Secret command... so sush!");
    setPermission(Permissions.DEFAULT);

    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command.then(argument("input", StringArgumentType.greedyString())
        .executes(c -> {
          String input = c.getArgument("input", String.class);
          var convos = DialogueManager.getDialogues();
          convos.run(getUserSender(c), new StringReader(input));
          return 0;
        })
    );
  }
}