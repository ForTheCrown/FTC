package net.forthecrown.pirates.commands;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.CrownCommand;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.pirates.Pirates;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandParrot extends CrownCommand {

    public CommandParrot(){
        super("parrot", Pirates.plugin);

        setPermission(null);
        register();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) throw new CrownException(sender, "Only players may execute this command!");
        Player player = (Player) sender;
        CrownUser user = FtcCore.getUser(player.getUniqueId());

        if(user.getBranch() != Branch.PIRATES) throw new CrownException(sender, "&7Only pirates can have a parrot!");

        List<String> colors = new ArrayList<>();
        colors.add("gray"); colors.add("green"); colors.add("aqua"); colors.add("blue"); colors.add("red");

        if (args.length == 0) {
            if (Pirates.plugin.events.parrots.containsValue(player.getUniqueId())) {
                removeOldParrot(player, (Parrot) player.getShoulderEntityLeft());
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
                return true;
            }
            else if (player.getShoulderEntityLeft() != null) {
                player.sendMessage(ChatColor.GRAY + "Don't try to remove that pretty parrot D:");
                return false;
            }
            else {
                player.sendMessage(ChatColor.RED + "/parrot [gray/green/aqua/blue/red]");
                player.sendMessage(ChatColor.RED + "/parrot silent " + ChatColor.GRAY + "to silence your parrot.");
                player.sendMessage(ChatColor.RED + "/parrot " + ChatColor.GRAY + "to hide your parrot.");
                return false;
            }
        }

        if ((!colors.contains(args[0])) && (!args[0].equalsIgnoreCase("silent"))) {
            player.sendMessage(ChatColor.RED + "/parrot [gray/green/aqua/blue/red]");
            player.sendMessage(ChatColor.RED + "/parrot silent " + ChatColor.GRAY + "to silence your parrot.");
            player.sendMessage(ChatColor.RED + "/parrot " + ChatColor.GRAY + "to hide your parrot.");
            return false;
        }
        if (player.getShoulderEntityLeft() != null && (!Pirates.plugin.events.parrots.containsValue(player.getUniqueId()))) {
            player.sendMessage(ChatColor.GRAY + "You have a regular parrot on your shoulder atm!");
            return false;
        }


        List<String> pets = user.getPets();
        switch (args[0]) {
            case "gray":
                if (pets.contains("gray_parrot")) {
                    makeParrot(Parrot.Variant.GRAY, player, false);
                    return true;
                }
                else {
                    player.sendMessage(ChatColor.GRAY + "You need to buy a gray parrot first.");
                    return false;
                }
            case "green":
                if (pets.contains("green_parrot")) {
                    makeParrot(Parrot.Variant.GREEN, player, false);
                    return true;
                }
                else {
                    player.sendMessage(ChatColor.GRAY + "You need to buy a green parrot first.");
                    return false;
                }
            case "blue":
                if (pets.contains("blue_parrot")) {
                    makeParrot(Parrot.Variant.BLUE, player, false);
                    return true;
                }
                else {
                    player.sendMessage(ChatColor.GRAY + "You need to buy a blue parrot first.");
                    return false;
                }
            case "red":
                if (player.hasPermission("ftc.donator2")) {
                    makeParrot(Parrot.Variant.RED, player, false);
                    return true;
                }
                else {
                    player.sendMessage(ChatColor.GRAY + "Only captains can have a red parrot pet.");
                    return false;
                }
            case "aqua":
                if (player.hasPermission("ftc.donator3")) {
                    makeParrot(Parrot.Variant.CYAN, player, false);
                    return true;
                }
                else {
                    player.sendMessage(ChatColor.GRAY + "Only admirals can have an aqua parrot pet.");
                    return false;
                }
            case "silent":
                Parrot parrot = (Parrot) player.getShoulderEntityLeft();
                if (parrot != null)
                {
                    if (!parrot.isSilent())
                    {
                        player.sendMessage(ChatColor.GRAY + "Your parrot will stay quiet now.");
                        Parrot.Variant color = parrot.getVariant();
                        removeOldParrot(player, parrot);
                        makeParrot(color, player, true);
                    }
                    else
                    {
                        player.sendMessage(ChatColor.GRAY + "Your parrot will no longer stay quiet.");
                        Parrot.Variant color = parrot.getVariant();
                        removeOldParrot(player, parrot);
                        makeParrot(color, player, false);
                    }
                    return true;
                }
        }

        return false;
    }

    @SuppressWarnings("deprecation")
    private void removeOldParrot(Player player, Parrot parrot) {
        player.setShoulderEntityLeft(null);
        if (parrot != null)
        {
            Pirates.plugin.events.parrots.remove(parrot.getUniqueId());
            parrot.remove();
        }
    }

    @SuppressWarnings("deprecation")
    private void makeParrot(Parrot.Variant color, Player player, Boolean silent) {
        Parrot parrot = player.getWorld().spawn(player.getLocation(), Parrot.class);
        parrot.setVariant(color);
        parrot.setSilent(silent);
        Pirates.plugin.events.parrots.put(parrot.getUniqueId(), player.getUniqueId());
        player.setShoulderEntityLeft(parrot);
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
    }
}
