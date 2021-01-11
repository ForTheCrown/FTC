package net.forthecrown.core.chat;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.chat.commands.*;
import net.forthecrown.core.chat.commands.emotes.*;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class Chat {

    private final FtcCore main = FtcCore.getInstance();
    private static Chat chatMain;
    private static Set<Player> sctPlayers = new HashSet<>();
    private static String discord;

    private static Set<Player> onCooldown = new HashSet<>();

    public Chat(){
        chatMain = this;
        discord = FtcCore.getInstance().getConfig().getString("discord");
        Server server = main.getServer();

        server.getPluginManager().registerEvents(new ChatEvents(), main);

        server.getPluginCommand("staffchat").setExecutor(new StaffChatCommand());
        server.getPluginCommand("staffchat").setTabCompleter(new EmojiTabCompleter());
        server.getPluginCommand("staffchattoggle").setExecutor(new StaffChatToggleCommand());

        server.getPluginCommand("broadcast").setExecutor(new BroadcastCommand());

        server.getPluginCommand("discord").setExecutor(new Discord());
        server.getPluginCommand("findpost").setExecutor(new  FindPost());
        server.getPluginCommand("posthelp").setExecutor(new  PostHelp());
        server.getPluginCommand("spawn").setExecutor(new  SpawnCommand());

        server.getPluginCommand("tpask").setExecutor(new  TpaskCommand());
        server.getPluginCommand("tpaskhere").setExecutor(new  TpaskHereCommand());

        server.getPluginCommand("toggleemotes").setExecutor(new  ToggleEmotes());
        server.getPluginCommand("bonk").setExecutor(new Bonk());
        server.getPluginCommand("mwah").setExecutor(new Mwah());
        server.getPluginCommand("poke").setExecutor(new Poke());
        server.getPluginCommand("scare").setExecutor(new Scare());
        server.getPluginCommand("jingle").setExecutor(new Jingle());
    }


    public static Chat getInstance(){
        return chatMain;
    }

    public static Set<Player> getSCTPlayers(){
        return sctPlayers;
    }
    public static void setSCTPlayers(Set<Player> sctPlayers){
        Chat.sctPlayers = sctPlayers;
    }
    public static String getDiscord(){
        return ChatColor.translateAlternateColorCodes('&', discord);
    }
    public static void setDiscord(String discord){
        Chat.discord = discord;
    }

    public static String replaceEmojis(String string){
        String message = string;
        message = message.replaceAll(":shrug:", "¯\\\\_(ツ)_/¯");
        message = message.replaceAll(":ughcry:", "(ಥ�?ಥ)");
        message = message.replaceAll(":gimme:", "༼ つ ◕_◕ ༽つ");
        message = message.replaceAll(":gimmecry:", "༼ つ ಥ_ಥ ༽つ");
        message = message.replaceAll(":bear:", "ʕ• ᴥ •ʔ");
        message = message.replaceAll(":smooch:", "( ^ 3^) ❤");
        message = message.replaceAll(":why:", "ლ(ಠ益ಠლ)");
        message = message.replaceAll(":tableflip:", "(ノಠ益ಠ)ノ彡┻━┻");
        message = message.replaceAll(":tableput:", " ┬──┬ ノ( ゜-゜ノ)");
        message = message.replaceAll(":pretty:", "(◕‿◕ ✿)");
        message = message.replaceAll(":sparkle:", "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧");
        message = message.replaceAll(":blush:", "(▰˘◡˘▰)");
        message = message.replaceAll(":sad:", "(._. )");
        message = message.replaceAll(":pleased:", "(ᵔᴥᵔ)");
        message = message.replaceAll(":fedup:", "(¬_¬)");
        return message;
    }

    public static void senderEmoteOffMessage(Player player){
        player.sendMessage(ChatColor.GRAY + "You have emotes turned off.");
        player.sendMessage(ChatColor.GRAY + "Do " + ChatColor.RESET + "/toggleemotes" + ChatColor.GRAY + " to enable them.");
    }

    public static void addToEmoteCooldown(Player player, int timeinDelay){
        if(!player.isOp()){
            onCooldown.add(player);
            new BukkitRunnable() {
                @Override
                public void run() {
                    onCooldown.remove(player);
                }
            }.runTaskLater(FtcCore.getInstance(), timeinDelay);
        }
    }
    public static boolean isOnCooldown(Player player){
        return onCooldown.contains(player);
    }
}
