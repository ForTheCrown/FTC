package net.forthecrown.core.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.types.TargetSelectorType;
import net.forthecrown.core.commands.brigadier.types.Vector3DType;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import javax.annotation.Nullable;
import java.util.Collection;

public class CommandHologram extends CrownCommandBuilder {

    public CommandHologram(){
        super("hologram", FtcCore.getInstance());

        setPermission("ftc.admin");
        register();
    }

    public static final NamespacedKey HOLOGRAM_KEY = new NamespacedKey(FtcCore.getInstance(), "hologram");

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Creates an invisible armorstand to create a holographic text effect
     *
     * Note: Use {NL} To make a new line in the <text> argument
     *
     * Valid usages of command:
     * - /hologram <location> <text>
     * - /hologram <text>
     *
     * Main Author: Botul
     */

    @Override
    protected void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command) {
        command
                .then(argument("remove")
                        .then(argument("holograms", TargetSelectorType.entities())
                                .executes(c -> {
                                    Collection<? extends Entity> entities = TargetSelectorType.getEntities(c, "holograms");

                                    int removed = 0;
                                    for (Entity e: entities){
                                        if(!(e instanceof ArmorStand)) continue;
                                        if(!e.getPersistentDataContainer().has(HOLOGRAM_KEY, PersistentDataType.BYTE)) continue;
                                        e.remove();
                                        removed++;
                                    }

                                    broadcastAdmin(c.getSource(), "Removed " + removed + " holograms");
                                    return removed;
                                })
                        )
                )

                .then(argument("create")
                        .then(argument("text", StringArgumentType.greedyString())
                                .executes(c -> {
                                    Player p = getPlayerSender(c);
                                    createHologram(c.getSource(), p.getLocation(), c.getArgument("text", String.class));
                                    return 0;
                                })
                        )

                        .then(argument("location", Vector3DType.vec3())
                                .then(argument("text", StringArgumentType.greedyString())
                                        .executes(c -> {
                                            Location loc = Vector3DType.getLocation(c, "location");
                                            createHologram(c.getSource(), loc, c.getArgument("text", String.class));
                                            return 0;
                                        })
                                )
                        )
                );
    }

    public static void createHologram(@Nullable CommandListenerWrapper source, Location location, String input){
        String[] names = input.split("\\{NL}");
        location.add(0, (names.length-1)*0.25, 0);

        for (String name : names){
            if(!name.isBlank()){
                ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
                stand.setAI(false);
                stand.setVisible(false);
                stand.setGravity(false);
                stand.setInvulnerable(true);
                stand.setCustomNameVisible(true);
                stand.setCustomName(CrownUtils.translateHexCodes(name));

                stand.getPersistentDataContainer().set(HOLOGRAM_KEY, PersistentDataType.BYTE, (byte) 1);
            }
            location.subtract(0, 0.25, 0);
        }

        if(source != null){
            broadcastAdmin(source, "Created hologram(s) named:");
            broadcastAdmin(source, input.replaceAll("\\{NL}", "\n"));
        }
    }
}
