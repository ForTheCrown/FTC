package net.forthecrown.useables.test;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.Messages;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.useables.CheckHolder;
import net.forthecrown.useables.ConstructType;
import net.forthecrown.useables.UsableConstructor;
import net.forthecrown.useables.UsageTest;
import net.forthecrown.useables.UsageType;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public class TestPermission extends UsageTest {

  public static final UsageType<TestPermission> TYPE = UsageType.of(TestPermission.class)
      .setSuggests((context, builder) -> {
        return Completions.suggest(
            builder,

            Bukkit.getPluginManager()
                .getPermissions()
                .stream()
                .map(Permission::getName)
        );
      });

  // --- INSTANCE FIELDS ---

  private final String permission;

  public TestPermission(String permission) {
    super(TYPE);
    this.permission = permission;
  }

  @Override
  public Component displayInfo() {
    return Text.format("'{0}'", permission);
  }

  @Override
  public BinaryTag save() {
    return BinaryTags.stringTag(permission);
  }

  @Override
  public boolean test(Player player, CheckHolder holder) {
    return player.hasPermission(permission);
  }

  @Override
  public Component getFailMessage(Player player, CheckHolder holder) {
    return Messages.NO_PERMISSION;
  }

  // --- TYPE CONSTRUCTORS ---

  @UsableConstructor
  public static TestPermission parse(StringReader reader, CommandSource source)
      throws CommandSyntaxException
  {
    return new TestPermission(reader.readString());
  }

  @UsableConstructor(ConstructType.TAG)
  public static TestPermission load(BinaryTag element) {
    return new TestPermission(element.toString());
  }
}