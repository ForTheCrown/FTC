package net.forthecrown.commands.user;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.ArrayArgument;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.user.User;
import net.forthecrown.user.data.RankTier;
import net.forthecrown.user.data.UserRank;
import net.forthecrown.user.data.RanksComponent;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.TextJoiner;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;

class UserTitlesNode extends UserCommandNode {

  private static final EnumArgument<RankTier> TIER_ARG
      = ArgumentTypes.enumType(RankTier.class);

  private static final RegistryArguments<UserRank> TITLE_ARG
      = RegistryArguments.RANKS;

  private static final ArrayArgument<Holder<UserRank>> TITLE_ARRAY_ARG
      = ArgumentTypes.array(TITLE_ARG);

  public UserTitlesNode() {
    super("user_titles", "titles");
  }

  @Override
  void createUsages(UsageFactory factory) {
    factory.usage("")
        .addInfo("Displays a user's active title,")
        .addInfo("available titles and tier");

    factory.usage("tier", "Shows a user's tier");
    factory.usage("tier <tier>", "Sets the user's tiers");

    factory.usage("title", "Show's a user's active title");
    factory.usage("title <title>", "Sets a user's active title");

    var prefix = factory.withPrefix("available_titles");

    prefix.usage("", "Lists a user's available titles");
    prefix.usage("add <title list>", "Adds all titles to a user");
    prefix.usage("remove <title list>", "Removes all titles from a user");
  }

  @Override
  protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command,
                                                                      UserProvider provider
  ) {
    command
        .executes(c -> {
          User user = provider.get(c);
          RanksComponent title = user.getTitles();

          if (title.getAvailable().isEmpty()) {
            throw Exceptions.NOTHING_TO_LIST;
          }

          TextWriter writer = TextWriters.newWriter();

          writer.formatted("{0, user}'s title data: ", user);

          writer.field("Active title", title.getTitle());
          writer.field("Tier", title.getTier());
          writer.field("Available titles",
              TextJoiner.onComma()
                  .add(title.getAvailable())
          );

          c.getSource().sendMessage(writer);
          return 0;
        })

        .then(literal("title")
            .executes(c -> {
              User user = provider.get(c);
              RanksComponent titles = user.getTitles();

              c.getSource().sendMessage(
                  Text.format("{0, user}'s title: {1}",
                      user, titles.getTitle()
                  )
              );
              return 0;
            })

            .then(argument("title", TITLE_ARG)
                .executes(c -> {
                  User user = provider.get(c);
                  Holder<UserRank> title = c.getArgument("title", Holder.class);

                  user.getTitles().setTitle(title.getValue());

                  c.getSource().sendSuccess(
                      Text.format("Set {0, user}'s title to {1}",
                          user, title.getValue()
                      )
                  );
                  return 0;
                })
            )
        )

        .then(literal("tier")
            .executes(c -> {
              User user = provider.get(c);
              RanksComponent titles = user.getTitles();

              c.getSource().sendMessage(
                  Text.format("{0, user}'s tier: {1}",
                      user, titles.getTier()
                  )
              );
              return 0;
            })

            .then(literal("set")
                .then(argument("tier", TIER_ARG)
                    .executes(c -> {
                      User user = provider.get(c);
                      RanksComponent titles = user.getTitles();
                      RankTier tier = c.getArgument("tier", RankTier.class);
                      var current = titles.getTier();

                      if (tier == current) {
                        throw Exceptions.format(
                            "User {0, user}'s tier is already {1}",
                            user, current.getDisplayName()
                        );
                      }

                      if (tier.ordinal() < current.ordinal()) {
                        titles.demote(tier);

                        c.getSource().sendSuccess(
                            Text.format("Demoted {0, user} to {1}",
                                user,
                                tier.getDisplayName()
                            )
                        );
                      } else {
                        titles.addTier(tier);

                        c.getSource().sendSuccess(
                            Text.format("Set {1, user}'s tier to {0}",
                                tier, user
                            )
                        );
                      }

                      return 0;
                    })
                )
            )
        )

        .then(literal("available_titles")
            .executes(c -> {
              var user = provider.get(c);
              RanksComponent titles = user.getTitles();
              var titleList = titles.getAvailable();

              if (titleList.isEmpty()) {
                throw Exceptions.NOTHING_TO_LIST;
              }

              c.getSource().sendMessage(
                  Text.format("{0, user}'s titles: {1}",
                      user, TextJoiner.onComma()
                          .add(titleList)
                  )
              );
              return 0;
            })

            .then(literal("add")
                .then(argument("titles", TITLE_ARRAY_ARG)
                    .executes(c -> addRemoveArgument(c, provider, false))
                )
            )

            .then(literal("remove")
                .then(argument("titles", TITLE_ARRAY_ARG)
                    .executes(c -> addRemoveArgument(c, provider, true))
                )
            )
        );
  }

  private int addRemoveArgument(CommandContext<CommandSource> c,
                                UserProvider provider,
                                boolean remove
  ) throws CommandSyntaxException {
    var user = provider.get(c);
    RanksComponent userTitles = user.getTitles();

    Collection<Holder<UserRank>> rankHolders
        = c.getArgument("titles", Collection.class);

    var titles = rankHolders.stream()
        .map(Holder::getValue)
        .toList();

    for (var t : titles) {
      if (t.isDefaultTitle()) {
        throw Exceptions.defaultTitle(t);
      }
    }

    for (var t : titles) {
      if (remove) {
        userTitles.removeTitle(t);
      } else {
        userTitles.addTitle(t);
      }
    }

    String format;

    if (remove) {
      format = "Removed titles from {0, user}: {1}";
    } else {
      format = "Added titles to {0, user}: {1}";
    }

    c.getSource().sendSuccess(
        Text.format(format,
            user, TextJoiner.onComma()
                .add(titles)
        )
    );
    return 0;
  }
}