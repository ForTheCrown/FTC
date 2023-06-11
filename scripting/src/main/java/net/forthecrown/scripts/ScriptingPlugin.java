package net.forthecrown.scripts;

import java.nio.file.Path;
import net.forthecrown.command.Commands;
import net.forthecrown.grenadier.annotations.AnnotatedCommandContext;
import net.forthecrown.scripts.commands.ScriptingCommand;
import org.bukkit.plugin.java.JavaPlugin;

class ScriptingPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    ScriptService service = new ServiceImpl(Path.of("scripts"), this);
    Scripts.setService(service);

    AnnotatedCommandContext ctx = Commands.createAnnotationContext();
    ctx.registerCommand(new ScriptingCommand());
  }

  @Override
  public void onDisable() {
    ServiceImpl service = (ServiceImpl) Scripts.getService();
    service.close();
  }
}