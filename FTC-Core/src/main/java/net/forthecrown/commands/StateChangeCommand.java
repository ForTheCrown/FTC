package net.forthecrown.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.book.SettingsBook;
import net.forthecrown.commands.arguments.UserArgument;
import net.forthecrown.commands.click.ClickableTextNode;
import net.forthecrown.commands.click.ClickableTexts;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserInteractions;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.Buildable;
import org.bukkit.permissions.Permission;

public class StateChangeCommand extends FtcCommand {
    public static final ClickableTextNode STATE_CHANGE_ROOT = ClickableTexts.register(new ClickableTextNode("state_change_root"));

    private final StateGetter getter;
    private final StateSetter setter;
    private final StateUsageValidator validator;

    private final String onTranslationKey;
    private final String offTranslationKey;

    private final ClickableTextNode allowNode, denyNode;

    private final String displayName;

    public StateChangeCommand(
            String name,
            String displayName,
            StateGetter getter,
            StateSetter setter,
            String onTranslationKey, String offTranslationKey,
            Permission permission,
            String description,
            String... aliases
    ) {
        this(name, displayName, null, getter, setter, onTranslationKey, offTranslationKey, permission, description, aliases);
    }

    public StateChangeCommand(
            String name,
            String displayName,
            StateUsageValidator validator,
            StateGetter getter,
            StateSetter setter,
            String onTranslationKey, String offTranslationKey,
            Permission permission,
            String description,
            String... aliases
    ) {
        super(name);
        this.displayName = displayName;
        this.getter = getter;
        this.setter = setter;
        this.onTranslationKey = onTranslationKey;
        this.offTranslationKey = offTranslationKey;
        this.validator = validator;
        this.description = description;

        setPermission(permission);
        setAliases(aliases);

        register();
        SettingsBook.addCmdToBook(this);

        allowNode = createNode(true);
        denyNode = createNode(false);

        STATE_CHANGE_ROOT.addNode(allowNode);
        STATE_CHANGE_ROOT.addNode(denyNode);
    }

    // Here, we create a clickable text node for this command
    private ClickableTextNode createNode(boolean state) {
        ClickableTextNode node = new ClickableTextNode(getName());

        // We give the node an executor which sets the user's state
        node.setExecutor(user -> {
            setState(user, state);
            SettingsBook.open(user); // reopen book
        });

        node.setPrompt(user -> {
            TextComponent.Builder b = Component.text();
            if (state) { // [Allow] option
                TextComponent.Builder allowComponent = Component.text();
                allowComponent.append(Component.text("[Allow]"));

                if (getState(user)) allowComponent.color(NamedTextColor.DARK_AQUA); // If already allowed
                else allowComponent.hoverEvent(Component.text("Click to Allow")).color(NamedTextColor.GRAY);

                return allowComponent.build();
            }
            else { // [Deny] option
                TextComponent.Builder denyComponent = Component.text();
                denyComponent.append(Component.text("[Deny]"));

                if (!getState(user)) denyComponent.color(NamedTextColor.DARK_AQUA); // If already denied
                else denyComponent.hoverEvent(Component.text("Click to Deny")).color(NamedTextColor.GRAY);

                return denyComponent.build();
            }
        });

        return node;
    }

    public boolean getState(CrownUser user) {
        return getter.getState(user);
    }

    public void setState(CrownUser user, boolean state) {
        if(validator != null) {
            try {
                validator.test(user, state);
            } catch (CommandSyntaxException e) {
                FtcUtils.handleSyntaxException(user, e);
                return;
            }
        }

        setter.setState(user, state);

        user.sendMessage(
                Component.translatable(state ? onTranslationKey : offTranslationKey)
                        .color(state ? NamedTextColor.YELLOW : NamedTextColor.GRAY)
        );
    }

    public ClickableTextNode getAllowNode() {
        return allowNode;
    }

    public ClickableTextNode getDenyNode() {
        return denyNode;
    }

    // Call this to get a component which combines both the true and false nodes into one,
    // it should return a component like "[Deny] [Allow]"
    public Component getButtonComponent(CrownUser user) {
        return Component.text()
                .append(getAllowNode().prompt(user))
                .append(Component.space())
                .append(getDenyNode().prompt(user))
                .build();
    }

    public Component getDisplayName() {
        return Component.text(displayName);
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command
                .executes(c -> {
                    CrownUser user = getUserSender(c);
                    boolean state = !getter.getState(user);

                    if(validator != null) validator.test(user, state);

                    setter.setState(user, state);

                    user.sendMessage(
                            Component.translatable(state ? onTranslationKey : offTranslationKey)
                                    .color(state ? NamedTextColor.YELLOW : NamedTextColor.GRAY)
                    );
                    return 0;
                })

                .then(argument("user", UserArgument.user())
                        .requires(s -> s.hasPermission(Permissions.ADMIN))

                        .executes(c -> {
                            CrownUser user = UserArgument.getUser(c, "user");
                            boolean state = !getter.getState(user);

                            setter.setState(user, state);

                            c.getSource().sendAdmin(
                                    Component.text("Set " + getName() + " of ")
                                            .append(user.nickDisplayName().color(NamedTextColor.YELLOW))
                                            .append(Component.text(" to "))
                                            .append(Component.text(state).color(NamedTextColor.YELLOW))
                            );
                            return 0;
                        })
                );
    }

