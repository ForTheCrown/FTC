package net.forthecrown.july.items;

import net.forthecrown.july.EventConstants;
import net.forthecrown.july.offset.BlockOffset;

public class GemSpawnData {

    private final int amount;
    private final BlockOffset offset;

    private final boolean spawnsAlways;
    private final boolean secret;
    private final boolean gravity;

    public GemSpawnData(int amount, boolean spawnsAlways, int x, int y, int z){
        this(amount, false, spawnsAlways, x, y, z);
    }

    public GemSpawnData(int amount, boolean secret, boolean spawnsAlways, int x, int y, int z) {
        this(amount, false, secret, spawnsAlways, x, y, z);
    }

    public GemSpawnData(int amount, boolean gravity, boolean secret, boolean spawnsAlways, int x, int y, int z) {
        this.amount = amount;
        this.offset = BlockOffset.of(EventConstants.minLoc(), x, y, z);

        this.spawnsAlways = spawnsAlways;
        this.secret = secret;
        this.gravity = gravity;
    }

    public int getWorth() {
        return amount;
    }

    public BlockOffset getOffset() {
        return offset;
    }

    public boolean spawnsAlways() {
        return spawnsAlways;
    }

    public boolean isSecret() {
        return secret;
    }

    public boolean hasGravity() {
        return gravity;
    }
}
