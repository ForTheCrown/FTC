package net.forthecrown.utils.math;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.function.IntBinaryOperator;

public abstract class AbstractVector3i<T extends AbstractVector3i<T>> implements ImmutableVector3i {
    protected int x;
    protected int y;
    protected int z;

    protected final boolean immutable;

    public AbstractVector3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;

        immutable = true;
    }

    public AbstractVector3i(int x, int y, int z, boolean immutable) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.immutable = immutable;
    }

    protected abstract T getThis();
    protected abstract T cloneAt(int x, int y, int z, boolean immutable);

    public T set(int x, int y, int z) {
        if(immutable) {
            return cloneAt(x, y, z, true);
        }

        this.x = x;
        this.y = y;
        this.z = z;

        return getThis();
    }

    public int getX() {
        return x;
    }

    public T setX(int x) {
        return set(x, y, z);
    }

    public int getY() {
        return y;
    }

    public T setY(int y) {
        return set(x, y, z);
    }

    public int getZ() {
        return z;
    }

    public T setZ(int z) {
        return set(x, y, z);
    }

    public T above(int amount) { return inDirection(BlockFace.UP, amount); }
    public T above() { return inDirection(BlockFace.UP, 1); }

    public T below(int amount) { return inDirection(BlockFace.DOWN, amount); }
    public T below() { return inDirection(BlockFace.DOWN, 1); }

    public T inDirection(BlockFace face){ return inDirection(face, 1); }

    public T inDirection(BlockFace face, int amount){
        final int x = face.getModX() * amount;
        final int y = face.getModY() * amount;
        final int z = face.getModZ() * amount;

        return add(x, y, z);
    }

    public T subtract(ImmutableVector3i pos){ return subtract(pos.getX(), pos.getY(), pos.getZ()); }
    public T subtract(Location l){ return subtract(l.getBlockX(), l.getBlockY(), l.getBlockZ()); }
    public T subtract(Vector vector){ return subtract(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ()); }
    public T subtract(Vec3i vec3i){ return subtract(vec3i.getX(), vec3i.getY(), vec3i.getZ()); }

    public T add(ImmutableVector3i pos){ return add(pos.getX(), pos.getY(), pos.getZ()); }
    public T add(Location l){ return add(l.getBlockX(), l.getBlockY(), l.getBlockZ()); }
    public T add(Vector vector){ return add(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ()); }
    public T add(Vec3i vec3i){ return add(vec3i.getX(), vec3i.getY(), vec3i.getZ()); }

    public T add(int x, int y, int z){
        return set(
                getX() + x,
                getY() + y,
                getZ() + z
        );
    }

    public T subtract(int x, int y, int z){
        return set(
                getX() - x,
                getY() - y,
                getZ() - z
        );
    }

    public T divide(ImmutableVector3i vec) { return divide(vec.getX(), vec.getY(), vec.getZ()); }
    public T divide(int n) { return divide(n, n, n); }

    public T divide(int x, int y, int z) {
        return set(
                getX() / x,
                getY() / y,
                getZ() / z
        );
    }

    public T multiply(ImmutableVector3i vec) { return multiply(vec.getX(), vec.getY(), vec.getZ()); }
    public T multiply(int n) { return multiply(n, n, n); }

    public T multiply(int x, int y, int z) {
        return set(
                getX() * x,
                getY() * y,
                getZ() * z
        );
    }

    public T shiftLeft(int amount) {
        return set(
                getX() << amount,
                getY() << amount,
                getZ() << amount
        );
    }

    public T shiftRight(int amount) {
        return set(
                getX() >> amount,
                getY() >> amount,
                getZ() >> amount
        );
    }

    @Override
    public T clone() {
        return cloneAt(getX(), getY(), getZ(), immutable);
    }

    public T immutable() {
        return immutable ? getThis() : cloneAt(getX(), getY(), getZ(), true);
    }

    public T mutable() {
        if(!immutable) return getThis();
        return cloneAt(getX(), getY(), getZ(), false);
    }

    public Vector toVec(){
        return new Vector(getX(), getY(), getZ());
    }

    public BlockVector2 to2D() {
        return BlockVector2.at(getX(), getZ());
    }

    public net.minecraft.core.BlockPos toNms(){
        return new net.minecraft.core.BlockPos(getX(), getY(), getZ());
    }

    public BlockVector3 toWE(){
        return BlockVector3.at(getX(), getY(), getZ());
    }

    public ChunkPos getChunkPos() {
        return new ChunkPos(
                SectionPos.blockToSectionCoord(getX()),
                SectionPos.blockToSectionCoord(getZ())
        );
    }

    public T zero() {
        return cloneAt(0, 0, 0, immutable);
    }

    public T getMinimum(ImmutableVector3i o) {
        return apply(Math::min, o);
    }

    public T getMaximum(ImmutableVector3i o) {
        return apply(Math::max, o);
    }

    public T apply(IntBinaryOperator o, ImmutableVector3i other) {
        return set(
                o.applyAsInt(other.getX(), x),
                o.applyAsInt(other.getY(), y),
                o.applyAsInt(other.getZ(), z)
        );
    }

    public T clampX(int min, int max) {
        return set(
                Mth.clamp(x, min, max),
                y,
                z
        );
    }

    public T clampY(int min, int max) {
        return set(
                x,
                Mth.clamp(y, min, max),
                z
        );
    }

    public T clampZ(int min, int max) {
        return set(
                x,
                y,
                Mth.clamp(z, min, max)
        );
    }

    public T invert() {
        return set(-getX(), -getY(), -getZ());
    }

    public Tag saveAsTag() {
        int[] cords = {x, y, z};
        return new IntArrayTag(cords);
    }

    public long toLong() {
        return toNms().asLong();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        AbstractVector3i<?> i = (AbstractVector3i<?>) o;

        return new EqualsBuilder()
                .append(getX(), i.getX())
                .append(getY(), i.getY())
                .append(getZ(), i.getZ())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getX())
                .append(getY())
                .append(getZ())
                .toHashCode();
    }

    @Override
    public String toString() {
        return "(" + getX() + ", " + getY() + ", " + getZ() + ")";
    }
}