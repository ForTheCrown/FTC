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
                sender.sendMessage("The argument must be a number!");
                return false;
            }
        }
        sendBaltopMessage(sender, page);
        return true;
    }

    private void sendBaltopMessage(CommandSender sender, @Nonnegative int page){
        List<String> baltopList = getBaltopList();
        int index = page;
        if(page != 0) index = page*10;

        if(page > baltopList.size()/10) {
            sender.sendMessage("Out of range");
            return;
        }

        sender.sendMessage("----- Top balances -----");
        for(int i = index+1 ; i <= index+10 ; i++){
            if(baltopList.get(i) == null) break;
            sender.sendMessage(ChatColor.GOLD + "" + i + ". " + ChatColor.RESET + baltopList.get(i));
        }
        sender.sendMessage("------ Page " + (page+1) + "/" + (baltopList.size()/10) + " ------");
    }

    private List<String> getBaltopList(){
        Map<UUID, Integer> map = getSortedBalances();
        List<String> list = new ArrayList<>();
        list.add("Dummy");

        for(UUID id : getSortedBalances().keySet()){
            String message;

            message = getPlayerName(id) + ": " + map.get(id);
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
