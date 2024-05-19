package fr.Boulldogo.AssaultPlugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;

public class Main extends JavaPlugin {
	
	public BukkitRunnable verification = null;
	public Main plugin;
	
	public void onEnable() {
		saveDefaultConfig();
		
		if(this.getConfig().contains("assault")) {
			this.getConfig().set("assault", null);
		}
		
        GithubVersion updateChecker = new GithubVersion(this, "Boulldogo", "AssaultPlugin");
        updateChecker.checkForUpdates();
		
		this.plugin = this;
		startCooldownVerification();
		
		this.getCommand("assault").setExecutor(new AssaultCommand(this));
		this.getServer().getPluginManager().registerEvents(new AssaultListener(this), this);
		this.getServer().getPluginManager().registerEvents(new GithubVersion(this, "Boulldogo", "AssaultPlugin"), this);
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


}
