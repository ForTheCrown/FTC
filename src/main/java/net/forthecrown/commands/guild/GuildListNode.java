package net.forthecrown.commands.guild;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Commands;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.text.format.page.Footer;
import net.forthecrown.utils.text.format.page.PageEntryIterator;
import net.forthecrown.utils.text.format.page.PageFormat;
import net.forthecrown.utils.text.writer.TextWriter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

import static net.forthecrown.commands.UserMapTopCommand.DEF_PAGE_SIZE;
import static net.kyori.adventure.text.Component.text;

class GuildListNode extends GuildCommandNode {
    public static final PageFormat<Guild> FORMAT = Util.make(() -> {
        PageFormat<Guild> result = PageFormat.create();

        result.setHeader(text("Guilds", NamedTextColor.YELLOW));
        result.setFooter(Footer.ofButton("/g list %s %s"));

        result.setEntry((writer, entry, viewerIndex) -> {
            int members = entry.getMemberSize();

            writer.formatted("{0} - {1, number} member{2}",
                    entry.getName(),
                    members,
                    Util.conditionalPlural(members)
            );
        });

        return result;
    });

    GuildListNode() {
        super("guilds", "l", "list");
        setAliases("guildlist");
    }

    @Override
    protected void writeHelpInfo(TextWriter writer, CommandSource source) {
        writer.field("list", "Lists all guilds");
    }

    @Override
    protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
        command
                .executes(c -> {
                    c.getSource().sendMessage(listGuilds(0, DEF_PAGE_SIZE));
                    return 0;
                })

                .then(argument("page", IntegerArgumentType.integer(1))
                        .executes(c -> {
                            c.getSource().sendMessage(
                                    listGuilds(
                                            c.getArgument("page", Integer.class) - 1,
                                            DEF_PAGE_SIZE
                                    )
                            );
                            return 0;
                        })

                        .then(argument("pageSize", IntegerArgumentType.integer(5, 20))
                                .executes(c -> {
                                    int page = c.getArgument("page", Integer.class) - 1;
                                    int pageSize = c.getArgument("pageSize", Integer.class);

                                    c.getSource().sendMessage(listGuilds(page, pageSize));
                                    return 0;
                                })
                        )
                );
    }

    private Component listGuilds(int page, int pageSize) throws CommandSyntaxException {
        List<Guild> guilds = GuildManager.get()
                .getGuilds();

        Commands.ensurePageValid(page, pageSize, guilds.size());

        PageEntryIterator<Guild> it = PageEntryIterator.of(guilds, page, pageSize);
        return FORMAT.format(it);
    }
}