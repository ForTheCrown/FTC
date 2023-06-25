package net.forthecrown.command.settings;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.command.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.text.Messages;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.user.UserProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEventSource;
import org.bukkit.permissions.Permission;

@Getter
@Setter
@Accessors(chain = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Setting {

  private final SettingAccess access;

  private SettingValidator validator = SettingValidator.NOP;

  private String displayName;

  private String description = "";

  private String enableDescription = "Enables this setting";
  private String disableDescription = "Disables this setting";

  private String toggleMessage = "{0} this setting";

  @Setter(AccessLevel.PRIVATE)
  private SettingCommand command;

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

  public void toggleState(User user) throws CommandSyntaxException {
    boolean newState = !access.getState(user);

    if (validator != null) {
      validator.test(user, newState);
    }

    access.setState(user, newState);

    Component message = Messages.toggleMessage(toggleMessage, newState);
    user.sendMessage(message);
  }

  public void toggleOther(CommandSource source, User target) throws CommandSyntaxException {
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

  public BookSetting<User> toSetting() {
    Objects.requireNonNull(command, "Command not created yet");

    return new BookSetting<>() {
      @Override
      public Component displayName() {
        return Component.text(getDisplayName());
      }

      @Override
      public Component createButtons(User context) {
        String command = "/" + getCommand().getName();
        boolean state = access.getState(context);

        Component enable = BookSetting.createButton(
            true, state, command, createSource(enableDescription)
        );

        Component disable = BookSetting.createButton(
            false, state, command, createSource(disableDescription)
        );

        return Component.textOfChildren(enable, Component.space(), disable);
      }

      private HoverEventSource createSource(String desc) {
        return Component.text(desc);
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