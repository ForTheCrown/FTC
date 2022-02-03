package net.forthecrown.protection;

import com.sk89q.worldedit.math.BlockVector2;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Crown;
import net.forthecrown.utils.math.Vector3i;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ClaimVisualizer {
    private static final Map<Player, Visualization> VISUALIZATIONS = new Object2ObjectOpenHashMap<>();
    public static final int SHOW_TIME_TICKS = 30 * 20;

    public static void visualize(Context context) {
        Visualization vis = VISUALIZATIONS.computeIfAbsent(context.player, player1 -> visCreate(context));

        if(vis.getClaimID() != context.getClaim().getClaimID()) {
            vis.cancelSchedule();
            vis.normal();

            vis = visCreate(context);
            VISUALIZATIONS.put(context.getPlayer(), vis);
        }

        vis.schedule();
        vis.visualize();
    }

    public static void stopVisualization(Player player) {
        Visualization vis = VISUALIZATIONS.get(player);
        if(vis == null) return;

        vis.cancelSchedule();
        vis.normal();

        VISUALIZATIONS.remove(player);
    }

    static Visualization visCreate(Context context) {
        return new Visualization(context.getWorld(), context.getPlayer(), visCreatePieces(context), context.getClaim().getClaimID());
    }

    static List<Piece> visCreatePieces(Context context) {
        World world = context.getWorld();
        List<Piece> pieces = new ObjectArrayList<>();

        addPieces(context.claim, world, context.isErrorType(), pieces::add);

        if(context.includeChildren()) {
            for (ProtectedClaim c: context.claim.getSubClaims()) {
                addPieces(c, world, false, pieces::add);
            }
        }

        return pieces;
    }

    private static void addPieces(ProtectedClaim claim, World world, boolean illegal, Consumer<Piece> adder) {
        PieceType corner = claim.isSubClaim() ? PieceType.CHILD_CLAIM_CORNER : PieceType.CORNER;
        PieceType normal = claim.isSubClaim() ? PieceType.CHILD_CLAIM : PieceType.NORMAL;

        if(illegal) normal = PieceType.ILLEGAL;
        else if(claim.getType() == ClaimType.ADMIN && !claim.isSubClaim()) normal = PieceType.ADMIN;

        Bounds2i bounds = claim.getBounds();

        for (int x = bounds.minX() + 1; x < bounds.maxX(); x += 3) {
            for (int z = bounds.minZ() + 1; z < bounds.maxZ(); z += 3) {
                int y = world.getHighestBlockYAt(x, z);

                Piece p = new Piece(new Vector3i(x, y, z), normal);
                adder.accept(p);
            }
        }

        for (BlockVector2 v: bounds.corners()) {
            int y = world.getHighestBlockYAt(v.getX(), v.getZ());

            Piece p = new Piece(new Vector3i(v.getX(), y, v.getZ()), corner);
            adder.accept(p);
        }
    }

    public static class Context {
        private final World world;
        private final Player player;
        private final ProtectedClaim claim;

        private boolean includeChildren;
        private boolean errorType;

        public Context(World world, Player player, ProtectedClaim claim) {
            this.world = world;
            this.player = player;
            this.claim = claim;
        }

        public World getWorld() {
            return world;
        }

        public Player getPlayer() {
            return player;
        }

        public ProtectedClaim getClaim() {
            return claim;
        }

        public boolean includeChildren() {
            return includeChildren && !isErrorType();
        }

        public Context setIncludeChildren(boolean includeChildren) {
            this.includeChildren = includeChildren;
            return this;
        }

        public boolean isErrorType() {
            return errorType;
        }

        public Context setErrorType(boolean errorType) {
            this.errorType = errorType;
            return this;
        }
    }
}

class Visualization {
    private final World world;
    private final Player player;
    private final List<Piece> pieces;
    private final long claimID;
    private BukkitTask task;
    private boolean active;

    public Visualization(World world, Player player, List<Piece> pieces, long id) {
        this.world = world;
        this.player = player;
        this.pieces = pieces;
        claimID = id;
    }

    void schedule() {
        cancelSchedule();

        this.task = Bukkit.getScheduler().runTaskLater(Crown.inst(), () -> {
            ClaimVisualizer.stopVisualization(player);
        }, ClaimVisualizer.SHOW_TIME_TICKS);
    }

    void cancelSchedule() {
        if(task == null || task.isCancelled()) return;

        task.cancel();
        task = null;
    }

    void visualize() {
        if(active) return;
        active = true;

        for (Piece p: pieces) {
            p.visualize(player, world);
        }
    }

    void normal() {
        if(!active) return;
        active = false;

        for (Piece p: pieces) {
            p.normal(player, world);
        }
    }

    public long getClaimID() {
        return claimID;
    }
}

record Piece(Vector3i pos, PieceType type) {
    void visualize(Player player, World world) {
        player.sendBlockChange(pos.toLoc(world), type.data());
    }

    void normal(Player player, World world) {
        player.sendBlockChange(pos.toLoc(world), pos.getBlock(world).getBlockData());
    }
}

enum PieceType {
    ADMIN (Material.PUMPKIN),
    NORMAL (Material.GOLD_BLOCK),
    ILLEGAL (Material.REDSTONE_BLOCK),
    CORNER (Material.GLOWSTONE),
    CHILD_CLAIM_CORNER (Material.IRON_BLOCK),
    CHILD_CLAIM (Material.WHITE_WOOL);

    final Material mat;

    PieceType(Material mat) {
        this.mat = mat;
    }

    public BlockData data() {
        return mat.createBlockData();
    }
}
