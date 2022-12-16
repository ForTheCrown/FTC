package net.forthecrown.commands.punish;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Commands;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.EntryNote;
import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;

import java.util.List;

public class CommandNotes extends FtcCommand {

    public CommandNotes() {
        super("Notes");

        setPermission(Permissions.PUNISH_NOTES);
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
                .then(argument("user", Arguments.USER)
                        .executes(c -> {
                            PunishEntry entry = entry(c);

                            if (entry.getNotes().isEmpty()) {
                                throw Exceptions.noNotes(entry);
                            }

                            List<EntryNote> notes = entry.getNotes();
                            TextWriter writer = TextWriters.newWriter();

                            EntryNote.writeNotes(notes, writer, entry.getUser());

                            c.getSource().sendMessage(writer);
                            return 0;
                        })

                        .then(literal("add")
                                .then(argument("str", StringArgumentType.greedyString())
                                        .executes(c -> {
                                            PunishEntry entry = entry(c);

                                            String msg = c.getArgument("str", String.class);
                                            EntryNote note = EntryNote.of(msg, c.getSource());

                                            entry.getNotes().add(0, note);

                                            c.getSource().sendMessage(
                                                    Text.format("Added note '{0}' to {1, user}",
                                                            msg, entry.getUser()
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
                                            var notes = entry.getNotes();

                                            if (notes.isEmpty()) {
                                                throw Exceptions.noNotes(entry);
                                            }

                                            int index = c.getArgument("index", Integer.class);
                                            Commands.ensureIndexValid(index, notes.size());

                                            notes.remove(index - 1);

                                            c.getSource().sendAdmin("Removed note from " + entry.getUser().getName());
                                            return 0;
                                        })
                                )
                        )
                );
    }

    private PunishEntry entry(CommandContext<CommandSource> c) throws CommandSyntaxException {
        return Punishments.get().getEntry(Arguments.getUser(c, "user").getUniqueId());
    }
}