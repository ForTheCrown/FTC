package net.forthecrown.pirates.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.pirates.Pirates;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CommandGhTarget extends FtcCommand {

    public CommandGhTarget() {
        super("ghtarget", Pirates.inst);

        setPermission("ftc.pirates.ghtarget");
        register();
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(argument("id", IntegerArgumentType.integer())
                .then(argument("type", IntegerArgumentType.integer())
                        .suggests(suggestMatching( "1", "2", "3"))
                .then(argument("yaw", IntegerArgumentType.integer())
                        .suggests(suggestMatching( "0", "90", "180", "270", "360", "-90", "-180", "-270", "-360"))

                .then(argument("dest", PositionArgument.position())
                        .executes(c -> standArg(c, null, null))

                        .then(argument("hooks", IntegerArgumentType.integer())
                                .executes(c -> standArg(c, "hooks", null))

                                .then(argument("distance", IntegerArgumentType.integer())
                                        .executes(c -> standArg(c, "hooks", "distance"))
                                )
                        )
                ))));
    }

    private int standArg(CommandContext<CommandSource> c, String hooks, String distance) throws CommandSyntaxException {
        Player player = getPlayerSender(c);
        int idArg, typeArg, yawArg, hookArg, distanceArg;

        idArg = c.getArgument("id", Integer.class);
        typeArg = c.getArgument("type", Integer.class);
        yawArg = c.getArgument("yaw", Integer.class);
        hookArg = hooks == null ? -1 : c.getArgument(hooks, Integer.class);
        distanceArg = distance == null ? -1 : c.getArgument(distance, Integer.class);

        //destination
        Location d = PositionArgument.getLocation(c, "dest");

        createStand(player.getLocation(), idArg, typeArg, yawArg, hookArg, distanceArg, d.getX(), d.getY(), d.getZ());
        return 0;
    }

    private void createStand(Location location,
                             int id, int type, int yaw, int hooks, int distance,
                             double x, double y, double z) {

        Map<String, Object> sectionMap = new HashMap<>();
        sectionMap.put("XToCords", x);
        sectionMap.put("YToCords", y);
        sectionMap.put("ZToCords", z);

        sectionMap.put("StandClass", type);
        sectionMap.put("YawToCords", yaw);
        if(hooks != -1) sectionMap.put("NextLevelHooks", hooks);
        if(distance != -1) sectionMap.put("NextLevelDistance", distance);

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(Pirates.inst.grapplingHook.getArmorStandFile());
        String section = "Stand_" + id;
        yaml.createSection(section, sectionMap);
        Pirates.inst.saveYaml(yaml, Pirates.inst.grapplingHook.getArmorStandFile());

        ArmorStand ghEStandTarget = location.getWorld().spawn(location, ArmorStand.class);

        ghEStandTarget.setCustomName("GHTargetStand " + id);
        ghEStandTarget.setCustomNameVisible(false); //Sets the name to be invisible, currently visible
        ghEStandTarget.setInvulnerable(true); //makes it god lol
        ghEStandTarget.getEquipment().setHelmet(new ItemStack(Material.GLOWSTONE)); //gives it glowstone for a head
        ghEStandTarget.setVisible(false); //makes it invis
        ghEStandTarget.setGravity(false); // removes its gravity
    }
}