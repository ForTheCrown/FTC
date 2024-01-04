package net.forthecrown.mail;

import lombok.Getter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
@Getter
public class MailConfig {

  private boolean discordForwardingAllowed = true;
}
