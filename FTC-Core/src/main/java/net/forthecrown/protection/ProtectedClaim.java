package net.forthecrown.protection;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;

import java.util.Map;
import java.util.UUID;

public class ProtectedClaim {
    private final Map<UUID, TrustLevel> trustLevelMap = new Object2ObjectOpenHashMap<>();
    private SubClaimMap subClaims;

    private final long id;
    private ClaimType type;
    private UUID owner;
    private Bounds2i bounds;
    private ProtectedClaim parent;

    private long lastEdit;
    private UUID lastEditor;

    public ProtectedClaim(long id) {
        this.id = id;
    }

    public long getClaimID() {
        return id;
    }

    public Bounds2i getBounds() {
        return bounds;
    }

    void setBounds(Bounds2i bounds) {
        this.bounds = bounds;
    }

    public ClaimMap getSubClaims() {
        if(isSubClaim()) return null;
        return subClaims == null ? subClaims = new SubClaimMap(this) : subClaims;
    }

    public boolean hasSubClaims() {
        return !isSubClaim() && subClaims != null;
    }

    public TrustLevel getTrustLevel(UUID uuid) {
        return trustLevelMap.getOrDefault(uuid, TrustLevel.NONE);
    }

    public void setTrustLevel(UUID uuid, TrustLevel level) {
        if(level == TrustLevel.NONE) {
            trustLevelMap.remove(uuid);
            return;
        }

        trustLevelMap.put(uuid, level);
    }

    public boolean canGiveTrust(UUID trustGiver, TrustLevel level) {
        if(isOwner(trustGiver)) return true;
        TrustLevel giverLevel = getTrustLevel(trustGiver);

        return giverLevel == TrustLevel.MANAGE && level.ordinal() > TrustLevel.MANAGE.ordinal();
    }

    public boolean isOwner(UUID uuid) {
        if(getOwner() == null) return false;
        return getOwner().equals(uuid);
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public ClaimType getType() {
        return type;
    }

    public void setType(ClaimType type) {
        this.type = type;
    }

    public ProtectedClaim getParent() {
        return parent;
    }

    void setParent(ProtectedClaim parent) {
        this.parent = parent;
    }

    public boolean isSubClaim() {
        return getParent() != null;
    }

    public boolean isLegalAreaForSubClaim(Bounds2i b) {
        if(!getBounds().contains(b)) return false;
        return getSubClaims().isLegalForNewClaim(b);
    }

    public long getLastEdit() {
        return lastEdit;
    }

    public UUID getLastEditor() {
        return lastEditor;
    }

    public void edit(UUID editor) {
        this.lastEdit = System.currentTimeMillis();
        this.lastEditor = editor;
    }

    public void save(CompoundTag tag) {
        tag.put("bounds", bounds.save());
        tag.putByte("type", (byte) type.ordinal());

        if(owner != null) {
            tag.putUUID("owner", owner);
        }

        if(lastEdit != 0L) {
            tag.putLong("last_edit", lastEdit);
            if(lastEditor != null) tag.putUUID("last_editor", lastEditor);
        }

        if(!subClaims.isEmpty()) {
            CompoundTag sub = new CompoundTag();
            subClaims.save(sub);

            tag.put("sub_claims", sub);
        }

        if(!trustLevelMap.isEmpty()) {
            CompoundTag trust = new CompoundTag();

            for (Map.Entry<UUID, TrustLevel> e: trustLevelMap.entrySet()) {
                trust.putByte(e.getKey().toString(), (byte) e.getValue().ordinal());
            }

            tag.put("trust", trust);
        }
    }

    public void load(CompoundTag tag) {
        this.bounds = Bounds2i.load(tag.get("bounds"));
        this.type = ClaimType.values()[tag.getByte("type")];

        if(tag.contains("owner")) {
            this.owner = tag.getUUID("owner");
        }

        if(tag.contains("last_edit")) {
            this.lastEdit = tag.getLong("last_edit");
            if(tag.contains("last_editor")) this.lastEditor = tag.getUUID("last_editor");
        }

        if(tag.contains("subClaims")) {
            CompoundTag sub = tag.getCompound("sub_claims");
            subClaims.load(sub);
        }

        if(tag.contains("trust")) {
            CompoundTag trust = tag.getCompound("trust");

            for (Map.Entry<String, Tag> e: trust.tags.entrySet()) {
                UUID uuid = UUID.fromString(e.getKey());
                byte ordinal = ((IntTag) e.getValue()).getAsByte();
                TrustLevel level = TrustLevel.values()[ordinal];

                setTrustLevel(uuid, level);
            }
        }
    }
}
