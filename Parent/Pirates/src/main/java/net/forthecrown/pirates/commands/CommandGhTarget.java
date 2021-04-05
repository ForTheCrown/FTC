package net.forthecrown.pirates.commands;

import net.forthecrown.core.commands.CrownCommand;
import net.forthecrown.core.exceptions.CrownException;
import net.forthecrown.pirates.Pirates;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandGhTarget extends CrownCommand {

    public CommandGhTarget() {
        super("ghtarget", Pirates.inst);
        setPermission("ftc.pirates.ghtarget");
        register();
    }

    @Override
    public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) throw new CrownException(sender, "Only players may execute this command!");
        Player player = (Player) sender;

        if (args.length < 6) {
            player.sendMessage(ChatColor.RED + "Invalid use of command:");
            player.sendMessage(ChatColor.GRAY + "/ghtarget [id] [class] x y z yaw (hookuses) (distance)");
            return false;
        }
        else {
            int ghStandCreateID, ghStandClass, ghYawToCords,  ghHookUsesToGive = 0, ghHookDistanceToGive = 0;
            double ghXToCords, ghYToCords, ghZToCords;
            try {
                ghStandCreateID = Integer.parseInt(args[0]); // Will be used in YML file to understand...
                ghStandClass = Integer.parseInt(args[1]); // 1 for normal, 2 for biome, 3 for final
                ghXToCords = Double.parseDouble(args[2]);
                ghYToCords = Double.parseDouble(args[3]);
                ghZToCords = Double.parseDouble(args[4]);
                ghYawToCords = Integer.parseInt(args[5]);
                if (args.length > 6) ghHookUsesToGive = Integer.parseInt(args[6]); //Amount of uses for the hook that will be given to the player
                if (args.length > 7) ghHookDistanceToGive = Integer.parseInt(args[7]);
            }
            catch (Exception e) {
                player.sendMessage(ChatColor.GRAY + "All args have to be numbers.");
                return false;
            }

            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(Pirates.inst.grapplingHook.getArmorStandFile());
            String section = "Stand_" + ghStandCreateID;
            yaml.createSection(section);
            yaml.set(section + ".StandClass", ghStandClass);
            yaml.set(section + ".XToCords", ghXToCords);
            yaml.set(section + ".YToCords", ghYToCords);
            yaml.set(section + ".ZToCords", ghZToCords);
            yaml.set(section + ".YawToCords", ghYawToCords);
            if (args.length > 6) yaml.set(section + ".NextLevelHooks", ghHookUsesToGive);
            if (args.length > 7) yaml.set(section + ".NextLevelDistance", ghHookDistanceToGive);
            Pirates.inst.saveyaml(yaml, Pirates.inst.grapplingHook.getArmorStandFile());

            ArmorStand ghEStandTarget = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);

            ghEStandTarget.setCustomName("GHTargetStand " + args[0]);
            ghEStandTarget.setCustomNameVisible(false); //Sets the name to be invisible, currently visible
            ghEStandTarget.setInvulnerable(true); //makes it god lol
            ghEStandTarget.getEquipment().setHelmet(new ItemStack(Material.GLOWSTONE)); //gives it glowstone for a head
            ghEStandTarget.setVisible(false); //makes it invis
            ghEStandTarget.setGravity(false); // removes its gravity

            return true;
        }
    }
}
