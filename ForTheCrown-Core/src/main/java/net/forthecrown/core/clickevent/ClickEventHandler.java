package net.forthecrown.core.clickevent;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.exceptions.CrownException;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ClickEventHandler {

    private static final Map<String, ClickEventTask> registeredClickEvents = new HashMap<>();
    private static final Set<Player> allowedToUseCommand = new HashSet<>();
    private static final ClickEventCommand CLICK_COMMAND = new ClickEventCommand();

    private ClickEventHandler(){
    }

    /**
     * Registers a click event and returns a string ID, which is used to call it later
     * @param classToCall The ClickEventTask class that gets called when the click event is executed
     * @return the string ID used to later call the click event
     */
    public static String registerClickEvent(ClickEventTask classToCall){
        String npcID = generateRandomID();
        if(npcID == null) throw new NullPointerException("Unable to generate ID for NPC");

        registeredClickEvents.put(npcID, classToCall);
        return npcID;
    }

    /**
     * Unregisters a click event
     * @param id the ID of the event to unregister
     */
    public static void unregisterClickEvent(String id){
        if(registeredClickEvents.get(id) == null) throw new NullPointerException("No Npc with this ID exists: " + id);
        registeredClickEvents.remove(id);
    }

    /**
     * Gets all registered click event IDs
     * @return registered click event IDs
     */
    public static Set<String> getRegisteredClickEvents(){
        return registeredClickEvents.keySet();
    }

    /**
     * Calls a click event
     * @param id The ID of the ClickEventTask to call
     * @param args Any additional args you might want to parse
     * @param player The player that calls the click event
     */
    public static void callClickEvent(String id, String[] args, Player player){
        if(registeredClickEvents.get(id) == null) throw new NullPointerException("No Npc with this ID exists: " + id);

        ClickEventTask task = registeredClickEvents.get(id);
        try {
            task.run(player, args);
        } catch (CrownException ignored) {}
    }

    public static boolean isRegistered(String id){
        return registeredClickEvents.containsKey(id) && registeredClickEvents.get(id) != null;
    }

    public static boolean isAllowedToUseCommand(Player player){
        return allowedToUseCommand.contains(player);
    }

    public static void allowCommandUsage(Player player, boolean allow){
        if(allow){
            if(allowedToUseCommand.contains(player)) return;
            allowedToUseCommand.add(player);
            Bukkit.getScheduler().runTaskLater(FtcCore.getInstance(), () -> allowedToUseCommand.remove(player), 30*20); //automatically makes it so you can't use the NPC command after a minute
        } else allowedToUseCommand.remove(player);
    }

    public static String getCommand(String id, String... args){
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
