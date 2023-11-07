package net.forthecrown.structure;

import java.time.Duration;
import net.forthecrown.structure.commands.CommandFtcStruct;
import net.forthecrown.structure.commands.CommandStructFunction;
import net.forthecrown.utils.PeriodicalSaver;
import org.bukkit.plugin.java.JavaPlugin;

public class StructuresPlugin extends JavaPlugin {

  private PeriodicalSaver saver;

  @Override
  public void onEnable() {
    Structures.get().load();

    new CommandFtcStruct();
    new CommandStructFunction();

    saver = PeriodicalSaver.create(this::save, () -> Duration.ofMinutes(30));
    saver.start();
  }

  @Override
  public void onDisable() {
    Structures.get().save();
    saver.stop();
  }

  void save() {
    Structures.get().save();
  }
}