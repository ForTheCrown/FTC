package net.forthecrown.challenges;

import static net.forthecrown.Permissions.register;

import org.bukkit.permissions.Permission;

public interface ChallengePermissions {
  Permission CHALLENGES              = register("ftc.challenges");
  Permission CHALLENGES_ADMIN        = register(CHALLENGES, "admin");
}
