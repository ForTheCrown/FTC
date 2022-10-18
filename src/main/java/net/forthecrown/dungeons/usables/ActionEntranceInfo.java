package net.forthecrown.dungeons.usables;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.RequiredArgsConstructor;
import net.forthecrown.text.Text;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.useables.*;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.TagUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.Tag;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class ActionEntranceInfo extends UsageAction {

    public static final EnumArgument<Type> TYPE_PARSER = EnumArgument.of(Type.class);

    // --- TYPE ---
    public static final UsageType<ActionEntranceInfo> TYPE = UsageType.of(ActionEntranceInfo.class)
            .setSuggests(TYPE_PARSER::listSuggestions);

    private final Type type;

    public ActionEntranceInfo(Type type) {
        super(TYPE);
        this.type = type;
    }

    @Override
    public void onUse(Player player, ActionHolder holder) {
        player.sendMessage(Text.renderString(type.message));
    }

    @Override
    public @Nullable Component displayInfo() {
        return Component.text(type.name().toLowerCase());
    }

    @Override
    public @Nullable Tag save() {
        return TagUtil.writeEnum(type);
    }

    // --- TYPE CONSTRUCTORS ---

    @UsableConstructor(ConstructType.PARSE)
    public static ActionEntranceInfo parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new ActionEntranceInfo(TYPE_PARSER.parse(reader));
    }

    @UsableConstructor(ConstructType.JSON)
    public static ActionEntranceInfo fromJson(JsonElement element) {
        return new ActionEntranceInfo(JsonUtils.readEnum(Type.class, element));
    }

    @UsableConstructor(ConstructType.TAG)
    public static ActionEntranceInfo load(Tag tag) {
        return new ActionEntranceInfo(TagUtil.readEnum(Type.class, tag));
    }

    @RequiredArgsConstructor
    public enum Type {
        BOSS_SPAWN ("If you have all the required items, using this, will spawn the boss."),
        DEATH ("Dying takes a random amount of Dungeon items. (mob drops)"),
        LEVEL_INFO ("Right-clicking this will show you a list of items needed to spawn the level's boss.");

        private final String message;
    }
}