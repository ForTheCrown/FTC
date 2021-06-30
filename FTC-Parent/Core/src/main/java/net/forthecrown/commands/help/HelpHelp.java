package net.forthecrown.commands.help;

import com.google.common.base.Joiner;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.arguments.CommandHelpType;
import net.forthecrown.commands.manager.CoreCommands;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.utils.CrownUtils;
import net.forthecrown.utils.ListUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HelpHelp extends FtcCommand {

    public HelpHelp(){
        super("fhelp", CrownCore.inst());

        setPermission(Permissions.HELP);
        setAliases("help", "ehelp");
        setDescription("Shows you where to find help lol");

        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> sendHelp(getSender(c), 0, c.getInput()))
                .then(argument("page", CommandHelpType.HELP_TYPE)
                              .executes(c -> sendHelp(getSender(c), c.getArgument("page", Integer.class), c.getInput()))
                )

                .then(literal("specific")
                        .then(argument("command", StringArgumentType.word())
                                .executes(c -> {
                                    String cmd = c.getArgument("command", String.class);
                                    CommandSource source = c.getSource();

                                    if(CoreCommands.BY_NAME.containsKey(cmd)){
                                        FtcCommand lookup = CoreCommands.BY_NAME.get(cmd);

                                        source.sendMessage(
                                                commandMessage(
                                                        lookup.getName(),
                                                        lookup.getPerm(),
                                                        lookup.getAliases(),
                                                        lookup.getDescription(),
                                                        "FTC",
                                                        source.hasPermission(Permissions.CORE_ADMIN)
                                                )
                                        );
                                        return 0;
                                    }

                                    if(!source.hasPermission(Permissions.CORE_ADMIN)){
                                        source.sendMessage(Component.translatable("commands.generic.permission", NamedTextColor.RED));
                                        return 0;
                                    }

                                    Command lookup = Bukkit.getCommandMap().getCommand(cmd);
                                    if(lookup == null) throw FtcExceptionProvider.create("No command with that name exists");

                                    source.sendMessage(commandMessage(lookup.getName(),
                                            lookup.getPermission(),
                                            lookup.getAliases().toArray(String[]::new),
                                            lookup.getDescription(),
                                            ((lookup instanceof PluginIdentifiableCommand) ? ((PluginIdentifiableCommand) lookup).getPlugin().getName() : null),
                                            true
                                    ));
                                    return 0;
                                })
                ));
    }

    private Component commandMessage(String label, String permission, String[] aliases, String description, String plugin, boolean showDetails){
        Component border = Component.text("---------").style(Style.style(NamedTextColor.YELLOW, TextDecoration.STRIKETHROUGH));
        Style header = Style.style(NamedTextColor.YELLOW);
        Style cell = Style.style(NamedTextColor.WHITE);

        TextComponent.Builder builder = Component.text()
                .append(border)
                .append(Component.space())
                .append(Component.text(label).color(NamedTextColor.GOLD))
                .append(Component.space())
                .append(border);

        if(showDetails && !CrownUtils.isNullOrBlank(permission)){
            builder
                    .append(Component.newline())
                    .append(Component.text("Permission: ").style(header).append(Component.text(permission).style(cell)));
        }

        if(showDetails && !CrownUtils.isNullOrBlank(plugin)){
            builder
                    .append(Component.newline())
                    .append(Component.text("Plugin: ").style(header).append(Component.text(plugin).style(cell)));
        }

        if(!ListUtils.isNullOrEmpty(aliases)){
            builder
                    .append(Component.newline())
                    .append(Component.text("Aliases: ").style(header).append(Component.text(listAliases(aliases)).style(cell)));
        }

        if(!CrownUtils.isNullOrBlank(description)){
            builder
                    .append(Component.newline())
                    .append(Component.text("Description: ").style(header).append(Component.text(description).style(cell)));
        }

        builder
                .append(Component.newline())
                .append(border, border);
        return builder.build();
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
        Collection<FtcCommand> commands = CoreCommands.BY_NAME.values();
        List<Component> formatted = new ArrayList<>();

        for (FtcCommand c: commands){
            if(!c.testPermissionSilent(sender)) continue;
            String description = c.getDescription();

            formatted.add(
                    Component.text("/" + c.getName())
                            .color(NamedTextColor.YELLOW)
                            .append(Component.text(": " + (CrownUtils.isNullOrBlank(description) ? "no description" : (description + (description.endsWith(".") ? "" : ".")))).color(NamedTextColor.WHITE))
                            .hoverEvent(aliasMessage(c.getAliases()))
                            .append(Component.newline())
            );
        }

        return formatted;
    }

    private Component aliasMessage(String[] command){
        return ListUtils.isNullOrEmpty(command) ? null :
                Component.text()
                        .append(Component.text("Aliases: "))
                        .append(Component.text(listAliases(command)))
                        .build();
    }

    private String listAliases(String[] aliases){
        return ListUtils.isNullOrEmpty(aliases) ? "" : "/" + Joiner.on(", /").join(aliases);
    }
}
