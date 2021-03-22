package net.forthecrown.randomfeatures.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.Cooldown;
import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.types.TargetSelectorType;
import net.forthecrown.randomfeatures.RandomFeatures;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CommandWild extends CrownCommandBuilder {

    public CommandWild(RandomFeatures plugin){
        super("wild", plugin);

        setPermission(null);
        register();
    }

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
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

                    wildTP(p);
                    return 0;
                })
                .then(argument("player", TargetSelectorType.player())
                        .requires(c -> !(c.getBukkitSender() instanceof Player))

                        .executes(c -> {
                            Player p = TargetSelectorType.getPlayer(c, "player");
                            wildTP(p);
                            return 1;
                        })
                );
    }

    public static void wildTP(Player p){
        int x;
        if (Math.random() < 0.5) x = CrownUtils.getRandomNumberInRange(200, 1800);
        else x = CrownUtils.getRandomNumberInRange(-1800, -200);

        int z;
        if (Math.random() < 0.5) z = CrownUtils.getRandomNumberInRange(200, 1800);
        else z = CrownUtils.getRandomNumberInRange(-1800, -200);

        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 400, 1));
        p.teleport(new Location(p.getWorld(), x, 150, z));

        if(p.getWorld().getName().contains("world_resouce")) p.sendMessage(ChatColor.GRAY + "You've been teleported, do " + ChatColor.YELLOW + "/warp portal" + ChatColor.GRAY + " to get back.");
        Cooldown.add(p, "RandomFeatures_Wild", 600);
    }
}
