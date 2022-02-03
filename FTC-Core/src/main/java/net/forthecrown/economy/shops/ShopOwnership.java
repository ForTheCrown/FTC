package net.forthecrown.economy.shops;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.forthecrown.economy.houses.House;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.JsonDeserializable;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.JsonUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Set;
import java.util.UUID;

public class ShopOwnership implements JsonSerializable, JsonDeserializable {
    private UUID owner;
    private House owningHouse;
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

    public UUID getOwner() {
        return owner;
    }

    public CrownUser ownerUser() {
        return getOwner() == null ? null : UserManager.getUser(getOwner());
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public House getOwningHouse() {
        return owningHouse;
    }

    public void setOwningHouse(House owningHouse) {
        this.owningHouse = owningHouse;
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

    public Set<UUID> getCoOwners() {
        return coOwners;
    }

    @Override
    public void deserialize(JsonElement element) {
        setOwner(null);
        setOwningHouse(null);
        clearCoOwners();

        if(element == null) return;

        if(element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();

            if(primitive.isNumber()) {
                setOwner(JsonUtils.readUUID(element));
            } else setOwningHouse(Registries.HOUSES.get(JsonUtils.readKey(element)));

            return;
        }

        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        if(json.has("owner")) setOwner(json.getUUID("owner"));

        if(json.has("owningHouse")) {
            setOwningHouse(Registries.HOUSES.get(json.getKey("owningHouse")));
        }

        if(json.has("coOwners")) {
            coOwners.addAll(json.getList("coOwners", JsonUtils::readUUID));
        }
    }

    @Override
    public JsonElement serialize() {
        if(hasOwner() && !hasCoOwners() && !hasOwningHouse()) return JsonUtils.writeUUID(getOwner());
        if(hasOwningHouse() && !hasOwner()) return JsonUtils.writeKey(owningHouse);

        JsonWrapper json = JsonWrapper.empty();
        if(hasOwner()) json.addUUID("owner", getOwner());
        if(hasOwningHouse()) json.add("owningHouse", owningHouse);
        if(hasCoOwners()) json.addList("coOwners", coOwners, JsonUtils::writeUUID);

        return json.getSource();
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
}
