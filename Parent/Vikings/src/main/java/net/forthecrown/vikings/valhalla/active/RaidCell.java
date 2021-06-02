package net.forthecrown.vikings.valhalla.active;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class RaidCell {
    public final int x;
    public final int z;

    public RaidCell(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        RaidCell cell = (RaidCell) o;

        return new EqualsBuilder()
                .append(x, cell.x)
                .append(z, cell.z)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(x)
                .append(z)
                .toHashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{x=" + x + ",z=" + z + '}';
    }
}
