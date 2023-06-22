package net.forthecrown.core.user;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextWriter;
import net.forthecrown.text.TextWriters;
import net.forthecrown.user.NameElements;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;
import net.forthecrown.user.UserNameFactory;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.Nullable;

public class NameFactoryImpl implements UserNameFactory {

  private NameElement prefix = NameElements.EMPTY;
  private NameElement suffix = NameElements.EMPTY;

  private final Map<String, ProfileDisplayElement> profileFields = new HashMap<>();
  private final Map<String, ProfileDisplayElement> adminProfileFields = new HashMap<>();

  public NameFactoryImpl() {
    NameElements.registerAll(this);
  }

  @Override
  public Component formatDisplayName(User user, @Nullable Audience viewer, int flags) {
    DisplayContext ctx = createContext(user, viewer, flags);

    Component name;
    Component tabName = user.get(Properties.TAB_NAME);

    if (!Text.isEmpty(tabName)) {
      name = tabName;
    } else {
      if (ctx.useNickName()) {
        name = user.nickOrName();
      } else {
        name = Component.text(user.getName());
      }
    }

    TextComponent.Builder builder = Component.text();

    if (ctx.joinMessage()) {
      builder.color(NamedTextColor.YELLOW);
    }

    Component prefix = this.prefix.createDisplay(user, ctx);

    if (!Text.isEmpty(prefix)) {
      builder.append(prefix);
    }

    builder.append(name);

    Component suffix = this.suffix.createDisplay(user, ctx);

    if (!Text.isEmpty(suffix)) {
      builder.append(suffix);
    }

    Component hover = createProfileText(user, ctx, true);
    builder.hoverEvent(hover);

    return builder.build();
  }

  @Override
  public Component formatProfileDisplay(User user, Audience viewer) {
    DisplayContext context = createContext(user, viewer, FOR_HOVER);
    return createProfileText(user, context, false);
  }

  private Component createProfileText(User user, DisplayContext context, boolean formattingHover) {
    TextComponent.Builder builder = Component.text();

    TextWriter writer = TextWriters.wrap(builder);
    writer.setFieldStyle(Style.style(NamedTextColor.YELLOW));

    boolean showAdmin = context.viewerHasPermission("ftc.profiles.admin");

    Stream<ProfileDisplayElement> elementStream;

    if (showAdmin) {
      elementStream = Stream.concat(
          profileFields.values().stream(),
          adminProfileFields.values().stream()
      );
    } else {
      elementStream = profileFields.values().stream();
    }

    elementStream = elementStream.filter(element -> {
      var placement = element.placement();

      if (placement == FieldPlacement.ALL) {
        return true;
      }

      if (placement == FieldPlacement.IN_HOVER && !formattingHover) {
        return false;
      }

      // At this point, placement = IN_PROFILE, which requires that this
      // not be in a hover text
      return !formattingHover;
    });

    elementStream.forEach(element -> {
      element.write(writer, user, context);
    });

    return builder.build();
  }

  @Override
  public DisplayContext createContext(User user, Audience viewer, int flags) {
    return new DisplayContext(
        viewer,

        (flags & ALLOW_NICKNAME) == ALLOW_NICKNAME,
        (flags & FOR_HOVER)      == FOR_HOVER,
        (flags & JOIN_MESSAGE)   == JOIN_MESSAGE,

        DisplayContext.userFromAudience(viewer)
            .map(user1 -> user1.equals(user))
            .orElse(false)
    );
  }

  @Override
  public void addPrefix(NameElement element) {
    if (prefix != NameElements.EMPTY) {
      prefix = NameElements.combine(prefix, element);
    } else {
      prefix = element;
    }
  }

  @Override
  public void addSuffix(NameElement element) {
    if (suffix != NameElements.EMPTY) {
      suffix = NameElements.combine(suffix, element);
    } else {
      suffix = element;
    }
  }

  @Override
  public void addProfileField(String id, ProfileDisplayElement element) {
    profileFields.put(id, element);
  }

  @Override
  public void removeField(String id) {
    profileFields.remove(id);
  }

  @Override
  public void addAdminProfileField(String id, ProfileDisplayElement element) {
    adminProfileFields.put(id, element);
  }

  @Override
  public void removeAdminField(String id) {
    adminProfileFields.remove(id);
  }
}