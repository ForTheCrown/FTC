package net.forthecrown.utils.inventory;

import com.destroystokyo.paper.profile.PlayerProfile;
import lombok.Getter;
import net.forthecrown.user.User;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

@Getter
public class SkullItemBuilder extends BaseItemBuilder<SkullItemBuilder> {
    public SkullItemBuilder(int amount) {
        super(Material.PLAYER_HEAD, amount);
    }

    public SkullItemBuilder(ItemStack stack, ItemMeta baseMeta) {
        super(stack, baseMeta);
    }

    private SkullMeta meta() {
        return (SkullMeta) baseMeta;
    }

    public SkullItemBuilder setProfile(PlayerProfile profile) {
        meta().setPlayerProfile(profile);
        return this;
    }

    public SkullItemBuilder setProfile(User profile) {
        return setProfile(profile.getProfile());
    }

    public SkullItemBuilder setProfile(OfflinePlayer profile) {
        return setProfile((PlayerProfile) profile.getPlayerProfile());
    }

    @Override
    protected SkullItemBuilder getThis() {
        return this;
    }
}