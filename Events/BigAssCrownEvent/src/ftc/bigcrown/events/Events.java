package ftc.bigcrown.events;

import ftc.bigcrown.Main;
import ftc.bigcrown.challenges.ChallengeClass;
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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Events implements Listener {

    @EventHandler
    public void onPresentUse(PlayerInteractEntityEvent event) {
    	// Biggest checks first -> lowest amount of code to execute each time event gets called
    	if (event.getHand() == EquipmentSlot.OFF_HAND) return;
		if (!event.getRightClicked().isInvulnerable()) return;
    	
    	// Only look for Present slime interactions
        if (!(event.getRightClicked() instanceof Slime)) return;
        Slime slime = (Slime) event.getRightClicked();
        if (slime.getCustomName() == null || (!slime.getCustomName().contains(ChatColor.GOLD + "Present"))) return;

        event.setCancelled(true);
        Player player = event.getPlayer();


        double initialChance = Math.random();
        if (initialChance <= 0.25){ //Challange

        	player.sendMessage("Challenge!");
        	new ChallengeClass(player).randomChallenge();
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
            	
            	items.add(Main.plugin.makeItem(Material.EMERALD, randomAmount, false, null));
            	items.add(Main.plugin.makeItem(Material.SWEET_BERRIES, 1, false, null));
            	items.add(Main.plugin.makeItem(Material.GOLD_NUGGET, randomAmount, false, null));
            	items.add(Main.plugin.makeItem(Material.COOKED_CHICKEN, randomAmount, false, null));
            	items.add(Main.plugin.makeItem(Material.SNOWBALL, 5, false, null));
            	items.add(Main.plugin.makeItem(Material.SPRUCE_BOAT, 1, false, color + "Sleigh", archive));
            	
            	itemToGive = items.get(Main.plugin.getRandomNumberInRange(0, items.size()-1));
            } 
            
            // 20% item range
            else if (initialChance > 0.75 && initialChance <= 0.95) { 
            	List<ItemStack> items = new ArrayList<ItemStack>();

            	items.add(Main.plugin.makeItem(Material.GOLD_INGOT, randomAmount, false, null));
            	items.add(Main.plugin.makeItem(Material.GOLD_NUGGET, 1, false, ChatColor.YELLOW + "Rhines", ChatColor.GOLD + "Worth 1000 rhines", ChatColor.DARK_GRAY + "Right click to add to your balance.")); // Rhines
            	items.add(Main.plugin.makeItem(Material.DIAMOND, 1, false, null));
            	items.add(Main.plugin.makeItem(Material.BELL, 1, false, color + "Jingle Bell", archive));
            	items.add(Main.plugin.makeItem(Material.TOTEM_OF_UNDYING, 1, false, color + "Baby Jesus", archive));
            	
            	itemToGive = items.get(Main.plugin.getRandomNumberInRange(0, items.size()-1));
            }
            
            // 5% item range
            else { 
            	List<ItemStack> items = new ArrayList<ItemStack>();
            	
            	items.add(Main.plugin.makeItem(Material.CHARCOAL, 1, false, color + "Great grandmother who couldn't be here", archive));
            	items.add(Main.plugin.makeItem(Material.SPRUCE_SAPLING, 1, false, color + "Christmas Tree", archive));
            	
            	items.add(Main.plugin.makeItem(Material.ACACIA_BOAT, 1, false, null)); // Gems
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
	            	player.sendMessage(ChatColor.YELLOW + "You've received " + amountOfGems + " Gems!");
	            }
	            else {
	            	Item item = player.getLocation().getWorld().dropItemNaturally(slime.getLocation(), itemToGive);
	            	item.setVelocity(new Vector(0, 0.2, 0));
	            }
            }
        }

        slime.getLocation().getBlock().setType(Material.AIR);
        slime.remove();
        
    	Scoreboard mainScoreboard = Main.plugin.getServer().getScoreboardManager().getMainScoreboard();
    	Score crownScore = mainScoreboard.getObjective("crown").getScore(player.getName());
    	crownScore.setScore(crownScore.getScore() + 1);
    }
    
	@EventHandler
	public void onLoginAfterLeavingChallenge(PlayerJoinEvent event) {
		if (Main.plugin.playersThatQuitDuringChallenge.contains(event.getPlayer().getName())) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> {
				event.getPlayer().sendMessage(ChatColor.RED + "[FTC]" + ChatColor.GRAY + " Your challenge got interrupted :(");
				Main.plugin.playersThatQuitDuringChallenge.remove(event.getPlayer().getName());
				event.getPlayer().teleport(new Location(Bukkit.getWorld("world"), 200.5, 70, 1000.5));
			}, 20L);
		}
		
	}
}
