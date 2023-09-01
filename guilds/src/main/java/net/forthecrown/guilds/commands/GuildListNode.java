package net.forthecrown.guilds.commands;

import static net.forthecrown.command.UserMapTopCommand.DEF_PAGE_SIZE;
import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import net.forthecrown.command.Commands;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextWriter;
import net.forthecrown.text.TextWriters;
import net.forthecrown.text.page.Footer;
import net.forthecrown.text.page.PageFormat;
import net.forthecrown.text.page.PagedIterator;
import net.forthecrown.user.User;
import net.forthecrown.utils.context.ContextOption;
import net.forthecrown.utils.context.ContextSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;

class GuildListNode extends GuildCommandNode {
  public static final ContextSet SET = ContextSet.create();
  public static final ContextOption<User> VIEWER = SET.newOption();

  public static final PageFormat<Guild> FORMAT;

  static {
    PageFormat<Guild> result = PageFormat.create();

    result.setHeader(text("Guilds", NamedTextColor.YELLOW));

    result.getHeader()
        .append(Component.newline())
        .append(Component.text("[Discovery menu]", NamedTextColor.AQUA)
            .clickEvent(ClickEvent.runCommand("/g discover"))
        );

    result.setFooter(Footer.ofButton("/g list %s %s"));

    result.setEntry((writer, entry, viewerIndex, context, it) -> {
      int members = entry.getMemberSize();

      User viewer = context.getOrThrow(VIEWER);
      Component display = entry.displayName();
      TextWriter hoverWriter = TextWriters.newWriter();
      hoverWriter.setFieldStyle(Style.style(NamedTextColor.GRAY));
      entry.writeDiscoverInfo(hoverWriter, viewer);

      writer.formatted("{0} - {1, number} member{2}",
          display.hoverEvent(hoverWriter.asComponent()),
          members,
          Text.conditionalPlural(members)
      );
    });

    FORMAT = result;
  }

  GuildListNode() {
    super("guilds", "l", "list");
    setAliases("guildlist", "glist");
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("", "Lists all guilds");
  }

  @Override
  protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
    command
        .executes(c -> {
          c.getSource().sendMessage(listGuilds(
              0, DEF_PAGE_SIZE, getUserSender(c))
          );
          return 0;
        })

        .then(argument("page", IntegerArgumentType.integer(1))
            .executes(c -> {
              c.getSource().sendMessage(
                  listGuilds(
                      c.getArgument("page", Integer.class) - 1,
                      DEF_PAGE_SIZE,
                      getUserSender(c)
                  )
              );
              return 0;
            })

            .then(argument("pageSize", IntegerArgumentType.integer(5, 20))
                .executes(c -> {
                  int page = c.getArgument("page", Integer.class) - 1;
                  int pageSize = c.getArgument("pageSize", Integer.class);

                  c.getSource().sendMessage(
                      listGuilds(page, pageSize, getUserSender(c))
                  );
                  return 0;
                })
            )
        );
  }

  private Component listGuilds(int page, int pageSize, User viewer)
      throws CommandSyntaxException
  {
    List<Guild> guilds = Guilds.getManager().getGuilds();

    Commands.ensurePageValid(page, pageSize, guilds.size());
    PagedIterator<Guild> it = PagedIterator.of(guilds, page, pageSize);
    return FORMAT.format(it, SET.createContext().set(VIEWER, viewer));
  }
}