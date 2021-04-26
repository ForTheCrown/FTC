package net.forthecrown.core.commands;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.BrigadierCommand;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CannotAffordTransactionException;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.forthecrown.core.commands.brigadier.types.custom.UserType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.v1_16_R3.ArgumentEntity;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.EnumChatFormat;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public class CommandPay extends CrownCommandBuilder {

    private final int maxMoneyAmount;

    public CommandPay(){
        super("pay", FtcCore.getInstance());

        maxMoneyAmount = FtcCore.getMaxMoneyAmount();

        setDescription("Pays another player money");
        setUsage("&7Usage: &r/pay <user> <amount>");
        register();
    }

    private final Balances bals = FtcCore.getBalances();

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Pays another player a set amount of money, removes the money from the player as well
     *
     *
     * Valid usages of command:
     * - /pay <player> <amount>
     *
     * Author: Botul
     */

    @Override
    protected void registerCommand(BrigadierCommand command) {
        command
                .then(argument("players", UserType.users())
                      .suggests(UserType::suggestSelector)

                      .then(argument("amount", IntegerArgumentType.integer(1, maxMoneyAmount))
                              .suggests((c, b) -> {
                                  if(!(c.getSource().getBukkitEntity() instanceof Player)) return Suggestions.empty();
                                  UUID id = getPlayerSender(c).getUniqueId();

                                  b.suggest(bals.get(id), new LiteralMessage("Your entire balance"));

                                  b.suggest(1);
                                  suggestIf(id, 10, b);
                                  suggestIf(id, 100, b);
                                  suggestIf(id, 1000, b);

                                  b.suggest(5);
                                  suggestIf(id, 50, b);
                                  suggestIf(id, 500, b);
                                  suggestIf(id, 5000, b);

                                  return b.buildFuture();
                              })

                            .executes(c -> {
                                CrownUser user = getUserSender(c);
                                int amount = c.getArgument("amount", Integer.class);
                                Collection<CrownUser> users = UserType.getUsers(c, "players");

                                return pay(user, users, amount);
                            })
                      )
                );
    }

    private void suggestIf(UUID id, int amount, SuggestionsBuilder builder){
        if(bals.get(id) > amount) builder.suggest(amount);
    }

    private int pay(CrownUser user, Collection<CrownUser> targets, int amount) throws CommandSyntaxException {
        if(amount > bals.get(user.getUniqueId())) throw new CannotAffordTransactionException();

        byte paidAmount = 0;

        for (CrownUser target: targets){
            if(user.equals(target)){
                if(targets.size() == 1) throw ArgumentEntity.e.create();
                continue;
            }

            if(bals.get(user.getUniqueId()) < amount){
                user.sendMessage(new ChatComponentText("You cannot afford that").a(EnumChatFormat.GRAY));
                break;
            }

            bals.add(target.getUniqueId(), amount, false);
            bals.add(user.getUniqueId(), -amount, false);

            user.sendMessage(
                    Component.text("You've paid ")
                            .color(NamedTextColor.GRAY)
                            .append(Balances.formatted(amount).color(NamedTextColor.GOLD))
                            .append(Component.text(" to "))
                            .append(
                                    Component.text(target.getName())
                                            .color(NamedTextColor.YELLOW)
                                            .hoverEvent(target)
                                            .clickEvent(target.asClickEvent())
                            )
            );

            target.sendMessage(
                    Component.text("You've received ")
                            .color(NamedTextColor.GRAY)
                            .append(Balances.formatted(amount).color(NamedTextColor.GOLD))
                            .append(Component.text(" from "))
                            .append(
                                    Component.text(user.getName())
                                            .color(NamedTextColor.YELLOW)
                                            .hoverEvent(user)
                                            .clickEvent(user.asClickEvent())
                            )
            );
            paidAmount++;
        }

        if(paidAmount == 0) throw new CrownCommandException("&7Found no players to pay");
        if(paidAmount > 1) user.sendMessage(
                Component.text("Paid " + paidAmount + " people. Lost ")
                        .color(NamedTextColor.GRAY)
                        .append(Balances.formatted(paidAmount * amount).color(NamedTextColor.YELLOW))
        );
        return 0;
    }
}
