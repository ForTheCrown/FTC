package net.forthecrown.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.inventories.SellShop;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.entity.Player;

public class ShopCommand extends CrownCommandBuilder {

    public ShopCommand(){
        super("shop", FtcCore.getInstance());

        setUsage("&7Usage:&r /shop [mining | farming | drops]");
        setDescription("Opens the Shop GUI in which one can sell things");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Opens the ShopGUI and allows players to sell their items
     *
     *
     * Valid usages of command:
     * - /shop
     * - /shop <farming | mining | drops | web>
     *
     * Referenced other classes:
     * - SellShop
     *
     * Author: Botul
     */

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command
                .executes(c ->{
                    Player player = getPlayerSender(c);
                    player.openInventory(new SellShop(player).mainMenu());
                    return 0;
                })
                .then(argument("drops")
                    .executes(c -> {
                        Player player = getPlayerSender(c);
                        player.openInventory(new SellShop(player).dropsMenu());
                        return 0;
                    })
                )
                .then(argument("mining")
                        .executes(c -> {
                            Player player = getPlayerSender(c);
                            player.openInventory(new SellShop(player).miningMenu());
                            return 0;
                        })
                )
                .then(argument("drops")
                        .executes(c -> {
                            Player player = getPlayerSender(c);
                            player.openInventory(new SellShop(player).dropsMenu());
                            return 0;
                        })
                )
                .then(argument("web")
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            user.sendMessage("&7Our webstore:");
                            user.sendMessage("&bhttps://for-the-crown.tebex.io/");
                            return 0;
                        })
                );
    }

    /*@Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);

        Player player = (Player) sender;
        SellShop sellShop = new SellShop(player);

        if(args.length == 0) player.openInventory(sellShop.mainMenu());
        else {
         switch (args[0]){
             default: throw new InvalidArgumentException(sender);

             case "drops":
                 player.openInventory(sellShop.dropsMenu());
                 break;
             case "mining":
                 player.openInventory(sellShop.miningMenu());
                 break;
             case "farming":
                 player.openInventory(sellShop.farmingMenu());
                 break;
             case "web":
                 player.performCommand("buy");
                 break;
             }
        }
        return true;
    }

    private static final List<String> argL = ImmutableList.of("drops", "farming", "mining", "web");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(args.length == 1) return argL;

        return null;
    }*/
}
