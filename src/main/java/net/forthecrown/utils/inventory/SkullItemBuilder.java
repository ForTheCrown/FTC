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
    private PlayerProfile profile;

    public SkullItemBuilder(int amount) {
        super(Material.PLAYER_HEAD, amount);
    }

    public SkullItemBuilder setProfile(PlayerProfile profile) {
        this.profile = profile;
        return this;
    }

    public SkullItemBuilder setProfile(User profile) {
        this.profile = profile.getProfile();
        return this;
    }

    public SkullItemBuilder setProfile(OfflinePlayer profile) {
        this.profile = (PlayerProfile) profile.getPlayerProfile();
        return this;
    }

    @Override
    protected SkullItemBuilder getThis() {
        return this;
    }

    @Override
    protected void onBuild(ItemStack item, ItemMeta meta) {
        var skullMeta = (SkullMeta) meta;
        skullMeta.setPlayerProfile(profile);
    }
}