package net.forthecrown.repop;

import net.querz.nbt.tag.CompoundTag;

public final class PopUtil {

    public static CompoundTag entitySpawnHelp(boolean persistent, boolean canPickUpLoot){
        CompoundTag tag = new CompoundTag();

        tag.putString("Paper.SpawnReason", "DEFAULT");
        tag.putShort("DeathTime", (short) 0);
        tag.putInt("Bukkit.updateLevel", 2);
        tag.putInt("APX", -538);
        tag.putByte("LeftHanded", (byte) 0);
        tag.putByte("OnGround", (byte) 0);
        tag.putFloat("AbsorptionAmount", 0.0f);

        CompoundTag brain = new CompoundTag();
        brain.put("memories", new CompoundTag());
        tag.put("Brain", brain);

        tag.putBoolean("Invulnerable", false);
        tag.putByte("AttachFace", (byte) 0);
        tag.putByte("Peek", (byte) 0);
        tag.putInt("HurtByTimestamp", 0);

        tag.putInt("Spigot.ticksLived", 0);
        tag.putInt("APY", 145);
        tag.putInt("APZ", -891);
        tag.putFloat("FallDistance", 0.0f);

        tag.putIntArray("Motion", new int[3]);

        tag.putShort("Fire", (short) -1);
        tag.putBoolean("CanPickUpLoot", canPickUpLoot);

        tag.putShort("HurtTime", (short) 0);
        tag.putByte("FallFlying", (byte) 0);
        tag.putBoolean("PersistenceRequired", persistent);
        tag.putInt("PortalCooldown", 0);

        return tag;
    }

}
