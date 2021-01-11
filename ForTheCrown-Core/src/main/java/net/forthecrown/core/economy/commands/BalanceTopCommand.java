package net.forthecrown.core.economy.commands;

import net.forthecrown.core.economy.Economy;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnegative;
import java.util.*;

public class BalanceTopCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int page = 0;
        if(args.length >= 1) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (Exception e){
                sender.sendMessage(ChatColor.GRAY + "The argument must be a number!");
                return false;
            }

            if(page < 0){
                sender.sendMessage(ChatColor.GRAY + "Negative numbers cannot be used!");
                return false;
            }
        }
        sendBaltopMessage(sender, page);
        return true;
    }

    private void sendBaltopMessage(CommandSender sender, @Nonnegative int page){
        List<String> baltopList = getBaltopList();
        Collections.reverse(baltopList);
        int index = page;
        if(index != 0) index--;

        if(page > ((baltopList.size()-1)/10)) {
            sender.sendMessage("Out of range");
            return;
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7------ &eTop balances &7------"));
        for(int i = 0 ; i < 10 ; i++){
            if((index*10) + i > baltopList.size()) break;
            sender.sendMessage(ChatColor.GOLD + "" + ((index*10) + i+1) + ") " + ChatColor.RESET + baltopList.get((index*10) + i));
        }
        sender.sendMessage(ChatColor.GRAY + "------ "  + ChatColor.YELLOW + "Page " + (index+1) + "/" + ((baltopList.size())/10) + ChatColor.GRAY + " ------");
    }

    private List<String> getBaltopList(){
        Map<UUID, Integer> map = getSortedBalances();
        List<String> list = new ArrayList<>();

        for(UUID id : getSortedBalances().keySet()){
            String message;

            message = Bukkit.getOfflinePlayer(id).getName() + " - " + ChatColor.YELLOW + map.get(id) + " Rhines";
            list.add(message);
        }
        return list;
    }

    private Map<UUID, Integer> getSortedBalances(){
        List<Map.Entry<UUID, Integer>> list = new ArrayList<>(Economy.getBalances().getBalances().entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<UUID, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<UUID, Integer> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private String getPlayerName(UUID base){
        String name;
        try{
            name = Bukkit.getPlayer(base).getName();
        } catch (Exception e){
            try {
                name = Bukkit.getOfflinePlayer(base).getName();
            } catch (Exception e1){
                return null;
            }
        }
        return name;
    }
}
