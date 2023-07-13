package net.forthecrown.titles;

import static net.forthecrown.titles.TitleSettings.SEE_RANKS;

import java.nio.file.Path;
import net.forthecrown.FtcServer;
import net.forthecrown.Loggers;
import net.forthecrown.command.Commands;
import net.forthecrown.grenadier.annotations.AnnotatedCommandContext;
import net.forthecrown.registry.Registries;
import net.forthecrown.titles.commands.TitlesCommand;
import net.forthecrown.user.name.UserNameFactory;
import net.forthecrown.user.name.DisplayIntent;
import net.forthecrown.user.UserService;
import net.forthecrown.user.Users;
import net.forthecrown.utils.io.SerializationHelper;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

public class TitlesPlugin extends JavaPlugin {

  private static final Logger LOGGER = Loggers.getLogger();

  @Override
  public void onEnable() {
    UserService service = Users.getService();
    service.registerComponent(UserTitles.class);
    addPrefixElement(service.getNameFactory());

    loadTitles();

    AnnotatedCommandContext ctx = Commands.createAnnotationContext();
    ctx.registerCommand(new TitlesCommand());

    FtcServer server = FtcServer.server();
    TitleSettings.add(server.getGlobalSettingsBook());
  }

  void addPrefixElement(UserNameFactory factory) {
    factory.addPrefix((user, context) -> {
      // Don't display rank prefix if the user has disabled it,
      // only in certain circumstances though
      if (context.intentMatches(DisplayIntent.UNSET, DisplayIntent.HOVER_TEXT)
          && !context.viewerProperty(SEE_RANKS)
      ) {
        return null;
      }

      UserTitles titles = user.getComponent(UserTitles.class);
      UserRank rank = titles.getTitle();

      if (rank == UserRanks.DEFAULT) {
        return null;
      }

      return rank.getPrefix();
    });
  }

  public void loadTitles() {
    UserRanks.clearNonConstants();

    saveResource("ranks.toml", false);

    SerializationHelper.readAsJson(ranksFile(), wrapper -> {
      for (var e : wrapper.entrySet()) {
        if (!Registries.isValidKey(e.getKey())) {
          LOGGER.warn("{} is an invalid registry key", e.getKey());
          continue;
        }

        if (!(e.getValue().isJsonObject())) {
          LOGGER.warn("Expected {} to be JSON object, was {}",
              e.getKey(), e.getValue()
          );
          continue;
        }

        // IDK if it matters, but even though there's an init call in
        // Bootstrap, this is at the moment, the first call to this class
        // during startup, meaning this call loads the class
        UserRanks.deserialize(e.getValue())
            .apply(s -> {
              LOGGER.warn("Couldn't parse rank at {}: {}", e.getKey(), s);
            }, rank -> {
              UserRanks.REGISTRY.register(e.getKey(), rank);
            });
      }
    });
  }

  private Path ranksFile() {
    return getDataFolder().toPath().resolve("ranks.toml");
  }
}