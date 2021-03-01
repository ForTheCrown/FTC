package net.forthecrown.core.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DepositCommand extends CrownCommandBuilder {

    public DepositCommand(){
        super("deposit", FtcCore.getInstance());

        setUsage("&7Usage: &r/deposit");
        register();
    }

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command.executes(c -> {
            Player player = getPlayerSender(c);

            if(player.getInventory().getItemInMainHand().getType() != Material.SUNFLOWER
                    && (!player.getInventory().getItemInMainHand().hasItemMeta()
                    || !player.getInventory().getItemInMainHand().getItemMeta().lore().get(0).contains(Component.text("Worth "))))
                throw new CrownCommandException("You need to be holding the coins you wish to deposit");

            ItemStack mainItem = player.getInventory().getItemInMainHand();

            int amount;
            try {
                Component component = mainItem.getItemMeta().lore().get(0);
                String lore = ChatColor.stripColor(CrownUtils.getStringFromComponent(component)).replaceAll("[\\D]", "").trim();
                amount = Integer.parseInt(lore);
            } catch (NumberFormatException e) { throw new CrownCommandException( "You need to be holding the coins you wish to deposit 2"); }

            Balances bals = FtcCore.getBalances();
            bals.addBalance(player.getUniqueId(), amount, false);
            player.sendMessage(CrownUtils.translateHexCodes("&7You deposited " + mainItem.getAmount() + " coins and received &6" + amount + " Rhines"));
            player.getInventory().removeItem(mainItem);

            return 0;
        });
    }

    /*@Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args)  {
        if(!(sender instanceof Player)) throw new NonPlayerExecutor(sender);

        Player player = (Player) sender;

        if(player.getInventory().getItemInMainHand().getType() != Material.SUNFLOWER
                && (!player.getInventory().getItemInMainHand().hasItemMeta()
                || !player.getInventory().getItemInMainHand().getItemMeta().getLore().get(0).contains("Worth ")))
            throw new InvalidCommandExecution(sender, "&cYou need to be holding the coins you wish to deposit");

        ItemStack mainItem = player.getInventory().getItemInMainHand();

        int amount;
        try {
            amount = Integer.parseInt(ChatColor.stripColor(mainItem.getItemMeta().getLore().get(0)).replaceAll("Worth ", "").replaceAll(" Rhines", ""));
        } catch (Exception e) { throw new InvalidCommandExecution(sender, "&cYou need to be holding the coins you wish to deposit"); }

        amount = amount * mainItem.getAmount();

        Balances bals = FtcCore.getBalances();
        bals.addBalance(player.getUniqueId(), amount, false);
        player.sendMessage(CrownUtils.translateHexCodes("&7You deposited " + mainItem.getAmount() + " coins and received &6" + amount + " Rhines"));
        player.getInventory().removeItem(mainItem);
        return true;
    }*/
}
