package net.forthecrown.guilds.menu;

import static net.forthecrown.guilds.menu.GuildMenus.GUILD;

import net.forthecrown.guilds.GuildMessage;
import net.forthecrown.menu.MenuBuilder;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.menu.Menus;
import net.forthecrown.menu.page.MenuPage;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.paper.PaperNbt;
import net.forthecrown.user.User;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.sign.Side;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MessageCreationMenu extends MenuPage {

  public static final NamespacedKey MESSAGE_SIGN_KEY = new NamespacedKey("guilds", "message_sign");

  public MessageCreationMenu(MenuPage parent) {
    super(parent);
    initMenu(Menus.builder(Menus.MIN_INV_SIZE, "Pick a sign type"), true);
  }

  @Override
  protected void addBorder(MenuBuilder builder) {
  }

  @Override
  protected void createMenu(MenuBuilder builder) {
    int i = -1;
    for (var type : GuildMessage.SIGN_TYPES) {
      i++;

      builder.add(i,
          MenuNode.builder()
              .setItem((user, context) -> new ItemStack(type))

              .setRunnable((user, context, click) -> {
                var guild = context.getOrThrow(GUILD);
                Location loc = user.getLocation();
                loc.setY(loc.getWorld().getMinHeight());

                Block signBlock = loc.getBlock();
                BlockData data = signBlock.getBlockData().clone();

                BlockData newData = type.createBlockData();
                signBlock.setBlockData(newData, false);

                Sign sign = (Sign) signBlock.getState();
                var pdc = sign.getPersistentDataContainer();

                CompoundTag tag = BinaryTags.compoundTag();
                tag.putUUID("guild", guild.getId());
                tag.put("previous_state", PaperNbt.saveBlockData(data));

                pdc.set(
                    MESSAGE_SIGN_KEY,
                    PersistentDataType.TAG_CONTAINER,
                    PaperNbt.toDataContainer(tag, pdc.getAdapterContext())
                );
                sign.update();
                user.getPlayer().openSign(sign, Side.FRONT);
              })

              .build()
      );
    }
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user, @NotNull Context context) {
    return ItemStacks.builder(Material.WRITABLE_BOOK)
        .setName("&eWrite a new message")
        .addLore("&7Click to write a new message")
        .build();
  }
}