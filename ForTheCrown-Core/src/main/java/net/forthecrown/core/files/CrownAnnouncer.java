package net.forthecrown.core.files;

import net.forthecrown.core.FtcCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CrownAnnouncer extends FtcFileManager  {

    private List<String> thingsToAnnounce = new ArrayList<>();
    private long delay;
    int id = -1;

    public CrownAnnouncer() {
        super("announcer");

        if(fileDoesntExist) addDefaults();
        else reload();

        startAnnouncer();
    }
    private void addDefaults(){
        getFile().addDefault("delay", 12000);
        List<String> list = new ArrayList<>();
        list.add("To visit a region, do &e/visit &rwhen you are near a region pole.");
        list.add("Please use the Resource World in Hazelguard if you need a lot of materials.");
        list.add("You can find more information about &e/ranks &rin the shop.");
        list.add("If you want others to know you are afk, type &e/afk&r.");
        list.add("If you like a challenge, you can try to complete &ethe Dungeons&r. Follow the signs in Hazelguard.");
        list.add("Remember, there are item shops in Hazelguard.");
        list.add("If you want to, you can join us on &e/discord&r.");
        list.add("There is a skeleton farm in Hazelguard if you need xp or bones.");
        list.add("Type &e/findpole &rto find the closest regionpole.");
        list.add("You can get the Knight tag and a &eRoyal Sword&r for completing the Dungeons.");
        list.add("You can do &e/polehelp &rif you need some help with regionpoles.");
        list.add("You can &e/vote&r for a chance to grab as much items as you can in the &eBank Vault&r!");
        list.add("&eThe End &ris only open the first 7 days of every month, it closes and resets again after.");
        getFile().addDefault("announcements", list);
        getFile().options().copyDefaults(true);
        super.save();
        reload();
    }

    public void reload(){
        super.reload();
        thingsToAnnounce = getFile().getStringList("announcements");
        delay = getFile().getLong("delay");
    }
    public void save(){
        getFile().set("delay", getDelay());
        getFile().set("announcements", getAnnouncements());
        super.save();
    }

    public long getDelay(){
        return delay;
    }
    public void setDelay(long delay){
        this.delay = delay;
    }
    public List<String> getAnnouncements(){
        return thingsToAnnounce;
    }
    public void setAnnouncements(List<String> announcements){
        this.thingsToAnnounce = announcements;
    }

    public void stopAnnouncer(){
        Bukkit.getScheduler().cancelTask(id);
    }

    public void startAnnouncer(){
        if(id != -1) stopAnnouncer();
        announcerProper();
    }

    public void announceToAll(String message) {
        Bukkit.broadcastMessage(FtcCore.getPrefix() + FtcCore.translateHexCodes(FtcCore.replaceEmojis(message)));
    }

    public void announce(String message) {
        for (Player player : Bukkit.getOnlinePlayers())
        {
            // Don't broadcast info messages to players in the Senate.
            if (player.getWorld().getName().contains("senate")) continue;

            player.sendMessage(FtcCore.translateHexCodes(message));
        }
    }

    private void announcerProper(){
        id = Bukkit.getScheduler().scheduleSyncRepeatingTask(FtcCore.getInstance(), new Runnable() {
            int counter = 0;
            @Override
            public void run() {
                String message = FtcCore.getPrefix() + ChatColor.translateAlternateColorCodes('&', getAnnouncements().get(counter));
                for (Player player : Bukkit.getOnlinePlayers())
                {
                    // Don't broadcast info messages to players in the Senate.
                    if (player.getWorld().getName().contains("senate")) continue;

                    player.sendMessage(message);
                    if (player.getWorld().getName().equalsIgnoreCase("world_resource"))
                        player.sendMessage(ChatColor.GRAY + "You are in the resource world! To get back to the normal survival world, do /warp portal.");
                }
                if (getAnnouncements().size() == counter+1) counter = 0;
                else counter++;
            }
        }, 100, delay);
    }
}
