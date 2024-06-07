package fr.Boulldogo.AssaultPlugin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.event.FPlayerJoinEvent;
import com.massivecraft.factions.event.FactionDisbandEvent;
import com.massivecraft.factions.event.FactionRelationWishEvent;

public class AssaultListener implements Listener {
	
	private final Main plugin;
	
    public AssaultListener(Main plugin) {
        this.plugin = plugin;
    }
    
    public static List<Integer> attackScoreList = new ArrayList<>();
    public static List<Integer> defenseScoreList = new ArrayList<>();
	
    @SuppressWarnings("unlikely-arg-type")
	@EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        Faction playerFac = FPlayers.getInstance().getByPlayer(player).getFaction();
        if (!playerFac.isWilderness()) {
            int index = 0;
            if (!AssaultCommand.attackAssaultList.isEmpty() && AssaultCommand.attackAssaultList.contains(playerFac)) {
                index = AssaultCommand.attackAssaultList.indexOf(playerFac);
                int scoreToAdd = plugin.getConfig().getInt("point-per-kill");
                defenseScoreList.set(index, defenseScoreList.get(index) + scoreToAdd);
            } else if (!AssaultCommand.defenseAssaultList.isEmpty() && AssaultCommand.defenseAssaultList.contains(playerFac)) {
                index = AssaultCommand.defenseAssaultList.indexOf(playerFac);
                int scoreToAdd = plugin.getConfig().getInt("point-per-kill");
                attackScoreList.set(index, attackScoreList.get(index) + scoreToAdd);
            } else if(!AssaultCommand.attackJoinList.isEmpty() && AssaultCommand.attackJoinList.contains(playerFac)) {
				for(int i = 0; i < AssaultCommand.attackAssaultList.size(); i++) {
					if(plugin.getConfig().getStringList("assault.join.attack." + AssaultCommand.attackAssaultList.get(i).getTag()).contains(playerFac)) {
						index = AssaultCommand.attackAssaultList.lastIndexOf(AssaultCommand.attackAssaultList.get(i));
					}
				}
                int scoreToAdd = plugin.getConfig().getInt("point-per-kill");
                defenseScoreList.set(index, defenseScoreList.get(index) + scoreToAdd);
			} else if(!AssaultCommand.defenseJoinList.isEmpty() && AssaultCommand.defenseJoinList.contains(playerFac)) {
				for(int i = 0; i < AssaultCommand.defenseAssaultList.size(); i++) {
					if(plugin.getConfig().getStringList("assault.join.defense." + AssaultCommand.defenseAssaultList.get(i).getTag()).contains(playerFac)) {
						index = AssaultCommand.defenseAssaultList.lastIndexOf(AssaultCommand.defenseAssaultList.get(i));
					}
				}
                int scoreToAdd = plugin.getConfig().getInt("point-per-kill");
                defenseScoreList.set(index, attackScoreList.get(index) + scoreToAdd);
			}
            updateScoreboard(index);
        }
    }
	
	@SuppressWarnings("unlikely-arg-type")
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		Faction playerFac = FPlayers.getInstance().getByPlayer(player).getFaction();
		if(!playerFac.isWilderness()) {
			if(!AssaultCommand.attackAssaultList.isEmpty() && AssaultCommand.attackAssaultList.contains(playerFac)) {
				int index = AssaultCommand.attackAssaultList.lastIndexOf(playerFac);
				createScoreboard(player, index, false, true);
			} else if(!AssaultCommand.defenseAssaultList.isEmpty() && AssaultCommand.defenseAssaultList.contains(playerFac)) {
				int index = AssaultCommand.defenseAssaultList.lastIndexOf(playerFac);
				createScoreboard(player, index, false, false);
			} else if(!AssaultCommand.attackJoinList.isEmpty() && AssaultCommand.attackJoinList.contains(playerFac)) {
				int index = 0;
				for(int i = 0; i < AssaultCommand.attackAssaultList.size(); i++) {
					if(plugin.getConfig().getStringList("assault.join.attack." + AssaultCommand.attackAssaultList.get(i).getTag()).contains(playerFac)) {
						index = AssaultCommand.attackAssaultList.lastIndexOf(AssaultCommand.attackAssaultList.get(i));
					}
				}
				createScoreboard(player, index, true, true);
			} else if(!AssaultCommand.defenseJoinList.isEmpty() && AssaultCommand.defenseJoinList.contains(playerFac)) {
				int index = 0;
				for(int i = 0; i < AssaultCommand.defenseAssaultList.size(); i++) {
					if(plugin.getConfig().getStringList("assault.join.defense." + AssaultCommand.defenseAssaultList.get(i).getTag()).contains(playerFac)) {
						index = AssaultCommand.defenseAssaultList.lastIndexOf(AssaultCommand.defenseAssaultList.get(i));
					}
				}
				createScoreboard(player, index, true, true);
			}
		}
		
		if(player.hasPermission("assault.update")) {
			if(plugin.getConfig().get("latest-version") != Main.V) {
				player.sendMessage(ChatColor.RED + "A new version of plugin Assault is avaiable !");
				player.sendMessage(ChatColor.RED + "Download it at : https://www.spigotmc.org/resources/assaultplugin.116864/");
			}
		}
	}
	
	@SuppressWarnings("unlikely-arg-type")
	@EventHandler
	public void onPlayerJoinFac(FPlayerJoinEvent e) {
		Player player = e.getfPlayer().getPlayer();
		Faction playerFac = FPlayers.getInstance().getByPlayer(player).getFaction();
		if(plugin.getConfig().getBoolean("allow_assault_join")) {
			if(!playerFac.isWilderness()) {
				if(!AssaultCommand.attackAssaultList.isEmpty() && AssaultCommand.attackAssaultList.contains(e.getFaction())) {
					int index = AssaultCommand.attackAssaultList.lastIndexOf(playerFac);
					createScoreboard(player, index, false, true);
				} else if(!AssaultCommand.defenseAssaultList.isEmpty() && AssaultCommand.defenseAssaultList.contains(e.getFaction())) {
					int index = AssaultCommand.attackAssaultList.lastIndexOf(playerFac);
					createScoreboard(player, index, false, false);
				} else if(!AssaultCommand.attackJoinList.isEmpty() && AssaultCommand.attackJoinList.contains(e.getFaction())) {
					int index = 0;
					for(int i = 0; i < AssaultCommand.attackAssaultList.size(); i++) {
						if(plugin.getConfig().getStringList("assault.join.attack." + AssaultCommand.attackAssaultList.get(i).getTag()).contains(e.getFaction())) {
							index = AssaultCommand.attackAssaultList.lastIndexOf(AssaultCommand.attackAssaultList.get(i));
						}
					}
					createScoreboard(player, index, true, true);
				} else if(!AssaultCommand.defenseJoinList.isEmpty() && AssaultCommand.defenseJoinList.contains(e.getFaction())) {
					int index = 0;
					for(int i = 0; i < AssaultCommand.defenseAssaultList.size(); i++) {
						if(plugin.getConfig().getStringList("assault.join.defense." + AssaultCommand.defenseAssaultList.get(i).getTag()).contains(e.getFaction())) {
							index = AssaultCommand.defenseAssaultList.lastIndexOf(AssaultCommand.defenseAssaultList.get(i));
						}
					}
					createScoreboard(player, index, true, false);
				}
			}
		} else {
	        String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
			e.setCancelled(true);
			player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_cant_join_faction_in_assault")));
		}
	}
	
	@EventHandler
	public void onFactionDisband(FactionDisbandEvent e) {
		String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
		Faction faction = e.getFaction();
		
		if(AssaultCommand.attackAssaultList.contains(faction)
		|| AssaultCommand.defenseAssaultList.contains(faction)
		|| AssaultCommand.attackJoinList.contains(faction)
		|| AssaultCommand.defenseJoinList.contains(faction)) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_cant_disband")));
		}
	}
	
	@EventHandler
	public void onFactionChangeRelation(FactionRelationWishEvent e) {
		String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
		Player player = e.getfPlayer().getPlayer();
		if(plugin.getConfig().getBoolean("use-countdown-non-assault-after-enemy")) {
			if(!plugin.getConfig().getBoolean("disable-direct-enemy-system") && e.getTargetRelation().isEnemy()) {
				int cd = plugin.getConfig().getInt("countdown-non-assault-after-enemy");
				Faction playerFac = e.getFaction();
				Faction faction = e.getTargetFaction();
					
				if(plugin.getConfig().contains("cooldown." + playerFac + "." + faction)) {
					int cooldown = plugin.getConfig().getInt("cooldown." + playerFac + "." + faction);
					if(cooldown <= cd) {
						plugin.getConfig().set("cooldown." + playerFac + "." + faction, cd);
						plugin.saveConfig();
					}
				} else {
					plugin.getConfig().set("cooldown." + playerFac + "." + faction, cd);
					plugin.saveConfig();
				}
					
				if(plugin.getConfig().contains("cooldown." + faction + "." + playerFac)) {
					int cooldown = plugin.getConfig().getInt("cooldown." + faction + "." + playerFac);
					if(cooldown <= cd) {
						plugin.getConfig().set("cooldown." + faction + "." + playerFac, cd);
						plugin.saveConfig();
					}
				} else {
					plugin.getConfig().set("cooldown." + faction + "." + playerFac, cd);
					plugin.saveConfig();
				}
			} else if(e.getTargetRelation().isEnemy() && plugin.getConfig().getBoolean("disable-direct-enemy-system")) {
				if(!player.hasPermission("assault.use_enemy")) {
					player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_cant_enemy")));
					e.setCancelled(true);
				} else {
					int cd = plugin.getConfig().getInt("countdown-non-assault-after-enemy");
					Faction playerFac = e.getFaction();
					Faction faction = e.getTargetFaction();
					
					if(plugin.getConfig().contains("cooldown." + playerFac + "." + faction)) {
						int cooldown = plugin.getConfig().getInt("cooldown." + playerFac + "." + faction);
						if(cooldown <= cd) {
							plugin.getConfig().set("cooldown." + playerFac + "." + faction, cd);
							plugin.saveConfig();
						}
					} else {
						plugin.getConfig().set("cooldown." + playerFac + "." + faction, cd);
						plugin.saveConfig();
					}
					
					if(plugin.getConfig().contains("cooldown." + faction + "." + playerFac)) {
						int cooldown = plugin.getConfig().getInt("cooldown." + faction + "." + playerFac);
						if(cooldown <= cd) {
							plugin.getConfig().set("cooldown." + faction + "." + playerFac, cd);
							plugin.saveConfig();
						}
					} else {
						plugin.getConfig().set("cooldown." + faction + "." + playerFac, cd);
						plugin.saveConfig();
					}
				}
			}
		}
	}
	
	public void updateScoreboard(int index) {
	    Faction attackFac = AssaultCommand.attackAssaultList.get(index);
	    Faction defenseFac = AssaultCommand.defenseAssaultList.get(index);

	    String attackS = ChatColor.DARK_RED + "   • " + attackFac.getTag() + " : ";
	    String defenseS = ChatColor.GOLD + "   • " + defenseFac.getTag() + " : ";

        for (Player player : attackFac.getOnlinePlayers()) {
            Scoreboard board = player.getScoreboard();
            if (board == null) continue;

            Objective objective = board.getObjective("assault");
            if (objective == null) continue;

            for (String entry : board.getEntries()) {
                if (entry.startsWith(attackS)) {
                    board.resetScores(entry);
                    break;
                }
            }
            
            objective.getScore(attackS + attackScoreList.get(index) + " points").setScore(8);
            
            for (String entry : board.getEntries()) {
                if (entry.startsWith(defenseS)) {
                    board.resetScores(entry);
                    break;
                }
            }
            
            objective.getScore(defenseS + defenseScoreList.get(index) + " points").setScore(7);
        }
        
        for (Player player : defenseFac.getOnlinePlayers()) {
            Scoreboard board = player.getScoreboard();
            if (board == null) continue;

            Objective objective = board.getObjective("assault");
            if (objective == null) continue;

            for (String entry : board.getEntries()) {
                if (entry.startsWith(attackS)) {
                    board.resetScores(entry);
                    break;
                }
            }
            
            objective.getScore(attackS + attackScoreList.get(index) + " points").setScore(8);
            
            for (String entry : board.getEntries()) {
                if (entry.startsWith(defenseS)) {
                    board.resetScores(entry);
                    break;
                }
            }
            
            objective.getScore(defenseS + defenseScoreList.get(index) + " points").setScore(7);
        }
        
    	if(plugin.getConfig().contains("assault.join.attack." + attackFac.getTag())) {
    		List<String> facName = plugin.getConfig().getStringList("assault.join.attack." + attackFac.getTag());
    		for(int i = 0; i < facName.size(); i++) {
    			Faction faction = Factions.getInstance().getByTag(facName.get(i));
		        for (Player player : faction.getOnlinePlayers()) {
		            Scoreboard board = player.getScoreboard();
		            if (board == null) continue;

		            Objective objective = board.getObjective("assault");
		            if (objective == null) continue;

		            for (String entry : board.getEntries()) {
		                if (entry.startsWith(attackS)) {
		                    board.resetScores(entry);
		                    break;
		                }
		            }
		            
		            objective.getScore(attackS + attackScoreList.get(index) + " points").setScore(8);
		            
		            for (String entry : board.getEntries()) {
		                if (entry.startsWith(defenseS)) {
		                    board.resetScores(entry);
		                    break;
		                }
		            }
		            
		            objective.getScore(defenseS + defenseScoreList.get(index) + " points").setScore(7);
		        }
    		}
        }
    	
    	if(plugin.getConfig().contains("assault.join.defense." + defenseFac.getTag())) {
    		List<String> facName = plugin.getConfig().getStringList("assault.join.defense." + attackFac.getTag());
    		for(int i = 0; i < facName.size(); i++) {
    			Faction faction = Factions.getInstance().getByTag(facName.get(i));
		        for (Player player : faction.getOnlinePlayers()) {
		            Scoreboard board = player.getScoreboard();
		            if (board == null) continue;

		            Objective objective = board.getObjective("assault");
		            if (objective == null) continue;

		            for (String entry : board.getEntries()) {
		                if (entry.startsWith(attackS)) {
		                    board.resetScores(entry);
		                    break;
		                }
		            }
		            
		            objective.getScore(attackS + attackScoreList.get(index) + " points").setScore(8);
		            
		            for (String entry : board.getEntries()) {
		                if (entry.startsWith(defenseS)) {
		                    board.resetScores(entry);
		                    break;
		                }
		            }
		            
		            objective.getScore(defenseS + defenseScoreList.get(index) + " points").setScore(7);
		        }
    		}
    	}
	}

    public void createScoreboard(Player player, int index, boolean isAlly, boolean isAttack) {
    	Faction attackFac = AssaultCommand.attackAssaultList.get(index);
    	Faction defenseFac = AssaultCommand.defenseAssaultList.get(index);
    	
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        Objective objective = board.registerNewObjective("assault", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        String displayName = ChatColor.RED + "⚔ Assault " + attackFac.getTag() + " VS " + defenseFac.getTag() + " ⚔";
        if(displayName.length() <= 32) {
           objective.setDisplayName(ChatColor.RED + "⚔ Assault " + attackFac.getTag() + " VS " + defenseFac.getTag() + " ⚔");
        } else {
            objective.setDisplayName(displayName.substring(0, 32));
        }

        Score space1 = objective.getScore("");
        space1.setScore(11);
        
        Score scoreString = objective.getScore(ChatColor.DARK_GRAY + "• Scores :");
        scoreString.setScore(10);
        
        Score space2 = objective.getScore(" ");
        space2.setScore(9);

        Score attackScore = objective.getScore(ChatColor.DARK_RED + "   • " + attackFac.getTag() + " : " + 0 + " points");
        attackScore.setScore(8);

        Score defenseScore = objective.getScore(ChatColor.GOLD + "   • " + defenseFac.getTag() + " : " + 0 + " points");
        defenseScore.setScore(7);

        Score space3 = objective.getScore(ChatColor.GRAY + "   ");
        space3.setScore(6);

        Score allyScore = objective.getScore(ChatColor.GREEN + "• Alliés Attaque: +" + 0);
        allyScore.setScore(5);
        
        Score eAllyScore = objective.getScore(ChatColor.RED + "• Alliés Défense: +" + 0);
        eAllyScore.setScore(4);
        
        String startTime = plugin.getConfig().getString("assault.start_time." + attackFac.getTag());
        
        Score space4 = objective.getScore(ChatColor.GRAY + "    ");
        space4.setScore(1);

        Score startTimeScore = objective.getScore(ChatColor.YELLOW + "• Lancé à: " + startTime);
        startTimeScore.setScore(0);

        player.setScoreboard(board);
    }
    
    public String translateString(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

}
