package net.forthecrown.core.inventory;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class SkullItems {
    public static ItemStack make(String name, String texture){
        return make(name, null, texture);
    }

    public static ItemStack make(String name, String signature, String texture){
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        CraftPlayerProfile playerProfile = new CraftPlayerProfile(UUID.randomUUID(), name);
        playerProfile.setProperty(new ProfileProperty("textures", texture, signature));

        meta.setPlayerProfile(playerProfile);
        item.setItemMeta(meta);

        return item;
    }
}
