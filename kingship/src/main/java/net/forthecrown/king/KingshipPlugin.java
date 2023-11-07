package net.forthecrown.king;

import lombok.Getter;
import net.forthecrown.events.Events;
import net.forthecrown.user.Users;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class KingshipPlugin extends JavaPlugin {

  private Kingship kingship;

  @Override
  public void onEnable() {
    kingship = new Kingship();

    MonarchNameElement element = new MonarchNameElement(kingship);
    var nameFactory = Users.getService().getNameFactory();
    nameFactory.addPrefix(MonarchNameElement.NAME, 10, element);

    Events.register(new CoinListener(kingship));

    new CommandKingMaker(kingship);

    kingship.load();
  }

  @Override
  public void onDisable() {
    var nameFactory = Users.getService().getNameFactory();
    nameFactory.removePrefix(MonarchNameElement.NAME);

    kingship.save();
  }
}
