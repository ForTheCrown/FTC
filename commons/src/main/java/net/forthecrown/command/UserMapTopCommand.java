package net.forthecrown.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import java.util.UUID;
import java.util.function.Function;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.Text;
import net.forthecrown.text.UnitFormat;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.text.page.Header;
import net.forthecrown.text.page.PageFormat;
import net.forthecrown.user.Users;
import net.forthecrown.utils.ScoreIntMap;
import net.forthecrown.utils.context.Context;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class UserMapTopCommand extends FtcCommand {

  public static final int DEF_PAGE_SIZE = 10;

  private final ScoreIntMap<UUID> map;
  private final PageFormat<ScoreIntMap.Entry<UUID>> format;
  private final Component title;

  public UserMapTopCommand(
      String name,
      ScoreIntMap<UUID> map,
      Long2ObjectFunction<Component> unitMaker,
      Component title,
      String... aliases
  ) {
    this(name, map, unitMaker, title, id -> {
      var user = Users.get(id);
      return viewer -> user.displayName(viewer);
    }, aliases);
  }

  public UserMapTopCommand(
      String name,
      ScoreIntMap<UUID> map,
      Long2ObjectFunction<Component> unitMaker,
      Component title,
      Function<UUID, ViewerAwareMessage> display,
      String... aliases
  ) {
    super(name);

    this.map = map;
    this.title = title;

    // Create format
    this.format = PageFormat.create();
    format
        .setHeader(Header.<ScoreIntMap.Entry<UUID>>create()
            // Set title
            .title(title.color(NamedTextColor.GOLD))

            // Write the server total if not
            // on first page
            .append((it, writer, context) -> {
              if (!it.isFirstPage()) {
                return;
              }
              writer.formattedLine("Server total: {0}",
                  NamedTextColor.YELLOW,
                  unitMaker.apply(map.total())
              );
            })
        )

        .setEntry((writer, entry, viewerIndex, context, it) -> {
          writer.formatted("{0} - &e{1}",
              display.apply(entry.key()),
              unitMaker.apply(entry.value())
          );
        })

        // First argument is page, second is page size
        .setPageButton("/" + getName() + " %s %s");

    // Set command data and register
    setAliases(aliases);
    register();
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    String sTitle = Text.plain(title);

    factory.usage("")
        .addInfo("Shows you the " + sTitle);

    factory.usage("<page> [<page size: number(5..20)>]")
        .addInfo("Shows you the " + sTitle + " on <page>")
        .addInfo("If [page size] is not set, then it defaults to " + DEF_PAGE_SIZE);
  }

  @Override
  public void createCommand(GrenadierCommand command) {
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

    Commands.ensurePageValid(page, pageSize, map.size());

    source.sendMessage(format.format(
        map.pageIterator(page, pageSize),
        Context.EMPTY
    ));
    return 0;
  }
}