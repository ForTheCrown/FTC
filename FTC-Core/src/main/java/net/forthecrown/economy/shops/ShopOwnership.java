package net.forthecrown.economy.shops;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.economy.houses.House;
import net.forthecrown.registry.Registries;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.TagUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public class ShopOwnership implements ShopComponent {
    @Getter @Setter
    private UUID owner;
    @Getter @Setter
    private House owningHouse;

    @Getter
    private final Set<UUID> coOwners = new ObjectOpenHashSet<>();

    public boolean hasOwner() {
        return owner != null;
    }

    public boolean hasOwningHouse() {
        return owningHouse != null;
    }

    public boolean hasCoOwners() {
        return !coOwners.isEmpty();
    }

    public CrownUser ownerUser() {
        return getOwner() == null ? null : UserManager.getUser(getOwner());
    }

    public boolean isCoOwner(UUID uuid) {
        return coOwners.contains(uuid);
    }

    public void addCoOwner(UUID uuid) {
        coOwners.add(uuid);
    }

    public void removeCoOwner(UUID uuid) {
        coOwners.remove(uuid);
    }

    public void clearCoOwners() {
        coOwners.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ShopOwnership ownership = (ShopOwnership) o;

        return new EqualsBuilder()
                .append(getOwner(), ownership.getOwner())
                .append(getOwningHouse(), ownership.getOwningHouse())
                .append(getCoOwners(), ownership.getCoOwners())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getOwner())
                .append(getOwningHouse())
                .append(getCoOwners())
                .toHashCode();
    }

    public boolean isOwner(UUID id) {
        if(owner == null) return false;
        return id.equals(getOwner());
    }

    public boolean mayEditShop(UUID uuid) {
        return isOwner(uuid) || isCoOwner(uuid);
    }

    @Nullable
    @Override
    public Tag save() {
        if(hasOwner() && !hasCoOwners() && !hasOwningHouse()) return TagUtil.writeUUID(getOwner());
        if(hasOwningHouse() && !hasOwner()) return TagUtil.writeKey(owningHouse);

        CompoundTag tag = new CompoundTag();
        if(hasOwner()) tag.putUUID("owner", getOwner());
        if(hasOwningHouse()) tag.put("owningHouse", TagUtil.writeKey(owningHouse));
        if(hasCoOwners()) tag.put("coOwners", TagUtil.writeList(coOwners, TagUtil::writeUUID));

        return tag;
    }

    @Override
    public void load(@Nullable Tag t) {
        setOwner(null);
        setOwningHouse(null);
        clearCoOwners();

        if(t == null) return;

        if(t.getId() == Tag.TAG_STRING) {
            setOwningHouse(Registries.HOUSES.read(t));
            return;
        }

        if(t.getId() == Tag.TAG_INT_ARRAY) {
            setOwner(TagUtil.readUUID(t));
            return;
        }

        CompoundTag tag = (CompoundTag) t;

        if(tag.contains("owner")) {
            setOwner(tag.getUUID("owner"));
        }

        if(tag.contains("owningHouse")) {
            setOwningHouse(Registries.HOUSES.read(tag.get("owningHouse")));
        }

        if(tag.contains("coOwners")) {
            coOwners.addAll(TagUtil.readList(tag.getList("coOwners", Tag.TAG_INT_ARRAY), TagUtil::readUUID));
        }
    }

    @Override
    public String getSerialKey() {
        return "owner";
    }
}