package fr.Boulldogo.AssaultPlugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin {
	
	public BukkitRunnable verification = null;
	public Main plugin;
	
	public void onEnable() {
		saveDefaultConfig();
		
		if(this.getConfig().contains("assault")) {
			this.getConfig().set("assault", null);
		}
		
        GithubVersion versionChecker = new GithubVersion(this, "1.0.0", "https://api.github.com/repos/Boulldog0/AssaultPlugin/releases/latest");
        versionChecker.checkVersion();
		
		this.plugin = this;
		startCooldownVerification();
		
		this.getCommand("assault").setExecutor(new AssaultCommand(this));
		this.getServer().getPluginManager().registerEvents(new AssaultListener(this), this);
		this.getLogger().info("Plugin assault version 1.0.0 by Boulldogo loaded with success !");
	}
	
	public void onDisable() {
		this.getLogger().info("Plugin assault version 1.0.0 by Boulldogo unloaded with success !");
	}
	
	public void startCooldownVerification() {
	    verification = new BukkitRunnable() {

	        @Override
	        public void run() {
	            boolean hasChanges = false;

	            for (Faction faction : Factions.getInstance().getAllFactions()) {
	                String factionTag = faction.getTag();
	                if (plugin.getConfig().contains("cooldowns." + factionTag)) {
	                    for (Faction targetFaction : Factions.getInstance().getAllFactions()) {
	                        String targetTag = targetFaction.getTag();
	                        String path = "cooldowns." + factionTag + "." + targetTag;

	                        if (plugin.getConfig().contains(path)) {
	                            int cooldown = plugin.getConfig().getInt(path);
	                            if (cooldown - 1 <= 0) {
	                                plugin.getConfig().set(path, null);
	                            } else {
	                                plugin.getConfig().set(path, cooldown - 1);
	                            }
	                            hasChanges = true;
	                        }
	                    }
	                }
	            }

	            if (hasChanges) {
	                plugin.saveConfig();
	            }
	        }
	    };
	    verification.runTaskTimer(plugin, 0, 1200);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		
		if(player.hasPermission("assault.update") && GithubVersion.newVersion) {
			player.sendMessage(ChatColor.RED + "A new version of Assault Plugin is avaiable !");
			player.sendMessage(ChatColor.RED + "Download it at : https://www.spigotmc.org/resources/assaultplugin.116864/");
		}
	}


}
