package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnegative;
import java.util.*;

public class CommandBalanceTop extends CrownCommandBuilder {
    public CommandBalanceTop() {
        super("balancetop", FtcCore.getInstance());

        maxPage = Math.round(((float) FtcCore.getBalances().getBalanceMap().size())/10);

        setAliases("baltop", "banktop", "cashtop", "topbals", "ebaltop", "ebalancetop");
        setDescription("Displays all the player's balances in order from biggest to smallest");
        register();
    }

    private final int maxPage;

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
     * Author: Botul
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> { //No args -> show first page
                    sendBaltopMessage(c.getSource().asBukkit(), 0);
                    return 0;
                })
                .then(argument("page", IntegerArgumentType.integer(1, maxPage))
                        .executes(c -> { //Page number given -> show that page
                            Integer soup = c.getArgument("page", Integer.class); //Delicious soup
                            sendBaltopMessage(c.getSource().asBukkit(), soup);
                            return 0;
                        })
                );
    }

    //Send the message
    private void sendBaltopMessage(CommandSender sender, @Nonnegative int page){
        List<Component> baltopList = getSortedBalances();
        if(page > 0) page--;

        final TextComponent border = Component.text("------").color(NamedTextColor.GRAY).decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.TRUE);
        TextComponent.Builder text = Component.text()
                .append(border)
                .append(Component.text(" Top balances ").color(NamedTextColor.YELLOW))
                .append(border)
                .append(Component.newline());

        for(int i = 0 ; i < 10 ; i++){
            if((page*10) + i >= baltopList.size()) break;
            int index = (page*10) + i;

            text.append(Component.text((index+1) + ") ").color(NamedTextColor.GOLD))
                    .append(baltopList.get(index))
                    .append(Component.newline());
        }
        text
                .append(border)
                .append(Component.text(" Page " +  (page+1) + "/" + maxPage + " ").color(NamedTextColor.YELLOW))
                .append(border);

        //ngl, now that this is just sending one message that's appended together, there's no weird 1 frame thing where
        // the text gets sent line by line lol. It just comes out as one :D
        sender.sendMessage(text);
    }

    //Gets a sorted list of balances, descending order, and formats it into components
    private List<Component> getSortedBalances(){
        List<Map.Entry<UUID, Integer>> list = new ArrayList<>(FtcCore.getBalances().getBalanceMap().entrySet());
        list.sort(Map.Entry.comparingByValue());

        List<Component> sortedList = new ArrayList<>();
        for (Map.Entry<UUID, Integer> entry : list) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getKey());
            if(player == null || player.getName() == null) continue;

            sortedList.add(Component.text()
                    .append(Component.text(player.getName()))
                    .append(Component.text(" - "))
                    .append(Balances.formatted(entry.getValue()).color(NamedTextColor.YELLOW))
                    .build());
        }

        Collections.reverse(sortedList);
        return sortedList;
    }
}
