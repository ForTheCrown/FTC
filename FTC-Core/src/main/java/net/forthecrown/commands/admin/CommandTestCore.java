package net.forthecrown.commands.admin;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.Region;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.book.builder.TextInfo;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.animation.AnimationBuilder;
import net.forthecrown.core.animation.BlockAnimation;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.core.chat.TimePrinter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.block.BlockArgument;
import net.forthecrown.grenadier.types.block.ParsedBlock;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.inventory.ItemStackBuilder;
import net.forthecrown.inventory.weapon.RoyalSword;
import net.forthecrown.inventory.weapon.RoyalWeapons;
import net.forthecrown.structure.PlaceRotation;
import net.forthecrown.structure.StructureTransform;
import net.forthecrown.structure.tree.NodePlaceContext;
import net.forthecrown.structure.tree.StructureTree;
import net.forthecrown.structure.tree.test.TestNode;
import net.forthecrown.structure.tree.test.TestNodes;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.math.Vector3i;
import net.forthecrown.utils.math.Vector3iOffset;
import net.forthecrown.utils.math.WorldBounds3i;
import net.forthecrown.utils.transformation.BoundingBoxes;
import net.forthecrown.utils.transformation.FtcBoundingBox;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec2;
import org.apache.logging.log4j.Logger;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.awt.Color;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.UUID;

public class CommandTestCore extends FtcCommand {
    private static final Logger LOGGER = Crown.logger();

    static final BlockAnimation TEST_ANIM = new AnimationBuilder("test_animation")
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

    public static final Vector3i PLACE_POS = new Vector3i(200, 24, -587);
    private static final List<StructureTree.Entry<TestNode>> entries = new ObjectArrayList<>();
    private static int index;

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            CrownUser user = getUserSender(c);
            Component gradient = FtcFormatter.gradientText("Giving RoyalSword", NamedTextColor.RED, NamedTextColor.BLUE);

            user.getInventory().addItem(RoyalWeapons.make(user.getUniqueId()));

