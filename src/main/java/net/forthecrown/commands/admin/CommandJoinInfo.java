package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.config.JoinInfo;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;


public class CommandJoinInfo extends FtcCommand {

  public CommandJoinInfo() {
    super("JoinInfo");

    setPermission(Permissions.ADMIN);
    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /JoinInfo
   *
   * Permissions used:
   *
   * Main Author:
   */

  @Override
  protected void createCommand(BrigadierCommand command) {
    command
        .then(literal("view")
            .executes(c -> {
              c.getSource().sendMessage(JoinInfo.display());
              return 0;
            })
        )

        .then(literal("should_show")
            .executes(c -> {
              c.getSource().sendMessage(
                  Component.text("Should show join info: ")
                      .append(Component.text(JoinInfo.info.visible))
              );
              return 0;
            })

            .then(argument("shouldShow", BoolArgumentType.bool())
                .executes(c -> {
                  boolean bool = c.getArgument("shouldShow", Boolean.class);
                  JoinInfo.info.visible = bool;

                  c.getSource().sendAdmin(
                      Component.text("Set should show join message: ")
                          .append(Component.text(bool))
                  );
                  return 0;
                })
            )
        )

        .then(literal("should_show_end")
            .executes(c -> {
              c.getSource().sendMessage(
                  Component.text("Should show join end info: ")
                      .append(Component.text(JoinInfo.endInfo.visible))
              );
              return 0;
            })

            .then(argument("shouldShow", BoolArgumentType.bool())
                .executes(c -> {
                  boolean bool = c.getArgument("shouldShow", Boolean.class);
                  JoinInfo.endInfo.visible = bool;

                  c.getSource().sendAdmin(
                      Component.text("Set should show join end message: ")
                          .append(Component.text(bool))
                  );
                  return 0;
                })
            )
        )

        .then(literal("set")
            .then(argument("component", Arguments.CHAT)
                .executes(c -> {
                  Component component = c.getArgument("component", Component.class);
                  JoinInfo.info.text = component;

                  c.getSource().sendMessage(
                      Text.format("Set join info to '{0}'", component)
                  );
                  return 0;
                })
            )
        )

        .then(literal("set_end")
            .then(argument("component", Arguments.CHAT)
                .executes(c -> {
                  Component component = c.getArgument("component", Component.class);
                  JoinInfo.endInfo.text = component;

                  c.getSource().sendMessage(
                      Text.format("Set end join info to '{0}'", component)
                  );
                  return 0;
                })
            )
        );
  }
}