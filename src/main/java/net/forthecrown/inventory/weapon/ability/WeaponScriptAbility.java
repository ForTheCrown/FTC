package net.forthecrown.inventory.weapon.ability;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.script2.Script;
import net.forthecrown.core.script2.ScriptResult;
import net.forthecrown.core.script2.ScriptSource;
import net.forthecrown.inventory.weapon.RoyalSword;
import net.forthecrown.inventory.weapon.ability.WeaponAbilityType.Factory;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import org.apache.logging.log4j.Logger;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

public class WeaponScriptAbility extends WeaponAbility {

  private static final Logger LOGGER = Loggers.getLogger();

  private Script script;
  private final long baseCooldown;

  public WeaponScriptAbility(WeaponAbilityType type,
                             Script script,
                             long baseCooldown
  ) {
    super(type);
    this.script = script;
    this.baseCooldown = baseCooldown;

    assert script.isCompiled() : "Script not compiled when given to ability";

    setLevel(START_LEVEL);
  }

  @Override
  public Component displayName() {
    return script.invokeIfExists("displayName")
        .flatMap(ScriptResult::result)
        .map(Text::valueOf)
        .orElse(getType().fullDisplayName());
  }

  @Override
  public long getCooldownTicks() {
    return script.invokeIfExists("getCooldown")
        .flatMap(ScriptResult::result)
        .flatMap(o -> {
          if (o instanceof String str) {
            return Optional.of(
                SwordAbilityManager.getInstance()
                    .parseTicks(str)
            );
          }

          if (o instanceof Number number) {
            return Optional.of(number.longValue());
          }

          return Optional.empty();
        })

        .orElseGet(() -> scaledCooldown(baseCooldown));
  }

  @Override
  public boolean onRightClick(Player player,
                              @Nullable Entity clicked,
                              @Nullable Block block
  ) {
    return invokeClickCallback("onRightClick", player, clicked, block);
  }

  @Override
  public boolean onLeftClick(Player player,
                             @Nullable Entity clicked,
                             @Nullable Block block
  ) {
    return invokeClickCallback("onLeftClick", player, clicked, block);
  }

  private boolean invokeClickCallback(String method, Player player,
                                      @Nullable Entity entity,
                                      @Nullable Block block
  ) {
    Object clickedInput = block == null ? entity : block;

    // Default to true, as that will cause a weapon cooldown, which should
    // happen, if the method wasn't declared, or failed, or didn't return
    // a result
    return script.invokeIfExists(method, player, clickedInput)
        .flatMap(ScriptResult::asBoolean)
        .orElse(true);
  }

  @Override
  public void setLevel(int level) {
    super.setLevel(level);
    script.put("level", level);
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    script.invokeIfExists("onSave", tag);
  }

  @Override
  protected void loadAdditional(CompoundTag tag) {
    script.invokeIfExists("onLoad", tag);
  }

  @Override
  public void onUpdate(ItemStack item, ItemMeta meta, RoyalSword royalSword) {
    //script.close();
  }

  @RequiredArgsConstructor
  public static class ScriptAbilityFactory implements Factory {
    private final long baseCooldown;
    private final ScriptSource source;
    private final String[] inputArgs;

    @Override
    public WeaponAbility newAbility(WeaponAbilityType type) {
      Script script = Script.of(source);
      script.compile(inputArgs);
      script.put("baseCooldown", baseCooldown);

      script.eval().throwIfError();

      return new WeaponScriptAbility(type, script, baseCooldown);
    }
  }
}