package fr.Boulldogo.AssaultPlugin.API;

import com.massivecraft.factions.Faction;

import fr.Boulldogo.AssaultPlugin.Main;
import fr.Boulldogo.AssaultPlugin.Commands.AssaultCommand;

public class AssaultAPI {
    
    private static AssaultAPI instance;
    private final Main plugin;

    public AssaultAPI(Main plugin) {
        this.plugin = plugin;
    }

    public static void init(Main plugin) {
        if(instance == null) {
            instance = new AssaultAPI(plugin);
        }
    }

    public static AssaultAPI getInstance() {
        if(instance == null) {
            throw new IllegalStateException("API not initialized. Call init(Main plugin) first.");
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
    	if(AssaultCommand.attackAssaultList.isEmpty()) return false;
    	return AssaultCommand.attackAssaultList.contains(faction)
    		|| AssaultCommand.defenseAssaultList.contains(faction)
    		|| AssaultCommand.attackJoinList.contains(faction)
    		|| AssaultCommand.defenseJoinList.contains(faction);
    }
    
    public boolean isDirectAssaultFaction(Faction faction) {
    	if(AssaultCommand.attackAssaultList.isEmpty()) return false;
    	return AssaultCommand.attackAssaultList.contains(faction)
    		|| AssaultCommand.defenseAssaultList.contains(faction);
    }
    
    public boolean isTheSameAssault(Faction faction, Faction faction2) {
    	if(AssaultCommand.attackAssaultList.isEmpty()) return false;
    	if(!isDirectAssaultFaction(faction)) return false;
    	if(!isDirectAssaultFaction(faction2)) return false;
    	boolean isAttack = AssaultCommand.attackAssaultList.contains(faction);
    	
    	int index = 0;
    	if(isAttack) {
    		index = AssaultCommand.attackAssaultList.lastIndexOf(faction);
    	} else {
    		index = AssaultCommand.defenseAssaultList.lastIndexOf(faction);
    	}
    	
    	if(isAttack) {
    		return AssaultCommand.defenseAssaultList.get(index) == faction2;
    	} else {
    		return AssaultCommand.attackAssaultList.get(index) == faction2;
    	}
    }
}
