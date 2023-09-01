package net.forthecrown.resourceworld;

import lombok.Getter;
import net.forthecrown.command.Commands;
import net.forthecrown.events.Events;
import net.forthecrown.grenadier.annotations.AnnotatedCommandContext;
import net.forthecrown.resourceworld.commands.CommandResourceWorld;
import net.forthecrown.resourceworld.listeners.BlockListener;
import net.forthecrown.resourceworld.listeners.WorldResetListener;
import net.forthecrown.utils.PeriodicalSaver;
import net.forthecrown.utils.TomlConfigs;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class RwPlugin extends JavaPlugin {

  private RwConfig rwConfig;

  private ResourceWorld resourceWorld;

  private ResourceWorldTracker tracker;

  private PeriodicalSaver saver;

  @Override
  public void onEnable() {
    this.saver = PeriodicalSaver.create(this::save, () -> rwConfig.autosaveInterval);

    this.tracker = new ResourceWorldTracker(this);
    this.resourceWorld = new ResourceWorld(this);

    reload();

    Events.register(new WorldResetListener(this));
    Events.register(new BlockListener(this));

    AnnotatedCommandContext ctx = Commands.createAnnotationContext();
    ctx.registerCommand(new CommandResourceWorld(this));
  }

  @Override
  public void onDisable() {

  }

  public void reload() {
    reloadConfig();
    resourceWorld.load();
    tracker.reload();
  }

  public void save() {
    resourceWorld.save();
    tracker.save();
  }

  @Override
  public void reloadConfig() {
    rwConfig = TomlConfigs.loadPluginConfig(this, RwConfig.class);
    saver.start();
  }
}
