package net.forthecrown.economy.market.commands;

import static net.forthecrown.economy.EconMessages.MARKET_MERGE_BLOCKED_SENDER;
import static net.forthecrown.economy.EconMessages.MARKET_MERGE_BLOCKED_TARGET;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.Duration;
import java.util.UUID;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.command.request.PlayerRequest;
import net.forthecrown.command.request.RequestTable;
import net.forthecrown.command.request.RequestValidator;
import net.forthecrown.economy.EconExceptions;
import net.forthecrown.economy.EconMessages;
import net.forthecrown.economy.EconPermissions;
import net.forthecrown.economy.ShopsPlugin;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.text.Messages;
import net.forthecrown.user.User;
import net.forthecrown.user.UserBlockList;
import net.forthecrown.utils.Audiences;
import net.kyori.adventure.audience.Audience;

public class CommandMergeShop extends FtcCommand {

  private final RequestTable<MergeRequest> requests;

  public CommandMergeShop() {
    super("mergeshop");

    requests = new RequestTable<>();
    requests.setValidator(new MergeValidator());

    setPermission(EconPermissions.MARKETS);
    setAliases("mergemarket", "shopmerge", "marketmerge");
    setDescription("Request to merge your shop with someone else's");

    register();
  }

  /*
   * ----------------------------------------
   * 			Command description:
   * ----------------------------------------
   *
   * Valid usages of command:
   * /mergeshop <user>
   * /mergeshop <user> <cancel | confirm | deny>
   *
   * Permissions used:
   * ftc.markets
   *
   * Main Author: Julie
   */

  @Override
  public void populateUsages(UsageFactory factory) {
    factory = factory.withPrefix("<user>");

    factory.usage("").addInfo("Requests to merge your shop with a <user>'s shop");
    factory.usage("confirm").addInfo("Confirms a merge request that came from a <user>");
    factory.usage("deny").addInfo("Denies a merge request that came from a <user>");
    factory.usage("cancel").addInfo("Cancels a merge request sent to a <user>");
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("user", Arguments.ONLINE_USER)

            // mergeshop <user>
            .executes(c -> {
              User user = getUserSender(c);
              User target = Arguments.getUser(c, "user");

              MergeRequest request = new MergeRequest(user.getUniqueId(), target.getUniqueId());
              requests.sendRequest(request);

              return 0;
            })

            // mergeshop <user> confirm
            .then(literal("confirm")
                .executes(c -> {
                  User user = getUserSender(c);
                  User target = Arguments.getUser(c, "user");

                  var request = requests.getIncoming(user, target);
                  if (request == null) {
                    throw Exceptions.noIncoming(target);
                  }

                  request.accept();
                  return 0;
                })
            )

            // mergeshop <user> deny
            .then(literal("deny")
                .executes(c -> {
                  User user = getUserSender(c);
                  User target = Arguments.getUser(c, "user");

                  var incoming = requests.getIncoming(user, target);

                  if (incoming == null) {
                    throw Exceptions.noIncoming(target);
                  }

                  incoming.deny();
                  return 0;
                })
            )

            // mergeshop <user> cancel
            .then(literal("cancel")
                .executes(c -> {
                  User user = getUserSender(c);
                  User target = Arguments.getUser(c, "user");

                  var request = requests.getOutgoing(user, target);
                  if (request == null) {
                    throw Exceptions.noOutgoing(target);
                  }

                  request.cancel();
                  return 0;
                })
            )
        );
  }

  private static class MergeValidator implements RequestValidator<MergeRequest> {

    @Override
    public void validate(MergeRequest request, Audience viewer) throws CommandSyntaxException {
      if (request.getTargetId().equals(request.getSenderId())) {
        throw EconExceptions.MERGE_SELF;
      }

      boolean viewerIsTarget = Audiences.equals(viewer, request.getTarget());

      User viewUser = viewerIsTarget ? request.getTarget() : request.getSender();
      User target   = viewerIsTarget ? request.getSender() : request.getTarget();

      if (Markets.ownsShop(viewUser)) {
        throw EconExceptions.NO_SHOP_OWNED;
      }

      if (Markets.ownsShop(target)) {
        throw EconExceptions.noShopOwned(target);
      }

      var markets = ShopsPlugin.getPlugin().getMarkets();
      var targetShop = markets.get(target.getUniqueId());
      var viewerShop = markets.get(viewUser.getUniqueId());

      if (viewerShop.isConnected(targetShop)) {
        throw EconExceptions.marketNotConnected(target);
      }

      if (viewerShop.isMerged()) {
        throw EconExceptions.ALREADY_MERGED;
      }

      if (targetShop.isMerged()) {
        throw EconExceptions.marketTargetMerged(target);
      }

      UserBlockList.testBlockedException(
          viewUser,
          target,
          MARKET_MERGE_BLOCKED_SENDER,
          MARKET_MERGE_BLOCKED_TARGET
      );

    }
  }

  private static class MergeRequest extends PlayerRequest {

    public MergeRequest(UUID senderId, UUID targetId) {
      super(senderId, targetId);
    }

    @Override
    protected Duration getExpiryDuration() {
      return Duration.ofMinutes(5);
    }

    @Override
    public void onBegin() {
      var target = getTarget();
      var sender = getSender();

      target.sendMessage(EconMessages.marketMergeTarget(sender));

      sender.sendMessage(
          Messages.requestSent(
              target,
              Messages.crossButton("/mergeshop %s cancel", target.getName())
          )
      );
    }

    @Override
    public void deny() {
      var target = getTarget();
      var sender = getSender();

      target.sendMessage(Messages.REQUEST_DENIED);
      sender.sendMessage(Messages.requestDenied(target));

      stop();
    }

    @Override
    public void cancel() {
      var sender = getSender();
      var target = getTarget();

      target.sendMessage(Messages.REQUEST_CANCELLED);
      sender.sendMessage(Messages.requestCancelled(target));

      stop();
    }

    @Override
    public void accept() throws CommandSyntaxException {
      super.accept();

      var sender = getSender();
      var target = getTarget();

      var markets = ShopsPlugin.getPlugin().getMarkets();
      var senderShop = markets.get(getSenderId());
      var targetShop = markets.get(getTargetId());

      targetShop.merge(senderShop);

      sender.sendMessage(EconMessages.marketMerged(target));
      target.sendMessage(EconMessages.marketMerged(sender));
    }
  }
}