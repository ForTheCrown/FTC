package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.CrownItems;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.entity.Player;

public class CommandWithdraw extends CrownCommandBuilder {

    public CommandWithdraw(){
        super("withdraw", FtcCore.getInstance());

        maxMoney = FtcCore.getMaxMoneyAmount();

        setUsage("&7Usage:&r /withdraw <amount>");
        setDescription("Used to get cold coins from your balance");
        register();
    }

    private final int maxMoney;

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command.then(argument("amount", IntegerArgumentType.integer(1, maxMoney))
                .suggests((c, b) -> suggestMatching(b, "1", "5", "10", "50", "100", "500", "1000", "5000"))

                .executes(c -> {
                    Player player = getPlayerSender(c);
                    Balances bals = FtcCore.getBalances();
                    CrownUser user = getUserSender(c);

                    int amount = c.getArgument("amount", Integer.class);

                    if(amount > bals.get(player.getUniqueId())) throw new CrownCommandException("You cannot afford that!");
                    if(player.getInventory().firstEmpty() == -1) throw new CrownCommandException("Your inventory is full! No space for coins!");

                    bals.add(player.getUniqueId(), -amount, false);
                    player.getInventory().addItem(CrownItems.getCoins(amount));
                    user.sendMessage("&7You got a coin that's worth &6" + amount + " Rhines");
                    return 0;
                })
        );
    }

    /*@Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);

        if(args.length != 1) throw new InvalidCommandExecution(sender, CrownUtils.translateHexCodes(getUsage())); // return false just doesn't work wtf

        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        }catch (Exception e) { throw new InvalidArgumentException(sender); }

        if(amount <= 0) throw new InvalidArgumentException(sender, "The amount cannot be negative or zero!");

        Player player = (Player) sender;
        Balances bals = FtcCore.getBalances();
        CrownUser user = FtcCore.getUser(player.getUniqueId());

        if(amount > bals.getBalance(player.getUniqueId())) throw new CannotAffordTransaction(player);
        if(player.getInventory().firstEmpty() == -1) throw new InvalidCommandExecution(player, "&cYour inventory is full! &7No space for the coin");

        bals.setBalance(player.getUniqueId(), bals.getBalance(player.getUniqueId()) - amount);
        player.getInventory().setItem(player.getInventory().firstEmpty(), CrownItems.getCoins(amount));
        user.sendMessage("&7You got a coin that's worth &6" + amount + " Rhines");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> argList = new ArrayList<>();

        argList.add("1");
        argList.add("5");
        argList.add("10");
        argList.add("50");
        argList.add("100");
        argList.add("500");
        argList.add("1000");
        argList.add("5000");

        return argList;
    }*/
}
