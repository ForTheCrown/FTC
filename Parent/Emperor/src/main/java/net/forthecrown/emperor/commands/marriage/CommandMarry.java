package net.forthecrown.emperor.commands.marriage;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.commands.manager.FtcCommand;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.UserInteractions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;

public class CommandMarry extends FtcCommand {
    public static final NamespacedKey KEY = new NamespacedKey(CrownCore.inst(), "marriage");

    public CommandMarry() {
        super("marry", CrownCore.inst());

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
     * Main Author: Ants
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserType.onlineUser())
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            CrownUser target = UserType.getUser(c, "user");

                            if(target.equals(user)) throw FtcExceptionProvider.translatable("marriage.marrySelf");

                            UserInteractions uInter = user.getInteractions();
                            UserInteractions tInter = target.getInteractions();

                            if(uInter.getMarriedTo() != null) throw FtcExceptionProvider.senderAlreadyMarried();
                            if(tInter.getMarriedTo() != null) throw FtcExceptionProvider.targetAlreadyMarried(target);

                            if(!uInter.canChangeMarriageStatus()) throw FtcExceptionProvider.cannotChangeMarriageStatus();
                            if(!tInter.canChangeMarriageStatus()) throw FtcExceptionProvider.cannotChangeMarriageStatusTarget(target);

                            tInter.setLastMarriageRequest(user.getUniqueId());

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