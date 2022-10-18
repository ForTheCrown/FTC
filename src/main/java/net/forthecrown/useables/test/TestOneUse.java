package net.forthecrown.useables.test;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.forthecrown.text.Text;
import net.forthecrown.useables.CheckHolder;
import net.forthecrown.useables.ConstructType;
import net.forthecrown.useables.UsableConstructor;
import net.forthecrown.useables.UsageTest;
import net.forthecrown.useables.UsageType;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.TagUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.Tag;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public class TestOneUse extends UsageTest {
    // --- TYPE ---
    public static final UsageType<TestOneUse> TYPE = UsageType.of(TestOneUse.class);

    private final Set<UUID> used = new ObjectOpenHashSet<>();

    public TestOneUse() {
        super(TYPE);
    }

    @Override
    public @Nullable Component displayInfo() {
        return Text.format("used_count={0, number}", used.size());
    }

    @Override
    public @Nullable Tag save() {
        return TagUtil.writeCollection(used, TagUtil::writeUUID);
    }

    @Override
    public boolean test(Player player, CheckHolder holder) {
        return !used.contains(player.getUniqueId());
    }

    @Override
    public @Nullable Component getFailMessage(Player player, CheckHolder holder) {
        return Component.text("You can only use this once!", NamedTextColor.GRAY);
    }

    @Override
    public void postTests(Player player, CheckHolder holder) {
        used.add(player.getUniqueId());
    }

    // --- TYPE CONSTRUCTORS ---

    @UsableConstructor(ConstructType.EMPTY)
    public static TestOneUse create() {
        return new TestOneUse();
    }

    @UsableConstructor(ConstructType.JSON)
    public static TestOneUse fromJson(JsonElement element) {
        var result = create();

        for (var e: element.getAsJsonArray()) {
            result.used.add(
                    JsonUtils.readUUID(e)
            );
        }

        return result;
    }

    @UsableConstructor(ConstructType.TAG)
    public static TestOneUse load(Tag tag) {
        var result = create();
        result.used.addAll(TagUtil.readCollection(tag, TagUtil::readUUID));

        return result;
    }
}