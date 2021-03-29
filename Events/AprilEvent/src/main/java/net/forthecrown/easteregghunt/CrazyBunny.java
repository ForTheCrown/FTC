package net.forthecrown.easteregghunt;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.apache.commons.lang.math.RandomUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class CrazyBunny {

    private boolean alive;
    private Zombie entity;

    public void spawn(Location location){
        entity = location.getWorld().spawn(getFreeLocation(location), Zombie.class, zomzom -> {
           zomzom.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.5);
           zomzom.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 1000000, 2, false, false, false));

           double health = 500;
           zomzom.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
           zomzom.setHealth(health);

            EntityEquipment equipment = zomzom.getEquipment();
            equipment.setHelmet(headItem());
            equipment.setChestplate(makeWhiteLeather(Material.LEATHER_CHESTPLATE));
            equipment.setLeggings(makeWhiteLeather(Material.LEATHER_LEGGINGS));
            equipment.setBoots(makeWhiteLeather(Material.LEATHER_BOOTS));

            zomzom.setBaby();
        });
        alive = true;
    }

    private Location getFreeLocation(Location banned){
        Location toSpawnAt = EasterMain.eggSpawns.get(RandomUtils.nextInt(EasterMain.eggSpawns.size()));
        short safeGuard = 300;
        if(banned != null){
            while (banned.distance(toSpawnAt) < 20) {
                toSpawnAt = EasterMain.eggSpawns.get(RandomUtils.nextInt(EasterMain.eggSpawns.size()));
                safeGuard--;
                if(safeGuard <= 0) return null;
            }
        }
        return toSpawnAt;
    }

    private ItemStack makeWhiteLeather(Material material){
        ItemStack result = new ItemStack(material, 1);
        LeatherArmorMeta meta = (LeatherArmorMeta) result.getItemMeta();
        meta.setColor(Color.WHITE);
        result.setItemMeta(meta);
        return result;
    }

    private ItemStack headItem(){
        ItemStack result = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) result.getItemMeta();

        PlayerProfile profile = new CraftPlayerProfile(UUID.randomUUID(), "easter_bunny_head");
        profile.setProperty(new ProfileProperty("textures",
                "ewogICJ0aW1lc3RhbXAiIDogMTU4Nzc1NTcwNzc1MSwKICAicHJvZmlsZUlkIiA6ICI0OGI0NDQzYmY5YzU0ZWQ1YTBmYjk5ODk1ZDVjNjA0YyIsCiAgInByb2ZpbGVOYW1lIiA6ICJQbGF5ZXJOYW1lSGVyZSIsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xNDc5Y2U2OTkyODFiYzNjZTI1NWY2NjcxZDY2ZTU4MzhkMjdkM2NkMTQ3NmRkYmQ0ZmUwNTVhOTQ3NGFmYjIzIgogICAgfQogIH0KfQ==",
                "cygQce9jPtTMJyNugFz65bYH1amqzQClFq2alYdu1jXhnijSvhJVlYP6MIy90kq5RHc8mDApEsiOcbfppCS2bHElRGYzl6/Qb6ZBk7cg3p1MMi1l2aG96xIYz+VGbavTBZvVf9Ib1hzEyiMZEO83xkrAagtkQX/dclqq6OLpUYKv9FBcdX5elZo/nCzyzivTkDwuy43tysw9wJ059qJVYDH7//Hrk4j2vEAHQ7QNMXyr1JLSEku4VFIdHdEtp7undAZ+O2oTeHSOmMKNozVxVPfoSOEfXh6AjNlelpP+ofu0QGINNAuCW1riZHZIfm/Xsl650BgJqbnhn0XEEjYLJDpXK5IyPUjSxDjFTzm72F27dwpN99loyMxAHAuKmC0AMYLgf2yxKxCeBUl7Z8JbucUaRr02MypWvoXfpl4aXbgTeHgM+V8WW+1eBcnewHM8prDz3N1j9BGvfxRAmGH82x0fK77qVxYQceLQR8lHjP1WPcKU1YUGQpSH9oo2jUDyazykMrkl4pVynKegYCS1DVRS+yzycfcB+adPS8ktzPUGpq84v9BXDVFKjL19BiCztWf6TStn/KrKtnJE0EaWjwm1U+qNUk+Y095qx5SEOFV6YTgcZOb7VY2ir2NvXUwJoyLwKUmH3howPADNvP423Zbs2qVpcObKBvtDOA/MZ/Y="));
        meta.setPlayerProfile(profile);

        result.setItemMeta(meta);
        return result;
    }

    public void kill(){
        getEntity().remove();
        alive = false;
        entity = null;
    }

    public boolean isAlive(){
        return alive;
    }

    public Zombie getEntity(){
        return entity;
    }
}
