package net.forthecrown.usables.conditions;

import com.mojang.serialization.DataResult;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.text.Messages;
import net.forthecrown.usables.BuiltType;
import net.forthecrown.usables.Condition;
import net.forthecrown.usables.Interaction;
import net.forthecrown.usables.UsableComponent;
import net.forthecrown.usables.ObjectType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

public class TestPermission implements Condition {

  static final ObjectType<TestPermission> TYPE = BuiltType.<TestPermission>builder()
      .saver((value, ops) -> DataResult.success(ops.createString(value.permission)))
      .loader(dynamic -> dynamic.asString().map(TestPermission::new))
      .parser((reader, source) -> {
        String remaining = reader.getRemaining();
        reader.setCursor(reader.getTotalLength());
        return new TestPermission(remaining);
      })
      .suggester((context, builder) -> {
        var pl = Bukkit.getPluginManager();
        return Completions.suggest(builder, pl.getPermissions().stream().map(Permission::getName));
      })
      .build();

  private final String permission;

  public TestPermission(String permission) {
    this.permission = permission;
  }

  @Override
  public boolean test(Interaction interaction) {
    return interaction.player().hasPermission(permission);
  }

  @Override
  public Component failMessage(Interaction interaction) {
    return Messages.NO_PERMISSION;
  }

  @Override
  public ObjectType<? extends UsableComponent> getType() {
    return TYPE;
  }

  @Override
  public @Nullable Component displayInfo() {
    return Component.text(permission);
  }
}
