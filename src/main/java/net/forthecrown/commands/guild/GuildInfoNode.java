package net.forthecrown.commands.guild;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.menu.GuildMenus;
import net.forthecrown.utils.text.writer.TextWriter;

class GuildInfoNode extends GuildCommandNode {
    public GuildInfoNode() {
        super("guildinfo", "info", "i");
    }

    @Override
    protected void writeHelpInfo(TextWriter writer, CommandSource source) {
        writer.field("info", "Displays info about your guild");
        writer.field("info <guild>", "Displays info about a specific guild");
    }

    @Override
    protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
        command
                .executes(c -> showInfo(c, GuildProvider.SENDERS_GUILD))

                .then(guildArgument()
                        .executes(c -> showInfo(c, providerForArgument()))
                );
    }

    private int showInfo(CommandContext<CommandSource> c, GuildProvider provider) throws CommandSyntaxException {
        var guild = provider.get(c);
        var user = getUserSender(c);

        GuildMenus.open(
                GuildMenus.MAIN_MENU.getStats(),
                user, guild
        );
        return 0;
    }

}