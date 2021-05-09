package net.forthecrown.vikings.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.vikings.Vikings;
import net.forthecrown.vikings.blessings.VikingBlessing;
import net.forthecrown.vikings.inventory.BlessingSelector;
import net.forthecrown.vikings.valhalla.RaidParty;
import net.forthecrown.vikings.valhalla.VikingRaid;

import java.util.ArrayList;
import java.util.List;

public class CommandViking extends CrownCommandBuilder {

    public CommandViking() {
        super("viking", Vikings.inst());

        setPermission("ftc.vikings.admin");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("reload")
                        .executes(c -> {
                            Vikings.reloadVikings();
                            getSender(c).sendMessage("Vikings reloaded");
                            return 0;
                        })
                )
                .then(argument("save")
                        .executes(c ->{
                            Vikings.saveVikings();
                            getSender(c).sendMessage("Vikings saved");
                            return 0;
                        })
                )

                .then(argument("raid")
                        .then(argument("generate")
                                .executes(c -> {
                                    RaidAreaCreator generator = Vikings.getRaidManager().fromName("Monastery").getGenerator();

                                    try {
                                        generator.create();
                                    } catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    return 0;
                                })
                        )

                        .then(argument("create")
                                .then(argument("raid", StringArgumentType.word())
                                        .executes(c -> {
                                            CrownUser u = getUserSender(c);
                                            VikingRaid raid = Vikings.getRaidManager().fromName(c.getArgument("raid", String.class));
                                            if(raid == null) throw new CrownCommandException("Invalid raid name!");

                                            RaidParty party = new RaidParty(raid, -1, u.getPlayer());
                                            Vikings.getRaidManager().registerParty("TestParty", party);
                                            broadcastAdmin(c.getSource(), "Created RaidParty");
                                            return 0;
                                        })
                                )
                        )
                        .then(argument("join")
                                .executes(c -> {
                                    CrownUser u = getUserSender(c);
                                    RaidParty party = Vikings.getRaidManager().partyFromName("TestParty");
                                    if(party == null) throw new CrownCommandException("Part is null?????");

                                    party.join(u.getPlayer());
                                    broadcastAdmin(c.getSource(), "Joined RaidParty");
                                    return 0;
                                })
                        )
                        .then(argument("start")
                                .executes(c -> {
                                    testPlayerSender(c.getSource());
                                    RaidParty party = Vikings.getRaidManager().partyFromName("TestParty");
                                    if(party == null) throw new CrownCommandException("Part is null?????");

                                    party.startRaid();
                                    broadcastAdmin(c.getSource(), "Starting Raid");
                                    return 0;
                                })
                        )
                )
                .then(argument("blessing")
                        .then(argument("blessingName", StringArgumentType.word())
                                .suggests((c, b) -> suggestMatching(b, blessingCompletions()))

                                .then(argument("beginUsage")
                                        .executes(c -> {
                                            CrownUser user = getUserSender(c);

                                            VikingBlessing b = VikingBlessing.fromName(c.getArgument("blessingName", String.class));
                                            if(b == null) throw new CrownCommandException("Invalid blessing!");

                                            b.beginUsage(user);
                                            user.sendMessage("Starting blessing usage!");
                                            return 0;
                                        })
                                )
                                .then(argument("endUsage")
                                        .executes(c ->{
                                            CrownUser user = getUserSender(c);

                                            VikingBlessing b = VikingBlessing.fromName(c.getArgument("blessingName", String.class));
                                            if(b == null) throw new CrownCommandException("Invalid blessing!");

                                            b.endUsage(user);
                                            user.sendMessage("Stopping blessing usage!");
                                            return 0;
                                        })
                                )
                        )
                        .then(argument("inventory")
                                .executes(c->{
                                    CrownUser user = getUserSender(c);

                                    try {
                                        user.getPlayer().openInventory(new BlessingSelector(user).getInventory());
                                    } catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    return 0;
                                })
                        )
                );
    }

    private List<String> raidCompletions(){
        List<String> list = new ArrayList<>();
        for (VikingRaid r: Vikings.getRaidManager().getRaids()){
            list.add(r.getName());
        }
        return list;
    }

    private List<String> blessingCompletions(){
        List<String> list = new ArrayList<>();
        for (VikingBlessing b: VikingBlessing.getBlessings()){
            list.add(b.getName());
        }
        return list;
    }
}
