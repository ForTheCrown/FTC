package net.forthecrown.core.user;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.forthecrown.Permissions;
import net.forthecrown.text.BufferedTextWriter;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextInfo;
import net.forthecrown.text.TextWriter;
import net.forthecrown.text.TextWriters;
import net.forthecrown.user.NameElements;
import net.forthecrown.user.NameRenderFlags;
import net.forthecrown.user.Properties;
import net.forthecrown.user.User;
import net.forthecrown.user.name.DisplayContext;
import net.forthecrown.user.name.DisplayIntent;
import net.forthecrown.user.name.FieldPlacement;
import net.forthecrown.user.name.NameElement;
import net.forthecrown.user.name.ProfileDisplayElement;
import net.forthecrown.user.name.UserNameFactory;
import net.forthecrown.utils.Audiences;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class NameFactoryImpl implements UserNameFactory {

  private static final char BORDER_CHAR = ' ';
  private static final int MAX_CHAT_WIDTH = 320; // Font pixels aren't real pixels
  private static final int BORDER_CHAR_LENGTH = 4; // ' ' is 3 px, but +1 for space between chars
  private static final int FOOTER_SIZE = MAX_CHAT_WIDTH / BORDER_CHAR_LENGTH;

  private static final Style BORDER_STYLE
      = Style.style(NamedTextColor.GOLD, TextDecoration.STRIKETHROUGH);

  private NameElement prefix = NameElements.EMPTY;
  private NameElement suffix = NameElements.EMPTY;

  // Array maps to make the order fields are displayed in be more consistent
  private final Map<String, ProfileDisplayElement> profileFields = new Object2ObjectArrayMap<>();
  private final Map<String, ProfileDisplayElement> adminFields = new Object2ObjectArrayMap<>();

  public NameFactoryImpl() {
    NameElements.registerAll(this);
    ProfileFields.registerAll(this);
  }

  @Override
  public Component formatDisplayName(User user, DisplayContext ctx) {
    Component name;
    Component tabName = user.get(Properties.TAB_NAME);

    if (!Text.isEmpty(tabName)) {
      name = tabName;
    } else {
      if (ctx.useNickName()) {
        name = user.nickOrName();
      } else {
        name = text(user.getName());
      }
    }

    TextComponent.Builder builder = text();

    Component prefix = formatPrefix(user, ctx);
    if (prefix != null) {
      builder.append(prefix);
    }

    builder.append(name);

    Component suffix = formatSuffix(user, ctx);
    if (suffix != null) {
      builder.append(suffix);
    }

    if (ctx.intent().isHoverTextAllowed()) {
      Component hover = createHoverText(user, ctx);
      builder.hoverEvent(hover);
    }

    return builder.build();
  }

  @Override
  public @Nullable Component formatPrefix(User user, DisplayContext context) {
    return formatElement(user, context, prefix);
  }

  @Override
  public @Nullable Component formatSuffix(User user, DisplayContext context) {
    return formatElement(user, context, suffix);
  }

  private Component formatElement(User user, DisplayContext ctx, NameElement element) {
    if (element == null) {
      return null;
    }

    Component text = element.createDisplay(user, ctx);

    if (Text.isEmpty(text)) {
      return null;
    }

    return text;
  }

  @Override
  public Component formatProfileDisplay(User user, Audience viewer) {
    var writer = createProfileWriter();
    writeProfileDisplay(writer, user, viewer);
    return writer.asComponent();
  }

  @Override
  public void writeProfileDisplay(TextWriter writer, User user, Audience viewer) {
    DisplayContext context = createContext(user, viewer, user.defaultRenderFlags());
    createProfileText(writer, user, context, false);
  }

  private Component createHoverText(User user, DisplayContext context) {
    var writer = createProfileWriter();
    createProfileText(writer, user, context, true);
    return writer.asComponent();
  }

  TextWriter createProfileWriter() {
    TextWriter writer = TextWriters.newWriter();

    writer.setFieldStyle(Style.style(NamedTextColor.YELLOW));
    writer.setFieldValueStyle(Style.style(NamedTextColor.WHITE));
    writer.setFieldSeparator(Component.text(": ", NamedTextColor.YELLOW));

    return writer;
  }

  private void createProfileText(
      TextWriter writer,
      User user,
      DisplayContext context,
      boolean formattingHover
  ) {
    writer.viewer(context.viewer());

    boolean showAdmin = context.viewerHasPermission(Permissions.PROFILE_BYPASS);

    List<ProfileDisplayElement> normalFields = new ArrayList<>(profileFields.values());
    List<ProfileDisplayElement> adminFields = new ArrayList<>(this.adminFields.values());

    filterFields(normalFields, formattingHover);
    filterFields(adminFields, formattingHover);

    if (!showAdmin) {
      adminFields.clear();
    }

    if (formattingHover) {
      writeFields(normalFields, adminFields, writer, user, context);
      return;
    }

    BufferedTextWriter buffered = TextWriters.buffered();
    buffered.copyStyle(writer);
    buffered.viewer(context.viewer());

    writeFields(normalFields, adminFields, buffered, user, context);

    List<Component> lines = buffered.getBuffer();
    Component headerName;

    if (context.self()) {
      headerName = text("Your profile");
    } else {
      Component displayName = formatDisplayName(user, context.withIntent(DisplayIntent.HOVER_TEXT));
      headerName = Text.format("{0, user}'s profile", displayName);
    }

    headerName = text()
        .append(space(), headerName, space())
        .color(NamedTextColor.YELLOW)
        .build();

    int headerLength = TextInfo.length(headerName);
    int bordersSize = (MAX_CHAT_WIDTH - headerLength) / 2;
    int borderChars = bordersSize / BORDER_CHAR_LENGTH;

    String headerBorder = String.valueOf(BORDER_CHAR).repeat(borderChars);
    String footer = String.valueOf(BORDER_CHAR).repeat(FOOTER_SIZE);
    Component borderText = text(headerBorder, BORDER_STYLE);

    writer.line(borderText);
    writer.write(headerName);
    writer.write(borderText);

    lines.forEach(writer::line);
    writer.line(text(footer, BORDER_STYLE));
  }

  private void writeFields(
      List<ProfileDisplayElement> normalElements,
      List<ProfileDisplayElement> adminElements,
      TextWriter writer,
      User user,
      DisplayContext context
  ) {
    normalElements.forEach(element -> {
      element.write(writer, user, context);
    });

    if (adminElements.isEmpty()) {
      return;
    }

    writer.newLine();
    writer.newLine();
    writer.line("Admin Info:", writer.getFieldStyle());
    writer.newLine();

    adminElements.forEach(element -> {
      element.write(writer, user, context);
    });
  }

  private void filterFields(
      List<ProfileDisplayElement> elements,
      boolean formattingHover
  ) {
    elements.removeIf(element -> {
      var placement = element.placement();

      if (placement == FieldPlacement.ALL) {
        return false;
      }

      if (placement == FieldPlacement.IN_HOVER && !formattingHover) {
        return true;
      }

      // At this point, placement = IN_PROFILE, which requires that this
      // not be in a hover text
      return formattingHover;
    });
  }

  @Override
  public DisplayContext createContext(User user, Audience viewer, Set<NameRenderFlags> flags) {
    boolean self;

    if (viewer != null) {
      Player plr = Audiences.getPlayer(viewer);

      if (plr == null) {
        self = false;
      } else {
        self = plr.getUniqueId().equals(user.getUniqueId());
      }
    } else {
      self = false;
    }

    return new DisplayContext(
        viewer,
        flags.contains(NameRenderFlags.ALLOW_NICKNAME),
        flags.contains(NameRenderFlags.USER_ONLINE),
        self,
        DisplayIntent.UNSET
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
    adminFields.put(id, element);
  }

  @Override
  public void removeAdminField(String id) {
    adminFields.remove(id);
  }
}