package net.forthecrown.useables.actions;

import static net.minecraft.nbt.Tag.TAG_STRING;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.core.script2.Script;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.ActionHolder;
import net.forthecrown.useables.ConstructType;
import net.forthecrown.useables.UsableBlock;
import net.forthecrown.useables.UsableConstructor;
import net.forthecrown.useables.UsableEntity;
import net.forthecrown.useables.UsageAction;
import net.forthecrown.useables.UsageType;
import net.forthecrown.useables.UsageTypeHolder;
import net.forthecrown.user.Users;
import net.forthecrown.utils.io.TagUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class ActionScript extends UsageAction {

  public static final UsageType<ActionScript> TYPE = UsageType.of(ActionScript.class)
      .setSuggests(Arguments.SCRIPT::listSuggestions);

  private final String script;
  private final String[] args;

  public ActionScript(String script, String... args) {
    super(TYPE);
    this.script = script;
    this.args = args;
  }

  @Override
  public void onUse(Player player, ActionHolder holder) {
    try (var _script = getScript(script, holder, args)) {
      _script.invoke("onUse", Users.get(player));
    }
  }

  public static Script getScript(String name,
                                 UsageTypeHolder holder,
                                 String... args
  ) {
    Script _script = Script.of(name).compile(args);

    _script.put("_holder", holder);

    holder.as(UsableBlock.class).ifPresent(block -> {
      _script.put("_location", block.getBlock().getLocation());
      _script.put("_block", block.getBlock());
    });

    holder.as(UsableEntity.class).ifPresent(entity -> {
      var ent = entity.getEntity();
      _script.put("_entity", ent);
      _script.put("_location", ent.getLocation());
    });

    _script.eval().throwIfError();
    return _script;
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

  @UsableConstructor(ConstructType.PARSE)
  public static ActionScript parse(StringReader reader, CommandSource source)
      throws CommandSyntaxException {
    String script = Arguments.SCRIPT.parse(reader);
    String[] args = ArrayUtils.EMPTY_STRING_ARRAY;

    reader.skipWhitespace();

    if (reader.canRead()) {
      String remaining = reader.getRemaining().trim();
      reader.setCursor(reader.getTotalLength());
      args = remaining.split("\s");
    }

    return new ActionScript(script, args);
  }

  @UsableConstructor(ConstructType.TAG)
  public static ActionScript readTag(Tag tag) {
    if (tag.getId() == TAG_STRING) {
      return new ActionScript(tag.getAsString());
    }

    CompoundTag compound = (CompoundTag) tag;
    String script = compound.getString("script");

    String[] args = compound.getList("args", TAG_STRING)
        .stream()
        .map(Tag::getAsString)
        .toArray(String[]::new);

    return new ActionScript(script, args);
  }
}