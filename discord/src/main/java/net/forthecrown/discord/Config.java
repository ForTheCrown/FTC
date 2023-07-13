package net.forthecrown.discord;

import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@Getter
@ConfigSerializable
class Config {

  private boolean forwardingEnabled = true;
  private String forwarderLevel = "ERROR";
  private String forwardingChannel = "staff-log";
}