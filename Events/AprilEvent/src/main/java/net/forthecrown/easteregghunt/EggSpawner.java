package net.forthecrown.easteregghunt;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.forthecrown.core.api.Announcer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.TileEntitySkull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Rotatable;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Slime;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.util.*;

public class EggSpawner {
    private final Random random;

    public static final String[] SKULL_TEXTURES = {
            "819611e7bee7abbbe9a46a8a99caa421dfeaf5dcbe7ce589b53ffd796a188d38",
            "17f0dc98de4bd231ee8348d777d291b7a4a2ad9df41bb39edcf870a9e3fac6e4",
            "fde0a50b2c0765f06868f60cacec3f2caacdcdd8554b4cadb9d64640275d7196",
            "184e76dc8733f96a8468d38f73a5f770896d5a129cca8c29efe99197cb666af2",
            "1e3a0238bf1203afa84f523fdf15763cff0e37ead0b318078dab90f934e2c58e",
            "83df652b0c7ddb94461fc6c074aa5e5fd15731d7dd3d533351c7065bd82ba554",
            "6b7446550f0f9576b731728b5cbeb2bcea25fd1a5560a17b2357e616fbc65621",
            "f7b323b84576f8354c5337c7f16f11f30c1cffe08054d2689d06c7a9ab0ca03f",
            "b59ae72588db766f25a97770bb7ce41c8172ec7765d71843d02248ba30aaf17b",
            "80d49ac768f016b61a08de44f058782bb2a4c1809a75c8b159fdc063868ecf65",
            "14c6afae76e645ce512065b8b46abb8f357cb8b47559aa781909e0f908dd2583",
            "d1df29146067785fb0f4a766044ca6693bd9d24c869f12e34406e95f7c38dee7",
            "ae869cdca37511edfaf6ebf8a24324c97faa6e1a25f1f8686716389ed0406d89",
            "58b9e29ab1a795e2b887faf1b1a31025e7cc3073330afec375393b45fa335d1",
            "1213d67bc72f3085337abc9bd81373ed589435ac2f0e829bed7b3040efb55a",
            "e9b7d8c0636f73932eba734bad1826cc8c16d783aa9a5cfccda461a0105932",
            "d1874346c1ffab96c76c6dc3fa2f1921ef56209b8836e2ed7c4eb5d95c5f58"
    };
    public static final Team NO_CLIP_TEAM = Objects.requireNonNull(Bukkit.getScoreboardManager().getMainScoreboard().getTeam("NoClip"));

    public final NamespacedKey key;
    public final List<Slime> placed_eggs = new ArrayList<>();

    public EggSpawner(){
        this.random = new Random();
        key = new NamespacedKey(EasterMain.inst, "slime");
    }

    public void removeAllEggs(){
        for (Slime s: placed_eggs){
            s.getLocation().getBlock().setType(Material.AIR);
            s.remove();
        }
        placed_eggs.clear();
    }

    public void removeEgg(Slime s){
        placed_eggs.remove(s);
        s.getLocation().getBlock().setType(Material.AIR);
        s.remove();
    }

    public int placeEggs(){
        short safeGuard = 300;
        short placedEggAmount = 0;
        int maxAmount = EasterMain.eggSpawns.size();
        final List<Location> indexed = new ArrayList<>();

        while (placedEggAmount < (maxAmount/3*2)){
            if(safeGuard < 0) return indexed.size();
            Location location = EasterMain.eggSpawns.get(random.nextInt(EasterMain.eggSpawns.size()));
            if(indexed.contains(location)) continue;

            spawnEgg(location);
            indexed.add(location);

            placedEggAmount++;
            safeGuard--;
        }
        return indexed.size();
    }

    public void placeRandomEgg(@Nullable Location banned){
        Location toSpawnAt = EasterMain.eggSpawns.get(random.nextInt(EasterMain.eggSpawns.size()));
        short safeGuard = 300;
        if(banned != null){
            while (banned.distance(toSpawnAt) < 10 && (toSpawnAt.getBlock().getType() == Material.PLAYER_HEAD || !toSpawnAt.getNearbyEntitiesByType(Slime.class, 1.5).isEmpty())) {
                toSpawnAt = EasterMain.eggSpawns.get(random.nextInt(EasterMain.eggSpawns.size()));
                safeGuard--;
                if(safeGuard <= 0) return;
            }
        }
        if(!toSpawnAt.getNearbyEntitiesByType(Slime.class, 1.5).isEmpty() || toSpawnAt.getBlock().getType() == Material.PLAYER_HEAD) return;
        spawnEgg(toSpawnAt);
    }

