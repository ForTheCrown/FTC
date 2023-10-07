package net.forthecrown.guilds.listeners;

import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import github.scarsz.discordsrv.util.DiscordUtil;
import net.forthecrown.guilds.GuildManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

class ServerLoadListener implements Listener {

  private final GuildManager manager;

  public ServerLoadListener(GuildManager manager) {
    this.manager = manager;
  }

  @EventHandler(ignoreCancelled = true)
  public void onServerLoad(ServerLoadEvent event) {
    JDA jda = DiscordUtil.getJda();
    jda.addEventListener(new GuildBoostListener(manager));
  }
}
