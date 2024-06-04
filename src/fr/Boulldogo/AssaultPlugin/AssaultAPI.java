package fr.Boulldogo.AssaultPlugin;

public class AssaultAPI {
	
	public final Main plugin; 
	
	public AssaultAPI(Main plugin) {
		this.plugin = plugin;
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

}
