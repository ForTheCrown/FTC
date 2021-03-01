package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnegative;
import java.text.DecimalFormat;
import java.util.*;

public class BalanceTopCommand extends CrownCommandBuilder {
    public BalanceTopCommand() {
        super("balancetop", FtcCore.getInstance());

        setAliases("baltop", "banktop", "cashtop", "topbals");
        setDescription("Displays all the player's balances in order from biggest to smallest");
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Explain what command is supposed to be used for..
     *
     *
     * Valid usages of command:
     * - /baltop
     * - /baltop <page number>
     *
     * Referenced other classes:
     * - Balances:
     * - Economy: Economy.getBalances
     * - FtcCore: FtcCore.getPrefix
     *
     * Author: Botul
     */

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command
                .executes(context -> {
                    sendBaltopMessage(context.getSource().getBukkitSender(), 0);
                    return 0;
                })
                .then(argument("page", IntegerArgumentType.integer(0, Math.round(((float) FtcCore.getBalances().getBalanceMap().size())/10)))
                        .executes(context -> {
                            Integer soup = context.getArgument("page", Integer.class);
                            sendBaltopMessage(context.getSource().getBukkitSender(), soup);
                            return 0;
                        })
                );
    }

    /*@Override
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) throws CrownException {
        int page = 0;
        if(args.length >= 1) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (Exception e){ throw new InvalidArgumentException(sender, "Page number must be a number"); }

            if(page < 0){ throw new InvalidArgumentException(sender, "Page number cannot be negative"); }
        }
        sendBaltopMessage(sender, page);
        return true;
    }*/

    private void sendBaltopMessage(CommandSender sender, @Nonnegative int page){
        List<String> baltopList = getBaltopList();
        Collections.reverse(baltopList);
        int index = page;
        if(index != 0) index--;

        int stupidity = Math.round(((float) baltopList.size())/10); //This is so that if you have a weird number of balances, say 158, there's an extra page for those last 8 ones

        if(page > stupidity) {
            sender.sendMessage(ChatColor.GRAY + "Out of range");
            return;
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7------ &eTop balances &7------"));
        for(int i = 0 ; i < 10 ; i++){
            if((index*10) + i >= baltopList.size()) break;
            sender.sendMessage(ChatColor.GOLD + "" + ((index*10) + i+1) + ") " + ChatColor.RESET + baltopList.get((index*10) + i));
        }
        sender.sendMessage(ChatColor.GRAY + "------- "  + ChatColor.YELLOW + "Page " + (index+1) + "/" + stupidity + ChatColor.GRAY + " -------");
    }

    private List<String> getBaltopList(){
        Map<UUID, Integer> map = getSortedBalances();
        List<String> list = new ArrayList<>();

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        decimalFormat.setGroupingUsed(true);
        decimalFormat.setGroupingSize(3);

        for(UUID id : getSortedBalances().keySet()){
            String message;

            message = Bukkit.getOfflinePlayer(id).getName() + " - " + ChatColor.YELLOW + decimalFormat.format(map.get(id)) + " Rhines";
            list.add(message);
        }
        return list;
    }

    private Map<UUID, Integer> getSortedBalances(){
        List<Map.Entry<UUID, Integer>> list = new ArrayList<>(FtcCore.getBalances().getBalanceMap().entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<UUID, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<UUID, Integer> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
