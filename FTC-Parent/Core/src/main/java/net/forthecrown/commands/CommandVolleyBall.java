package net.forthecrown.commands;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.squire.Squire;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CommandVolleyBall extends FtcCommand {
    public static final NamespacedKey KEY = Squire.createFtcKey("volley_ball");
    public static final int chickenHealth = 1024;

    public CommandVolleyBall() {
        super("volleyball", CrownCore.inst());

        setPermission(Permissions.HELPER);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /volleyball
     *
     * Permissions used:
     * ftc.helper
     *
     * Main Author: Wout
     * Edit: Ants
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(context -> {
            Player player = context.getSource().asPlayer();
            createChicken(player.getLocation());

            player.sendMessage(Component.text("Spawned volleyball chicken!").color(NamedTextColor.GRAY));
            return 0;
        });
    }

    private void createChicken(Location loc) {
        Chicken chicken = loc.getWorld().spawn(loc, Chicken.class);
        chicken.setAdult();
        chicken.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(0);
        chicken.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(chickenHealth);
        chicken.setHealth(chickenHealth);

        chicken.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 9, false, false, false));
        chicken.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 9, false, false, false));

        chicken.setPersistent(true);
        chicken.setRemoveWhenFarAway(false);

        chicken.getPersistentDataContainer().set(KEY, PersistentDataType.BYTE, (byte) 1);
    }
}