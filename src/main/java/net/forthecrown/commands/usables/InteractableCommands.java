package net.forthecrown.commands.usables;

import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;

public class InteractableCommands {
    static final InteractableNode[] NODES = {
            new TriggerNode(),
            new UsableBlockNode(),
            new UsableEntityNode()
    };

    public static void createCommands() {
        new CommandInteractable().register();
    }

    static class CommandInteractable extends FtcCommand {
        public CommandInteractable() {
            super("interactable");

            setPermission(Permissions.ADMIN);
            setAliases("usable");
        }

        @Override
        protected void createCommand(BrigadierCommand command) {
            for (var n: NODES) {
                n.register();

                var literal = literal(n.argumentName);
                n.create(literal);

                command.then(literal);
            }
        }
    }

}