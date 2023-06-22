package net.forthecrown.discord;

import lombok.Getter;

@Getter
class Config {

  private boolean forwardingEnabled = true;
  private String forwarderLevel = "ERROR";
  private String forwardingChannel = "staff-log";
}