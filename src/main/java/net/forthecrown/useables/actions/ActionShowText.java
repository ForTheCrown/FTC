package net.forthecrown.useables.actions;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.text.Text;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.*;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.TagUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.Tag;
import org.bukkit.entity.Player;

public class ActionShowText extends UsageAction {
    public static final UsageType<ActionShowText> TYPE = UsageType.of(ActionShowText.class)
            .setSuggests(Arguments.CHAT::listSuggestions);

    private final Component text;

    public ActionShowText(Component text) {
        super(TYPE);
        this.text = text;
    }

    @Override
    public void onUse(Player player, ActionHolder holder) {
        player.sendMessage(text);
    }

    @Override
    public Component displayInfo() {
        return Text.format("'{0}'", text);
    }

    @Override
    public Tag save() {
        return TagUtil.writeText(text);
    }

    @UsableConstructor(ConstructType.PARSE)
    public static ActionShowText parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new ActionShowText(Arguments.CHAT.parse(reader));
    }

    @UsableConstructor(ConstructType.JSON)
    public static ActionShowText readJson(JsonElement element) {
        return new ActionShowText(JsonUtils.readText(element));
    }

    @UsableConstructor(ConstructType.TAG)
    public static ActionShowText readTag(Tag tag) {
        return new ActionShowText(TagUtil.readText(tag));
    }
}