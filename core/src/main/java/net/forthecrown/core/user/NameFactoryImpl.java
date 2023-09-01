package net.forthecrown.core.user;

import static net.kyori.adventure.text.Component.text;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.forthecrown.Permissions;
import net.forthecrown.text.BufferedTextWriter;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextWriter;
import net.forthecrown.text.TextWriters;
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
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class NameFactoryImpl implements UserNameFactory {

  private static final Style BORDER_STYLE
      = Style.style(NamedTextColor.GOLD, TextDecoration.STRIKETHROUGH);

  private static final Comparator<ElementInfo> INFO_COMPARATOR
      = Comparator.comparingInt(ElementInfo::priority);

  private final List<ElementInfo> prefix = new ArrayList<>();
  private final List<ElementInfo> suffix = new ArrayList<>();

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
        name = user.name();
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
      Component hover = createHoverText(user, ctx.withIntent(DisplayIntent.HOVER_TEXT));
      builder.hoverEvent(hover);
    }

    return builder
        .clickEvent(userClickEvent(ctx.userOnline(), user))
        .insertion(user.getUniqueId().toString())
        .build();
  }

  private ClickEvent userClickEvent(boolean online, User user) {
    if (online) {
      return ClickEvent.suggestCommand("/tell " + user.getNickOrName());
    } else {
      return ClickEvent.suggestCommand("/profile " + user.getNickOrName());
    }
  }

  @Override
  public @Nullable Component formatPrefix(User user, DisplayContext context) {
    return formatElement(user, context, prefix);
  }

  @Override
  public @Nullable Component formatSuffix(User user, DisplayContext context) {
    return formatElement(user, context, suffix);
  }

  private Component formatElement(User user, DisplayContext ctx, List<ElementInfo> list) {
    if (list == null || list.isEmpty()) {
      return null;
    }

    for (ElementInfo elementInfo : list) {
      var element = elementInfo.element;
      Component text = element.createDisplay(user, ctx);

      if (Text.isEmpty(text)) {
        continue;
      }

      return text;
    }

    return null;
  }

  @Override
  public Component formatProfileDisplay(User user, Audience viewer) {
    var writer = createProfileWriter();
    writeProfileDisplay(writer, user, viewer);
    return writer.asComponent();
  }

  @Override
  public void writeProfileDisplay(TextWriter writer, User user, DisplayContext context) {
    createProfileText(writer, user, context, context.intentMatches(DisplayIntent.HOVER_TEXT));
  }

  private Component createHoverText(User user, DisplayContext context) {
    var writer = createProfileWriter();
    createProfileText(writer, user, context, true);
    return writer.asComponent();
  }

  TextWriter createProfileWriter() {
    TextWriter writer = TextWriters.newWriter();
    applyProfileStyle(writer);
    return writer;
  }

  @Override
  public void applyProfileStyle(TextWriter writer) {
    writer.setFieldStyle(Style.style(NamedTextColor.YELLOW));
    writer.setFieldValueStyle(Style.style(NamedTextColor.WHITE));
    writer.setFieldSeparator(Component.text(": ", NamedTextColor.YELLOW));
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
        .append(headerName)
        .color(NamedTextColor.YELLOW)
        .build();

    Component footer = Text.chatWidthBorder(null).style(BORDER_STYLE);
    Component borderText = Text.chatWidthBorder(headerName).style(BORDER_STYLE);

    writer.line(borderText);
    writer.space();
    writer.write(headerName);
    writer.space();
    writer.write(borderText);

    lines.forEach(writer::line);
    writer.line(footer);
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

      if (placement == FieldPlacement.IN_HOVER) {
        return !formattingHover;
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
        DisplayIntent.UNSET,
        this
    );
  }

  @Override
  public void addPrefix(String id, int priority, NameElement element) {
    addElement(prefix, id, priority, element);
  }

  @Override
  public void addSuffix(String id, int priority, NameElement element) {
    addElement(suffix, id, priority, element);
  }

  @Override
  public void removePrefix(String id) {
    removeElement(prefix, id);
  }

  @Override
  public void removeSuffix(String id) {
    removeElement(suffix, id);
  }

  void addElement(List<ElementInfo> list, String id, int prio, NameElement element) {
    Objects.requireNonNull(id, "Null ID");
    Objects.requireNonNull(element, "Null element");

    int existing = findElement(id, list);

    if (existing != -1) {
      throw new IllegalStateException("Name element with ID '" + id + "' already registered");
    }

    ElementInfo info = new ElementInfo(id, prio, element);
    list.add(info);
    list.sort(INFO_COMPARATOR);
  }

  void removeElement(List<ElementInfo> info, String id) {
    Objects.requireNonNull(id, "Null ID");
    int index = findElement(id, info);

    if (index == -1) {
      return;
    }

    info.remove(index);
  }

  int findElement(String id, List<ElementInfo> infos) {
    if (infos == null || infos.isEmpty()) {
      return -1;
    }

    for (int i = 0; i < infos.size(); i++) {
      var info = infos.get(i);

      if (info.id.equals(id)) {
        return i;
      }
    }

    return -1;
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

  record ElementInfo(String id, int priority, NameElement element) {

  }
}