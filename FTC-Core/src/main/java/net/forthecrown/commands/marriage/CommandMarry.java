package net.forthecrown.commands.marriage;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserInteractions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandMarry extends FtcCommand {

    public CommandMarry() {
        super("marry", Crown.inst());

        setDescription("Marry a person");
        setPermission(Permissions.MARRY);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /marry
     *
     * Permissions used:
     * ftc.marry
     *
     * Main Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserArgument.onlineUser())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            CrownUser target = UserArgument.getUser(c, "user");

                            if(target.equals(user)) throw FtcExceptionProvider.translatable("marriage.marrySelf");

                            UserInteractions uInter = user.getInteractions();
                            UserInteractions tInter = target.getInteractions();

                            if(uInter.getSpouse() != null) throw FtcExceptionProvider.senderAlreadyMarried();
                            if(tInter.getSpouse() != null) throw FtcExceptionProvider.targetAlreadyMarried(target);

                            if(!uInter.canChangeMarriageStatus()) throw FtcExceptionProvider.cannotChangeMarriageStatus();
                            if(!tInter.canChangeMarriageStatus()) throw FtcExceptionProvider.cannotChangeMarriageStatusTarget(target);

                            tInter.setLastProposal(user.getUniqueId());

                            Component acceptButton = Component.text("[Accept]")
                                    .color(NamedTextColor.GREEN)
                                    .hoverEvent(Component.translatable("marriage.request.button.accept"))
                                    .clickEvent(ClickEvent.runCommand("/marryaccept " + user.getName()));

                            Component denyButton = Component.text("[Deny]")
                                    .color(NamedTextColor.RED)
                                    .hoverEvent(Component.translatable("marriage.request.button.deny"))
                                    .clickEvent(ClickEvent.runCommand("/marrydeny"));

                            Component targetMessage = Component.text()
                                    .append(Component.translatable("marriage.request.target", user.nickDisplayName().color(NamedTextColor.GOLD)).color(NamedTextColor.YELLOW))
                                    .append(Component.space())
                                    .append(acceptButton)
                                    .append(Component.space())
                                    .append(denyButton)
                                    .build();

                            Component senderMessage = Component.translatable("marriage.request.sender", target.nickDisplayName().color(NamedTextColor.GOLD)).color(NamedTextColor.YELLOW);

                            user.sendMessage(senderMessage);
                            target.sendMessage(targetMessage);
                            return 0;
                        })
                );
    }
}