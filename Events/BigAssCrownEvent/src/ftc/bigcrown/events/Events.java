package ftc.bigcrown.events;

import ftc.bigcrown.Main;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Events implements Listener {

    @EventHandler
    public void onPresentUse(PlayerInteractEntityEvent event) {
    	// Biggest checks first -> lowest amount of code to execute each time event gets called
    	if (event.getHand() == EquipmentSlot.OFF_HAND) return;
    	if (event.getRightClicked() == null || (!event.getRightClicked().isInvulnerable())) return;
    	
    	// Only look for Present slime interactions
        if (!(event.getRightClicked() instanceof Slime)) return;
        Slime slime = (Slime) event.getRightClicked();
        if (slime.getCustomName() == null || (!slime.getCustomName().contains(ChatColor.GOLD + "Present"))) return;

        Player player = event.getPlayer();
        

        double initialChance = Math.random();
        if (initialChance <= 0.25){ //Challange
            
        	player.sendMessage("Challenge!");
        	
        	List<Location> locList = Main.plugin.getChallengeList();
            if(!(locList == null || locList.size() <= 0)) {
            	 //new ChallengeClass(player);
            }
        } 
        else {
            String color = ChatColor.of("#c2fffe") + "";
            String archive = ChatColor.WHITE + "Earned during the Christmas Event.";
            int randomAmount = Main.plugin.getRandomNumberInRange(3, 10);
            ItemStack itemToGive;

            // 50% item range
            if (initialChance > 0.25 && initialChance <= 0.75) { 
            	List<ItemStack> items = new ArrayList<ItemStack>();
            	items.add(null);
            	
            	items.add(Main.plugin.makeItem(Material.EMERALD, randomAmount, null));
            	items.add(Main.plugin.makeItem(Material.SWEET_BERRIES, 1, null));
            	items.add(Main.plugin.makeItem(Material.GOLD_NUGGET, randomAmount, null));
            	items.add(Main.plugin.makeItem(Material.COOKED_CHICKEN, randomAmount, null));
            	items.add(Main.plugin.makeItem(Material.SNOWBALL, 5, null));
            	items.add(Main.plugin.makeItem(Material.SPRUCE_BOAT, 1, color + "Sleigh", archive));
            	
            	itemToGive = items.get(Main.plugin.getRandomNumberInRange(0, items.size()-1));
            } 
            
            // 20% item range
            else if (initialChance > 0.75 && initialChance <= 0.95) { 
            	List<ItemStack> items = new ArrayList<ItemStack>();

            	items.add(Main.plugin.makeItem(Material.GOLD_INGOT, randomAmount, null));
            	items.add(Main.plugin.makeItem(Material.GOLD_NUGGET, 1, ChatColor.YELLOW + "Rhines", ChatColor.GOLD + "Worth 1000 rhines", ChatColor.DARK_GRAY + "Right click to add to your balance.")); // Rhines
            	items.add(Main.plugin.makeItem(Material.DIAMOND, 1, null));
            	items.add(Main.plugin.makeItem(Material.BELL, 1, color + "Jingle Bell", archive));
            	items.add(Main.plugin.makeItem(Material.TOTEM_OF_UNDYING, 1, color + "Baby Jesus", archive));
            	
            	itemToGive = items.get(Main.plugin.getRandomNumberInRange(0, items.size()-1));
            }
            
            // 5% item range
            else { 
            	List<ItemStack> items = new ArrayList<ItemStack>();
            	
            	items.add(Main.plugin.makeItem(Material.CHARCOAL, 1, color + "Great grandmother who couldn't be here", archive));
            	items.add(Main.plugin.makeItem(Material.SPRUCE_SAPLING, 1, color + "Christmas Tree", archive));
            	
            	items.add(Main.plugin.makeItem(Material.ACACIA_BOAT, 1, null)); // Gems
            	itemToGive = items.get(Main.plugin.getRandomNumberInRange(0, items.size()-1));
            }

            if (itemToGive == null) {
            	player.getWorld().playSound(slime.getLocation(), Sound.ENTITY_PLAYER_BURP, 1, 1);
	            player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, slime.getLocation(), 10, 0.1, 0, 0.1, 0.05);
            } 
            else {
            	player.getWorld().playSound(slime.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1);
            	player.getWorld().playSound(slime.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
	            player.getWorld().spawnParticle(Particle.SNOW_SHOVEL, slime.getLocation(), 25, 0.1, 0, 0.1, 1);
	            player.getWorld().spawnParticle(Particle.END_ROD, slime.getLocation(), 50, 0.1, 0, 0.1, 0.1);
	            
            	if (itemToGive.getType() == Material.ACACIA_BOAT) {
	            	int amountOfGems = 100;
	            	Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "addgems " + player.getName() + " " + amountOfGems);
	            	player.sendMessage("You've received " + amountOfGems + " Gems!");
	            }
	            else {
	            	Item item = player.getLocation().getWorld().dropItemNaturally(slime.getLocation(), itemToGive);
	            	item.setVelocity(new Vector(0, 0.2, 0));
	            }
            }
        }

        slime.getLocation().getBlock().setType(Material.AIR);
        slime.remove();
    }
}
