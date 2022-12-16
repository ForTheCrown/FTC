package net.forthecrown.commands.guild;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;

import static net.kyori.adventure.text.Component.text;

class GuildHelpNode extends GuildCommandNode {
    public GuildHelpNode() {
        super("guildhelp", "help", "?");
    }

    @Override
    protected void writeHelpInfo(TextWriter writer, CommandSource source) {
        writer.field("?", "Shows help information");
    }

    @Override
    protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
        command.executes(c -> {
            var writer = createHelpWriter(getLabel(c));

            for (var n: GuildCommands.NODES) {
                if (!writer.isLineEmpty()) {
                    writer.newLine();
                }

                n.writeHelpInfo(writer, c.getSource());
            }

            c.getSource().sendMessage(writer);
            return 0;
        });
    }

    private TextWriter createHelpWriter(String label) {
        var writer = TextWriters.newWriter();
        writer.setFieldSeparator(text(" - "));
        writer.setFieldStyle(Style.style(NamedTextColor.GOLD));
        writer.setFieldValueStyle(Style.empty());

        return writer.withPrefix(
                Component.text(label + " ", writer.getFieldStyle())
        );
    }

    private String getLabel(CommandContext<CommandSource> c) {
        var filtered = GrenadierUtils.filterCommandInput(c.getInput());
        return "/" + filtered.readUnquotedString();
    }
}