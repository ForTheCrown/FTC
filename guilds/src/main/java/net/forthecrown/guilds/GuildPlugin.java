package net.forthecrown.guilds;

import java.time.Duration;
import lombok.Getter;
import net.forthecrown.FtcServer;
import net.forthecrown.guilds.commands.GuildCommands;
import net.forthecrown.guilds.leaderboards.GuildLeaderboardSource;
import net.forthecrown.guilds.leaderboards.GuildLeaderboardSource.ScoreAccessor;
import net.forthecrown.guilds.listeners.GuildEvents;
import net.forthecrown.guilds.unlockables.Unlockables;
import net.forthecrown.guilds.waypoints.GuildWaypoints;
import net.forthecrown.leaderboards.Leaderboards;
import net.forthecrown.user.UserService;
import net.forthecrown.user.Users;
import net.forthecrown.utils.PeriodicalSaver;
import net.forthecrown.utils.TomlConfigs;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class GuildPlugin extends JavaPlugin {

  private PeriodicalSaver saver;

  private GuildManager manager;
  private GuildConfig guildConfig;

  public static GuildPlugin get() {
    return JavaPlugin.getPlugin(GuildPlugin.class);
  }

  @Override
  public void onEnable() {
    manager = new GuildManager(this);
    reloadConfig();

    FtcServer server = FtcServer.server();
    GUserProperties.init(server.getGlobalSettingsBook());

    UserService service = Users.getService();
    service.getCurrencies().register("guildExp", new GuildExpCurrency(manager));

    Unlockables.registerAll();
    GuildEvents.registerAll(this);
    GuildCommands.createCommands();
    GuildWaypoints.init(manager);

    manager.load();

    Users.getService().getNameFactory().addProfileField("ftc_guild", 34, new GuildProfileElement());

    saver = PeriodicalSaver.create(manager::save, () -> Duration.ofMinutes(30));
    saver.start();

    var sources = Leaderboards.getSources();
    sources.register("guilds/members", new GuildLeaderboardSource(manager, ScoreAccessor.MEMBERS));
    sources.register("guilds/exp", new GuildLeaderboardSource(manager, ScoreAccessor.GUILD_EXP));
    sources.register("guilds/chunks", new GuildLeaderboardSource(manager, ScoreAccessor.CHUNKS));
  }

  @Override
  public void onDisable() {
    manager.save();

    GuildWaypoints.close();
    Users.getService().getNameFactory().removeField("ftc_guild");
  }

  @Override
  public void reloadConfig() {
    guildConfig = TomlConfigs.loadPluginConfig(this, GuildConfig.class);
  }
}
