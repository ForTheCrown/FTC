package net.forthecrown.commands.admin;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.forthecrown.text.Text;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;

import static net.forthecrown.core.Crown.getJoinInfo;

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
                            c.getSource().sendMessage(getJoinInfo().display());
                            return 0;
                        })
                )

                .then(literal("should_show")
                        .executes(c -> {
                            c.getSource().sendMessage(
                                    Component.text("Should show join info: ")
                                            .append(Component.text(getJoinInfo().isVisible()))
                            );
                            return 0;
                        })

                        .then(argument("shouldShow", BoolArgumentType.bool())
                                .executes(c -> {
                                    boolean bool = c.getArgument("shouldShow", Boolean.class);
                                    getJoinInfo().setVisible(bool);

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
                                            .append(Component.text(getJoinInfo().isEndVisible()))
                            );
                            return 0;
                        })

                        .then(argument("shouldShow", BoolArgumentType.bool())
                                .executes(c -> {
                                    boolean bool = c.getArgument("shouldShow", Boolean.class);
                                    getJoinInfo().setEndVisible(bool);

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
                                    getJoinInfo().setInfo(component);

                                    c.getSource().sendMessage(
                                            Text.format("Set join info to '{0}'", command)
                                    );
                                    return 0;
                                })
                        )
                )

                .then(literal("set_end")
                        .then(argument("component", Arguments.CHAT)
                                .executes(c -> {
                                    Component component = c.getArgument("component", Component.class);
                                    getJoinInfo().setEndInfo(component);

                                    c.getSource().sendMessage(
                                            Text.format("Set end join info to '{0}'", command)
                                    );
                                    return 0;
                                })
                        )
                );
    }
}