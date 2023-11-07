package net.forthecrown.titles.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import net.forthecrown.command.Commands;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.RegistryArguments;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.CommandData;
import net.forthecrown.grenadier.annotations.VariableInitializer;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.ArrayArgument;
import net.forthecrown.registry.Holder;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.text.TextWriters;
import net.forthecrown.titles.RankMenu;
import net.forthecrown.titles.RankTier;
import net.forthecrown.titles.TitlesPlugin;
import net.forthecrown.titles.UserRank;
import net.forthecrown.titles.UserRanks;
import net.forthecrown.titles.UserTitles;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.plugin.java.JavaPlugin;

@CommandData("file = titles_command.gcn")
public class TitlesCommand {

  static final String ADMIN_PERMISSION = "ftc.commands.ranks.admin";

  static final String ARG = "user";
  static final String TITLE = "title";
  static final String TITLES = "titles";
  static final String TIER = "tier";

  @VariableInitializer
  void createVars(Map<String, Object> map) {
    map.put("tier", ArgumentTypes.enumType(RankTier.class));

    ArgumentType<Holder<UserRank>> rankType = new RegistryArguments<>(UserRanks.REGISTRY, "Rank");

    ArgumentType<UserRank> mapped = ArgumentTypes.map(rankType, Holder::getValue);
    ArrayArgument<UserRank> array = ArgumentTypes.array(mapped);

    map.put("title", mapped);
    map.put("titles", array);
  }

  void openMenu(CommandSource source) throws CommandSyntaxException {
    User user = Commands.getUserSender(source);
    RankMenu.getInstance().open(user);
  }

  void reloadPlugin(CommandSource source) {
    TitlesPlugin plugin = JavaPlugin.getPlugin(TitlesPlugin.class);
    plugin.loadTitles();

    source.sendSuccess(Component.text("Reloaded user ranks"));
  }

  void showTitlesInfo(CommandSource source, @Argument(ARG) User user) {
    UserTitles titles = user.getComponent(UserTitles.class);

    var writer = TextWriters.newWriter();
    writer.setFieldValueStyle(Style.style(NamedTextColor.GRAY));

    writer.formatted("{0, user}'s rank tier and titles:", user);
    writer.field("Tier", titles.getTier().getDisplayName());

    UserRank title = titles.getTitle();
    if (title != UserRanks.DEFAULT) {
      writer.field("Title", title);
    }

    Set<UserRank> available = titles.getAvailable();
    if (!available.isEmpty()) {
      writer.field("Available", TextJoiner.onComma().add(available));
    }

    source.sendMessage(writer.asComponent());
  }

  void setTitle(CommandSource source, @Argument(ARG) User user, @Argument(TITLE) UserRank rank)
      throws CommandSyntaxException
  {
    UserTitles titles = user.getComponent(UserTitles.class);

    if (titles.getTitle().equals(rank)) {
      throw Exceptions.format("{0} is already {1, user}'s title", rank, user);
    }

    titles.setTitle(rank);

    source.sendSuccess(
        Text.format("Set &f{0}&r as &e{1, user}&r's active title.",
            NamedTextColor.GRAY,
            rank, user
        )
    );
  }

  void addTitles(
      CommandSource source,
      @Argument(ARG) User user,
      @Argument(TITLES) Collection<UserRank> ranks
  ) throws CommandSyntaxException {

    UserTitles titles = user.getComponent(UserTitles.class);

    for (UserRank rank : ranks) {
      ensureNotDefault(rank);

      if (titles.hasTitle(rank)) {
        throw Exceptions.format("{0, user} already has the title {1}", user, rank);
      }
    }

    ranks.forEach(titles::addTitle);

    if (ranks.size() == 1) {
      source.sendSuccess(
          Text.format("Gave &e{0, user}&r the &f{1}&r title.",
              user, ranks.iterator().next()
          )
      );
    } else {
      source.sendSuccess(
          Text.format("Gave &e{0, user}&r &f{1, number}&r titles.",
              user, ranks.size()
          )
      );
    }
  }

  void removeTitles(
      CommandSource source,
      @Argument(ARG) User user,
      @Argument(TITLES) Collection<UserRank> ranks
  ) throws CommandSyntaxException {
    UserTitles titles = user.getComponent(UserTitles.class);

    for (UserRank rank : ranks) {
      ensureNotDefault(rank);

      if (!titles.hasTitle(rank)) {
        throw Exceptions.format("{0, user} already doesn't have the title {1}", user, rank);
      }
    }

    ranks.forEach(titles::removeTitle);

    if (ranks.size() == 1) {
      source.sendSuccess(
          Text.format("Removed the &f{1}&r title from &e{0, user}&r.",
              user, ranks.iterator().next()
          )
      );
    } else {
      source.sendSuccess(
          Text.format("Removed &f{1}&r titles from &e{0, user}&r.",
              user, ranks.size()
          )
      );
    }
  }

  void setTier(CommandSource source, @Argument(ARG) User user, @Argument(TIER) RankTier tier)
      throws CommandSyntaxException
  {
    UserTitles titles = user.getComponent(UserTitles.class);

    if (titles.getTier() == tier) {
      throw Exceptions.format("{0} is already {1, user}'s rank tier", tier.getDisplayName(), user);
    }

    titles.setTier(tier);

    source.sendSuccess(
        Text.format("Set &e{0, user}&r's rank tier to {1}",
            user, tier.getDisplayName()
        )
    );
  }

  static void ensureNotDefault(UserRank rank) throws CommandSyntaxException {
    if (!rank.isDefaultTitle()) {
      return;
    }

    throw Exceptions.format("{0} is a tier-default title, these cannot be removed or added",
        rank
    );
  }
}