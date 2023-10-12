package net.forthecrown.usables;

import java.time.Duration;
import lombok.Getter;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.usables.actions.Actions;
import net.forthecrown.usables.commands.UsablesCommands;
import net.forthecrown.usables.conditions.Conditions;
import net.forthecrown.usables.listeners.UsablesListeners;
import net.forthecrown.usables.objects.Kit;
import net.forthecrown.usables.objects.Warp;
import net.forthecrown.usables.trigger.TriggerManager;
import net.forthecrown.utils.PeriodicalSaver;
import net.forthecrown.utils.TomlConfigs;
import net.forthecrown.utils.io.PathUtil;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class UsablesPlugin extends JavaPlugin {

  private Registry<ObjectType<? extends Action>> actions;
  private Registry<ObjectType<? extends Condition>> conditions;

  private CmdUsables<Warp> warps;
  private CmdUsables<Kit> kits;

  private TriggerManager triggers;

  private PeriodicalSaver saver;

  private UsablesConfig usablesConfig;

  public static UsablesPlugin get() {
    return JavaPlugin.getPlugin(UsablesPlugin.class);
  }

  @Override
  public void onEnable() {
    actions = Registries.newFreezable();
    conditions = Registries.newFreezable();
    registerDefaults();

    reloadConfig();

    var dir = PathUtil.pluginPath();
    kits = new CmdUsables<>(dir.resolve("kits.dat"), Kit::new);
    warps = new CmdUsables<>(dir.resolve("warps.dat"), Warp::new);

    triggers = new TriggerManager(dir.resolve("triggers.dat"));

    UsablesListeners.registerAll(this);

    saver = PeriodicalSaver.create(this::save, () -> Duration.ofMinutes(30));

    UsablesCommands.createCommands(this);
  }

  public void save() {
    warps.save();
    kits.save();
    triggers.save();
  }

  @Override
  public void onDisable() {
    actions = null;
    conditions = null;

    save();
  }

  public void reload() {
    reloadConfig();

    warps.load();
    kits.load();
    triggers.load();
  }

  @Override
  public void reloadConfig() {
    usablesConfig = TomlConfigs.loadPluginConfig(this, UsablesConfig.class);
  }

  private void registerDefaults() {
    Actions.registerAll(actions);
    Conditions.registerAll(conditions);
  }

  public void freezeRegistries() {
    actions.freeze();
    conditions.freeze();
  }
}
