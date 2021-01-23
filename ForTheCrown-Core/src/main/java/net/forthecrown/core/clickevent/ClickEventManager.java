package net.forthecrown.core.clickevent;

import net.forthecrown.core.FtcCore;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public final class ClickEventManager {

    private static final Map<String, ClickEventTask> registeredClickEvents = new HashMap<>();
    private static final Set<Player> allowedToUseCommand = new HashSet<>();

    private ClickEventManager(){
    }

    public static String registerClickEvent(ClickEventTask classToCall){
        String npcID = generateRandomID();
        if(npcID == null) throw new NullPointerException("Unable to generate ID for NPC");

        registeredClickEvents.put(npcID, classToCall);
        return npcID;
    }

    public static void unregisterClickEvent(String id){
        if(registeredClickEvents.get(id) == null) throw new NullPointerException("No Npc with this ID exists: " + id);
        registeredClickEvents.remove(id);
    }

    public static Set<String> getRegisteredClickEvents(){
        return registeredClickEvents.keySet();
    }

    public static void callClickEvent(String id, String[] args, Player player){
        if(registeredClickEvents.get(id) == null) throw new NullPointerException("No Npc with this ID exists: " + id);

        ClickEventTask task = registeredClickEvents.get(id);
        task.run(player, args);
    }

    public static boolean isClickEventRegistered(String id){
        return registeredClickEvents.containsKey(id);
    }

    public static boolean isAllowedToUseCommand(Player player){
        return allowedToUseCommand.contains(player);
    }

    public static void allowCommandUsage(Player player, boolean allow){
        if(allow){
            if(allowedToUseCommand.contains(player)) return;
            allowedToUseCommand.add(player);
            Bukkit.getScheduler().runTaskLater(FtcCore.getInstance(), () -> allowedToUseCommand.remove(player), 60*20); //automatically makes it so you can't use the NPC command after a minute
        } else allowedToUseCommand.remove(player);
    }

    public static String getClickEventCommand(String id, String... args){
        return "/npcconverse " + id + " " + String.join(" ", args);
    }





    private static String generateRandomID(){
        String npcID = RandomStringUtils.random(16, true, true);

        int safeGuard = 300;
        while (registeredClickEvents.containsKey(npcID)){
            npcID = RandomStringUtils.random(16, true, true);
            safeGuard--;
            if(safeGuard <= 0) return null;
        }
        return npcID;
    }
}
