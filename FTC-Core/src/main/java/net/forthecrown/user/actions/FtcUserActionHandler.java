package net.forthecrown.user.actions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.EavesDropper;
import net.forthecrown.core.admin.MuteStatus;
import net.forthecrown.core.chat.BannedWords;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.regions.visit.RegionVisit;
import net.forthecrown.regions.visit.VisitPredicate;
import net.forthecrown.regions.visit.handlers.OwnedEntityHandler;
import net.forthecrown.regions.visit.handlers.RidingVehicleHandler;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserInteractions;
import net.forthecrown.user.UserMail;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class FtcUserActionHandler implements UserActionHandler {
    @Override
    public void handleDirectMessage(DirectMessage message) {
        //Validate they didn't just use a slur or something lol
        if(BannedWords.checkAndWarn(message.getSender().asBukkit(), message.getFormattedText())) {
            message.setMuteStatus(MuteStatus.HARD);
        }

        EavesDropper.reportDM(message);

        UUID senderID = null;
        UUID receiverID = null;

        //If no mute what so ever
        if(message.getMuteStatus().maySpeak) {
            if(message.getTarget().isPlayer()){
                CrownUser user = UserManager.getUser(message.getTarget().asOrNull(Player.class));
                user.setLastMessage(message.getSender());

                receiverID = user.getUniqueId();

                if(message.getSender().isPlayer()){
                    CrownUser senderUser = UserManager.getUser(message.getSender().asOrNull(Player.class));
                    senderUser.setLastMessage(message.getTarget());

                    senderID = senderUser.getUniqueId();

                    //Blocked lol
                    if(user.getInteractions().isBlockedPlayer(senderUser.getUniqueId())){
                        message.getSender().sendMessage(Component.translatable("user.message.cannot", NamedTextColor.GRAY, user.nickDisplayName().color(NamedTextColor.YELLOW)));
                        return;
                    }
                }

                //If AFK, warn sender target might not see
                if(user.isAfk()) message.getSender().sendMessage(Component.translatable("user.message.afk").color(NamedTextColor.YELLOW));
            }

            Component receiverMessage = Component.text()
                    .append(message.getReceiverHeader())
                    .append(Component.text(" "))
                    .append(message.getFormattedText())
                    .build();
            message.getTarget().sendMessage(receiverMessage, senderID);
        }

        //If soft or no mute, send to sender
        if(message.getMuteStatus().senderMaySee){
            Component senderMessage = Component.text()
                    .append(message.getSenderHeader())
                    .append(Component.text(" "))
                    .append(message.getFormattedText())
                    .build();

            message.getSender().sendMessage(senderMessage, receiverID);
        }

        //Remove from command source tracker
        message.getTarget().onCommandComplete(null, message.getMuteStatus().maySpeak, 0);
    }

    @Override
    public void handleMarriageMessage(MarriageMessage message) {
        Component formatted = MarriageMessage.format(message.getSender().nickDisplayName(), message.getFormatted());

        //Validate they didn't just use a slur or something lol
        if(BannedWords.checkAndWarn(message.getSender(), formatted)) {
            message.setStatus(MuteStatus.HARD);
        }

        EavesDropper.reportMarriageDM(message);

        MuteStatus status = message.getMuteStatus();

        if(message.getSender().getInteractions().isBlockedPlayer(message.getTarget().getUniqueId())){

            //lmao, blocked by your spouse
            if(message.getSender().getInteractions().isOnlyBlocked(message.getTarget().getUniqueId())) {
                message.getSender().sendMessage(
                        Component.translatable("marriage.ignored", NamedTextColor.GOLD)
                );
            }

            //Ignored or seperated, don't send
            return;
        }

        //If the sender may send, aka if soft or no mute, send
        //If not muted at all, send to target
        if(status.senderMaySee) message.getSender().sendBlockableMessage(message.getTarget().getUniqueId(), formatted);
        if(status.maySpeak) message.getTarget().sendBlockableMessage(message.getSender().getUniqueId(), formatted);
    }

    @Override
    public void handleRegionInvite(InviteToRegion invite) {
        CrownUser sender = invite.sender();
        CrownUser target = invite.target();

        //Add the invites
        sender.getInteractions().addInvite(invite.target().getUniqueId());
        target.getInteractions().addInvitedTo(invite.sender().getUniqueId());

        //Tell sender they invited
        sender.sendMessage(
                Component.translatable("regions.invite.sender",
                        NamedTextColor.GOLD,
                        target.nickDisplayName()
                                .color(NamedTextColor.YELLOW))
        );

        //Tell target they were invited
        target.sendMessage(
                Component.translatable("regions.invite.target",
                        NamedTextColor.GOLD,
                        sender.nickDisplayName()
                                .color(NamedTextColor.YELLOW)
                )
        );
    }

    @Override
    public void handleTeleport(TeleportRequest request) {
        //Add the request
        request.getSender().getInteractions().addOutgoing(request);
        request.getTarget().getInteractions().addIncoming(request);

        //Start the countdown
        request.startCountdown();
    }

    @Override
    public void handleMailQuery(MailQuery query) throws CommandSyntaxException {
        if(!query.getSource().hasPermission(Permissions.MAIL_OTHERS) && !query.getMail().canSee(query.getSource().asBukkit())) {
            throw FtcExceptionProvider.translatable("mail.private", query.getUser().nickDisplayName());
        }

        boolean self = query.isSelfQuery();

        List<UserMail.MailMessage> messages = query.getMail().getMail();

        if(messages.isEmpty()) {
            throw FtcExceptionProvider.translatable("mail.none." + (self ? "self" : "other"));
        }

        TextComponent.Builder builder = Component.text()
                .color(NamedTextColor.YELLOW)
                .append(
                        Component.translatable(
                                "mail.header." + (self ? "self" : "other"),
                                query.getUser().nickDisplayName()
                                        .color(NamedTextColor.GOLD)
                        )
                );

        int index = 0;
        for (UserMail.MailMessage m: messages) {
            index++;

            Component senderMetadata = m.sender == null ? Component.empty() :
                    Component.newline()
                            .append(Component.translatable("mail.metadata.sender", UserManager.getUser(m.sender).nickDisplayName()));

            builder.append(
                    Component.text()
                            .color(NamedTextColor.WHITE)
                            .append(
                                    Component.text("\n" + index + ")")
                                            .color(NamedTextColor.GOLD)
                                            .hoverEvent(
                                                    Component.translatable(
                                                            "mail.metadata.date",
                                                            Component.text(new Date(m.sent).toString())
                                                    )
                                                            .append(senderMetadata)
                                            )
                            )

                            .append(
                                    Component.text()
                                            .color(m.read ? NamedTextColor.GRAY : NamedTextColor.YELLOW)
                                            .content(" [")
                                            .append(
                                                    Component.translatable(m.read ? "mail.read" : "mail.unread")
                                                            .clickEvent(ClickEvent.runCommand("/mail mark_read " + query.getMail().indexOf(m)))
                                                            .hoverEvent(Component.translatable(m.read ? "mail.read.hover" : "mail.unread.hover"))
                                            )
                                            .append(Component.text("] "))
                            )

                            .append(m.message)
            );
        }

        query.getSource().sendMessage(
                builder.build()
        );
    }

    @Override
    public void handleMailAdd(MailAddAction action) throws CommandSyntaxException {
        boolean online = action.getUser().isOnline();
        CrownUser user = action.getUser();
        UserMail mail = action.getMail();
        Component text = action.getText();
        Component senderDisplay = null;

        if(action.hasSender() && action.shouldValidateSender()) {
            UUID sender = action.getSender();
            CrownUser senderUser = UserManager.getUser(sender);
            senderDisplay = senderUser.nickDisplayName().color(NamedTextColor.YELLOW);

            if(user.getInteractions().isBlockedPlayer(sender)) {
                throw FtcExceptionProvider.blockedPlayer(user);
            }

            MuteStatus status = senderUser.getInteractions().muteStatus();

            if(BannedWords.checkAndWarn(senderUser, text)) {
                status = MuteStatus.HARD;
            }

            if(status.senderMaySee) {
                senderUser.sendMessage(
                        Component.translatable("mail.sent",
                                NamedTextColor.YELLOW,
                                user.nickDisplayName().color(NamedTextColor.GOLD),
                                action.getText().color(NamedTextColor.WHITE)
                        )
                );
            }

            senderUser.unloadIfOffline();
            if(!status.maySpeak) return;
        }

        mail.add(action.getText(), action.getSender());

        if(online) {
            if(action.hasSender()) {
                user.sendMessage(
                        Component.translatable("mail.received.humanSender", NamedTextColor.GRAY, senderDisplay)
                );
            } else {
                user.sendMessage(
                        Component.translatable("mail.received", NamedTextColor.GRAY)
                );
            }
        }
    }

    // Handle divorce hahahahahaha
    // What has this server come to lmao
    @Override
    public void handleDivorce(DivorceAction action) throws RoyalCommandException {
        CrownUser user = action.user();
        UserInteractions inter = user.getInteractions();

        if(inter.getSpouse() == null) throw FtcExceptionProvider.notMarried();
        if(!inter.canChangeMarriageStatus()) throw FtcExceptionProvider.cannotChangeMarriageStatus();

        CrownUser spouse = UserManager.getUser(inter.getSpouse());
        if(!spouse.getInteractions().canChangeMarriageStatus()) throw FtcExceptionProvider.cannotChangeMarriageStatusTarget(spouse);

        inter.setSpouse(null);
        inter.setMChatToggled(false);
        inter.setLastMarriageChange(System.currentTimeMillis());

        UserInteractions tInter = spouse.getInteractions();

        tInter.setMChatToggled(false);
        tInter.setSpouse(null);
        tInter.setLastMarriageChange(System.currentTimeMillis());

        if(action.informUsers()) {
            user.sendMessage(Component.translatable("marriage.divorce", spouse.nickDisplayName().color(NamedTextColor.GOLD)).color(NamedTextColor.YELLOW));

            spouse.sendOrMail(
                    Component.translatable("marriage.divorce.target",
                            user.nickDisplayName().color(NamedTextColor.GOLD)
                    ).color(NamedTextColor.YELLOW)
            );
        }
    }

    @Override
    public void handleMarry(MarryAction action) {
        CrownUser user = action.user();
        CrownUser target = action.target();

        UserInteractions inter = user.getInteractions();
        UserInteractions tInter = target.getInteractions();

        inter.setSpouse(target.getUniqueId());
        inter.setWaitingFinish(null);
        inter.setLastMarriageChange(System.currentTimeMillis());

        tInter.setSpouse(user.getUniqueId());
        tInter.setWaitingFinish(null);
        tInter.setLastMarriageChange(System.currentTimeMillis());

        if(action.informUsers()) {
            target.sendMessage(
                    Component.translatable("marriage.priestText.married", user.nickDisplayName().color(NamedTextColor.YELLOW)).color(NamedTextColor.GOLD)
            );
            user.sendMessage(
                    Component.translatable("marriage.priestText.married", target.nickDisplayName().color(NamedTextColor.YELLOW)).color(NamedTextColor.GOLD)
            );
        }

        Crown.getAnnouncer().announceToAll(
                Component.text()
                        .append(user.nickDisplayName())
                        .append(Component.text(" is now married to "))
                        .append(target.nickDisplayName())
                        .append(giveItAWeek())
                        .build()
        );
    }

    private Component giveItAWeek() {
        return FtcUtils.randomInRange(0, 1000) != 1 ? Component.text("!") :
                Component.text("... I give it a week").color(NamedTextColor.GRAY);
    }

    @Override
    public void handleVisit(RegionVisitAction visitAction) {
        new RegionVisit(visitAction.getVisitor(), visitAction.getRegion(), Crown.getRegionManager())
                .addPredicate(VisitPredicate.ensureRidingVehicle())
                .addPredicate(VisitPredicate.ensureNoPassengers())

                // This order matters, vehicles must be handled before
                // other passengers
                .addHandler(new RidingVehicleHandler())
                //.addHandler(new PassengerHandler())
                .addHandler(new OwnedEntityHandler())

                .run();
    }
}
