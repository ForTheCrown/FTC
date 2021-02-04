package net.forthecrown.core.commands.emotes;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.CrownCommand;
import net.forthecrown.core.exceptions.InvalidCommandExecution;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Mwah extends CrownCommand {

    public Mwah(){
        super("mwah", FtcCore.getInstance());

        setAliases("smooch", "kiss");
        setPermission("ftc.emotes");
        setDescription("Kisses another player.");
        register();
    }

    private List<Player> onCooldown = new ArrayList<>();

    @Override
    public boolean run(CommandSender sender, Command command, String label, String[] args) {
        // Sender must be a player:
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may execute this command.");
            return false;
        }
        Player player = (Player) sender;
        Location loc = player.getLocation();

        if(FtcCore.isOnCooldown(player)) throw new InvalidCommandExecution(player, "You kiss too much lol");

        // Command no args or target = sender:
        if (args.length < 1 || args[0].equalsIgnoreCase(player.getName())) {
            player.sendMessage(ChatColor.YELLOW + "Love yourself!" + ChatColor.RESET + " ( ^ 3^) ❤");
            player.playSound(loc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);
            player.spawnParticle(Particle.HEART, loc.getX(), loc.getY()+1, loc.getZ(), 5, 0.5, 0.5, 0.5);
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if(target == null){
            player.sendMessage(args[0] + " is not a currently online player.");
            return false;
        }

        CrownUser playerData = FtcCore.getUser(player.getUniqueId());
        CrownUser targetData = FtcCore.getUser(target.getUniqueId());

        // Sender should have emotes enabled:
        if(!playerData.allowsEmotes()){
            FtcCore.senderEmoteOffMessage(player);;
            return false;
        }
        if(!targetData.allowsEmotes()){
            player.sendMessage(ChatColor.GRAY + "This player has disabled emotes.");
            return false;
        }

        // Actual smooching:
        player.sendMessage(ChatColor.RED + "❤" + ChatColor.RESET + " You smooched " + target.getName() + ChatColor.RED + " ❤");

        TextComponent mwahBack = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&c♥ &e" + player.getName() + " &rsmooched you! &r( ^ 3^) &c❤"));
        mwahBack.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mwah " + player.getName()));
        mwahBack.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to smooch them back")));
        target.spigot().sendMessage(mwahBack);

        Location targetLoc = target.getLocation();
        target.getWorld().playSound(targetLoc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);
        target.getWorld().spawnParticle(Particle.HEART, targetLoc.getX(), targetLoc.getY()+1, targetLoc.getZ(), 5, 0.5, 0.5, 0.5);

        loc.getWorld().spawnParticle(Particle.HEART, loc.getX(), loc.getY()+1, loc.getZ(), 5, 0.5, 0.5, 0.5);
        loc.getWorld().playSound(loc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);

        // Put sender on cooldown:
        FtcCore.addToCooldown(player, 5*20, true);
        return true;
    }
}
