package net.forthecrown.july;

import com.google.common.collect.ImmutableList;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.commands.CommandLeave;
import net.forthecrown.core.comvars.ComVar;
import net.forthecrown.core.comvars.ComVars;
import net.forthecrown.core.comvars.types.ComVarType;
import net.forthecrown.core.crownevents.ArmorStandLeaderboard;
import net.forthecrown.core.crownevents.ObjectiveLeaderboard;
import net.forthecrown.core.crownevents.reporters.EventReporter;
import net.forthecrown.core.crownevents.reporters.ReporterFactory;
import net.forthecrown.core.utils.CrownBoundingBox;
import net.forthecrown.july.command.CommandJulyEvent;
import net.forthecrown.july.effects.BlockEffects;
import net.forthecrown.july.listener.GeneralListener;
import net.forthecrown.july.listener.OnTrackListener;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class JulyMain extends JavaPlugin {
    public static JulyMain inst;

    public static EventReporter reporter;
    public static ObjectiveLeaderboard leaderboard;
    public static ParkourEvent event;

    private static ComVar<Integer> potionAmplifier;
    private static ComVar<Integer> potionDurationTicks;
    private static ComVar<Integer> jumpBoost;

    @Override
    public void onEnable() {
        inst = this;

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        event = new ParkourEvent();
        reporter = ReporterFactory.of(this, event);

        new CommandJulyEvent();

        CrownCore.getCheckRegistry().register(CheckIsNotAlt.KEY, CheckIsNotAlt::new);

        BlockEffects.init();

        getServer().getPluginManager().registerEvents(new GeneralListener(), this);

        leaderboard = new ObjectiveLeaderboard("Times", EventConstants.CROWN, new Location(EventConstants.EVENT_WORLD, -34.5, 77, 162.5));

        leaderboard.setOrder(ArmorStandLeaderboard.Order.LOW_TO_HIGH);
        leaderboard.setTimerScore(true);
        updateLb();

        CommandLeave.add(EventConstants.WORLD_REGION, EventConstants.LOBBY, player -> {
            if(ParkourEvent.PRACTISE_TRACKER.containsKey(player)){
                event.endPractise(player);
                return true;
            }

            if(ParkourEvent.WAITING_FOR_START.containsKey(player)){
                ParkourEvent.WAITING_FOR_START.get(player).cancel();
                player.setCanPickupItems(true);
                return true;
            }

            if(ParkourEvent.ENTRIES.containsKey(player)){
                event.end(ParkourEvent.ENTRIES.get(player));
                return true;
            }

            return false;
        });
    }

    @Override
    public void onDisable() {
        try {
            reporter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        saveConfig();

        ImmutableList.copyOf(ParkourEvent.ENTRIES.values()).forEach(e -> event.end(e));
        ImmutableList.copyOf(ParkourEvent.PRACTISE_TRACKER.values()).forEach(OnTrackListener::end);
        ImmutableList.copyOf(ParkourEvent.WAITING_FOR_START.values()).forEach(EventBuilder::cancel);
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        potionAmplifier = ComVars.set(      "event_potionAmplifier",      ComVarType.INTEGER,     getConfig().getInt("PotionAmplifier"));
        potionDurationTicks = ComVars.set(  "event_potionDurationTicks",  ComVarType.INTEGER,     getConfig().getInt("PotionDurationTicks"));
        jumpBoost = ComVars.set(            "event_jumpBoost",            ComVarType.INTEGER,     getConfig().getInt("JumpBoost"));
    }

    @Override
    public void saveConfig() {
        getConfig().set("PotionAmplifier", potionAmplifier());
        getConfig().set("PotionDurationTicks", potionDuration());
        getConfig().set("JumpBoost", jumpBoost());

        super.saveConfig();
    }

    public static int potionAmplifier(){
        return potionAmplifier.getValue(2);
    }

    public static int potionDuration(){
        return potionDurationTicks.getValue(80);
    }

    public static int jumpBoost(){
        return jumpBoost.getValue(2);
    }

    public static void updateLb(){
        leaderboard.update();

        CrownBoundingBox.of(leaderboard.getLocation(), 3)
                .getEntitiesByType(ArmorStand.class)
                .forEach(stand -> {
                    stand.setPersistent(true);
                    stand.setRemoveWhenFarAway(false);
                });
    }
}