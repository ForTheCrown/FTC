package ftc.bigcrown.challenges;

import ftc.bigcrown.Main;
import org.bukkit.*;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Score;
import org.bukkit.util.Vector;

public class PinataChallenge extends GenericChallenge {

    public boolean canDrop;

    public PinataChallenge(Player player) {
        super(player, ChallengeType.PINATA, "pinata");
        //startLocation = new Location();

        sendTitle();
    }

    @EventHandler
    public void onPlayerHitPinata(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Husk && event.getDamager() instanceof Player)) return;
        Husk husk = (Husk) event.getEntity();
        Player player = (Player) event.getDamager();

        if (husk.getCustomName() == null && husk.getCustomName().contains(ChatColor.YELLOW + "$$$")) return;

        Score score = getScoreboardObjective().getScore(player.getName());
        score.setScore(score.getScore() + 1);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2f, 1.0f);

        if (!canDrop) return;

        double random = Math.random();
        if (random < 0.33) {
            Item gold = player.getWorld().dropItem(husk.getLocation(), new ItemStack(Material.GOLD_INGOT));
            gold.setVelocity(new Vector(0, 0.2, 0));
        } else if (random > 0.85) {
            Item diamond = player.getWorld().dropItem(husk.getLocation(), new ItemStack(Material.DIAMOND));
            diamond.setVelocity(new Vector(0, 0.2, 0));
        } else return;

        canDrop = false;
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> canDrop = true, 100L);
    }

    public void sendTitle() {
        this.getPlayer().sendTitle(ChatColor.YELLOW + "Punch the Pinata!", ChatColor.GOLD + "March Event", 5, 60, 5);
    }
}
