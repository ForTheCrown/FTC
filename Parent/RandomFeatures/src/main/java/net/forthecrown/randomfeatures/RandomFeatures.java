package net.forthecrown.randomfeatures;

import net.forthecrown.randomfeatures.command.CommandChristmasGift;
import net.forthecrown.randomfeatures.command.CommandCrowntop;
import net.forthecrown.randomfeatures.command.CommandDeathtop;
import net.forthecrown.randomfeatures.command.CommandWild;
import net.forthecrown.randomfeatures.features.MobHealthBar;
import net.forthecrown.randomfeatures.features.SmokeBomb;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Map;

public final class RandomFeatures extends JavaPlugin {

    public static RandomFeatures instance;
    public Map<LivingEntity, Component> withSetNames = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;

        getServer().getPluginManager().registerEvents(new MobHealthBar(), this);
        getServer().getPluginManager().registerEvents(new SmokeBomb(), this);

        new CommandWild(this);
        new CommandCrowntop(this);
        new CommandDeathtop(this);
        new CommandChristmasGift(this);
    }

    @Override
    public void onDisable() {
        for(LivingEntity e: withSetNames.keySet()){
            e.customName(withSetNames.getOrDefault(e, null));
        }
    }


    public void showLeaderboard(Player player, String objectiveName){
        Scoreboard mainScoreboard = getServer().getScoreboardManager().getMainScoreboard();
        Objective objective = mainScoreboard.getObjective(objectiveName);

        TextComponent displayName = Component.text()
                .color(NamedTextColor.GOLD)
                .append(Component.text("---"))
                .append(Component.text("Leaderboard").color(NamedTextColor.YELLOW))
                .append(Component.text("---"))
                .build();

        Scoreboard scoreboard = getServer().getScoreboardManager().getNewScoreboard();
        Objective newObj = scoreboard.registerNewObjective(player.getName(), "dummy", displayName);

        for(String name : objective.getScoreboard().getEntries()) {
            if(!objective.getScore(name).isScoreSet() || objective.getScore(name).getScore() == 0) continue;
            newObj.getScore(name).setScore(objective.getScore(name).getScore());
        }

        newObj.setDisplaySlot(DisplaySlot.SIDEBAR);
        player.setScoreboard(scoreboard);
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> player.setScoreboard(mainScoreboard), 300L);
    }
}
