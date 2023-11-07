package net.forthecrown.serverlist;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
@Getter
@Accessors(fluent = true)
public class ServerListConfig {
  private int maxPlayerRandomRange = 5;
  private boolean inferPlayerBasedOffIp = true;
  private boolean allowChangingProtocolVersions = true;
  private boolean allowChangingVersionText = true;
  private boolean appearOffline = false;
  private String baseMotd = "&6&lForTheCrown &7- &e${message}\n&7Currently on ${version}";
}
