package net.forthecrown.commands.punish;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.EntryNote;
import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.core.chat.ComponentWriter;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

public class CommandNotes extends FtcCommand {

    public CommandNotes() {
        super("Notes");

        setPermission(Permissions.HELPER);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /Notes
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("user", UserArgument.user())
                        .executes(c -> {
                            PunishEntry entry = entry(c);

                            if(entry.notes().isEmpty()) {
                                throw FtcExceptionProvider.create(entry.entryUser().getName() + " has no notes");
                            }

                            List<EntryNote> notes = entry.notes();
                            ComponentWriter writer = ComponentWriter.normal();
                            writer.write(entry.entryUser().nickDisplayName().color(NamedTextColor.YELLOW));
                            writer.write(Component.text("'s notes:"));

                            int index = 0;

                            for (EntryNote n: notes) {
                                index++;

                                writer.newLine();
                                writer.write(
                                        Component.text(index + ") ")
                                                .color(NamedTextColor.YELLOW)
                                                .clickEvent(ClickEvent.runCommand("/notes " + entry.entryUser().getName() + " remove " + index))
                                                .hoverEvent(Component.text("Click to remove"))
                                );

                                writer.write(
                                        FtcFormatter.formatString(n.info())
                                                .hoverEvent(
                                                        Component.text("Source: ")
                                                                .append(Component.text(n.source()).color(NamedTextColor.WHITE))
                                                                .color(NamedTextColor.GRAY)
                                                                .append(Component.newline())
                                                                .append(Component.text("Created: "))
                                                                .append(FtcFormatter.formatDate(n.issued()).color(NamedTextColor.WHITE))
                                                )
                                );
                            }

                            c.getSource().sendMessage(writer.get());
                            return 0;
                        })

                        .then(literal("add")
                                .then(argument("str", StringArgumentType.greedyString())
                                        .executes(c -> {
                                            PunishEntry entry = entry(c);

                                            String msg = c.getArgument("str", String.class);
                                            EntryNote note = new EntryNote(msg, System.currentTimeMillis(), c.getSource().textName());

                                            entry.notes().add(0, note);

                                            c.getSource().sendMessage(
                                                    ChatUtils.format("Added note '{}' to {}",
                                                            msg, entry.entryUser().getNickOrName()
                                                    )
                                            );
                                            return 0;
                                        })
                                )
                        )

                        .then(literal("remove")
                                .then(argument("index", IntegerArgumentType.integer(1))
                                        .executes(c -> {
                                            PunishEntry entry = entry(c);

                                            if(entry.notes().isEmpty()) {
                                                throw FtcExceptionProvider.create(entry.entryUser().getName() + " has no notes");
                                            }

                                            int index = c.getArgument("index", Integer.class);
                                            index--;

                                            try {
                                                entry.notes().remove(index);
                                            } catch (IndexOutOfBoundsException e) {
                                                throw FtcExceptionProvider.create("Invalid index: " + index);
                                            }

                                            c.getSource().sendAdmin("Removed note from " + entry.entryUser().getName());
                                            return 0;
                                        })
                                )
                        )
                );
    }

    private PunishEntry entry(CommandContext<CommandSource> c) throws CommandSyntaxException {
        return Crown.getPunisher().getEntry(UserArgument.getUser(c, "user").getUniqueId());
    }
}