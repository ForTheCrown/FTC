package net.forthecrown.user.property;

import dev.geco.gsit.api.GSitAPI;
import net.forthecrown.core.DynmapUtil;
import net.forthecrown.core.FtcDynmap;
import net.forthecrown.user.User;
import net.forthecrown.utils.Util;

public interface UserPreference {
  UserPreference PLAYER_RIDING = new UserPreference() {
    @Override
    public boolean getState(User user) {
      if (!Util.isPluginEnabled("GSit")) {
        return false;
      }

      return GSitAPI.canPlayerSit(user.getPlayer());
    }

    @Override
    public void setState(User user, boolean state) {
      if (!Util.isPluginEnabled("GSit")) {
        return;
      }

      GSitAPI.setCanPlayerSit(user.getPlayer(), state);
    }
  };

  UserPreference DYNMAP_HIDE = new UserPreference() {
    @Override
    public boolean getState(User user) {
      if (!DynmapUtil.isInstalled()) {
        return false;
      }

      return !FtcDynmap.getDynmap()
          .getPlayerVisbility(user.getName());
    }

    @Override
    public void setState(User user, boolean state) {
      if (DynmapUtil.isInstalled())  {
        return;
      }

      FtcDynmap.getDynmap()
          .setPlayerVisiblity(user.getName(), !state);
    }
  };

  boolean getState(User user);
  void setState(User user, boolean state);
}