            user.sendMessage(gradient);
            return 0;
        })

                .then(literal("test_bounds")
                        .then(argument("block", BlockArgument.block())
                                .executes(c -> {
                                    ParsedBlock block = c.getArgument("block", ParsedBlock.class);

                                    Player player = c.getSource().asPlayer();
                                    com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(player);
                                    Region selection = FtcUtils.getSelectionSafe(wePlayer);

                                    WorldBounds3i bounds3i = WorldBounds3i.of(player.getWorld(), selection);

                                    for (Block b: bounds3i) {
                                        block.place(b.getWorld(), b.getX(), b.getY(), b.getZ(), true);
                                    }

                                    c.getSource().sendAdmin(
                                            ChatUtils.format("Completed test for region {}, volume: {}, size: {}, world: {}",
                                                    bounds3i, bounds3i.volume(), bounds3i.span(), bounds3i.getWorld().getName()
                                            )
                                    );
                                    return 0;
                                })
                        )
                )

                .then(literal("test_font")
                        .then(argument("input", StringArgumentType.greedyString())
                                .executes(c -> {
                                    String input = c.getArgument("input", String.class);
                                    int legacySize = TextInfo.getPixLengthLegacy(input);
                                    int newSize = TextInfo.getPxLength(input);

                                    LOGGER.info("input: '{}'", input);
                                    LOGGER.info("legacySize: {}, newSize: {}", legacySize, newSize);

                                    return 0;
                                })
                        )

                        .executes(c -> {
                            Font f = TextInfo.MC_FONT;

                            String z = "Z";
                            BlockVector2 exampleSize = BlockVector2.at(5, 7);

                            Rectangle2D rec = f.getStringBounds(z, TextInfo.RENDER_CONTEXT);

                            double xSize = rec.getWidth();
                            double zSize = rec.getHeight();

                            LOGGER.info("recX: {}", rec.getWidth());
                            LOGGER.info("recZ: {}", rec.getHeight());

                            Vec2 pixelRatio = new Vec2((float) (exampleSize.getX() / xSize), (float) (exampleSize.getZ() / zSize));

                            String x = "i";
                            BlockVector2 expectedSize = BlockVector2.at(1, 7);

                            Rectangle2D gottenSize = f.getStringBounds(x, TextInfo.RENDER_CONTEXT);

                            LOGGER.info("expectedSize of 'x': ({} {})", expectedSize.getX(), expectedSize.getZ());
                            LOGGER.info("gottenSize of 'x': ({} {})", gottenSize.getWidth(), gottenSize.getHeight());

                            LOGGER.info("pixelSize: ({} {})", Math.ceil(gottenSize.getWidth() * pixelRatio.x), Math.ceil(gottenSize.getHeight() * pixelRatio.y));
                            return 0;
                        })
                )

                .then(literal("hardcoded_node_step")
                        .executes(c -> {
                            CrownUser user = getUserSender(c);
                            NodePlaceContext context = new NodePlaceContext(user.getWorld(), StructureTransform.DEFAULT, PLACE_POS, PlaceRotation.D_0);

                            if(entries.isEmpty()) {
                                StructureTree<TestNode> tree = TestNodes.createTestTree();

                                entries.add(tree.getStart());
                                tree.forEachEntry(entries::add);

                                index = 0;
                            }

                            StructureTree.Entry<TestNode> n = entries.get(index);
                            n.generate(context, false);

                            index++;

                            if (index <= 0 || index >= entries.size()) {
                                index = 0;
                            }

                            c.getSource().sendAdmin("Attempted to take structure placement step");
                            return 0;
                        })
                )

                .then(literal("test_struct_nodes")
                        .executes(c -> {
                            CrownUser user = getUserSender(c);

                            TestNodes.generateAndPlace(user.getWorld(), Vector3i.of(user.getLocation()));

                            c.getSource().sendAdmin("Attempted to generate and place structures");
                            return 0;
                        })
                )

                .then(literal("keys")
                        .executes(c -> {
                            for (World w: Bukkit.getWorlds()) {
                                Crown.logger().info(w.key().asString());
                            }
                            return 0;
                        })
                )

                .then(literal("block_iterator_test")
                        .then(argument("pos1", PositionArgument.blockPos())
                                .then(argument("pos2", PositionArgument.blockPos())
                                        .then(argument("mat", EnumArgument.of(Material.class))
                                                .executes(c -> {
                                                    Location pos1 = PositionArgument.getLocation(c, "pos1");
                                                    Location pos2 = PositionArgument.getLocation(c, "pos2");
                                                    Material mat = c.getArgument("mat", Material.class);

                                                    FtcBoundingBox box = FtcBoundingBox.of(pos1, pos2);

                                                    for (Block b : box) {
                                                        b.setType(mat);
                                                    }

                                                    c.getSource().sendAdmin("BlockIterator test passed");
                                                    return 0;
                                                })
                                        )
                                )
                        )
                )

                .then(literal("time_test")
                        .executes(c -> {
                            CrownRandom random = new CrownRandom();
                            CommandSource source = c.getSource();

                            for (int i = 0; i < 100; i++) {
                                long time = random.nextLong(1, Long.MAX_VALUE);

                                String oldVal = tOldValue(time);
                                String newVal = new TimePrinter(time).printString();

                                source.sendMessage("time: " + time);
                                source.sendMessage("oldValue: " + oldVal);
                                source.sendMessage("newValue: " + newVal);
                                source.sendMessage("--------------------------------");
                            }

                            long time = 100L;

                            String oldVal = tOldValue(time);
                            String newVal = new TimePrinter(time).printString();

                            source.sendMessage("time: " + time);
                            source.sendMessage("oldValue: " + oldVal);
                            source.sendMessage("newValue: " + newVal);
                            source.sendMessage("--------------------------------");

                            return 0;
                        })
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
                            sword1.incrementRank();
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

    private String tOldValue(long millis) {
        long hours = (millis / 3600000);
        long minutes = (millis /60000) % 60;
        long seconds = (millis / 1000) % 60;
        long days = hours / 24;
        hours -= days*24;

        StringBuilder stringBuilder = new StringBuilder();
        if(days != 0) stringBuilder.append(days).append(" day").append(s(days)).append(", ");
        if(hours != 0) stringBuilder.append(hours).append(" hour").append(s(hours)).append(", ");
        if(minutes != 0) stringBuilder.append(minutes).append(" minute").append(s(minutes)).append(" and ");
        if(seconds != 0) stringBuilder.append(seconds).append(" second").append(s(seconds));

        return stringBuilder.toString();
    }

    private static String s(long l){
        return l == 1 ? "" : "s";
    }
}
