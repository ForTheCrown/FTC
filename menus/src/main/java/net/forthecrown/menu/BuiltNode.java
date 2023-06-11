package net.forthecrown.menu;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.user.User;
import net.forthecrown.utils.context.Context;
import net.kyori.adventure.sound.Sound;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public class BuiltNode implements MenuNode {

  private static final Sound CLICK_SOUND = Sound.sound()
      .type(org.bukkit.Sound.UI_BUTTON_CLICK)
      .pitch(0.4f)
      .volume(1F)
      .build();

  private final MenuNodeItem item;
  private final MenuClickConsumer runnable;
  private final boolean playSound;

  @Override
  public void onClick(User user, Context context, ClickContext click)
      throws CommandSyntaxException
  {
    if (runnable == null) {
      return;
    }

    if (playSound) {
      user.playSound(CLICK_SOUND);
    }

    runnable.onClick(user, context, click);
  }

  @Override
  public ItemStack createItem(@NotNull User user, @NotNull Context context) {
    if (item == null) {
      return null;
    }

    return this.item.createItem(user, context);
  }
}