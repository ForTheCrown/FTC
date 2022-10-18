package net.forthecrown.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import net.forthecrown.commands.manager.CmdValidate;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Crown;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.text.format.UnitFormat;
import net.forthecrown.text.format.page.Header;
import net.forthecrown.text.format.page.PageFormat;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.UserScoreMap;
import net.forthecrown.user.Users;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.Logger;

public class UserMapTopCommand extends FtcCommand {
    private static final Logger LOGGER = Crown.logger();

    public static final int DEF_PAGE_SIZE = 10;

    private final UserScoreMap map;
    private final PageFormat<UserScoreMap.Entry> format;

    public UserMapTopCommand(String name, UserScoreMap map, Long2ObjectFunction<Component> unitMaker,
                             Component title,
                             String... aliases
    ) {
        super(name);

        this.map = map;

        // Create format
        this.format = PageFormat.create();
        format
                .setHeader(Header.<UserScoreMap.Entry>create()
                        // Set title
                        .title(title)

                        // Write the server total if not
                        // on first page
                        .append((it, writer) -> {
                            if (!it.isFirstPage()) {
                                return;
                            }
                            writer.formattedLine("Server total: {0}",
                                    NamedTextColor.YELLOW,
                                    unitMaker.apply(map.total())
                            );
                        })
                )

                .setEntry((writer, entry, viewerIndex) -> {
                    var user = Users.get(entry.getUniqueId());

                    writer.formatted("{0, user} - &e{1}",
                            user, unitMaker.apply(entry.getValue())
                    );

                    user.unloadIfOffline();
                })

                // First argument is page, second is page size
                .setPageButton("/" + getName() + " %s %s");

        // Set command data and register
        setAliases(aliases);
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                // /<command> -> show page 0 with default size
                .executes(c -> showPage(c.getSource(), 0, DEF_PAGE_SIZE))

                // /<command> <page> -> show given page with default size
                .then(argument("page", IntegerArgumentType.integer(1))
                        .executes(c -> {
                            int page = c.getArgument("page", Integer.class) - 1;

                            return showPage(c.getSource(), page, DEF_PAGE_SIZE);
                        })

                        // /<command> <page> <page size> -> show given page with given size
                        .then(argument("pageSize", IntegerArgumentType.integer(5, 20))
                                .executes(c -> {
                                    int page = c.getArgument("page", Integer.class) - 1;
                                    int pageSize = c.getArgument("pageSize", Integer.class);

                                    return showPage(c.getSource(), page, pageSize);
                                })
                        )
                );
    }

    private int showPage(CommandSource source, int page, int pageSize) throws CommandSyntaxException {
        if (map.isEmpty()) {
            throw Exceptions.NOTHING_TO_LIST;
        }

        CmdValidate.page(page, pageSize, map.size());

        source.sendMessage(format.format(map.pageIterator(page, pageSize)));
        return 0;
    }

    public static void createCommands() {
        var users = UserManager.get();

        new UserMapTopCommand(
                "baltop",
                users.getBalances(),
                UnitFormat::rhines,
                Component.text("Top balances"),
                "balancetop", "banktop", "topbals", "topbalances"
        );

        new UserMapTopCommand(
                "gemtop",
                users.getGems(),
                UnitFormat::gems,
                Component.text("Gem Top"),
                "topgems"
        );

        new UserMapTopCommand(
                "votetop",
                users.getVotes(),
                UnitFormat::votes,
                Component.text("Top voters"),
                "topvoters"
        );

        new UserMapTopCommand(
                "playtimetop",
                users.getPlayTime(),
                UnitFormat::playTime,
                Component.text("Top by playtime"),
                "nolifetop", "topplayers"
        );
    }
}