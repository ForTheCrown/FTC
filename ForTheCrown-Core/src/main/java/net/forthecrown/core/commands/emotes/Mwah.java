package net.forthecrown.core.commands.emotes;

import net.forthecrown.core.Cooldown;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.CrownCommand;
import net.forthecrown.core.exceptions.EmoteDisabledException;
import net.forthecrown.core.exceptions.InvalidCommandExecution;
import net.forthecrown.core.exceptions.NonPlayerExecutor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
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
    public boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        // Sender must be a player:
        if (!(sender instanceof Player)) throw new NonPlayerExecutor(sender);

        Player player = (Player) sender;
        Location loc = player.getLocation();

        if(Cooldown.contains(sender, "Core_Emote_Mwah")) throw new InvalidCommandExecution(player, "You kiss too much lol");

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
        if(!playerData.allowsEmotes()) throw new EmoteDisabledException(sender).senderDisabled();
        if(!targetData.allowsEmotes()) throw new EmoteDisabledException(sender).targetDisabled();

        Cooldown.add(sender, "Core_Emote_Mwah", 5*20);

        // Actual smooching:
        player.sendMessage(ChatColor.RED + "❤" + ChatColor.RESET + " You smooched " + target.getName() + ChatColor.RED + " ❤");

        TextComponent mwahBack = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&c♥ &e" + player.getName() + " &rsmooched you! &r( ^ 3^) &c❤"));
        mwahBack.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mwah " + player.getName()));
        mwahBack.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to smooch them back")));
        target.spigot().sendMessage(mwahBack);

        if(target.getGameMode() != GameMode.SPECTATOR){
            Location targetLoc = target.getLocation();
            target.getWorld().playSound(targetLoc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);
            target.getWorld().spawnParticle(Particle.HEART, targetLoc.getX(), targetLoc.getY()+1, targetLoc.getZ(), 5, 0.5, 0.5, 0.5);
        }

        loc.getWorld().spawnParticle(Particle.HEART, loc.getX(), loc.getY()+1, loc.getZ(), 5, 0.5, 0.5, 0.5);
        loc.getWorld().playSound(loc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);

        return true;
    }
}
