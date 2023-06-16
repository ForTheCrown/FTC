package net.forthecrown.titles.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Map;
import java.util.Set;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.RegistryArguments;
import net.forthecrown.command.arguments.UserParseResult;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.CommandData;
import net.forthecrown.grenadier.annotations.VariableInitializer;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.registry.Holder;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.text.TextWriters;
import net.forthecrown.titles.RankTier;
import net.forthecrown.titles.UserRank;
import net.forthecrown.titles.UserRanks;
import net.forthecrown.titles.UserTitles;
import net.forthecrown.user.User;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;

@CommandData("file = titles_command.gcn")
public class TitlesCommand {

  static final String ARG = "user";
  static final String TITLE = "title";
  static final String TIER = "tier";

  @VariableInitializer
  void createVars(Map<String, Object> map) {
    map.put("tier", ArgumentTypes.enumType(RankTier.class));

    ArgumentType<Holder<UserRank>> rankType
        = new RegistryArguments<>(UserRanks.REGISTRY, "UserRank");

    map.put("title", ArgumentTypes.map(rankType, Holder::getValue));
  }

  User resultToUser(CommandSource source, UserParseResult result) throws CommandSyntaxException {
    return result.get(source, false);
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

    source.sendMessage(
        Text.format("Set &f{0}&r as &e{1, user}&r's active title.",
            NamedTextColor.GRAY,
            rank, user
        )
    );
  }

  void addTitle(CommandSource source, @Argument(ARG) User user, @Argument(TITLE) UserRank rank)
      throws CommandSyntaxException
  {
    ensureNotDefault(rank);
    UserTitles titles = user.getComponent(UserTitles.class);

    if (titles.hasTitle(rank)) {
      throw Exceptions.format("{0, user} already has the title {1}", user, rank);
    }

    titles.addTitle(rank);

    source.sendMessage(
        Text.format("Gave &e{0, user}&r the &f{1}&r title.",
            user, rank
        )
    );
  }

  void removeTitle(CommandSource source, @Argument(ARG) User user, @Argument(TITLE) UserRank rank)
      throws CommandSyntaxException
  {
    ensureNotDefault(rank);
    UserTitles titles = user.getComponent(UserTitles.class);

    if (!titles.hasTitle(rank)) {
      throw Exceptions.format("{0, user} already doesn't have the title {1}", user, rank);
    }

    titles.removeTitle(rank);

    source.sendMessage(
        Text.format("Removed the &f{1}&r title from &e{0, user}&r.",
            user, rank
        )
    );
  }

  void setTier(CommandSource source, @Argument(ARG) User user, @Argument(TIER) RankTier tier)
      throws CommandSyntaxException
  {
    UserTitles titles = user.getComponent(UserTitles.class);

    if (titles.getTier() == tier) {
      throw Exceptions.format("{0} is already {1, user}'s rank tier", tier.getDisplayName());
    }

    titles.setTier(tier);

    source.sendMessage(
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