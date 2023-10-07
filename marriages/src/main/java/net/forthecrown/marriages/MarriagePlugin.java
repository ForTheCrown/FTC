package net.forthecrown.marriages;

import net.forthecrown.FtcServer;
import net.forthecrown.events.Events;
import net.forthecrown.marriages.commands.CommandDivorce;
import net.forthecrown.marriages.commands.CommandMarriageAccept;
import net.forthecrown.marriages.commands.CommandMarriageChat;
import net.forthecrown.marriages.commands.CommandMarriageDeny;
import net.forthecrown.marriages.commands.CommandMarry;
import net.forthecrown.marriages.listeners.ChatListener;
import net.forthecrown.marriages.listeners.MarriageListener;
import net.forthecrown.user.Users;
import org.bukkit.plugin.java.JavaPlugin;

public class MarriagePlugin extends JavaPlugin {

  @Override
  public void onEnable() {

    new CommandDivorce();
    new CommandMarriageAccept();
    new CommandMarriageChat();
    new CommandMarriageDeny();
    new CommandMarry();

    Events.register(new MarriageListener());
    Events.register(new ChatListener());

    var ftcServer = FtcServer.server();
    Marriages.defineSettings(ftcServer.getGlobalSettingsBook());

    var nameFactory = Users.getService().getNameFactory();
    nameFactory.addProfileField("spouse", new SpouseProfileElement());
  }

  @Override
  public void onDisable() {
    var nameFactory = Users.getService().getNameFactory();
    nameFactory.removeField("spouse");
  }
}
