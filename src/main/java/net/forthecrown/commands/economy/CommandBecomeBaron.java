package net.forthecrown.commands.economy;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.economy.TransactionType;
import net.forthecrown.economy.Transactions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.core.Messages;
import net.forthecrown.user.User;
import net.forthecrown.user.data.RankTitle;
import org.bukkit.Sound;

public class CommandBecomeBaron extends FtcCommand {
    public CommandBecomeBaron() {
        super("becomebaron");

        setPermission(Permissions.BECOME_BARON);
        setDescription("Allows you to become a baron");

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Explain what command is supposed to be used for..
     *
     *
     * Valid usages of command:
     * - /becomebaron
     * - /becomebaron confirm
     *
     * Permissions used:
     * - ftc.commands.becomebaron
     *
     * Author: Julie
     */

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                // /becomebaron
                .executes(c -> {
                    User p = getUserSender(c);

                    // Validate user can purchase baron
                    validate(p);

                    //send message
                    p.sendMessage(
                            Messages.becomeBaronConfirm(
                                    "/" + getName() + " confirm"
                            )
                    );
                    return 0;
                })

                // /becomebaron confirm
                .then(literal("confirm")
                        .executes(c -> {
                            User user = getUserSender(c);

                            // Validate user can purchase baron
                            validate(user);

                            user.removeBalance(GeneralConfig.baronPrice);
                            user.getTitles().addTitle(RankTitle.BARON);

                            Transactions.builder()
                                    .type(TransactionType.BECOME_BARON)
                                    .sender(user.getUniqueId())
                                    .amount(GeneralConfig.baronPrice)
                                    .log();

                            user.sendMessage(Messages.becomeBaron());
                            user.playSound(Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1)
                            ;
                            return 0;
                        })
                );
    }

    private static void validate(User user) throws CommandSyntaxException {
        var baronPrice = GeneralConfig.baronPrice;

        if(user.getTitles().hasTitle(RankTitle.BARON)) {
            throw Exceptions.ALREADY_BARON;
        }

        if (!user.hasBalance(baronPrice)) {
            throw Exceptions.cannotAfford(baronPrice);
        }
    }
}