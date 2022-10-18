package net.forthecrown.commands.user;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.text.writer.TextWriter;
import net.forthecrown.text.writer.TextWriters;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.ArrayArgument;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.user.User;
import net.forthecrown.user.data.RankTier;
import net.forthecrown.user.data.RankTitle;
import net.forthecrown.user.data.UserTitles;

import java.util.Collection;

class UserTitlesNode extends UserCommandNode {
    private static final EnumArgument<RankTier> TIER_ARG = EnumArgument.of(RankTier.class);
    private static final EnumArgument<RankTitle> TITLE_ARG = EnumArgument.of(RankTitle.class);

    private static final ArrayArgument<RankTitle> TITLE_ARRAY_ARG = ArrayArgument.of(TITLE_ARG);

    public UserTitlesNode() {
        super("user_titles", "titles");

    }

    @Override
    protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command, UserProvider provider) {
        command
                .executes(c -> {
                    User user = provider.get(c);
                    UserTitles title = user.getTitles();

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
                            UserTitles titles = user.getTitles();

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
                                    var title = c.getArgument("title", RankTitle.class);

                                    user.getTitles().setTitle(title);

                                    c.getSource().sendAdmin(
                                            Text.format("Set {0, user}'s title to {1}",
                                                    user, title
                                            )
                                    );
                                    return 0;
                                })
                        )
                )

                .then(literal("tier")
                        .executes(c -> {
                            User user = provider.get(c);
                            UserTitles titles = user.getTitles();

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
                                            UserTitles titles = user.getTitles();
                                            RankTier tier = c.getArgument("tier", RankTier.class);

                                            titles.setTier(tier);

                                            c.getSource().sendAdmin(
                                                    Text.format("Added tier {0} to {1, user}",
                                                            tier, user
                                                    )
                                            );
                                            return 0;
                                        })
                                )
                        )

                        .then(literal("add")
                                .then(argument("tier", TIER_ARG)
                                        .executes(c -> {
                                            User user = provider.get(c);
                                            UserTitles titles = user.getTitles();
                                            RankTier tier = c.getArgument("tier", RankTier.class);

                                            titles.addTier(tier);

                                            c.getSource().sendAdmin(
                                                    Text.format("Added tier {0} to {1, user}",
                                                            tier, user
                                                    )
                                            );
                                            return 0;
                                        })
                                )
                        )
                )

                .then(literal("available_titles")
                        .executes(c -> {
                            var user = provider.get(c);
                            UserTitles titles = user.getTitles();
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
                                .then(argument("to_add", TITLE_ARRAY_ARG)
                                        .executes(c -> addRemoveArgument(c, provider, false))
                                )
                        )

                        .then(literal("remove")
                                .then(argument("to_remove", TITLE_ARRAY_ARG)
                                        .executes(c -> addRemoveArgument(c, provider, true))
                                )
                        )
                );
    }

    private int addRemoveArgument(CommandContext<CommandSource> c, UserProvider provider, boolean remove) throws CommandSyntaxException {
        var user = provider.get(c);
        UserTitles userTitles = user.getTitles();

        Collection<RankTitle> titles = c.getArgument("to_add", Collection.class);

        for (var t: titles) {
            if (t.isDefaultTitle()) {
                throw Exceptions.defaultTitle(t);
            }
        }

        for (var t: titles) {
            userTitles.addTitle(t);
        }

        String format;

        if (remove) {
            format = "Removed titles from {0, user}: {1}";
        } else {
            format = "Added titles to {0, user}: {1}";
        }

        c.getSource().sendAdmin(
                Text.format(format,
                        user, TextJoiner.onComma()
                                .add(titles)
                )
        );
        return 0;
    }
}