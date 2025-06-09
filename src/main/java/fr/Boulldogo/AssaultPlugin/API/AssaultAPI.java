package fr.Boulldogo.AssaultPlugin.API;

import com.massivecraft.factions.Faction;

import fr.Boulldogo.AssaultPlugin.AssaultPlugin;
import fr.Boulldogo.AssaultPlugin.Utils.Assault;
import fr.Boulldogo.AssaultPlugin.Utils.AssaultManager;

public class AssaultAPI {
    
    private static AssaultAPI instance;
    private final AssaultPlugin plugin;

    public AssaultAPI(AssaultPlugin plugin) {
        this.plugin = plugin;
    }

    public static void init(AssaultPlugin plugin) {
        if(instance == null) {
            instance = new AssaultAPI(plugin);
        }
    }

    public static AssaultAPI getInstance() {
        if(instance == null) {
            throw new IllegalStateException("API not initialized. Call init(AssaultPlugin plugin) first.");
        }
        return instance;
    }

    public int getFactionPoints(String facName) {
        if(!plugin.getConfig().contains("ranking." + facName + ".points")) {
            return 0;
        }
        return plugin.getConfig().getInt("ranking." + facName + ".points");
    }

    public int getFactionWins(String facName) {
        if(!plugin.getConfig().contains("ranking." + facName + ".win")) {
            return 0;
        }
        return plugin.getConfig().getInt("ranking." + facName + ".win");
    }

    public int getFactionLooses(String facName) {
        if(!plugin.getConfig().contains("ranking." + facName + ".loose")) {
            return 0;
        }
        return plugin.getConfig().getInt("ranking." + facName + ".loose");
    }

    public int getFactionTotalAssault(String facName) {
        if(!plugin.getConfig().contains("stats." + facName + ".total_assaults")) {
            return 0;
        }
        return plugin.getConfig().getInt("stats." + facName + ".total_assaults");
    }

    public int getFactionWeeklyAssault(String facName) {
        if(!plugin.getConfig().contains("stats." + facName + ".total_weekly")) {
            return 0;
        }
        return plugin.getConfig().getInt("stats." + facName + ".total_weekly");
    }

    public void setFactionPoints(String facName, int points) {
        plugin.getConfig().set("ranking." + facName + ".points", points);
        plugin.saveConfig();
    }

    public void setFactionWins(String facName, int win) {
        plugin.getConfig().set("ranking." + facName + ".win", win);
        plugin.saveConfig();
    }

    public void setFactionLooses(String facName, int looses) {
        plugin.getConfig().set("ranking." + facName + ".loose", looses);
        plugin.saveConfig();
    }
    
    public void removeFaction(String facName) {
        plugin.getConfig().set("ranking." + facName, null);
        plugin.saveConfig();
    }
    
    public boolean isInAssault(Faction faction) {
    	return AssaultManager.isFactionInAssaultOrJoinAssault(faction);
    }
    
    public boolean isDirectAssaultFaction(Faction faction) {
    	return AssaultManager.isFactionInAssault(faction);
    }
    
    public Assault getFactionAssault(Faction faction) {
    	return AssaultManager.getFactionAssault(faction);
    }
    
    public boolean isTheSameAssault(Faction faction, Faction faction2) {
    	if(!AssaultManager.isFactionInAssault(faction2) || !AssaultManager.isFactionInAssault(faction)) return false;
    	if(faction.equals(faction2)) return true;
    	Assault facAssault = AssaultManager.getFactionAssault(faction);
    	
    	if(facAssault.belligerentAttackFaction.equals(faction2)
    	|| facAssault.belligerentDefenseFaction.equals(faction2)) {
    		return true;
    	}
    	
    	for(Faction f : facAssault.attackJoins) {
    		if(f.equals(faction2)) {
    			return true;
    		}
    	}
    	for(Faction f : facAssault.defenseJoins) {
    		if(f.equals(faction2)) {
    			return true;
    		}
    	}
    	return false;
    }
}
