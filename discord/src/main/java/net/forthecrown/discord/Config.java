package net.forthecrown.discord;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@Getter
@Accessors(fluent = true)
@ConfigSerializable
public class Config {

  private boolean forwardingEnabled = true;
  private String forwarderLevel = "ERROR";
  private String forwardingChannel = "staff-log";

  private long updateChannelId = 0;
  private boolean forwardDiscordAnnouncementsToServer = true;
}