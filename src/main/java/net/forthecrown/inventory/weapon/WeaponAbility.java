package net.forthecrown.inventory.weapon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.text.writer.TextWriter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spongepowered.math.GenericMath;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class WeaponAbility {
  public static final String
      TAG_LEVEL = "level",
      TAG_DURATION = "duration",
      TAG_EFFECT = "effectType";

  private final PotionEffectType effectType;
  private int level = 1;
  private int duration;

  public void write(TextWriter writer) {
    writer.line(Component.text("Effect: ", NamedTextColor.GRAY));

    Component effect = Component.translatable(effectType.translationKey());
    writer.write(effect);

    if (level > 1) {
      writer.formatted(" {0, number, -roman}", level);
    }
  }

  public void apply(Player player) {
    int time = getDuration();
    PotionEffect effect = new PotionEffect(
        effectType, time, level, false, true, true
    );

    player.addPotionEffect(effect);
  }

  public int getCooldownTicks() {
    int time = getDuration();
    return GenericMath.floor(time * GeneralConfig.swordAbilityCooldownScalar);
  }

  /* --------------------------- SERIALIZATION ---------------------------- */

  public Tag save() {
    CompoundTag tag = new CompoundTag();
    tag.putInt(TAG_LEVEL, level);
    tag.putInt(TAG_DURATION, duration);
    tag.put(TAG_EFFECT, TagUtil.writeKey(effectType.key()));

    return tag;
  }

  public static WeaponAbility load(Tag t) {
    if (!(t instanceof CompoundTag tag)) {
      return null;
    }

    NamespacedKey key = TagUtil.readKey(tag.get(TAG_EFFECT));
    PotionEffectType type = Registry.POTION_EFFECT_TYPE.get(key);

    if (type == null) {
      throw Util.newException(
          "Unknown potion effect type in Royal Sword: '%s'",
          key
      );
    }

    int level = tag.getInt(TAG_LEVEL);
    int duration = tag.getInt(TAG_DURATION);
    return new WeaponAbility(type, level, duration);
  }
}