    public static void init() {}

    //Tests if a user is allowed to change the state
    //Fails if exception is thrown
    public interface StateUsageValidator {
        void test(CrownUser user, boolean newState) throws CommandSyntaxException;
    }

    //Gets the state of a user
    public interface StateGetter {
        boolean getState(CrownUser user);
    }

    //Sets the state of a thing for a user
    public interface StateSetter {
        void setState(CrownUser user, boolean newState);
    }

    public static final StateChangeCommand PROFILE_PRIVATE = new StateChangeCommand(
            "profiletoggle", "Profile",
            CrownUser::isProfilePublic, CrownUser::setProfilePublic,
            "user.profile.public", "user.profile.private",
            Permissions.PROFILE,
            "Toggles your profile being private or public",
            "profileprivate", "profilepublic"
    );

    public static final StateChangeCommand IGNORE_BROADCASTS = new StateChangeCommand(
            "ignoreac", "FTC",
            CrownUser::ignoringBroadcasts, CrownUser::setIgnoringBroadcasts,
            "user.acIgnore.on", "user.acIgnore.off",
            Permissions.IGNORE_AC,
            "Toggles seeing automated server announcements",
            "ignorebroadcasts", "ignorebc", "ignoreannouncements"
    );

    public static final StateChangeCommand TOGGLE_PAY = new StateChangeCommand(
            "paytoggle", "Pay",
            CrownUser::allowsPaying, CrownUser::setAllowsPay,
            "commands.payToggle.on", "commands.payToggle.off",
            Permissions.PAY_TOGGLE,
            "Toggles being able to pay and be payed"
    );

    public static final StateChangeCommand TOGGLE_RIDING = new StateChangeCommand(
            "toggleriding", "Riding",
            CrownUser::allowsRiding, CrownUser::setAllowsRiding,
            "user.riding.allow", "user.riding.deny",
            Permissions.DEFAULT,
            "Toggles being able to ride and be ridden by players",
            "ridingtoggle"
    );

    public static final StateChangeCommand TOGGLE_TPA = new StateChangeCommand(
            "tpatoggle", "Tpa",
            CrownUser::allowsTPA, CrownUser::setAllowsTPA,
            "tpa.toggle.on", "tpa.toggle.off",
            Permissions.TPA,
            "Toggles being able to tpa to people",
            "toggletpa"
    );

    public static final StateChangeCommand TOGGLE_MARRIAGE_CHAT = new StateChangeCommand(
            "marriagechattoggle", "Mchat",
            (user, toggled) -> {
                UserInteractions inter = user.getInteractions();

                if(inter.getSpouse() == null) throw FtcExceptionProvider.notMarried();

                if(toggled){
                    CrownUser spouse = UserManager.getUser(inter.getSpouse());
                    if(!spouse.isOnline()) throw UserArgument.USER_NOT_ONLINE.create(spouse.nickDisplayName());
                }
            },

            user -> {
                UserInteractions inter = user.getInteractions();

                return inter.mChatToggled();
            },

            (user, newState) -> {
                UserInteractions inter = user.getInteractions();

                inter.setMChatToggled(newState);
            },

            "marriage.chat.on", "marriage.chat.off",
            Permissions.MARRY,
            "Toggles all your messages going to marriage chat",
            "mchattoggle", "mct", "mctoggle"
    );

    public static final StateChangeCommand TOGGLE_EMOTES = new StateChangeCommand(
            "toggleemotes", "Emotes",
            CrownUser::allowsEmotes, CrownUser::setAllowsEmotes,
            "emotes.toggle.on", "emotes.toggle.off",
            Permissions.EMOTES,
            "Toggles being able to emote to people and for people to emote at you",
            "emotetoggle"
    );

    public static final StateChangeCommand TOGGLE_MARRYING = new StateChangeCommand(
            "marrytoggle", "Marry",

            user -> user.getInteractions().acceptingProposals(),
            (user, newState) -> user.getInteractions().setAcceptingProposals(newState),

            "marriage.toggle.on", "marriage.toggle.off",
            Permissions.MARRY,
            "Toggles being able to marry and have people send you proposals",
            "togglemarry"
    );

    public static final StateChangeCommand TOGGLE_REGION_INVITES = new StateChangeCommand(
            "toggleinvites", "Invites",

            CrownUser::allowsRegionInvites,
            CrownUser::setAllowsRegionInvites,

            "regions.invite.toggle.on", "regions.invite.toggle.off",
            Permissions.REGIONS,
            "Toggles being able to invite and be invited to regions",
            "allowinvites", "denyinvites"
    );

    public static final StateChangeCommand TOGGLE_HULK_SMASHING = new StateChangeCommand(
            "hulksmash", "Hulk",

            CrownUser::hulkSmashesPoles,
            CrownUser::setHulkPoles,

            "regions.hulk.on", "regions.hulk.off",
            Permissions.REGIONS,
            "Toggles whether you quickly teleport to poles to hulk smash onto them",
            "togglehulk", "togglehulksmash"
    );
}