package net.forthecrown.commands.admin;

import com.mojang.brigadier.context.CommandContext;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Keys;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.registry.Registries;
import net.forthecrown.structure.*;
import net.forthecrown.structure.BlockPlacer;
import net.forthecrown.utils.math.Vector3i;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class CommandFtcStruct extends FtcCommand {

    public CommandFtcStruct() {
        super("FtcStruct");

        setAliases("ftcstructure");
        setPermission(Permissions.ADMIN);
        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     *
     * Valid usages of command:
     * /FtcStruct
     *
     * Permissions used:
     *
     * Main Author:
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .then(literal("create")
                        .then(argument("key", Keys.argumentType())
                                .executes(c -> {
                                    Player player = c.getSource().asPlayer();
                                    com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(player);

                                    Region region;
                                    try {
                                        region = wePlayer.getSession().getSelection();
                                    } catch (IncompleteRegionException e) {
                                        throw FtcExceptionProvider.create("Region selection not complete");
                                    }

                                    World world = player.getWorld();
                                    Vector3i start = Vector3i.of(region.getMinimumPoint());
                                    Vector3i size = Vector3i.of(region.getDimensions());

                                    BlockStructure structure = new BlockStructure(c.getArgument("key", NamespacedKey.class));

                                    StructureScanContext context = new StructureScanContext(world, start, size)
                                            .blockFilter(block -> block.getType() != Material.STRUCTURE_VOID)
                                            /*.includeEntities(true)*/;

                                    structure.scanFromWorld(context);

                                    Registries.STRUCTURES.register(structure.key(), structure);

                                    c.getSource().sendAdmin("Created structure named " + structure.key().asString());
                                    return 0;
                                })
                        )
                )

                .then(literal("place")
                        .then(argument("struct", RegistryArguments.structure())
                                .then(argument("pos", PositionArgument.position())
                                        .executes(c -> place(c, PlaceMirror.NONE, PlaceRotation.D_0))

                                        .then(argument("mirror", EnumArgument.of(PlaceMirror.class))
                                                .executes(c -> place(c, c.getArgument("mirror", PlaceMirror.class), PlaceRotation.D_0))

                                                .then(argument("rotation", EnumArgument.of(PlaceRotation.class))
                                                        .executes(c -> place(c,
                                                                c.getArgument("mirror", PlaceMirror.class),
                                                                c.getArgument("rotation", PlaceRotation.class)
                                                        ))
                                                )
                                        )
                                )
                        )
                );
    }

    private int place(CommandContext<CommandSource> c, PlaceMirror mirror, PlaceRotation rotation) {
        Location l = PositionArgument.getLocation(c, "pos");
        BlockStructure structure = c.getArgument("struct", BlockStructure.class);

        StructurePlaceContext context = new StructurePlaceContext(structure, Vector3i.of(l), BlockPlacer.world(l.getWorld()))
                .setMirror(mirror)
                .setRotation(rotation)
                .addEmptyProcessor()
                .addProccessor(new BlockTransformProcessor());

        structure.place(context);

        c.getSource().sendAdmin("Placed structure");
        return 0;
    }
}