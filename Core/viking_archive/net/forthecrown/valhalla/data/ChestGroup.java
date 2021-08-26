package net.forthecrown.valhalla.data;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.utils.math.Vector3i;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;

public class ChestGroup implements Keyed {

    private final Key groupKey;

    private Key lootTableKey;
    private byte maxChests;
    private ObjectList<Vector3i> possibleLocations;

    public ChestGroup(Key groupKey) {
        this.groupKey = groupKey;
    }

    public ChestGroup(Key groupKey, Key lootTableKey, byte maxChests, ObjectList<Vector3i> possibleLocations) {
        this.groupKey = groupKey;
        this.lootTableKey = lootTableKey;
        this.maxChests = maxChests;
        this.possibleLocations = possibleLocations;
    }

    public byte getMaxChests() {
        return maxChests;
    }

    public byte getMax() {
        return (byte) Math.min(possibleLocations.size(), maxChests);
    }

    public void setMaxChests(byte maxChests) {
        this.maxChests = maxChests;
    }

    public Key getLootTableKey() {
        return lootTableKey;
    }

    public void setLootTableKey(Key lootTableKey) {
        this.lootTableKey = lootTableKey;
    }

    public ObjectList<Vector3i> getPossibleLocations() {
        return possibleLocations;
    }

    public void setPossibleLocations(ObjectList<Vector3i> possibleLocations) {
        this.possibleLocations = possibleLocations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ChestGroup group = (ChestGroup) o;

        return new EqualsBuilder()
                .append(getMaxChests(), group.getMaxChests())
                .append(key(), group.key())
                .append(getLootTableKey(), group.getLootTableKey())
                .append(getPossibleLocations(), group.getPossibleLocations())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(key())
                .append(getLootTableKey())
                .append(getMaxChests())
                .append(getPossibleLocations())
                .toHashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "groupKey=" + groupKey +
                ", lootTableKey=" + lootTableKey +
                ", maxChests=" + maxChests +
                ", possibleLocations=" + possibleLocations +
                '}';
    }

    @Override
    public @NotNull Key key() {
        return groupKey;
    }
}
