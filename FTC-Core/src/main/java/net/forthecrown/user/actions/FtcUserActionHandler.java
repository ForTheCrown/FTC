package net.forthecrown.user.actions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.EavesDropper;
import net.forthecrown.core.admin.MuteStatus;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.core.chat.ComponentWriter;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.core.chat.PagedDisplay;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.regions.visit.RegionVisit;
import net.forthecrown.regions.visit.VisitPredicate;
import net.forthecrown.regions.visit.handlers.OwnedEntityHandler;
import net.forthecrown.regions.visit.handlers.RidingVehicleHandler;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserInteractions;
import net.forthecrown.user.UserMail;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class FtcUserActionHandler implements UserActionHandler {
    @Override
    public void handleDirectMessage(DirectMessage message) {
        MuteStatus mute = Punishments.checkMute(message.getSender().asBukkit());

        //Validate they didn't just use a slur or something lol
        if(Punishments.checkBannedWords(message.getSender().asBukkit(), message.getFormattedText())) {
            mute = MuteStatus.HARD;
        }

        EavesDropper.reportDM(message, mute);

        UUID senderID = null;
        UUID receiverID = null;

        //If no mute whatsoever
        if(mute.maySpeak) {
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
        if(mute.senderMaySee){
            Component senderMessage = Component.text()
                    .append(message.getSenderHeader())
                    .append(Component.text(" "))
                    .append(message.getFormattedText())
                    .build();

            message.getSender().sendMessage(senderMessage, receiverID);
        }
    }

    @Override
    public void handleMarriageMessage(MarriageMessage message) {
        Component formatted = MarriageMessage.format(message.getSender().nickDisplayName(), message.getFormatted());

        //Validate they didn't just use a slur or something lol
        if(Punishments.checkBannedWords(message.getSender(), formatted)) {
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
        // if(status.senderMaySee) message.getSender().sendBlockableMessage(message.getTarget().getUniqueId(), formatted);
        // if(status.maySpeak) message.getTarget().sendBlockableMessage(message.getSender().getUniqueId(), formatted);
        if(status.senderMaySee) message.getSender().sendMessage(formatted);
        if(status.maySpeak) message.getTarget().sendMessage(formatted);
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
        String selfTranslationKey = self ? "self" : "other";

        List<UserMail.MailMessage> messages = query.getMail().getMail();

        if(messages.isEmpty()) {
            throw FtcExceptionProvider.translatable("mail.none." + selfTranslationKey, query.getUser().nickDisplayName());
        }

        final Component border = Component.text("               ", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH);

        query.getSource().sendMessage(
                PagedDisplay.create(
                        query.getPage(),
                        UserMail.PAGE_SIZE,
                        query.getMail().getMail(),

                        // Entry
                        (m, index) -> {
                            Component senderMetadata = m.sender == null ? Component.empty() :
                                    Component.newline()
                                            .append(Component.translatable("mail.metadata.sender", UserManager.getUser(m.sender).nickDisplayName()));

                            Component itemClaim = Component.empty();

                            if (UserMail.hasAttachment(m)) {
                                ComponentWriter writer = ComponentWriter.normal();
                                m.attachment.writeHover(writer);

                                if (!self) {
                                    itemClaim = Component.text("[Attachment] ")
                                            .color(m.attachmentClaimed ? NamedTextColor.GRAY : NamedTextColor.AQUA)
                                            .hoverEvent(
                                                    Component.text(m.attachmentClaimed ? "Claimed" : "Not claimed")
                                                            .append(Component.newline())
                                                            .append(writer.get())
                                            );
                                } else {
                                    itemClaim = Component.translatable("mail.claimItem",
                                                    m.attachmentClaimed ? NamedTextColor.GRAY : NamedTextColor.AQUA
                                            )
                                            .hoverEvent(
                                                    Component.translatable("mail.claimItem.hover." + (m.attachmentClaimed ? "unavailable" : "available"))
                                                            .append(Component.newline())
                                                            .append(writer.get())
                                            )
                                            .clickEvent(ClickEvent.runCommand("/mail claim " + index))
                                            .append(Component.space());
                                }
                            }

                            return Component.text()
                                    // user-friendly mail index
                                    .append(
                                            Component.text("" + index + ")", NamedTextColor.GOLD)
                                                    .hoverEvent(
                                                            Component.translatable(
                                                                            "mail.metadata.date",
                                                                            FtcFormatter.formatDate(m.sent)
                                                                    )
                                                                    .append(senderMetadata)
                                                    )
                                    )

                                    // [Read] | [Unread] Messages
                                    .append(
                                            Component.text(" [", m.read ? NamedTextColor.GRAY : NamedTextColor.YELLOW)
                                                    .append(
                                                            Component.translatable(!m.read ? "mail.read" : "mail.unread")
                                                                    .clickEvent(self ? ClickEvent.runCommand("/mail mark_" + (m.read ? "un" : "") + "read " + index) : null)
                                                                    .hoverEvent(self ? Component.translatable(!m.read ? "mail.read.hover" : "mail.unread.hover") : null)
                                                    )
                                                    .append(Component.text("] "))
                                    )

                                    // [Claim Item] part
                                    .append(itemClaim)

                                    // The actual message
                                    .append(m.message)
                                    .build();
                        },

                        // Header
                        () -> {
                            return Component.text()
                                    .color(NamedTextColor.YELLOW)
                                    .append(border)
                                    .append(Component.space())
                                    .append(
                                            Component.translatable(
                                                    "mail.header." + selfTranslationKey,
                                                    query.getUser().nickDisplayName()
                                                            .color(NamedTextColor.GOLD)
                                            )
                                    )
                                    .append(Component.space())
                                    .append(border)
                                    .build();
                        },

                        // Footer
                        (currentPage, lastPage, firstPage, maxPage) -> {
                            Component nextPage = lastPage ? Component.space() : pageButton(currentPage + 1, '>', query);
                            Component prevPage = firstPage ? Component.space() : pageButton(currentPage - 1, '<', query);

                            return Component.text()
                                    .append(border)
                                    .append(prevPage)
                                    .append(Component.text(currentPage + "/" + maxPage))
                                    .append(nextPage)
                                    .append(border)
                                    .build();
                        }
                )
        );
    }

    private Component pageButton(int page, char pointer, MailQuery query) {
        String cmdTxt = !query.isSelfQuery() ? "read_other " + query.getUser().getName() + " " + page : "" + page;

        return Component.text(" " + pointer + " ", NamedTextColor.YELLOW, TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/mail " + cmdTxt))
                .hoverEvent(Component.text("Click me :D"));
    }

    @Override
    public void handleMailAdd(MailAddAction action) throws CommandSyntaxException {
        boolean online = action.getUser().isOnline();
        CrownUser user = action.getUser();
        UserMail mail = action.getMail();
        Component text = action.getText();
        Component senderDisplay = null;

        if (action.hasSender()) {
            UUID sender = action.getSender();
            CrownUser senderUser = UserManager.getUser(sender);
            senderDisplay = senderUser.nickDisplayName().color(NamedTextColor.YELLOW);
            MuteStatus status = MuteStatus.NONE;

            if (action.shouldValidateSender()) {
                status = Punishments.muteStatus(senderUser);

                if(Punishments.checkBannedWords(senderUser, text)) {
                    status = MuteStatus.HARD;
                }
            }

            if(status.senderMaySee && action.informSender()) {
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

        UserMail.MailMessage message = new UserMail.MailMessage(
                action.getText(), action.getSender(), System.currentTimeMillis()
        );

        if (!UserMail.isEmpty(action.getAttachment())) {
            message.attachment = action.getAttachment();
            message.attachmentClaimed = false;
        }

        mail.add(message);

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
        return FtcUtils.RANDOM.intInRange(0, 1000) != 1 ? Component.text("!") :
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