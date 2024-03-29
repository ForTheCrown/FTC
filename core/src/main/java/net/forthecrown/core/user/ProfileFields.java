package net.forthecrown.core.user;

import static net.kyori.adventure.text.Component.text;

import com.google.common.base.Strings;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import net.forthecrown.Permissions;
import net.forthecrown.core.CoreMessages;
import net.forthecrown.text.PeriodFormat;
import net.forthecrown.text.TextWriter;
import net.forthecrown.text.UnitFormat;
import net.forthecrown.user.Properties;
import net.forthecrown.user.TimeField;
import net.forthecrown.user.User;
import net.forthecrown.user.UserBlockList;
import net.forthecrown.user.name.DisplayContext;
import net.forthecrown.user.name.FieldPlacement;
import net.forthecrown.user.name.ProfileDisplayElement;
import net.forthecrown.user.name.UserNameFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Location;

interface ProfileFields {

  ProfileDisplayElement UUID = new ProfileDisplayElement() {
    @Override
    public void write(TextWriter writer, User user, DisplayContext context) {
      writer.field("UUID", user.getUniqueId());
    }

    @Override
    public FieldPlacement placement() {
      return FieldPlacement.IN_HOVER;
    }
  };

  ProfileDisplayElement REAL_NAME = (writer, user, context) -> {
    if (user.getNickname() == null && !user.has(Properties.TAB_NAME)) {
      return;
    }

    writer.field("Name", user.getName());
  };

  ProfileDisplayElement FIRST_JOIN = timestamp("First Join", TimeField.FIRST_JOIN);

  ProfileDisplayElement RHINES = unit(User::getBalance, UnitFormat.UNIT_RHINE, "Rhines");
  ProfileDisplayElement GEMS = unit(User::getGems, UnitFormat.UNIT_GEM, "Gems");
  ProfileDisplayElement VOTES = unit(User::getTotalVotes, UnitFormat.UNIT_VOTE, "Votes");

  ProfileDisplayElement PLAYTIME = new ProfileDisplayElement() {
    @Override
    public void write(TextWriter writer, User user, DisplayContext context) {
      int playtime = user.getPlayTime();
      long hours = TimeUnit.SECONDS.toHours(playtime);
      writer.field("Playtime", UnitFormat.unit(hours, UnitFormat.UNIT_HOUR));
    }

    @Override
    public FieldPlacement placement() {
      return FieldPlacement.ALL;
    }
  };

  ProfileDisplayElement PROFILE_PRIVATE_STATE = new ProfileDisplayElement() {
    @Override
    public void write(TextWriter writer, User user, DisplayContext context) {
      if (!context.self() && !context.viewerHasPermission(Permissions.PROFILE_BYPASS)) {
        return;
      }

      writer.field("Profile Public", !user.get(Properties.PROFILE_PRIVATE));
    }

    @Override
    public FieldPlacement placement() {
      return FieldPlacement.ALL;
    }
  };

  ProfileDisplayElement LAST_ONLINE = new ProfileDisplayElement() {
    @Override
    public void write(TextWriter writer, User user, DisplayContext context) {
      if (user.isOnline()) {
        return;
      }

      long lastJoin = user.getTime(TimeField.LAST_LOGIN);

      writer.field("Last Online",
          PeriodFormat.between(lastJoin, System.currentTimeMillis())
              .retainBiggest()
      );
    }

    @Override
    public FieldPlacement placement() {
      return FieldPlacement.ALL;
    }
  };

  ProfileDisplayElement IP = (writer, user, context) -> {
    String ip = user.getIp();

    if (Strings.isNullOrEmpty(ip)) {
      return;
    }

    writer.field("IP",
        text(ip)
            .hoverEvent(text("Click to copy"))
            .clickEvent(ClickEvent.copyToClipboard(ip))
    );
  };

  ProfileDisplayElement RETURN_LOCATION = (writer, user, context) -> {
    Location returnLoc = user.getReturnLocation();

    if (returnLoc == null) {
      return;
    }

    writer.formattedField("/back location", "{0, location, -c}", returnLoc);
  };

  ProfileDisplayElement LOCATION = (writer, user, context) -> {
    Location location = user.getLocation();
    if (location == null) {
      return;
    }

    writer.formattedField("Location", "{0, location, -c}", location);
  };

  ProfileDisplayElement SEPARATED_USERS = blockedUsers(UserBlockList::getSeparated, "Separated");

  ProfileDisplayElement IGNORED_USERS = blockedUsers(UserBlockList::getBlocked, "Blocked");

  static void registerAll(UserNameFactory factory) {
    factory.addProfileField("name", 0, REAL_NAME);
    factory.addProfileField("privacy_state", 10, PROFILE_PRIVATE_STATE);
    factory.addProfileField("last_online", 20, LAST_ONLINE);
    factory.addProfileField("first_join", 30, FIRST_JOIN);

    factory.addProfileField("rhines", 40, RHINES);
    factory.addProfileField("gems", 50, GEMS);
    factory.addProfileField("playtime", 60, PLAYTIME);
    factory.addProfileField("votes", 70, VOTES);

    factory.addProfileField("uuid", Integer.MAX_VALUE, UUID);

    factory.addAdminProfileField("ip", 10, IP);
    factory.addAdminProfileField("return_location", 20, RETURN_LOCATION);
    factory.addAdminProfileField("location", 30, LOCATION);
    factory.addAdminProfileField("separated", 40, SEPARATED_USERS);
    factory.addAdminProfileField("blocked", 50, IGNORED_USERS);
  }

  static ProfileDisplayElement blockedUsers(
      Function<UserBlockList, Collection<java.util.UUID>> getter,
      String name
  ) {
    return new ProfileDisplayElement() {
      @Override
      public void write(TextWriter writer, User user, DisplayContext context) {
        UserBlockList blockList = user.getComponent(UserBlockList.class);
        var separated = getter.apply(blockList);

        if (separated.isEmpty()) {
          return;
        }

        var blocked = CoreMessages.joinIds(separated, text(name + ": "), context.viewer());

        writer.field(name, text("[Hover to see]").hoverEvent(blocked));
      }

      @Override
      public FieldPlacement placement() {
        return FieldPlacement.IN_PROFILE;
      }
    };
  }

  static ProfileDisplayElement timestamp(String name, TimeField field) {
    return (writer, user, context) -> {
      long value = user.getTime(field);

      if (value == -1) {
        return;
      }

      writer.formattedField(name, "{0, date}", value);
    };
  }

  static ProfileDisplayElement unit(ToIntFunction<User> getter, String unit, String name) {
    return new ProfileDisplayElement() {
      @Override
      public void write(TextWriter writer, User user, DisplayContext context) {

        int value = getter.applyAsInt(user);

        if (value == 0) {
          return;
        }

        Component text = UnitFormat.unit(value, unit);

        writer.field(name, text);
      }

      @Override
      public FieldPlacement placement() {
        return FieldPlacement.ALL;
      }
    };
  }
}
