package net.forthecrown.core.commands;

import com.google.common.base.Joiner;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.core.utils.ListUtils;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HelpHelp extends CrownCommandBuilder {

    public HelpHelp(){
        super("help", FtcCore.getInstance());

        setPermission(null);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> sendHelp(getSender(c), 0, c.getInput()))
                .then(argument("page", IntegerArgumentType.integer(1))
                              .executes(c -> sendHelp(getSender(c), c.getArgument("page", Integer.class), c.getInput()))
                );
    }

    private int sendHelp(CommandSender sender, int page, String input) throws CommandSyntaxException { // D: Send help!!!!!!!!!!
        if(page > 0) page--;

        List<Component> commands = availableCommands(sender);
        int maxPage = Math.round(((float) commands.size())/10);

        if(page > maxPage){
            StringReader reader = new StringReader(input);
            reader.setCursor(input.indexOf(page + ""));
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooHigh().createWithContext(reader, page, maxPage);
        }

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
        Collection<Command> commands = Bukkit.getCommandMap().getKnownCommands().values();
        List<Component> formatted = new ArrayList<>();

        for (Command c: commands){
            if(!c.testPermissionSilent(sender)) continue;

            String description = c.getDescription();

            formatted.add(
                    Component.text("/" + c.getName())
                            .color(NamedTextColor.YELLOW)
                            .append(Component.text(": " + (CrownUtils.isNullOrBlank(description) ? "no description" : description)).color(NamedTextColor.WHITE))
                            .append(aliasMessage(c))
                            .append(Component.newline())
            );
        }

        return formatted;
    }

    private Component aliasMessage(Command command){
        return ListUtils.isNullOrEmpty(command.getAliases()) ? Component.empty() :
                Component.text()
                        .append(Component.newline())
                        .append(Component.text("  ⤷ Aliases: "))
                        .color(NamedTextColor.GRAY)
                        .append(Component.text(listAliases(command.getAliases())))
                        .build();
    }

    private String listAliases(List<String> aliases){
        return ListUtils.isNullOrEmpty(aliases) ? "" : "/" + Joiner.on(", /").join(aliases);
    }
}
