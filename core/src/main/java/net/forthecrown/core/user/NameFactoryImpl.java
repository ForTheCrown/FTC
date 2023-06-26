package net.forthecrown.core.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextWriter;
import net.forthecrown.text.TextWriters;
import net.forthecrown.user.NameElements;
import net.forthecrown.user.NameRenderFlags;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;
import net.forthecrown.user.UserNameFactory;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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
  public Component formatDisplayName(
      User user,
      @Nullable Audience viewer,
      Set<NameRenderFlags> flags
  ) {
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
    Component prefix = this.prefix.createDisplay(user, ctx);

    if (!Text.isEmpty(prefix)) {
      builder.append(prefix);
    }

    builder.append(name);

    Component suffix = this.suffix.createDisplay(user, ctx);

    if (!Text.isEmpty(suffix)) {
      builder.append(suffix);
    }

    Component hover = createHoverText(user, ctx);
    builder.hoverEvent(hover);

    return builder.build();
  }

  @Override
  public Component formatProfileDisplay(User user, Audience viewer) {
    var writer = TextWriters.newWriter();
    writeProfileDisplay(writer, user, viewer);
    return writer.asComponent();
  }

  @Override
  public void writeProfileDisplay(TextWriter writer, User user, Audience viewer) {
    DisplayContext context = createContext(user, viewer, user.defaultRenderFlags());
    createProfileText(writer, user, context, false);
  }

  private Component createHoverText(User user, DisplayContext context) {
    var writer = TextWriters.newWriter();
    createProfileText(writer, user, context, true);
    return writer.asComponent();
  }

  private void createProfileText(
      TextWriter writer,
      User user,
      DisplayContext context,
      boolean formattingHover
  ) {
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
  }

  @Override
  public DisplayContext createContext(User user, Audience viewer, Set<NameRenderFlags> flags) {
    return new DisplayContext(
        viewer,

        flags.contains(NameRenderFlags.ALLOW_NICKNAME),
        flags.contains(NameRenderFlags.FOR_HOVER),
        flags.contains(NameRenderFlags.USER_ONLINE),

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