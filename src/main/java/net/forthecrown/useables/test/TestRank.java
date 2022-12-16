package net.forthecrown.useables.test;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Commands;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.*;
import net.forthecrown.user.Users;
import net.forthecrown.user.data.RankTitle;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.Tag;
import org.bukkit.entity.Player;

public class TestRank extends UsageTest {
    public static final UsageType<TestRank> TYPE = UsageType.of(TestRank.class);

    private final RankTitle title;
    public TestRank(RankTitle title) {
        super(TYPE);
        this.title = title;
    }

    @Override
    public Component displayInfo() {
        return title.asComponent();
    }

    @Override
    public Tag save() {
        return TagUtil.writeEnum(title);
    }

    @Override
    public boolean test(Player player, CheckHolder holder) {
        return Users.get(player).getTitles().hasTitle(title);
    }

    @Override
    public Component getFailMessage(Player player, CheckHolder holder) {
        return Text.format("You need the &f{0}&r rank", NamedTextColor.GRAY, title);
    }

    // --- TYPE CONSTRUCTORS ---

    @UsableConstructor
    public static TestRank parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new TestRank(Commands.RANK.parse(reader));
    }

    @UsableConstructor(ConstructType.TAG)
    public static TestRank load(Tag tag) {
        return new TestRank(TagUtil.readEnum(RankTitle.class, tag));
    }
}