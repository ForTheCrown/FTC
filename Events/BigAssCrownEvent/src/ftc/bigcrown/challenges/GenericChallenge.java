package ftc.bigcrown.challenges;

import ftc.bigcrown.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class GenericChallenge implements Challenge, Listener {

    public Player player;

    public Location returnLocation;
    public Location startLocation/* = new Location(null, 0, 0, 0)*/; // TODO

    public TimerCountingDown timer;

    public boolean challengeCancelled = false;

    public ChallengeType challengeType;

    public String objectiveName;
    
    public GenericChallenge(Player player, ChallengeType challengeType, String objectiveName){
        this.player = player;
        this.challengeType = challengeType;
        this.objectiveName = objectiveName;

        startChallenge();
    }
    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Objective getScoreboardObjective() {
        Scoreboard mainScoreboard = Main.plugin.getServer().getScoreboardManager().getMainScoreboard();
        return mainScoreboard.getObjective(objectiveName);
    }

    @Override
    public Location getStartLocation() {
        return startLocation;
    }

    @Override
    public Location getReturnLocation() {
        return returnLocation;
    }

    @Override
    public void setReturnLocation(Location location) {
        returnLocation = location;
    }

    @Override
    @EventHandler
    public void onLogoutWhileInChallenge(PlayerQuitEvent event) {
        if (getPlayer() == null) return;
        if (event.getPlayer().getName() == getPlayer().getName()) {
            if (timer != null) {
                timer.stopTimer(true);
                timer = null;
            }
            // TODO: remove all bats
            challengeCancelled = true;
            setChallengeInUse(false);
            Main.plugin.playersThatQuitDuringChallenge.add(getPlayer().getName());
        }
    }

    @Override
    public void startChallenge() {
        // Teleport player to challenge:
        getPlayer().teleport(getStartLocation());
        getPlayer().playSound(getStartLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);

        // No countdown, so start timer immediately after title:
        GenericChallenge kbc = this;
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> {
            if (!challengeCancelled) timer = new TimerCountingDown(kbc, 30, false);
        }, 70L);
    }

    @Override
    public void endChallenge() {
        // TODO: remove all bats

        // Timer stopped:
        this.timer = null;
        getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 2f, 1.5f);

        /* // Amount of bats caught:
        int score = calculateScore();
        if (score != 1) getPlayer().sendMessage(ChatColor.YELLOW + "You've caught " + score + " bats!");
        else getPlayer().sendMessage(ChatColor.YELLOW + "You've caught 1 bat!");

        // Add to crown scoreboard:
        Scoreboard mainScoreboard = Main.plugin.getServer().getScoreboardManager().getMainScoreboard();
        Score crownScore = mainScoreboard.getObjective("crown").getScore(getPlayer().getName());
        crownScore.setScore(crownScore.getScore() + score);
        */

        //Honestly, I just have no idea what to do with this bit lol

        // Teleport to where player opened present:
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, () -> {
            getPlayer().teleport(getReturnLocation().add(0, 1, 0));
            getPlayer().playSound(getReturnLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            setChallengeInUse(false);
        }, 60L);
    }

    @Override
    public void setChallengeInUse(boolean bool) {
        Main.plugin.challengeIsFree.replace(getChallengeType(), bool);
    }

    @Override
    public boolean getChallengeInUse() {
        return Main.plugin.challengeIsFree.get(getChallengeType());
    }

    @Override
    public ChallengeType getChallengeType() {
        return challengeType;
    }

    @Override
    public boolean isChallengeCancelled() {
        return challengeCancelled;
    }
}
