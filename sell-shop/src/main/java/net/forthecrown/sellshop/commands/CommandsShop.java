package net.forthecrown.sellshop.commands;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.sellshop.SellPermissions;
import net.forthecrown.sellshop.SellShop;
import net.forthecrown.sellshop.SellShopPlugin;
import net.kyori.adventure.text.Component;

public class CommandsShop extends FtcCommand {

  private final SellShop shop;

  public CommandsShop(SellShop shop) {
    super("shop");

    this.shop = shop;

    setAliases("sellshop");
    setDescription("Opens the server's sell shop");
    simpleUsages();

    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .executes(c -> {
          shop.getMainMenu().open(getUserSender(c));
          return 0;
        })

        .then(literal("reload")
            .requires(source -> source.hasPermission(SellPermissions.SHOP_ADMIN))

            .executes(c -> {
              SellShopPlugin.getPlugin().reloadConfig();
              c.getSource().sendSuccess(Component.text("Reloaded SellShop plugin"));
              return 0;
            })
        );
  }
}
