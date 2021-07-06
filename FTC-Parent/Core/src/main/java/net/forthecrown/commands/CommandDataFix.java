package net.forthecrown.commands;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.serializer.UserYamlToJson;

public class CommandDataFix extends FtcCommand {

    public CommandDataFix() {
        super("datafix");

        setPermission(Permissions.CORE_ADMIN);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Runs a data fixer
     *
     * Valid usages of command:
     * /datafix <fixer>
     *
     * Permissions used:
     * ftc.core.admin
     *
     * Main Author: Ants
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("user_yamlToJson")
                        .executes(c -> {
                            CommandSource source = c.getSource();
                            source.sendAdmin("Starting user datafixer");

                            try {
                                UserYamlToJson.activateAsync();
                            } catch (Exception e){
                                source.sendAdmin(e.getMessage());
                                e.printStackTrace();
                            }

                            return 0;
                        })
                );
    }
}