package net.forthecrown.pirates.commands;

import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.pirates.Pirates;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class CommandLeave extends CrownCommandBuilder {

    public CommandLeave(){
        super("leave", Pirates.plugin);
        setPermission(null);
        register();
    }

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command.executes(c -> {
            Player player = getPlayerSender(c);
            Location playerloc = player.getLocation();

            if (!(playerloc.getWorld().getName().contains("world_void") && (playerloc.getX() < -880) && (playerloc.getZ() < 96) && (playerloc.getZ() > -520))) {
                player.sendMessage(ChatColor.GRAY + "This command can only be executed in the grappling parkour.");
                return 0;
            }

            player.getInventory().clear();
            player.teleport(new Location(Bukkit.getWorld("world_void"), -800.5, 232, 11.5, -90, 0));
            player.setBedSpawnLocation(new Location(Bukkit.getWorld("world"), 200, 70, 1000), true);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            return 0;
        });
    }

    /*@Override
    public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
        // Checks if sender is a player.
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can do this.");
            return false;
        }

        Player player = (Player) sender;
        Location playerloc = player.getLocation();
        if (!(playerloc.getWorld().getName().contains("world_void") && (playerloc.getX() < -880) && (playerloc.getZ() < 96) && (playerloc.getZ() > -520)))
        {
            player.sendMessage(ChatColor.GRAY + "This command can only be executed in the grappling parkour.");
            return false;
        }

        player.getInventory().clear();
        player.teleport(new Location(Bukkit.getWorld("world_void"), -800.5, 232, 11.5, -90, 0));
        player.setBedSpawnLocation(new Location(Bukkit.getWorld("world"), 200, 70, 1000), true);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
        return true;
    }*/
}
