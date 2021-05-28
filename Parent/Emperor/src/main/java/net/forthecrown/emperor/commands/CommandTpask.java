package net.forthecrown.emperor.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.commands.manager.FtcExceptionProvider;
import net.forthecrown.emperor.commands.arguments.UserType;
import net.forthecrown.emperor.user.CrownUser;
import net.forthecrown.emperor.user.UserInteractions;
import net.forthecrown.emperor.user.data.TeleportRequest;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandTpask extends CrownCommandBuilder {

    public CommandTpask(){
        super("tpask", CrownCore.inst());

        setAliases("tpa", "tprequest", "tpr", "etpa", "etpask");
        setDescription("Asks a to teleport to a player.");
        setPermission(Permissions.TPA);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Allows players to teleport to another player by asking them.
     *
     * Valid usages of command:
     * - /tpask <player>
     *
     * Permissions used:
     * - ftc.commands.tpa
     *
     * Main Author: Botul
     * Edit by: Wout
     */

    static TextComponent cancelRequest(CrownUser user){
        return Component.text("Request sent to ")
                .color(NamedTextColor.GOLD)
                .append(user.nickDisplayName().color(NamedTextColor.YELLOW))
                .append(Component.text(". "))
                .append(Component.text("[✖]")
                        .color(NamedTextColor.YELLOW)
                        .clickEvent(ClickEvent.runCommand("/tpacancel " + user.getName()))
                        .hoverEvent(Component.text("Cancel teleportation request."))
                );
    }

    private static final TextComponent ACCEPT_BUTTON = Component.text("[✔] ")
            .color(NamedTextColor.YELLOW)
            .hoverEvent(HoverEvent.showText(Component.text("Accept teleportation request.")));

    private static final TextComponent DENY_BUTTON = Component.text("[✖]")
            .color(NamedTextColor.GRAY)
            .hoverEvent(HoverEvent.showText(Component.text("Deny teleportation request.")));

    public static void checkPreconditions(CrownUser sender, CrownUser to, boolean tpaHere) throws CommandSyntaxException {
        if(sender.equals(to)) throw FtcExceptionProvider.cannotTpToSelf();

        if(!sender.allowsTPA()) throw FtcExceptionProvider.senderTpaDisabled();
        if(!to.allowsTPA()) throw FtcExceptionProvider.targetTpaDisabled(to);

        UserInteractions i = sender.getInteractions();
        if(i.getOutgoing(to) != null) throw FtcExceptionProvider.requestAlreadySent(sender);

        if(!tpaHere && !sender.canTeleport()) throw FtcExceptionProvider.cannotTeleport();
    }

    public static TextComponent acceptButton(CrownUser target){
        return ACCEPT_BUTTON.clickEvent(ClickEvent.runCommand("/tpaccept " + target.getName()));
    }

    public static TextComponent denyButton(CrownUser target){
        return DENY_BUTTON.clickEvent(ClickEvent.runCommand("/tpdeny " + target.getName()));
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.then(argument("player", UserType.onlineUser())
                .executes(c -> {
                    CrownUser player = getUserSender(c);
                    CrownUser target = UserType.getUser(c, "player");
                    checkPreconditions(player, target, false);

                    player.sendMessage(cancelRequest(target));

                    target.sendMessage(
                            Component.text()
                                    .color(NamedTextColor.GRAY)
                                    .append(player.nickDisplayName().color(NamedTextColor.YELLOW))
                                    .append(Component.text(" has requested to teleport to you "))
                                    .append(acceptButton(player))
                                    .append(denyButton(player))
                                    .build()
                    );

                    player.getInteractions().handleTeleport(new TeleportRequest(player, target, false));
                    return 0;
                })
        );
    }
}
