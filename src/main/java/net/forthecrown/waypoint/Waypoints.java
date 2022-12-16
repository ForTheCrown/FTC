package net.forthecrown.waypoint;

import com.google.common.base.Strings;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.experimental.UtilityClass;
import net.forthecrown.commands.arguments.WaypointArgument;
import net.forthecrown.commands.guild.GuildProvider;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.DynmapUtil;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.BannedWords;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.guilds.GuildMember;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.structure.FunctionInfo;
import net.forthecrown.structure.StructurePlaceConfig;
import net.forthecrown.structure.Structures;
import net.forthecrown.user.User;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.Users;
import net.forthecrown.user.data.TimeField;
import net.forthecrown.user.data.UserHomes;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.text.Text;
import net.forthecrown.waypoint.type.PlayerWaypointType;
import net.forthecrown.waypoint.type.WaypointType;
import net.forthecrown.waypoint.type.WaypointTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.Snow;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.math.vector.Vector3i;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static net.forthecrown.user.data.UserTimeTracker.UNSET;
import static net.kyori.adventure.text.Component.text;

public @UtilityClass class Waypoints {
    /* ------------------------- COLUMN CONSTANTS --------------------------- */

    /** The required center column for guild waypoints */
    public final Material[] GUILD_COLUMN = {
            Material.STONE_BRICKS,
            Material.STONE_BRICKS,
            Material.LODESTONE,
    };

    /** The required center column for player waypoints */
    public final Material[] PLAYER_COLUMN = {
            Material.STONE_BRICKS,
            Material.STONE_BRICKS,
            Material.CHISELED_STONE_BRICKS,
    };

    /** The required center column for region poles */
    public final Material[] REGION_POLE_COLUMN = {
            Material.GLOWSTONE,
            Material.GLOWSTONE,
            Material.SEA_LANTERN
    };

    public static final int COLUMN_TOP = PLAYER_COLUMN.length - 1;

    /** Name of the Region pole {@link net.forthecrown.structure.BlockStructure} */
    public final String POLE_STRUCTURE = "region_pole";

    public final String FUNC_REGION_NAME = "region_name";

    public final String FUNC_RESIDENTS = "region_residents";

    /** Default size of the pole (5, 5, 5) */
    public Vector3i DEFAULT_POLE_SIZE = Vector3i.from(5);

    public BlockStructure getRegionPole() {
        return Structures.get()
                .getRegistry()
                .orNull(POLE_STRUCTURE);
    }

    public Vector3i poleSize() {
        return Structures.get()
                .getRegistry()
                .get(POLE_STRUCTURE)
                .map(BlockStructure::getDefaultSize)
                .orElse(DEFAULT_POLE_SIZE);
    }

    public void placePole(Waypoint region) {
        var structure = getRegionPole();

        if (structure == null) {
            FTC.getLogger().warn("No pole structure found in registry! Cannot place!");
            return;
        }

        var config = StructurePlaceConfig.builder()
                .placeEntities(true)
                .addNonNullProcessor()
                .addRotationProcessor()
                .world(region.getWorld())

                .pos(region.getBounds().min())

                // Function processors to ensure signs on pole
                // display correct information
                .addFunction(
                        FUNC_REGION_NAME,
                        (info, c) -> processTopSign(region, info, c)
                )
                .addFunction(
                        FUNC_RESIDENTS,
                        (info, c) -> processResidentsSign(region, info, c)
                )

                .build();

        structure.place(config);
    }

    private void processTopSign(Waypoint region,
                                FunctionInfo info,
                                StructurePlaceConfig config
    ) {
        var pos = config.getTransform().apply(info.getOffset());
        var world = config.getWorld();

        var block = Vectors.getBlock(pos, world);

        org.bukkit.block.data.type.Sign signData =
                (org.bukkit.block.data.type.Sign)
                        Material.OAK_SIGN.createBlockData();

        signData.setRotation(BlockFace.NORTH);
        block.setBlockData(signData, false);

        Sign sign = (Sign) block.getState();

        sign.line(1, signName(region));
        sign.line(2, text("Waypoint"));

        sign.update();
    }

    private static void processResidentsSign(Waypoint region,
                                             FunctionInfo info,
                                             StructurePlaceConfig config
    ) {
        if (region.get(WaypointProperties.HIDE_RESIDENTS)
                || region.getResidents().isEmpty()
        ) {
            return;
        }

        var pos = config.getTransform().apply(info.getOffset());
        var world = config.getWorld();
        var block = Vectors.getBlock(pos, world);

        WallSign signData = (WallSign) Material.OAK_WALL_SIGN.createBlockData();
        signData.setFacing(info.getFacing().asBlockFace());
        block.setBlockData(signData);

        Sign sign = (Sign) block.getState();
        var residents = region.getResidents();

        if (residents.size() == 1) {
            sign.line(1, text("Resident:"));
            sign.line(2,
                    Text.format("{0, user}",
                            residents.keySet()
                                    .iterator()
                                    .next()
                    )
            );
        } else {
            sign.line(1, text("Residents:"));
            sign.line(2, text(residents.size()));
        }

        sign.update();
    }

    private Component signName(Waypoint waypoint) {
        var name = waypoint.get(WaypointProperties.NAME);
        return text(Strings.isNullOrEmpty(name) ? "Wilderness" : name);
    }

    /**
     * Gets all invulnerable waypoints within the given
     * bounds in the given world
     */
    public Set<Waypoint> getInvulnerable(Bounds3i bounds3i, World world) {
        return filterSet(
                WaypointManager.getInstance()
                        .getChunkMap()
                        .getOverlapping(world, bounds3i)
        );
    }

    /**
     * Gets all invulnerable waypoints at the given
     * position in the given world
     */
    public Set<Waypoint> getInvulnerable(Vector3i pos, World world) {
        return filterSet(
                WaypointManager.getInstance()
                        .getChunkMap()
                        .get(world, pos)
        );
    }

    /** Removes non-invulnerable waypoints from the given set */
    private Set<Waypoint> filterSet(Set<Waypoint> waypoints) {
        waypoints.removeIf(waypoint -> !waypoint.get(WaypointProperties.INVULNERABLE));
        return waypoints;
    }

    /**
     * Gets the waypoint the player is currently in
     * @param player The player to find the colliding waypoints of
     *
     * @return The waypoint the player is inside, null, if not
     *         inside any waypoints
     */
    public Waypoint getColliding(Player player) {
        return WaypointManager.getInstance()
                .getChunkMap()
                .getOverlapping(
                        player.getWorld(),
                        Bounds3i.of(player.getBoundingBox())
                )
                .stream()
                .findAny()
                .orElse(null);
    }

    /**
     * Gets the nearest waypoint to the given user
     * @param user The user to get the nearest waypoint of
     * @return The nearest waypoint to the user, null, if there are no
     *         waypoints or the user is in a world with no waypoints
     */
    public Waypoint getNearest(User user) {
        return WaypointManager.getInstance()
                .getChunkMap()
                .findNearest(user.getLocation())
                .left();
    }

    /**
     * Tests if the given name is a valid region name.
     * <p>
     * A name is valid if, and only if, it does not contain any banned words,
     * contains no white spaces and does not equal either of the 2 waypoint
     * parsing flags: '-nearest' and '-current'
     *
     * @param name The name to test
     * @return True, if the name is valid, as specified in the above paragraph,
     *         false otherwise.
     */
    public boolean isValidName(String name) {
        return !BannedWords.contains(name)
                && !name.contains(" ")
                && !name.equalsIgnoreCase(WaypointArgument.FLAG_NEAREST)
                && !name.equalsIgnoreCase(WaypointArgument.FLAG_CURRENT)
                && GuildManager.get().getGuild(name) == null
                && UserManager.get().getUserLookup().get(name) == null;
    }

    /**
     * Tests if a potential/existing waypoint is in a valid area by testing the
     * blocks within the waypoint boundaries. Optionally, this method will also
     * ensure the waypoint does not overlap with any other waypoints.
     * <p>
     * This method will ensure the waypoint's center column exists, as well as
     * a platform underneath it and that the waypoint's bounds are not
     * obstructed in any way.
     * <p>
     * If <code>testOverlap == true</code> then this method will also ensure
     * that no other waypoints overlap the given one. If any do, an exception
     * is returned.
     * <p>
     * If any of these tests fail, an optional containing the corresponding
     * error will be returned. If all tests are passed however, then an empty
     * optional is returned, indicating the area is valid.
     *
     * @param pos The position the waypoint will be placed at. Note that this
     *            parameter should be shifted 1 block upward for region pole
     *            waypoints. As the platform underneath the region pole is
     *            considered as the starting block, instead of being under it.
     *            To ensure the above is the case use
     *            {@link PlayerWaypointType#isValid(Waypoint)}, as that performs
     *            that operation for you
     *
     * @param type The type to use for validation, this is used to test the
     *             column in the center of the waypoint.
     *
     * @param w The world the waypoint is in.
     *
     * @param testOverlap True, to ensure the given parameters do not overlap
     *                    with another waypoint.
     *
     * @return An empty optional if the area is valid, an optional containing
     *         a corresponding error message, if the area is invalid
     */
    public Optional<CommandSyntaxException> isValidWaypointArea(Vector3i pos,
                                                                PlayerWaypointType type,
                                                                World w,
                                                                boolean testOverlap
    ) {
        var bounds = type.createBounds()
                .move(pos)
                .expand(0, 1, 0, 0, 0, 0)
                .toWorldBounds(w);

        Material[] column = type.getColumn();

        // Test to make sure the area is empty and
        // contains the given type's column
        for (Block b: bounds) {
            // If currently in column position
            if (b.getX() == pos.x() && b.getZ() == pos.z()) {
                int offset = b.getY() - pos.y();

                // Within column bounds
                if (offset < column.length
                        && offset >= 0
                ) {
                    Material required = column[offset];

                    // If the column block is not the block
                    // that is required to be here, then
                    // return exception, else, skip this block
                    if (b.getType() == required) {
                        continue;
                    }

                    return Optional.of(
                            Exceptions.brokenWaypoint(
                                    pos.add(0, offset, 0),
                                    b.getType(),
                                    required
                            )
                    );
                }
            }

            // If we're on the minY level, which would be the
            // layer right under the waypoint, return an exception,
            // since this layer must be solid, if it is solid,
            // skip block
            if (bounds.minY() == b.getY()) {
                if (b.isSolid()) {
                    continue;
                }

                return Optional.of(
                        Exceptions.waypointPlatform()
                );
            }

            // Test if block is empty
            // hardcoded exception for snow lmao
            if (b.getBlockData() instanceof Snow snow) {
                int dif = snow.getMaximumLayers() - snow.getMinimumLayers();
                int half = dif / 2;

                if (snow.getLayers() <= half) {
                    continue;
                }
            } else if (b.isEmpty() || !b.isCollidable() || b.isPassable()) {
                continue;
            }

            return Optional.of(
                    Exceptions.waypointBlockNotEmpty(b)
            );
        }

        if (testOverlap) {
            Set<Waypoint> overlapping = WaypointManager.getInstance()
                    .getChunkMap()
                    .getOverlapping(bounds);

            if (!overlapping.isEmpty()) {
                return Optional.of(
                        Exceptions.overlappingWaypoints(overlapping.size())
                );
            }
        }

        return Optional.empty();
    }

    /**
     * Sets the waypoint's name sign.
     *
     * @param waypoint The waypoint to set the name sign of
     * @param name The name to set the sign to, if null, the sign
     *             is removed
     *
     * @throws IllegalStateException If the given waypoint is not a
     *                               {@link PlayerWaypointType}
     */
    public void setNameSign(Waypoint waypoint, String name)
            throws IllegalStateException
    {
        if (!(waypoint.getType() instanceof PlayerWaypointType type)) {
            throw new IllegalStateException(
                    "Only player/guild waypoints can have manual name signs"
            );
        }

        Vector3i pos = waypoint.getPosition()
                .add(0, type.getColumn().length, 0);

        World w = waypoint.getWorld();
        Objects.requireNonNull(w, "World unloaded");

        Block b = Vectors.getBlock(pos, w);

        if (Strings.isNullOrEmpty(name)) {
            b.setType(Material.AIR);
        } else {
            b.setBlockData(Material.OAK_SIGN.createBlockData());

            Sign sign = (Sign) b.getState();
            sign.line(1, text(name));
            sign.line(2, text("Waypoint"));
            sign.update();
        }
    }

    /**
     * Tests if the given block is the top of a waypoint.
     *
     * @param block The block to test
     * @return True, if the block's type is either the top of
     *         {@link #GUILD_COLUMN} or {@link #PLAYER_COLUMN}
     */
    public boolean isTopOfWaypoint(Block block) {
        var t = block.getType();

        return t == GUILD_COLUMN[COLUMN_TOP]
                || t == PLAYER_COLUMN[COLUMN_TOP];
    }

    /**
     * Attempts to create a waypoint.
     * <p>
     * Override method for {@link #tryCreate(CommandSource, GuildProvider.Simple)}
     * with a {@link GuildProvider#SENDERS_GUILD} as the guild provider
     * parameter.
     *
     * @param source The source creating the waypoint
     * @return The created waypoint
     *
     * @throws CommandSyntaxException If the pole couldn't be created
     *
     * @see #tryCreate(CommandSource, GuildProvider.Simple)
     */
    public Waypoint tryCreate(CommandSource source)
            throws CommandSyntaxException
    {
        return tryCreate(source, GuildProvider.SENDERS_GUILD);
    }

    /**
     * Attempts to create a waypoint.
     * <p>
     * The given source must be a player. The player must be looking at a valid
     * waypoint top block. If they aren't, an exception is thrown. An exception
     * will also be thrown if the block they are looking at is a guild waypoint
     * block, and they are not in a guild, or do not have permission to
     * move the guild's waypoint, or are not in a chunk owned by their guild.
     * <p>
     * After that, this method will ensure the waypoint's area is valid, see
     * {@link #isValidWaypointArea(Vector3i, PlayerWaypointType, World, boolean)}.
     * If that fails, the returned exception is thrown.
     * <p>
     * Then the waypoint is created, if the created waypoint is for a guild,
     * then the guild's waypoint is set as the created waypoint, otherwise, in
     * the case of a player waypoint, the player's home is set to the created
     * waypoint.
     *
     * @param source The source attempting to create the waypoint.
     * @param provider The guild provider to get the source's guild or to access
     *                 the guild the (staff) source is moving the waypoint for.
     *
     * @return The created waypoint
     *
     * @throws CommandSyntaxException If the waypoint creation fails at any
     *                                stage
     */
    public Waypoint tryCreate(CommandSource source, GuildProvider.Simple provider)
            throws CommandSyntaxException
    {
        var player = source.asPlayer();

        Block b = player.getTargetBlockExact(
                5, FluidCollisionMode.NEVER
        );

        if (b == null) {
            throw Exceptions.FACE_WAYPOINT_TOP;
        }

        if (WaypointConfig.isDisabledWorld(b.getWorld())) {
            throw Exceptions.WAYPOINTS_WRONG_WORLD;
        }

        PlayerWaypointType type;
        Vector3i pos = Vectors.from(b);

        // If attempting to set guild waypoint
        if (b.getType() == GUILD_COLUMN[COLUMN_TOP]) {
            type = WaypointTypes.GUILD;

            User user = Users.get(player);
            Guild guild = provider.get(source);

            // Can't make a waypoint for a guild, if you're
            // not in a guild lol
            if (guild == null) {
                throw Exceptions.NOT_IN_GUILD;
            }

            // Ensure member has relocation permission
            GuildMember member = guild.getMember(user.getUniqueId());
            if (member == null) {
                if (!source.hasPermission(Permissions.GUILD_ADMIN)) {
                    throw Exceptions.NO_PERMISSION;
                }

            } else if (!member.hasPermission(GuildPermission.CAN_RELOCATE)) {
                throw Exceptions.G_NO_PERM_WAYPOINT;
            }

            // Ensure chunk is owned by the user's guild
            Guild chunkOwner = GuildManager.get()
                    .getOwner(Vectors.getChunk(pos));

            if (!Objects.equals(guild, chunkOwner)) {
                throw Exceptions.G_EXTERNAL_WAYPOINT;
            }
        } else if (b.getType() == PLAYER_COLUMN[COLUMN_TOP]) {
            type = WaypointTypes.PLAYER;
            validateMoveInCooldown(Users.get(source.asPlayer()));
        } else {
            throw Exceptions.invalidWaypointTop(b.getType());
        }

        var existing = WaypointManager.getInstance()
                .getChunkMap()
                .get(b.getWorld(), pos);

        Waypoint waypoint;
        if (existing.isEmpty()) {
            pos = pos.sub(0, type.getColumn().length - 1, 0);

            // Ensure the area is correct and validate the
            // center block column to ensure it's a proper waypoint
            var error = Waypoints.isValidWaypointArea(
                    pos,
                    type,
                    b.getWorld(),
                    true
            );

            if (error.isPresent()) {
                throw error.get();
            }

            waypoint = makeWaypoint(type, pos, source);
        } else {
            waypoint = existing.iterator().next();

            // Ensure the pole they're looking at is valid
            var error = waypoint.getType().isValid(waypoint);
            if (error.isPresent()) {
                throw error.get();
            }
        }

        if (type == WaypointTypes.GUILD) {
            var user = Users.get(player);
            var guild = provider.get(source);

            setGuildWaypoint(guild, waypoint, user);
        } else {
            if (waypoint.get(WaypointProperties.OWNER) == null) {
                waypoint.set(
                        WaypointProperties.OWNER,
                        player.getUniqueId()
                );
            }

            User user = Users.get(player);
            user.setTimeToNow(TimeField.LAST_MOVEIN);

            UserHomes homes = user.getHomes();
            homes.setHomeWaypoint(waypoint);
        }

        return waypoint;
    }

    /**
     * Tests if the user can move their home waypoint.
     * <p>
     * If {@link WaypointConfig#moveInHasCooldown} is false, then
     * this method will not throw an exception.
     * <p>
     * The cooldown length is determined by
     * {@link WaypointConfig#moveInCooldown}
     *
     * @param user The user to test
     *
     * @throws CommandSyntaxException If they cannot move their waypoint home
     */
    public void validateMoveInCooldown(User user)
            throws CommandSyntaxException
    {
        long lastMoveIn = user.getTime(TimeField.LAST_MOVEIN);

        // Unset cool downs mean they haven't
        // tried to set their home yet.
        // If movein cooldown disabled or the
        // cooldown length is less than 1
        if (lastMoveIn == UNSET
                || !WaypointConfig.moveInHasCooldown
                || WaypointConfig.moveInCooldown < 1
        ) {
            return;
        }

        long remainingCooldown = Time.timeUntil(
                lastMoveIn + WaypointConfig.moveInCooldown
        );

        if (remainingCooldown > 0) {
            throw Exceptions.cooldownEndsIn(remainingCooldown);
        }
    }

    public void setGuildWaypoint(Guild guild, Waypoint waypoint, User user) {
        var current = guild.getSettings().getWaypoint();

        if (current != null) {
            current.set(WaypointProperties.GUILD_OWNER, null);
            removeIfPossible(current);
        }

        guild.sendMessage(
                Messages.guildSetCenter(waypoint.getPosition(), user)
        );

        guild.getSettings()
                .setWaypoint(waypoint.getId());

        waypoint.set(WaypointProperties.GUILD_OWNER, guild.getId());
    }

    public Waypoint makeWaypoint(WaypointType type,
                                 @Nullable Vector3i pos,
                                 CommandSource source
    ) {
        Vector3i position;

        if (pos == null) {
            position = Vectors.intFrom(source.getLocation());
        } else {
            position = pos;
        }

        Waypoint waypoint = new Waypoint();
        waypoint.setType(type);
        waypoint.setPosition(position, source.getWorld());

        if (pos != null) {
            source.sendMessage(
                    Messages.createdWaypoint(position, type)
            );
        } else {
            source.sendAdmin(
                    Messages.createdWaypoint(position, type)
            );
        }

        WaypointManager.getInstance()
                .addWaypoint(waypoint);

        return waypoint;
    }

    public void removeIfPossible(Waypoint waypoint) {
        if (waypoint.getType().isDestroyed(waypoint)) {
            WaypointManager.getInstance()
                    .removeWaypoint(waypoint);

            return;
        }

        if (!waypoint.getResidents().isEmpty()
                || waypoint.getType() == WaypointTypes.ADMIN
                || waypoint.get(WaypointProperties.INVULNERABLE)
                || waypoint.get(WaypointProperties.GUILD_OWNER) != null
                || !Strings.isNullOrEmpty(waypoint.get(WaypointProperties.NAME))
        ) {
            return;
        }

        WaypointManager.getInstance()
                .removeWaypoint(waypoint);
    }

    /**
     * Gets the effective name of the waypoint.
     * <p>
     * What effective in this case means, is the name that should be displayed
     * on the waypoint. If the given waypoint is owned by a guild, but has no
     * custom name set, then this will return the guild's name. If a name is
     * set, then it is returned always, even if the waypoint has a name,
     *
     * @param waypoint The waypoint to get the name of
     * @return The gotten name, may be null
     */
    public @Nullable String getEffectiveName(@NotNull Waypoint waypoint) {
        if (!Strings.isNullOrEmpty(waypoint.get(WaypointProperties.NAME))) {
            return waypoint.get(WaypointProperties.NAME);
        }

        var guildId = waypoint.get(WaypointProperties.GUILD_OWNER);
        if (guildId == null) {
            return null;
        }

        var guild = GuildManager.get().getGuild(guildId);

        if (guild != null) {
            return guild.getSettings().getName();
        } else {
            return null;
        }
    }

    /**
     * Updates the given waypoint's dynmap marker, if and only if, the dynmap
     * plugin is installed.
     * @param waypoint The waypoint to update the marker of
     * @see WaypointDynmap#updateMarker(Waypoint)
     */
    public void updateDynmap(Waypoint waypoint) {
        if (!DynmapUtil.isInstalled()) {
            return;
        }

        WaypointDynmap.updateMarker(waypoint);
    }
}