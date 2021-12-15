package net.forthecrown.commands.admin;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.animation.AnimationBuilder;
import net.forthecrown.core.animation.BlockAnimation;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.inventory.weapon.RoyalSword;
import net.forthecrown.inventory.weapon.RoyalWeapons;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.ItemStackBuilder;
import net.forthecrown.utils.math.*;
import net.forthecrown.utils.transformation.BlockIterator;
import net.forthecrown.utils.transformation.BoundingBoxes;
import net.forthecrown.utils.transformation.FtcBoundingBox;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.core.Direction;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
import java.util.UUID;

public class CommandTestCore extends FtcCommand {

    static final BlockAnimation TEST_ANIM = new AnimationBuilder("ftccore:test_animation")
            .setTicksPerFrame(10)
            .addFrames(BoundingBoxes.createArray(
                    new Vector3i(273, 4, 219),
                    new Vector3iOffset(5, 5, 5),
                    Direction.EAST,
                    1, 12
            ))
            .buildAndRegister();

    public CommandTestCore(){
        super("coretest", Crown.inst());

        setAliases("testcore");
        setPermission(Permissions.ADMIN);
        register();
    }

    @Override
    public boolean test(CommandSource sender) { //test method used by Brigadier to determine who can use the command, from Predicate interface
        return sender.asBukkit().isOp() && testPermissionSilent(sender.asBukkit());
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            CrownUser user = getUserSender(c);
            Component gradient = FtcFormatter.gradientText("Giving RoyalSword", NamedTextColor.RED, NamedTextColor.BLUE);

            user.getInventory().addItem(RoyalWeapons.make(user.getUniqueId()));

            user.sendMessage(gradient);
            return 0;
        })
                .then(literal("block_iterator_test")
                        .then(argument("pos1", PositionArgument.blockPos())
                                .then(argument("pos2", PositionArgument.blockPos())
                                        .then(argument("mat", EnumArgument.of(Material.class))
                                                .executes(c -> {
                                                    Location pos1 = PositionArgument.getLocation(c, "pos1");
                                                    Location pos2 = PositionArgument.getLocation(c, "pos2");
                                                    Material mat = c.getArgument("mat", Material.class);

                                                    FtcBoundingBox box = FtcBoundingBox.of(pos1, pos2);
                                                    BlockIterator iterator = box.iterator();

                                                    while(iterator.hasNext()) {
                                                        Block b = iterator.next();

                                                        b.setType(mat);
                                                    }

                                                    c.getSource().sendAdmin("BlockIterator test passed");
                                                    return 0;
                                                })
                                        )
                                )
                        )
                )

                .then(literal("skin_profile_test")
                        .then(argument("id", StringArgumentType.greedyString())
                                .executes(c -> {
                                    CrownUser user = getUserSender(c);
                                    PlayerProfile profile = FtcUtils.profileWithTextureID("test", UUID.randomUUID(), StringArgumentType.getString(c, "id"));
                                    ItemStack item = new ItemStackBuilder(Material.PLAYER_HEAD, 1)
                                            .setProfile(profile)
                                            .build();

                                    user.getInventory().addItem(item);

                                    user.sendMessage("idk, I tired");
                                    return 0;
                                })
                        )
                )

                .then(literal("upgrade_sword")
                        .executes(c -> {
                            Player player = c.getSource().asPlayer();
                            ItemStack sword = player.getInventory().getItemInMainHand();
                            if(!RoyalWeapons.isRoyalSword(sword)) throw FtcExceptionProvider.create("Not holding sword");

                            RoyalSword sword1 = new RoyalSword(sword);
                            sword1.incrementGoal();
                            sword1.update();

                            c.getSource().sendAdmin("Upgraded sword");
                            return 0;
                        })
                )

                .then(argument("color1", EnumArgument.of(ChatColor.class))
                        .then(argument("color2", EnumArgument.of(ChatColor.class))
                                .then(argument("text", StringArgumentType.greedyString())
                                        .executes(c -> {
                                            Color c1 = c.getArgument("color1", ChatColor.class).asBungee().getColor();
                                            Color c2 = c.getArgument("color2", ChatColor.class).asBungee().getColor();
                                            TextColor color1 = TextColor.color(c1.getRed(), c1.getGreen(), c1.getBlue());
                                            TextColor color2 = TextColor.color(c2.getRed(), c2.getGreen(), c2.getBlue());
                                            String input = c.getArgument("text", String.class);

                                            Component gradient = FtcFormatter.gradientText(input, color1, color2);

                                            c.getSource().sendMessage(gradient);
                                            return 0;
                                        })
                                )
                        )
                );
    }
}
