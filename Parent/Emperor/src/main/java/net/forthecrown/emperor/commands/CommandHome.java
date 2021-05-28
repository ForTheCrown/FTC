package net.forthecrown.emperor.commands;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.commands.arguments.HomeParseResult;
import net.forthecrown.emperor.commands.arguments.HomeType;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.data.UserTeleport;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;

public class CommandHome extends CrownCommandBuilder {
    public CommandHome(){
        super("home", CrownCore.inst());

        setPermission(Permissions.HOME);
        setDescription("Takes you to one of your homes");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("home", HomeType.home())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            HomeParseResult result = c.getArgument("home", HomeParseResult.class);
                            Location l = result.getHome(c.getSource(), false);

                            user.createTeleport(() -> l, true, UserTeleport.Type.HOME)
                                    .setCompleteMessage(Component.text("Teleporting to " + result.getName()).color(NamedTextColor.GRAY))
                                    .start(true);
                            return 0;
                        })
                );
    }
}
