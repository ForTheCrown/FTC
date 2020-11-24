package ftc.bigcrown.challenges;

import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import ftc.bigcrown.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class TimerCountingDown {
	
	public Timer timer;
	public Challenge challenge;
	private int timeLeft;
	
	public TimerCountingDown(@Nonnull Challenge challenge, @Nonnegative int secondsToCountDown, boolean countdownBeforeStart) {
		this.challenge = challenge;
		this.timeLeft = secondsToCountDown * 1000;
		
    	if (countdownBeforeStart) {
			countDownStart(challenge.getPlayer(), 3);
		}
		else {
			startExtras(challenge.getPlayer());
			timer = new Timer();
			doTiming(challenge.getPlayer());
		}
	}
	
	private void countDownStart(Player player, int count) {
    	Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
	        @Override
	        public void run() {
	        	switch (count) {
	        		case 0:
        				player.sendTitle(ChatColor.YELLOW + "" + ChatColor.BOLD + "GO!!", "", 5, 20, 5);
        				
        				startExtras(player);
		        		timer = new Timer();
		        		doTiming(player);
		        		
		        		break;
	        		case 1:
	        			player.sendTitle(ChatColor.of("#FFFFA1") + "" + ChatColor.BOLD + count, "", 5, 20, 5);
	        			player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 2f, 1.8f);
	        			countDownStart(player, count-1);
	        			break;
	        		case 2:
	        			player.sendTitle(ChatColor.of("#FFFFA1") + "" + ChatColor.BOLD + count, "", 5, 20, 5);
	        			player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 2f, 1.8f);
	        			countDownStart(player, count-1);
	        			break;
	        		case 3:
	        			player.sendTitle(ChatColor.of("#FFFFA1")+ "" + ChatColor.BOLD + count, "", 5, 20, 5);
	        			player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 2f, 1.8f);
	        			countDownStart(player, count-1);
	        			break;
	        		default:
	        			break;
	        	}
	        }
	    }, 20L);
    	
    }
    
    private void startSound(Player player) {
    	for (int i = 0; i < 3; i++) {
    		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
		        @Override
		        public void run() {
			        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 2f, 2f);
		        }
    		}, i* 3L);
    	}
    }
    
    
    private void startExtras(Player player) {
    	startSound(player);
		player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 2f, 1.2f);
		
		for (PotionEffect effect : player.getActivePotionEffects()) player.removePotionEffect(effect.getType());
    }
    
    
    
    
    
    
    
	private String timerMessage = ChatColor.YELLOW + "Timer: " + ChatColor.of("#FFFFA1") + "%02d:%02d:%d";

    private void doTiming(Player player) {
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
            	timeLeft -= 100;

                int milliseconds = (timeLeft/100) % 10;
                int seconds = ((int) Math.floor(timeLeft / 1000)) % 60;
                int minutes = ((int) Math.floor(timeLeft / 60000));
                
                String message = String.format(timerMessage, minutes, seconds, milliseconds);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                
                if (timeLeft <= 0) {
                	stopTimer(false);
                }
            }
        }, 0, 100);
    }
    
    public void stopTimer(boolean timerWasInterrupted) {
    	timer.cancel();
	    timer.purge();
	    
	    if (!timerWasInterrupted) {
	    	// Async to sync >:|
		    Bukkit.getServer().getScheduler().runTask(Main.plugin, new Runnable() {
				@Override
				public void run() {
					challenge.endChallenge();
				}
			});
	    }
	    
	    
	}
    

}
