package net.forthecrown.mayevent.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.FtcExceptionProvider;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import net.forthecrown.mayevent.ArenaEntry;
import net.forthecrown.mayevent.DoomEvent;
import net.forthecrown.mayevent.MayMain;
import net.forthecrown.mayevent.MayUtils;
import net.forthecrown.mayevent.arena.EventArena;
import net.forthecrown.mayevent.guns.HitScanWeapon;
import org.bukkit.entity.Player;

import static net.forthecrown.core.api.Announcer.debug;

public class CommandMayEvent extends CrownCommandBuilder {

    public CommandMayEvent(){
        super("mayevent", MayMain.inst);

        setPermission("ftc.event.admin");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("updatelb")
                        .executes(c -> {
                            MayMain.leaderboard.update();

                            broadcastAdmin(c, "Updating Leaderboard");
                            return 0;
                        })
                )

                .then(argument("enter")
                        .then(argument("player", EntityArgument.player())
                                .executes(c -> {
                                    Player selected = EntityArgument.getPlayer(c, "player");
                                    if(DoomEvent.ENTRIES.containsKey(selected)){
                                        MayMain.inst.getLogger().warning(selected.getName() + " entered the portal but was still in the entries list");
                                        throw FtcExceptionProvider.create("What? You're already in the event!?!?");
                                    }

                                    MayMain.event.start(selected);
                                    return 0;
                                })
                        )
                )

                .then(argument("gun")
                        .then(argument("gunName", WeaponArgType.gun())
                                .suggests(WeaponArgType::suggest)

                                .executes(c -> {
                                    Player player = getPlayerSender(c);
                                    if(!DoomEvent.ENTRIES.containsKey(player)) throw FtcExceptionProvider.create("Gotta be in the event, honey");

                                    HitScanWeapon weapon = WeaponArgType.getGun(c, "gunName");
                                    MayUtils.dropItem(player.getLocation().add(2, 0, 0), weapon.item(), false);

                                    broadcastAdmin(c.getSource(), "Giving gun: " + weapon.name());
                                    return 0;
                                })
                        )
                )

                .then(argument("info")
                        .executes(c -> {
                            Player player = getPlayerSender(c);
                            if(!DoomEvent.ENTRIES.containsKey(player)) throw FtcExceptionProvider.create("Gotta be in the event for that one cutie");

                            ArenaEntry entry = DoomEvent.ENTRIES.get(player);
                            EventArena arena = entry.arena();

                            debug(entry.getHeldGun());

                            debug("currentMobAmount: " + arena.currentMobAmount);
                            debug("initialMobAmount: " + arena.initialMobAmount);

                            debug("bossbar progress: " + arena.bossBar.getProgress());

                            debug(arena.getWaveModifier());

                            return 0;
                        })
                )

                .then(argument("start")
                        .executes(c -> {
                            try {
                                Player player = getPlayerSender(c);

                                DoomEvent event = MayMain.event;
                                event.start(player);

                                broadcastAdmin(c.getSource(), "Starting event");
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                            return 0;
                        })
                )
                .then(argument("end").executes(c -> endEvent(c, false)))
                .then(argument("complete").executes(c -> endEvent(c, true)));
    }

    public int endEvent(CommandContext<CommandSource> c, boolean complete) throws CommandSyntaxException {
        Player player = getPlayerSender(c);
        if(!DoomEvent.ENTRIES.containsKey(player)) throw FtcExceptionProvider.create("You're not in the event");

        DoomEvent event = MayMain.event;
        ArenaEntry entry = DoomEvent.ENTRIES.get(player);

        if(complete) event.complete(entry);
        else event.end(entry);

        broadcastAdmin(c.getSource(), "stopping event, complete: " + complete);
        return 0;
    }
}
