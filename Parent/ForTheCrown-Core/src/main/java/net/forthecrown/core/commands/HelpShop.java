package net.forthecrown.core.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class HelpShop extends CrownCommandBuilder {

    public HelpShop(){
        super("shophelp", FtcCore.getInstance());

        setAliases("helpshop");
        setPermission(null);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            CommandSender sender = getSender(c);
            Component edit_message = Component.text("[editshop]").clickEvent(ClickEvent.runCommand("/editshop"));

            Component help_message = Component.text()
                    .append(FtcCore.prefix())
                    .append(Component.text("Sign Shop info:").color(NamedTextColor.YELLOW))
                    .append(Component.newline())

                    .append(Component.text("Sign shops can be created anywhere by anyone :D"))
                    .append(Component.newline())

                    .append(Component.text("To create a shop:"))
                    .append(Component.newline())

                    .append(lineText(1,
                            Component.text("[buy]")
                                    .color(NamedTextColor.YELLOW)
                                    .append(Component.text(" or ").color(NamedTextColor.GRAY))
                                    .append(Component.text("[sell]"))
                    ))
                    .append(lineText(2, Component.text("Can be anything").color(NamedTextColor.GRAY)))
                    .append(lineText(3, Component.text("Can be anything").color(NamedTextColor.GRAY)))
                    .append(lineText(4, Component.text("Item Price").color(NamedTextColor.YELLOW)))

                    .append(Component.text("If you're a Tier-1 Donator, you can use "))
                    .append((sender.hasPermission("ftc.commands.editshop") ? edit_message : Component.text("/editshop")).color(NamedTextColor.YELLOW))
                    .append(Component.text(" to make changes to your shop."))

                    .build();

            c.getSource().sendMessage(help_message);
            return 0;
        });
    }

    private static Component lineText(int line, Component info){
        return Component.text()
                .append(Component.text("Line " + line + ": ").color(NamedTextColor.GOLD))
                .append(info)
                .append(Component.newline())
                .build();
    }
}
