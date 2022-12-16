package net.forthecrown.guilds.unlockables;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildMember;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.user.User;
import net.forthecrown.utils.ThrowingRunnable;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.context.ClickContext;
import net.forthecrown.utils.inventory.menu.context.InventoryContext;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

import static net.forthecrown.guilds.menu.GuildMenus.GUILD;

public interface Unlockable {
    int EXP_COST = 50;
    Style STYLE = Style.style(NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false);

    default void onUnlock(Guild guild, User user) {}

    GuildPermission getPerm();

    String getKey();

    Component getName();

    int getSlot();

    int getExpRequired();

    default int getExpProgress(Guild guild) {
        return guild.getUnlockables().getExpProgress(this);
    }

    default void setExpProgress(Guild guild, int value) {
        guild.getUnlockables().setExpProgress(this, value);
    }

    default boolean isUnlocked(Guild guild) {
        return getExpProgress(guild) == getExpRequired();
    }

    default int progress(Guild guild, int amount) {
        int newProgress = getExpProgress(guild) + amount;

        // Add progress until full
        setExpProgress(guild, Math.min(getExpRequired(), newProgress));

        // If over required, return left over
        int leftOver = newProgress - getExpRequired();
        return Math.max(leftOver, 0);
    }

    default Component getProgressComponent(Guild guild) {
        return getProgressComponent(getExpProgress(guild));
    }

    default Component getProgressComponent( int progress) {
        return Text.format("Progress: {0, number}/{1, number}",
                           STYLE,

                           progress,
                           getExpRequired()
        );
    }

    default Component getClickComponent() {
        return Text.format("Click to spend {0, number} Guild Exp.",
                STYLE,
                EXP_COST
        );
    }

    default Component getShiftClickComponent() {
        return Component.text("Shift-click to spend all your Guild Exp.", STYLE);
    }

    MenuNode toInvOption();

    default void onClick(User user,
                         ClickContext context,
                         InventoryContext c,
                         ThrowingRunnable<CommandSyntaxException> r
    ) throws CommandSyntaxException {
        Guild guild = c.get(GUILD);

        if (guild == null) {
            return;
        }

        GuildMember member = guild.getMember(user.getUniqueId());

        if (member == null) {
            return;
        }

        context.shouldReloadMenu(true);

        if (this.isUnlocked(guild)) {
            if (member.hasPermission(getPerm())) {
                r.run();
            } else {
                // Member does not have permission
                throw Exceptions.NO_PERMISSION;
            }

            return;
        }

        int spentAmount;

        // Spend all available when shift clicking, else spend EXP_COST
        spentAmount = member.spendExp(this, context.getClickType().isShiftClick()
                ? member.getExpAvailable() : EXP_COST);

        // Send user message
        if (spentAmount == 0) {
            user.sendMessage(Component.text("You don't have any Guild Exp.")
                    .color(NamedTextColor.RED)
                    .append(Component.newline())
                    .append(Component.text("Complete Guild Challenges to earn some.")
                            .color(NamedTextColor.GRAY)));
        } else {
            user.sendMessage(
                    Text.format("You've spent &e{0, number}&r Guild Exp on &e{1}&r.",
                            NamedTextColor.GOLD,
                            spentAmount,
                            getName()
                    )
            );
        }

        // Check if unlocked now
        if (this.isUnlocked(guild)) {
            user.getGuild().sendMessage(
                    Text.format("&6{0}&r has been unlocked by &6{1, user}&r!",
                            NamedTextColor.YELLOW,
                            getName(),
                            user.displayName()
                    )
            );

            onUnlock(guild, user);
        }
    }
}