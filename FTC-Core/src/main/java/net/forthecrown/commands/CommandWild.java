package net.forthecrown.commands;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.WorldArgument;
import net.forthecrown.grenadier.types.selectors.EntityArgument;
import net.forthecrown.grenadier.types.selectors.EntitySelector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;

public class CommandWild extends FtcCommand {

    public CommandWild(){
        super("wild", Crown.inst());

        setPermission(Permissions.DEFAULT);
        setDescription("Puts you in the wild, only available in the Resource World");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Teleports either the command sender or a specified player
     * to a random coordinate.
     * Note: only peeps with ftc.admin perm can specify players
     *
     * Valid usages of command:
     * - /wild
     * - /wild <target selector>
     *
     * Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    Player p = getPlayerSender(c);

                    if (!p.getWorld().getName().equalsIgnoreCase("world_resource")) {
                        p.sendMessage(ChatColor.GRAY + "You can only do this in the resource world.");
                        p.sendMessage(ChatColor.GRAY + "The portal to get there is in Hazelguard.");
                        return 0;
                    }

                    if (Cooldown.contains(p, "RandomFeatures_Wild")) {
                        p.sendMessage(ChatColor.GRAY + "You can only do this command every 30 seconds.");
                        return 0;
                    }

                    wildTP(p, p.getWorld(), true);
                    return 0;
                })
                .then(argument("player", EntityArgument.multipleEntities())
                        .requires(c -> c.hasPermission(Permissions.FTC_ADMIN))

                        .then(argument("world", WorldArgument.world())
                                .requires(c -> c.hasPermission(Permissions.FTC_ADMIN))

                                .executes(c -> {
                                    Collection<? extends Entity> players = c.getArgument("player", EntitySelector.class).getEntities(c.getSource());
                                    World world = c.getArgument("world", World.class);

                                    for (Entity p: players) {
                                        wildTP(p, world, false);
                                    }

                                    return 1;
                                })
                        )
                     );
    }

    private static final Component rwWildMessage = Component.text("You've been teleported, do ")
            .color(NamedTextColor.GRAY)
            .append(Component.text("[warp portal]")
                    .color(NamedTextColor.YELLOW)
                    .clickEvent(ClickEvent.runCommand("/warp portal"))
                    .hoverEvent(Component.text("Warps you to the portal"))
            )
            .append(Component.text(" to get back"));

    public static void wildTP(Entity p, World world, boolean cooldown){
        if(p instanceof LivingEntity) ((LivingEntity) p).addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 400, 1));
        p.teleport(wildLocation(world));

        if(p.getWorld().getName().contains("world_resource")) p.sendMessage(rwWildMessage);
        if(cooldown) Cooldown.add(p, "RandomFeatures_Wild", 600);
    }

    private static boolean isInvalid(Location bottom){
        return isInvalidLocation(bottom) || isInvalidLocation(bottom.add(0, 1, 0));
    }

    private static boolean isInvalidLocation(Location location){
        Biome biome = location.getWorld().getBiome(location.getBlockX(), location.getBlockY(), location.getBlockX());
        if(biome.name().contains("OCEAN")) return true;

        Material mat = location.getBlock().getType();
        return !mat.isAir();
    }

    public static Location wildLocation(World world) {
        return wildLocation(world, new CrownRandom());
    }

    private static Location wildLocation(World world, CrownRandom random){
        final int maxSize = (int) ((world.getWorldBorder().getSize()/2) - 200);
        boolean changeY = world.getName().contains("nether");
        int x = rand(maxSize, random);
        int z = rand(maxSize, random);
        int y = changeY ? random.intInRange(30, 120) : 150;

        Location result = new Location(world, x, y, z);

        while (isInvalid(result)){
            x = rand(maxSize, random);
            z = rand(maxSize, random);
            if(changeY) y = random.intInRange(30, 120);

            result = new Location(world, x, y, z);
        }

        return result.toCenterLocation();
    }

    private static int rand(int maxSize, CrownRandom random){
        int result = 200 + random.nextInt(maxSize);
        return random.nextBoolean() ? -result : result;
    }
}
