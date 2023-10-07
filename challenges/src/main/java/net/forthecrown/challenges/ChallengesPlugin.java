package net.forthecrown.challenges;

import java.time.Duration;
import lombok.Getter;
import net.forthecrown.challenges.commands.CommandChallenges;
import net.forthecrown.challenges.listeners.ChallengeListeners;
import net.forthecrown.utils.PeriodicalSaver;
import net.forthecrown.utils.TomlConfigs;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class ChallengesPlugin extends JavaPlugin {

  private ChallengeManager challenges;
  private ChallengeConfig pluginConfig;

  private PeriodicalSaver saver;

  @Override
  public void onEnable() {
    DataFix.execute();

    challenges = new ChallengeManager(this);

    saver = PeriodicalSaver.create(challenges::save, () -> Duration.ofMinutes(30));
    saver.start();

    ChallengeListeners.registerAll(this);
    new CommandChallenges(challenges);
  }

  @Override
  public void onDisable() {
    challenges.save();
  }

  public void load() {
    reloadConfig();
    challenges.load();
  }

  @Override
  public void reloadConfig() {
    this.pluginConfig = TomlConfigs.loadPluginConfig(this, ChallengeConfig.class);
  }
}
