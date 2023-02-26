package net.forthecrown.useables.test;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.useables.CheckHolder;
import net.forthecrown.useables.ConstructType;
import net.forthecrown.useables.UsableConstructor;
import net.forthecrown.useables.UsableScriptHolder;
import net.forthecrown.useables.UsageTest;
import net.forthecrown.useables.UsageType;
import net.forthecrown.useables.util.UsablesScripts;
import net.forthecrown.user.Users;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class TestScript extends UsageTest implements UsableScriptHolder {

  public static final UsageType<TestScript> TYPE = UsageType.of(TestScript.class)
      .setSuggests(Arguments.SCRIPT::listSuggestions);

  @Getter
  private final String scriptName;

  @Getter
  private final String[] args;

  @Getter @Setter
  private String dataString;

  public TestScript(String script, String... args) {
    super(TYPE);
    this.scriptName = script;
    this.args = args;
  }

  @Override
  public @Nullable Component displayInfo() {
    return Component.text(
        String.format("'%s'", scriptName)
    );
  }

  @Override
  public @Nullable BinaryTag save() {
    return UsablesScripts.saveScript(this);
  }

  @Override
  public boolean test(Player player, CheckHolder holder) {
    var script = compileScript(holder);

    if (!script.hasMethod("test")) {
      script.close();
      return true;
    }

    var result = script.invoke("test", Users.get(player))
        .asBoolean()
        .orElse(false);

    script.close();
    return result;
  }

  @Override
  public @Nullable Component getFailMessage(Player player, CheckHolder holder) {
    var script = compileScript(holder);

    if (!script.hasMethod("getFailMessage")) {
      script.close();
      return null;
    }

    var result = script.invoke("getFailMessage", Users.get(player))
        .result();

    script.close();

    if (result.isEmpty()) {
      return null;
    }

    var obj = result.get();
    return Text.valueOf(obj);
  }

  @Override
  public void postTests(Player player, CheckHolder holder) {
    try (var script = compileScript(holder)) {
      script.invokeIfExists("onTestsPassed", Users.get(player));
    }
  }

  @UsableConstructor(ConstructType.PARSE)
  public static TestScript parse(StringReader reader, CommandSource source)
      throws CommandSyntaxException
  {
    return UsablesScripts.parseScript(reader, TestScript::new);
  }

  @UsableConstructor(ConstructType.TAG)
  public static TestScript readTag(BinaryTag tag) {
    return UsablesScripts.loadScript(tag, TestScript::new);
  }
}