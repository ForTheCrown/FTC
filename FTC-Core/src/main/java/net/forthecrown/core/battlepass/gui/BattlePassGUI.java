package net.forthecrown.core.battlepass.gui;

import net.forthecrown.book.builder.BookBuilder;
import net.forthecrown.core.Crown;
import net.forthecrown.core.battlepass.BattlePass;
import net.forthecrown.core.battlepass.RewardInstance;
import net.forthecrown.user.CrownUser;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Set;

public interface BattlePassGUI {

    static Book create(CrownUser user) {
        BookBuilder builder = new BookBuilder();
        GuiContext context = contextCreate(user, false);

        addStartPage(builder, context);
        builder.newPage();

        addInfoPage(builder, context);
        builder.newPage();

        for (BattlePass.Category c: BattlePass.Category.values()) {
            addChallengePage(builder, context, c);
            builder.newPage();
        }

        for (int i = 1; i <= BattlePass.MAX_LEVEL.get(); i++) {
            addReward(builder, context, i);
        }

        return builder.buildBook();
    }

    private static GuiContext contextCreate(CrownUser user, boolean append) {
        BattlePass pass = Crown.getBattlePass();
        return new GuiContext(pass.getProgress(user.getUniqueId()), user, pass, append);
    }

    private static void addStartPage(BookBuilder builder, GuiContext context) {
        builder.addCentered(Component.text("BattlePass").color(NamedTextColor.YELLOW));
    }

    private static void addInfoPage(BookBuilder builder, GuiContext context) {

    }

    private static void addChallengePage(BookBuilder builder, GuiContext context, BattlePass.Category category) {

    }

    private static void addReward(BookBuilder builder, GuiContext context, int level) {
        Set<RewardInstance> instances = context.pass.forLevel(level);
        int countedLines = lineCount(builder, instances);

        if (!builder.canAddLines(countedLines)) {
            builder.newPage();
        }


    }

    private static int lineCount(BookBuilder builder, Set<RewardInstance> rewards) {
        return rewards
                .stream()
                .mapToInt(value -> builder.lineLength(value.reward().display(value)))
                .sum();
    }

    record GuiContext(BattlePass.Progress progress, CrownUser user, BattlePass pass, boolean appendPageChangers) {}
}
