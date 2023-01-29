package net.forthecrown.useables.test;

import static net.minecraft.nbt.Tag.TAG_STRING;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.CheckHolder;
import net.forthecrown.useables.ConstructType;
import net.forthecrown.useables.UsableConstructor;
import net.forthecrown.useables.UsageTest;
import net.forthecrown.useables.UsageType;
import net.forthecrown.useables.actions.ActionScript;
import net.forthecrown.user.Users;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class TestScript extends UsageTest {

  public static final UsageType<TestScript> TYPE = UsageType.of(TestScript.class)
      .setSuggests(Arguments.SCRIPT::listSuggestions);

  private final String script;
  private final String[] args;

  public TestScript(String script, String... args) {
    super(TYPE);
    this.script = script;
    this.args = args;
  }

  @Override
  public @Nullable Component displayInfo() {
    return Component.text(
        String.format("'%s'", script)
    );
  }

  @Override
  public @Nullable Tag save() {
    CompoundTag tag = new CompoundTag();
    tag.putString("script", script);
    tag.put("args", TagUtil.writeArray(args, StringTag::valueOf));
    return tag;
  }

  @Override
  public boolean test(Player player, CheckHolder holder) {
    var script = ActionScript.getScript(this.script, holder, args);

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
    var script = ActionScript.getScript(this.script, holder, args);

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
    try (var script = ActionScript.getScript(this.script, holder, args)) {
      script.invokeIfExists("onTestsPassed", Users.get(player));
    }
  }

  @UsableConstructor(ConstructType.PARSE)
  public static TestScript parse(StringReader reader, CommandSource source)
      throws CommandSyntaxException
  {
    String script = Arguments.SCRIPT.parse(reader);
    String[] args = ArrayUtils.EMPTY_STRING_ARRAY;

    if (reader.canRead()) {
      String remaining = reader.getRemaining();
      reader.setCursor(reader.getTotalLength());
      args = remaining.split("\s");
    }

    return new TestScript(script, args);
  }

  @UsableConstructor(ConstructType.TAG)
  public static TestScript readTag(Tag tag) {
    if (tag.getId() == TAG_STRING) {
      return new TestScript(tag.getAsString());
    }

    CompoundTag compound = (CompoundTag) tag;
    String script = compound.getString("script");

    String[] args = compound.getList("args", TAG_STRING)
        .stream()
        .map(Tag::getAsString)
        .toArray(String[]::new);

    return new TestScript(script, args);
  }
}