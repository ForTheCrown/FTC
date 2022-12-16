package net.forthecrown.useables.test;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.Pair;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.*;
import net.forthecrown.useables.util.UsageUtil;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.UUID2IntMap;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.format.UnitFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.nbt.Tag;
import org.bukkit.entity.Player;

public class TestUserMapValue extends UsageTest {
    public static final UsageType<TestUserMapValue>
        TYPE_BALANCE = UsageType.of(TestUserMapValue.class),
        TYPE_GEMS    = UsageType.of(TestUserMapValue.class),
        TYPE_VOTES   = UsageType.of(TestUserMapValue.class);

    private final UUID2IntMap map;
    private final MinMaxBounds.Ints bounds;
    private final String unit;

    public TestUserMapValue(UsageType type, MinMaxBounds.Ints bounds) {
        super(type);
        var pair = findMap(type);

        this.unit = pair.second();
        this.map = pair.first();

        this.bounds = bounds;
    }

    private static Pair<UUID2IntMap, String> findMap(UsageType type) {
        var users = UserManager.get();

        if (type == TYPE_BALANCE) {
            return Pair.of(users.getBalances(), UnitFormat.UNIT_RHINE);
        }

        if (type == TYPE_GEMS) {
            return Pair.of(users.getGems(), UnitFormat.UNIT_GEM);
        }

        return Pair.of(users.getVotes(), UnitFormat.UNIT_VOTE);
    }

    @Override
    public Component displayInfo() {
        return Component.text(UsageUtil.boundsDisplay(bounds));
    }

    @Override
    public Tag save() {
        return UsageUtil.writeBounds(bounds);
    }

    @Override
    public boolean test(Player player, CheckHolder holder) {
        return bounds.matches(map.get(player.getUniqueId()));
    }

    @Override
    public Component getFailMessage(Player player, CheckHolder holder) {
        return Text.format("You don't the &e{0}&r. required",
                NamedTextColor.GRAY,
                UsageUtil.boundsDisplay(bounds) + " " + unit + "s"
        );
    }

    // --- TYPE CONSTRUCTORS ---

    @UsableConstructor
    public static TestUserMapValue parse(UsageType type, StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new TestUserMapValue(type, MinMaxBounds.Ints.fromReader(reader));
    }

    @UsableConstructor(ConstructType.TAG)
    public static TestUserMapValue load(UsageType type, Tag tag) {
        return new TestUserMapValue(type, UsageUtil.readBounds(tag));
    }
}