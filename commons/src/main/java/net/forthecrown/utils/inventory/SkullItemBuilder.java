package net.forthecrown.utils.inventory;

import com.destroystokyo.paper.profile.PlayerProfile;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

@Getter
public class SkullItemBuilder extends ItemBuilder<SkullItemBuilder> {

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
    if (!profile.hasTextures()) {
      CompletableFuture.runAsync(() -> {
        profile.complete(true);
      });
    }

    meta().setPlayerProfile(profile);
    return this;
  }

  public SkullItemBuilder setProfile(OfflinePlayer profile) {
    return setProfile(profile.getPlayerProfile());
  }

  @Override
  protected SkullItemBuilder getThis() {
    return this;
  }
}