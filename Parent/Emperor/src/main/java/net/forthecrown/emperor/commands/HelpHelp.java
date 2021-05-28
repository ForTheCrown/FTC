package net.forthecrown.emperor.commands;

import com.google.common.base.Joiner;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.Permissions;
import net.forthecrown.emperor.commands.arguments.CommandHelpType;
import net.forthecrown.emperor.commands.manager.CoreCommands;
import net.forthecrown.emperor.commands.manager.CrownCommandBuilder;
import net.forthecrown.emperor.utils.CrownUtils;
import net.forthecrown.emperor.utils.ListUtils;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HelpHelp extends CrownCommandBuilder {

    public HelpHelp(){
        super("help_ftc", CrownCore.inst());

        setPermission(Permissions.HELP);
        setAliases("help");
        setDescription("Shows you where to find help lol");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> sendHelp(getSender(c), 0, c.getInput()))
                .then(argument("page", CommandHelpType.HELP_TYPE)
                              .executes(c -> sendHelp(getSender(c), c.getArgument("page", Integer.class), c.getInput()))
                );
    }

    private int sendHelp(CommandSender sender, int page, String input) throws CommandSyntaxException { // D: Send help!!!!!!!!!!
        List<Component> commands = availableCommands(sender);
        int maxPage = CommandHelpType.MAX;

        final Component header = Component.text("--------").color(NamedTextColor.GRAY);
        TextComponent.Builder message = Component.text()
                .append(header).append(Component.text( " Command help ").append(header))
                .append(Component.newline());

        for (int i = 0; i < 10; i++){
            int index = page * 10 + i;
            if(index >= commands.size()) break;

            message.append(commands.get(index));
        }

        message.append(header, Component.text(" Page " + (page+1) + "/" + maxPage + " "), header);
        sender.sendMessage(message.build());
        return page;
    }

    private List<Component> availableCommands(CommandSender sender){
        Collection<CrownCommandBuilder> commands = CoreCommands.BY_NAME.values();
        List<Component> formatted = new ArrayList<>();

        for (CrownCommandBuilder c: commands){
            if(!c.testPermissionSilent(sender)) continue;
            String description = c.getDescription();

            formatted.add(
                    Component.text("/" + c.getName())
                            .color(NamedTextColor.YELLOW)
                            .append(Component.text(": " + (CrownUtils.isNullOrBlank(description) ? "no description" : (description + (description.endsWith(".") ? "" : ".")))).color(NamedTextColor.WHITE))
                            .hoverEvent(aliasMessage(c))
                            .append(Component.newline())
            );
        }

        return formatted;
    }

    private Component aliasMessage(CrownCommandBuilder command){
        return ListUtils.isNullOrEmpty(command.getAliases()) ? null :
                Component.text()
                        .append(Component.text("Aliases: "))
                        .append(Component.text(listAliases(command.getAliases())))
                        .build();
    }

    private String listAliases(String[] aliases){
        return ListUtils.isNullOrEmpty(aliases) ? "" : "/" + Joiner.on(", /").join(aliases);
    }
}
