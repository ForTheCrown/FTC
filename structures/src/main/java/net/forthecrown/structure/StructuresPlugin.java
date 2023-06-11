package net.forthecrown.structure;

import net.forthecrown.structure.commands.CommandFtcStruct;
import net.forthecrown.structure.commands.CommandStructFunction;
import org.bukkit.plugin.java.JavaPlugin;

public class StructuresPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    Structures.get().load();

    new CommandFtcStruct();
    new CommandStructFunction();
  }

  @Override
  public void onDisable() {
    Structures.get().save();
  }
}