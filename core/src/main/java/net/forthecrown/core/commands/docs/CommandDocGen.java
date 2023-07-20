package net.forthecrown.core.commands.docs;

import java.io.IOException;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.utils.io.PathUtil;
import net.kyori.adventure.text.Component;

public class CommandDocGen extends FtcCommand {

  public CommandDocGen() {
    super("DocGen");

    setDescription("Generates documentation of all FTC commands");
    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(literal("singleton")
            .executes(c -> {
              CommandDocs docs = new CommandDocs(false);
              docs.fill();

              try {
                docs.write(PathUtil.pluginPath("command_docs.md"));
              } catch (IOException exc) {
                throw Exceptions.create("Error generating command docs");
              }

              c.getSource().sendSuccess(
                  Component.text("Generated singleton command doc file")
              );
              return 0;
            })
        )

        .then(literal("separated")
            .executes(c -> {
              CommandDocs docs = new CommandDocs(false);
              docs.fill();

              try {
                docs.writeSeparated(PathUtil.pluginPath("command_docs"));
              } catch (IOException exc) {
                throw Exceptions.create("Error generating command docs");
              }

              c.getSource().sendSuccess(
                  Component.text("Generated separated command doc files")
              );
              return 0;
            })
        );
  }
}