package net.forthecrown.protection;

import com.sk89q.worldedit.math.BlockVector2;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.FtcUser;
import org.bukkit.World;

public class UserClaimSession {
    private final FtcUser user;
    private boolean admin, subClaim;
    private BlockVector2 first, second;
    private ProtectedClaim subClaimParent;
    private ProtectedClaim editing;
    private World world;

    public UserClaimSession(FtcUser user) {
        this.user = user;
    }

    public CrownUser getUser() {
        return user;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isSubClaim() {
        return subClaim;
    }

    public void setSubClaim(boolean subClaim) {
        this.subClaim = subClaim;
    }

    public BlockVector2 getFirst() {
        return first;
    }

    public void setFirst(BlockVector2 first) {
        this.first = first;
    }

    public BlockVector2 getSecond() {
        return second;
    }

    public void setSecond(BlockVector2 second) {
        this.second = second;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public ProtectedClaim getSubClaimParent() {
        return subClaimParent;
    }

    public void setSubClaimParent(ProtectedClaim subClaimParent) {
        this.subClaimParent = subClaimParent;
    }

    public boolean isEditing() {
        return editing != null;
    }

    public void setEditing(ProtectedClaim editing) {
        this.editing = editing;
    }

    public ProtectedClaim getEditing() {
        return editing;
    }

    public boolean claimReady() {
        return world != null
                && first != null
                && second != null;
    }

    public Bounds2i getSelected() {
        if(first == null || second == null) return null;
        return Bounds2i.of(first, second);
    }
}
