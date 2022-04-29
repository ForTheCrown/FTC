package net.forthecrown.commands.help;

import com.sk89q.worldedit.math.BlockVector2;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.FtcVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionUtil;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.actions.ActionFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class HelpSpawn extends FtcCommand {

    public HelpSpawn(){
        super("spawn", Crown.inst());

        setPermission(Permissions.HELP);
        setDescription("Shows info about spawn");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Explains how to get to spawn.
     *
     *
     * Valid usages of command:
     * - /spawn
     *
     * Author: Wout
     */

    public static final Component MESSAGE = Component.text()
            .append(Crown.prefix())
            .append(Component.text("Info about spawn: ").color(NamedTextColor.YELLOW))

            .append(Component.newline())
            .append(Component.text("Spawn is called Hazelguard, you can tp to it using region poles."))

            .append(Component.newline())
            .append(Component.text("Use"))
            .append(Component.text(" /findpole ")
                    .color(NamedTextColor.YELLOW)
                    .hoverEvent(Component.text("Click me :D"))
                    .clickEvent(ClickEvent.runCommand("/findpole"))
            )

            .append(Component.text("to find the closest pole."))

            .append(Component.newline())
            .append(Component.text("Then use"))
            .append(Component.text(" /visit Hazelguard or /spawn ")
                    .color(NamedTextColor.YELLOW)
                    .hoverEvent(Component.text("Click Me :D"))
                    .clickEvent(ClickEvent.runCommand("/vr Hazelguard"))
            )

            .append(Component.newline())
            .append(Component.text("[For more help, click here!]")
                    .clickEvent(ClickEvent.runCommand("/posthelp"))
                    .hoverEvent(Component.text("Click me for region pole info :D"))
                    .color(NamedTextColor.GRAY)
            )
            .build();

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c ->{
            CrownUser sender = getUserSender(c);

            PopulationRegion region = Crown.getRegionManager().get(FtcVars.spawnRegion.get());
            if (region != null) {
                BlockVector2 pole = Crown.getRegionManager().getData(sender.getRegionPos()).getPolePosition();

                if(RegionUtil.isCloseToPole(pole, sender)) {
                    ActionFactory.visitRegion(sender, region);
                    return 0;
                }
            }

            sender.sendMessage(MESSAGE);
            return 0;
        });
    }
}