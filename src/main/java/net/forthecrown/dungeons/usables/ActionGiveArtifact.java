package net.forthecrown.dungeons.usables;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.text.Text;
import net.forthecrown.dungeons.boss.DrawnedBoss;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.useables.*;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.Tag;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class ActionGiveArtifact extends UsageAction {
    private static final EnumArgument<DrawnedBoss.Artifacts> ARTIFACT_PARSER = EnumArgument.of(DrawnedBoss.Artifacts.class);

    // --- TYPE ---
    public static final UsageType<ActionGiveArtifact> TYPE = UsageType.of(ActionGiveArtifact.class)
            .setSuggests(ARTIFACT_PARSER::listSuggestions);

    private final DrawnedBoss.Artifacts artifacts;

    public ActionGiveArtifact(DrawnedBoss.Artifacts artifacts) {
        super(TYPE);
        this.artifacts = artifacts;
    }

    @Override
    public void onUse(Player player, ActionHolder holder) {
        var item = artifacts.item();
        Util.giveOrDropItem(player.getInventory(), player.getLocation(), item);

        player.sendMessage(
                Text.format("You got the &e{0, item}&r.",
                        NamedTextColor.GRAY,
                        item
                )
        );
    }

    @Override
    public @Nullable Component displayInfo() {
        return Text.format("artifact='{0}'", artifacts.name().toLowerCase());
    }

    @Override
    public @Nullable Tag save() {
        return TagUtil.writeEnum(artifacts);
    }

    // --- TYPE CONSTRUCTORS ---

    @UsableConstructor(ConstructType.PARSE)
    public static ActionGiveArtifact parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new ActionGiveArtifact(ARTIFACT_PARSER.parse(reader));
    }

    @UsableConstructor(ConstructType.JSON)
    public static ActionGiveArtifact fromJson(JsonElement element) {
        return new ActionGiveArtifact(JsonUtils.readEnum(DrawnedBoss.Artifacts.class, element));
    }

    @UsableConstructor(ConstructType.TAG)
    public static ActionGiveArtifact load(Tag tag) {
        return new ActionGiveArtifact(TagUtil.readEnum(DrawnedBoss.Artifacts.class, tag));
    }
}