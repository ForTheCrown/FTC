package net.forthecrown.afk;

import net.forthecrown.afk.commands.CommandAfk;
import net.forthecrown.afk.listeners.AfkListener;
import net.forthecrown.events.Events;
import net.forthecrown.user.Users;
import net.forthecrown.user.name.UserNameFactory;
import org.bukkit.plugin.java.JavaPlugin;

public class AfkPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    new CommandAfk();
    Events.register(new AfkListener());

    UserNameFactory factory = Users.getService().getNameFactory();
    factory.addSuffix("afk.suffix", 1, new AfkNameElement());
    factory.addProfileField("afk.reason", 31, new AfkProfileField());
  }

  @Override
  public void onDisable() {
    UserNameFactory factory = Users.getService().getNameFactory();
    factory.removeSuffix("afk.suffix");
    factory.removeField("afk.reason");
  }
}
