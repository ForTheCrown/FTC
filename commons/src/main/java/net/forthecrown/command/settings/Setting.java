package net.forthecrown.command.settings;

import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.Duration;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.command.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.SyntaxExceptions;
import net.forthecrown.text.Messages;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.user.UserProperty;
import net.forthecrown.utils.Audiences;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickCallback.Options;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

@Accessors(chain = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Setting {

  @Getter
  private final SettingAccess access;

  @Getter
  @Setter
  private SettingValidator validator = SettingValidator.NOP;

  @Getter
  @Setter
  private String displayName;

  @Getter
  @Setter
  private String description = "";

  @Getter
  @Setter
  private String enableDescription = "Enables this setting";

  @Getter
  @Setter
  private String disableDescription = "Disables this setting";

  @Getter
  @Setter
  private String toggle = "{0} this setting";

  @Getter
  private SettingCommand command;

  private BookSetting<User> setting;

  public static Setting create(SettingAccess access) {
    return new Setting(access);
  }

  public static Setting createInverted(SettingAccess access) {
    Setting setting = create(access);

    String disable = setting.disableDescription;
    setting.disableDescription = setting.enableDescription;
    setting.enableDescription = disable;

    return setting;
  }

  public static Setting create(UserProperty<Boolean> property) {
    return create(SettingAccess.property(property));
  }

  public static Setting createInverted(UserProperty<Boolean> property) {
    return createInverted(SettingAccess.property(property));
  }

  public Setting setToggleDescription(String base) {
    return setEnableDescription(formatToggle(base, "start", "enable"))
        .setDisableDescription(formatToggle(base, "stop", "disable"));
  }

  private static String formatToggle(String base, String startStop, String enableDisable) {
    return base
        .replace("{start}", startStop)
        .replace("{Start}", Text.capitalizeFully(startStop))
        .replace("{enable}", enableDisable)
        .replace("{Enable}", Text.capitalizeFully(enableDisable));
  }

  public void toggleState(User user) throws CommandSyntaxException {
    boolean newState = !access.getState(user);
    setState(user, newState);
  }

  public void setState(User user, boolean newState) throws CommandSyntaxException {
    if (validator != null) {
      validator.test(user, newState);
    }

    access.setState(user, newState);

    Component message = Messages.toggleMessage(toggle, newState);
    user.sendMessage(message);
  }

  public void toggleOther(CommandSource source, User target) throws CommandSyntaxException {
    if (source.isPlayer() && target.getName().equals(source.textName())) {
      toggleState(target);
      return;
    }

    boolean newState = !access.getState(target);

    if (validator != null) {
      try {
        validator.test(target, newState);
      } catch (CommandSyntaxException exc) {

        Component newMessage = Text.format(
            "Couldn't update setting for {0, user}: '{1}'",
            target, exc.componentMessage()
        );

        throw Exceptions.create(newMessage);
      }
    }

    access.setState(target, newState);

    Component message = Messages.toggleOther(getDisplayName(), target, newState);
    source.sendSuccess(message);
  }

  public Setting createCommand(
      String commandName,
      Permission permission,
      Permission othersPermission,
      String... aliases
  ) {
    if (this.command != null) {
      throw new IllegalStateException("Command already created");
    }

    this.command = new SettingCommand(commandName, this, permission, othersPermission, aliases);
    command.register();

    return this;
  }

  private ClickCallback<Audience> createCallback(boolean state, SettingsBook<User> book) {
    return new ClickCallback<Audience>() {
      @Override
      public void accept(@NotNull Audience audience) {
        User user = Audiences.getUser(audience);

        if (user == null) {
          return;
        }

        try {
          setState(user, state);
          book.open(user, user);
        } catch (CommandSyntaxException exc) {
          SyntaxExceptions.handle(exc, user.getCommandSource());
        }
      }
    };
  }

  public BookSetting<User> toBookSettng() {
    Objects.requireNonNull(command, "Command not created yet");
    Objects.requireNonNull(displayName, "displayName not set");

    if (setting != null) {
      return setting;
    }

    return setting = new BookSetting<>() {

      ClickCallback<Audience> enableCallback;
      ClickCallback<Audience> disableCallback;

      @Override
      public Component displayName() {
        return text(getDisplayName()).hoverEvent(text(getDescription()));
      }

      @Override
      public Component createButtons(User context) {
        boolean state = access.getState(context);

        if (enableCallback == null) {
          enableCallback = createCallback(true, getBook());
        }

        if (disableCallback == null) {
          disableCallback = createCallback(false, getBook());
        }

        final Options options = Options.builder()
            .uses(-1)
            .lifetime(Duration.ofDays(365))
            .build();

        Component enable = BookSetting.createButton(
            true,
            state,
            ClickEvent.callback(enableCallback, options),
            text(enableDescription)
        );

        Component disable = BookSetting.createButton(
            false,
            state,
            ClickEvent.callback(disableCallback, options),
            text(disableDescription)
        );

        return Component.textOfChildren(enable, Component.space(), disable);
      }

      @Override
      public boolean shouldInclude(User context) {
        CommandSource source = context.getCommandSource();
        return command.test(source);
      }
    };
  }

  public interface SettingValidator {

    SettingValidator NOP = (user, newState) -> {};

    void test(User user, boolean newState) throws CommandSyntaxException;
  }
}