    public void spawnEgg(Location location){
        location.setX(location.getBlockX() + .5);
        location.setZ(location.getBlockZ() + .5);

        location.getWorld().spawn(location, Slime.class, slime -> {
            slime.setRemoveWhenFarAway(false);
            slime.setPersistent(false);
            slime.setGravity(false);
            slime.setAI(false);
            slime.setSize(2);
            slime.setInvisible(true);
            slime.setCustomNameVisible(false);
            slime.customName(Component.text("Egg!").color(NamedTextColor.LIGHT_PURPLE));
            slime.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
            NO_CLIP_TEAM.addEntry(slime.getUniqueId().toString());

            int rotation = random.nextInt(16);
            rotation = setSkullUrl(SKULL_TEXTURES[random.nextInt(SKULL_TEXTURES.length)], location.getBlock(), rotation);
            slime.setRotation(rotation*45, 0);
            placed_eggs.add(slime);
        });
    }

    private int setSkullUrl(String skinUrl, Block block, int rotation) {
        block.setType(Material.PLAYER_HEAD);
        Skull skullData = (Skull)block.getState();
        Rotatable rotatable = (Rotatable) skullData.getBlockData();

        BlockFace face = BlockFace.values()[rotation];
        if(face == BlockFace.DOWN || face == BlockFace.UP || face == BlockFace.SELF){
            face = BlockFace.NORTH;
            rotation = 4;
        }

        rotatable.setRotation(face);
        skullData.setBlockData(rotatable);
        skullData.update();

        TileEntitySkull skullTile = (TileEntitySkull)((CraftWorld)block.getWorld()).getHandle().getTileEntity(new BlockPosition(block.getX(), block.getY(), block.getZ()));
        skullTile.setGameProfile(getNonPlayerProfile(skinUrl));
        skullTile.update();
        block.getState().update(true);
        return rotation;
    }

    public static GameProfile getNonPlayerProfile(String skinURL) {
        GameProfile newSkinProfile = new GameProfile(UUID.randomUUID(), null);
        skinURL = "http://textures.minecraft.net/texture/" + skinURL;
        newSkinProfile.getProperties().put("textures", new Property("textures", Base64Coder.encodeString("{textures:{SKIN:{url:\"" + skinURL + "\"}}}")));
        return newSkinProfile;
    }

    public void spawnRareEgg(Location banned){
        Location toSpawnAt = EasterMain.eggSpawns.get(random.nextInt(EasterMain.eggSpawns.size()));
        short safeGuard = 300;
        if(banned != null){
            while (banned.distance(toSpawnAt) < 10 && (toSpawnAt.getBlock().getType() == Material.PLAYER_HEAD || !toSpawnAt.getNearbyEntitiesByType(Slime.class, 1.5).isEmpty())) {
                toSpawnAt = EasterMain.eggSpawns.get(random.nextInt(EasterMain.eggSpawns.size()));
                safeGuard--;
                if(safeGuard <= 0) return;
            }
        }
        if(!toSpawnAt.getNearbyEntitiesByType(Slime.class, 1.5).isEmpty() || toSpawnAt.getBlock().getType() == Material.PLAYER_HEAD) return;
        Announcer.debug(toSpawnAt);

        Location finalToSpawnAt = toSpawnAt;
        toSpawnAt.getWorld().spawn(toSpawnAt, Slime.class, slime -> {
            slime.setRemoveWhenFarAway(false);
            slime.setPersistent(false);
            slime.setGravity(false);
            slime.setAI(false);
            slime.setSize(2);
            slime.setInvisible(true);
            slime.setCustomNameVisible(false);
            slime.customName(Component.text("Egg!").color(NamedTextColor.LIGHT_PURPLE));
            slime.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 2);
            NO_CLIP_TEAM.addEntry(slime.getUniqueId().toString());

            int rotation = random.nextInt(16);
            rotation = setSkullUrl("c679efc59e22fcf34f7448bf7ab6664f799c3dff656cf483098be36c9ae1", finalToSpawnAt.getBlock(), rotation);
            slime.setRotation(rotation*45, 0);
            placed_eggs.add(slime);
        });
    }
}