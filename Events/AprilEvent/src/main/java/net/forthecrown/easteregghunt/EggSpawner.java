package net.forthecrown.easteregghunt;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.TileEntitySkull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Slime;
import org.bukkit.persistence.PersistentDataType;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class EggSpawner {
    private final Random random;

    public static final String[] SKULL_DATAS = {
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODE5NjExZTdiZWU3YWJiYmU5YTQ2YThhOTljYWE0MjFkZmVhZjVkY2JlN2NlNTg5YjUzZmZkNzk2YTE4OGQzOCJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmFkMDZmMDRlYTkxYjIzNTI0OWFmZDg4NWY1ZTA0MDIyNDAxZGQ3N2I0NTI2MmE5ZjAyNGU0ZDdiN2E1MWM3OSJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjdiMzIzYjg0NTc2ZjgzNTRjNTMzN2M3ZjE2ZjExZjMwYzFjZmZlMDgwNTRkMjY4OWQwNmM3YTlhYjBjYTAzZiJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWUzYTAyMzhiZjEyMDNhZmE4NGY1MjNmZGYxNTc2M2NmZjBlMzdlYWQwYjMxODA3OGRhYjkwZjkzNGUyYzU4ZSJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWI4MGU0M2M0YTE0OGNiZTI1NmNiODMyOWU5YmRhYWE0ZjMwZDcwZDgzOTZkNDE5NGZlMDc1ZjA2ODUxY2MxZSJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzQ3Mzc1YzBhYzNlMjdhZGUyMTkzMDJhZGExODhkZDgzOTkyMTUxYTJhYzhhNGY0OWVlZjQxODk1YWQzNzU2In19fQ==",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTdmMGRjOThkZTRiZDIzMWVlODM0OGQ3NzdkMjkxYjdhNGEyYWQ5ZGY0MWJiMzllZGNmODcwYTllM2ZhYzZlNCJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2FjZTIxYTVhYWZmMzczMjliOGYwNTk1YmU5YmE4ODZiYmQ5OGI4NGI5YTg2OThiZTc5YmEwZjczYjg2In19fQ==",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTI0MGExYTZlMjNjYjc4N2Q0ZGFmMTMzYWIyYzhiOWFiYWM0ZWZiODFlNzM5Y2FlYzU4YjNlOGVhNTQ5ODcifX19=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTA2OWM1ZGIzMDI0ZGRmYzU2N2ZiZGI3Y2RlZjJmMTQ4MWRhMmNhYWNlZWYxNjM4MWU4OGJhNGU4NmIwYzBlZiJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTk5YzRlYTA0ZDhmZGUxM2QxMjRjYjJmNjc3M2I3MWY3YTQ3MjllZmY4NWE5NmNjMTU1ZjUyY2FkZjhlYzVlIn19fQ==",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2JlNzU0NTI5N2RmZDYyNjZiYmFhMjA1MTgyNWU4ODc5Y2JmYTQyYzdlN2UyNGU1MDc5NmYyN2NhNmExOCJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2YxYzI1YmY2MWEyYmJjN2E5OTU4ZTliOWRiYzlmZjdlMDg3N2M2MjJlNDdmYmNkNTUzNmU5YmUzZDAxMWIzMyJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjIzZmNiMTU2ZTQ1ZmViNGM3NzU5MjZiZWQ4MjMwZjhkNjUwYzdiZmJjMzQ2MGQwYmI2ZWExMDhiYjFjZWQ5ZCJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmNiZWQ4YTgzMzU4NTQ0MDMyYzMxNGIzODFlYmJiMWVjNGY0MGZiNTI3M2Y0NWUxNTZhZWM3YjJjMDdlZGZkZCJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjJiZWY3ZGY4NTY2ZDExYTk1NzljMzQ3OWY4NzM4NWYyZmMwMGFhNjA3MjJjOTZkMGQ3N2Y4NDQxZDIxOGJmOCJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmRlMGE1MGIyYzA3NjVmMDY4NjhmNjBjYWNlYzNmMmNhYWNkY2RkODU1NGI0Y2FkYjlkNjQ2NDAyNzVkNzE5NiJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2U4MzBlODEzMDBmZWYyMjdhYTI5ZWUwOGY2YjhkMDQ3YmE1ZTg0MTI4ZTJkMGQ1Njc4YmFhZWNkYjllOWRhNyJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmNhODdjZGE5MWQ2NzhiOGM0YjQzN2ZkNjJiODNlODU1MzE1ZWFlZDY1NjMzNGU0NTdmOTViNGJmYTUyYjc4MyJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWM1MDJiYWEwMTBiZDdmODA0ZDQ0YmMxNjNjYmUwMDc4YzUwYzY1ZjhkYTc3MmE1MGRkNzg4M2U5N2VjNzg5ZCJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmYyNTk0NGI3NDY1ZmY0NTg2YTM0MWY4ODFmNDg4YjZmOWQ0YjNmZmMxMDBmNTBhNzcwODcwNzViM2NlYjFiIn19fQ==",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWFmOTUxMDUwY2NkMWM3M2ZlZTRlMTM4Yzk1M2E0MmYwYzA3OGE5MmVkOGU2OTI2OTZiZjRhNjM5MTIyMDhkYyJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmU2ZmFiMDkxZTQ5NmMwOTY5MTA0ODBkYTBkODVlZTkxOWJjNDlhYTMxNzc1Y2FkYmJmNTA1ZWY0MTFiNWY5NCJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjk2OTIzYWQyNDczMTAwMDdmNmFlNWQzMjZkODQ3YWQ1Mzg2NGNmMTZjMzU2NWExODFkYzhlNmIyMGJlMjM4NyJ9fX0=",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNThiOWUyOWFiMWE3OTVlMmI4ODdmYWYxYjFhMzEwMjVlN2NjMzA3MzMzMGFmZWMzNzUzOTNiNDVmYTMzNWQxIn19fQ==",
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWYxOGI5YTQzNmEyN2Y4MTNjMjg3ZWI2NzM3OWVmOGFkYmZkYzcwYWZhZjMwNGM0M2IxNjZjZTk4NmRkOCJ9fX0="
    };

    public final NamespacedKey key;

    public EggSpawner(){
        this.random = new Random();
        key = new NamespacedKey(EasterMain.instance, "slime");
    }

    public void placeEggs(){
        short safeGuard = 300;
        short placedEggAmount = 0;
        int maxAmount = EasterMain.eggSpawns.size();
        final List<Location> indexed = new ArrayList<>();

        while (placedEggAmount < (maxAmount/3*2)){
            if(safeGuard < 0) return;
            Location location = EasterMain.eggSpawns.get(random.nextInt(EasterMain.eggSpawns.size()));
            if(indexed.contains(location)) continue;

            spawnEgg(location);
            indexed.add(location);

            placedEggAmount++;
            safeGuard--;
        }
    }

    public void placeRandomEgg(){
        spawnEgg(EasterMain.eggSpawns.get(random.nextInt(EasterMain.eggSpawns.size())));
    }

    public void spawnEgg(Location location){
        Slime slime = (Slime) location.getWorld().spawnEntity(location, EntityType.SLIME);

        slime.setGravity(false);
        slime.setAI(false);
        slime.setCustomNameVisible(true);
        slime.customName(Component.text("Egg!").color(NamedTextColor.LIGHT_PURPLE));
        slime.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);

        int rotation = random.nextInt(16);
        slime.setRotation(0, rotation*45);

        setSkullUrl(SKULL_DATAS[random.nextInt(SKULL_DATAS.length)], location.getBlock(), rotation);
    }

    private void setSkullUrl(String url, Block block, int rotation){
        block.setType(Material.PLAYER_HEAD);
        Skull skull = (Skull) block.getState();

        BlockData data = Bukkit.createBlockData("minecraft:player_head[rotation=" + rotation + "]");
        skull.setBlockData(data);

        TileEntitySkull skullTile = (TileEntitySkull)((CraftWorld)block.getWorld()).getHandle().getTileEntity(new BlockPosition(block.getX(), block.getY(), block.getZ()));
        skullTile.setGameProfile(getNonPlayerProfile(url));
        block.getState().update(true);
    }

    private GameProfile getNonPlayerProfile(String skinURL) {
        GameProfile newSkinProfile = new GameProfile(UUID.randomUUID(), null);
        newSkinProfile.getProperties().put("textures", new Property("textures", Base64Coder.encodeString("{textures:{Value:" + skinURL + "}}")));
        return newSkinProfile;
    }
}
