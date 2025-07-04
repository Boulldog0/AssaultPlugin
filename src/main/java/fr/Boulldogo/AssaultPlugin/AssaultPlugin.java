package fr.Boulldogo.AssaultPlugin;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;

import fr.Boulldogo.AssaultPlugin.Commands.AssaultCommand;
import fr.Boulldogo.AssaultPlugin.Commands.SubcommandManager;
import fr.Boulldogo.AssaultPlugin.Listeners.AssaultListener;
import fr.Boulldogo.AssaultPlugin.Listeners.FactionListener;
import fr.Boulldogo.AssaultPlugin.Listeners.InteractListener;
import fr.Boulldogo.AssaultPlugin.Listeners.PlayerListener;
import fr.Boulldogo.AssaultPlugin.Utils.AssaultManager;
import fr.Boulldogo.AssaultPlugin.Utils.GithubVersion;
import fr.Boulldogo.AssaultPlugin.Utils.YamlUpdater;

public class AssaultPlugin extends JavaPlugin {
	
	public BukkitRunnable verification = null;
	public BukkitRunnable weeklyVerif = null;
	private static AssaultPlugin plugin;
	public static String V = "";
	
	public void onEnable() {
        Server server = getServer();
        Pattern pattern = Pattern.compile("(^[^\\-]*)");
        Matcher matcher = pattern.matcher(server.getBukkitVersion());
        if(!matcher.find()) {
            this.getLogger().severe("Could not find Bukkit version... Disabling plugin...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
        File file = new File(this.getDataFolder(), "config.yml");
        if(!file.exists()) {	
        	this.getLogger().info("Creating new config.yml ! The server will restart to apply the changes. Consider configuring the configuration");
    		saveDefaultConfig();
    		Bukkit.getServer().spigot().restart();
        }
        
	    new Metrics(this, 22503);
        
		saveDefaultConfig();
		mergeConfigDefaults();
		
		if(this.getConfig().contains("assault")) {
			this.getConfig().set("assault", null);
		}
		
		YamlUpdater updater = new YamlUpdater(this);
		updater.updateYamlFiles(new String[] {"config.yml"});
		
		String version = this.getDescription().getVersion();
		AssaultPlugin.V = version;
		
        GithubVersion versionChecker = new GithubVersion(this, version, "https://api.github.com/repos/Boulldog0/AssaultPlugin/releases/latest");
        versionChecker.checkVersion();
		
		AssaultPlugin.plugin = this;
		startCooldownVerification();
		
		this.getCommand("assault").setExecutor(new AssaultCommand(this));
		this.getServer().getPluginManager().registerEvents(new AssaultListener(this), this);
		this.getServer().getPluginManager().registerEvents(new InteractListener(this), this);
		this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		this.getServer().getPluginManager().registerEvents(new FactionListener(this), this);
		AssaultManager.startCounter();
		SubcommandManager.registerSubcommands();
		if(this.getConfig().getBoolean("capturable-zone.enable-zones")) {
			AssaultManager.startZoneCounter();
		}
		this.getLogger().info("Plugin assault version " + this.getDescription().getVersion()  + " by Boulldogo loaded with success !");
	}
	
	public void onDisable() {
		this.getLogger().info("Plugin assault version " + this.getDescription().getVersion()  + " by Boulldogo unloaded with success !");
	}
	
	public void startCooldownVerification() {
	    new BukkitRunnable() {

	        @Override
	        public void run() {
	            boolean hasChanges = false;

	            for (Faction faction : Factions.getInstance().getAllFactions()) {
	                String factionTag = faction.getTag();
	                if(plugin.getConfig().contains("cooldowns." + factionTag)) {
	                    for (Faction targetFaction : Factions.getInstance().getAllFactions()) {
	                        String targetTag = targetFaction.getTag();
	                        String path = "cooldowns." + factionTag + "." + targetTag;

	                        if(plugin.getConfig().contains(path)) {
	                            int cooldown = plugin.getConfig().getInt(path);
	                            if(cooldown - 1 <= 0) {
	                                plugin.getConfig().set(path, null);
	                            } else {
	                                plugin.getConfig().set(path, cooldown - 1);
	                            }
	                            hasChanges = true;
	                        }
	                    }
	                }
	            }

	            if(hasChanges) {
	                plugin.saveConfig();
	            }
	        }
	    }.runTaskTimer(plugin, 0, 1200);
	    
	    new BukkitRunnable() {
	        @Override
	        public void run() {
	            Calendar calendar = Calendar.getInstance();
	            if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY &&
	                calendar.get(Calendar.HOUR_OF_DAY) == 0 &&
	                calendar.get(Calendar.MINUTE) == 0) {
	            	
	            	for(Faction faction : Factions.getInstance().getAllFactions()) {
	            		if(plugin.getConfig().contains("stats." + faction.getTag() + ".total_weekly")) {
	            			plugin.getConfig().set("stats." + faction.getTag() + ".total_weekly", null);
	            		}
	            	}
	            	
	            	plugin.saveConfig();
	            }
	        }
	    }.runTaskTimer(plugin, 0, 1200);
	}
	
    private void mergeConfigDefaults() {
        InputStream defaultConfigStream = this.getResource("config.yml");
        if(defaultConfigStream == null) {
            return;
        }

        FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream));
        FileConfiguration currentConfig = this.getConfig();

        mergeConfigurations(currentConfig, defaultConfig);
        this.saveConfig();
    }

    private void mergeConfigurations(FileConfiguration currentConfig, FileConfiguration defaultConfig) {
        for(String key : defaultConfig.getKeys(true)) {
            if(!currentConfig.contains(key)) {
                currentConfig.set(key, defaultConfig.get(key));
            }
        }
    }

    public static AssaultPlugin getInstance() {
    	return plugin;
    }
}
