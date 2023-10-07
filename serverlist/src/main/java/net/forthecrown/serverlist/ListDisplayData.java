package net.forthecrown.serverlist;

import com.google.common.base.Strings;
import java.util.Random;
import net.kyori.adventure.text.Component;
import org.bukkit.util.CachedServerIcon;

class ListDisplayData {

  Component motdPart;
  CachedServerIcon icon;
  String versionText;
  int protocolOverride;

  public void fillValues(DisplayEntry entry, Random random) {
    if (Strings.isNullOrEmpty(versionText)) {
      versionText = entry.getVersionText();
    }

    if (motdPart == null) {
      motdPart = entry.getMotdPart();
    }

    if (icon == null) {
      icon = entry.get(random);
    }

    if (protocolOverride < 1) {
      protocolOverride = entry.getProtocolOverride();
    }
  }
}
