package net.forthecrown.economy.signshops.commands;

import net.forthecrown.Permissions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.economy.signshops.ShopManager;
import net.forthecrown.economy.signshops.SignShop;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.Messages;

public class CommandShopReselling extends FtcCommand {

  private final ShopManager manager;

  public CommandShopReselling(ShopManager manager) {
    super("shopreselling");

    this.manager = manager;

    setAliases("shopresell");
    setPermission(Permissions.DEFAULT);
    setDescription("Shop reselling");
    simpleUsages();

    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command.executes(c -> {
      SignShop shop = CommandEditShop.getShop(manager, c.getSource().asPlayer());
      boolean state = !shop.isResellDisabled();

      shop.setResellDisabled(state);
      shop.update();

      c.getSource().sendMessage(Messages.toggleMessage("Shop reselling {3}", state));
      return 0;
    });
  }
}
