package fr.Boulldogo.AssaultPlugin.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;

import fr.Boulldogo.AssaultPlugin.AssaultPlugin;
import fr.Boulldogo.AssaultPlugin.Events.AssaultLooseEvent;
import fr.Boulldogo.AssaultPlugin.Events.AssaultStopEvent;
import fr.Boulldogo.AssaultPlugin.Events.AssaultWinEvent;

public class AssaultManager {
	
	public enum AssaultSide {
		NOASSAULT, ATTACK, DEFENSE;
	}

	public static List<Assault> assaults = new ArrayList<>();

	public static boolean isFactionInAssault(Faction fac) {
		for(Assault assault : assaults) {
			if(assault.belligerentAttackFaction.equals(fac) || assault.belligerentDefenseFaction.equals(fac)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isInSameAssaults(Player player1, Player player2) {
		Faction fPlayer1 = FPlayers.getInstance().getByPlayer(player1).getFaction();
		Faction fPlayer2 = FPlayers.getInstance().getByPlayer(player2).getFaction();
		
		if(!isFactionInAssaultOrJoinAssault(fPlayer1) || !isFactionInAssaultOrJoinAssault(fPlayer2)) return false;
		return getFactionAssault(fPlayer1).equals(getFactionAssault(fPlayer2));
	}
	
	public static boolean isSameAssaultFaction(Faction fac1, Faction fac2) {
		if(!isFactionInAssault(fac1) || !isFactionInAssault(fac2)) return false;
		return getFactionAssault(fac1).equals(getFactionAssault(fac2));
	}

	public static boolean isFactionInAssaultOrJoinAssault(Faction fac) {
		for(Assault assault : assaults) {
			if(assault.belligerentAttackFaction.equals(fac) || assault.belligerentDefenseFaction.equals(fac)) {
				return true;
			}
			for(Faction f : assault.attackJoins) {
				if(f.equals(fac)) {
					return true;
				}
			}
			for(Faction f : assault.defenseJoins) {
				if(f.equals(fac)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isPlayerInAssault(Player player) {
		Faction playerFac = FPlayers.getInstance().getByPlayer(player).getFaction();
		return isFactionInAssaultOrJoinAssault(playerFac);
	}
	
	public static int getAssaultIndex(Assault assault) {
		for(int i = 0; i < assaults.size(); i++) {
			if(assaults.get(i).belligerentAttackFaction.equals(assault.belligerentAttackFaction)) {
				return i;
			}
		}
		return -1;
	}
	
	public static void changeZone(Assault assault, Faction faction, @Nullable Location loc) {
		AssaultPlugin plugin = AssaultPlugin.getInstance();
		String prefix = AssaultPlugin.getInstance().getConfig().getBoolean("use-prefix") ? translateString(AssaultPlugin.getInstance().getConfig().getString("prefix")) : "";
		Location zoneLoc = loc;
		if(zoneLoc == null) {
	        zoneLoc = AssaultManager.getRandomLocationForFaction(faction);
	        if(zoneLoc == null) {
	        	AssaultSide side = getSide(faction);
        		assault.getAllPlayers().forEach(player -> {
        			player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.no-loc-avaiable")));
        		});
	        	if(side.equals(AssaultSide.ATTACK)) {
	        		changeZone(assault, assault.belligerentDefenseFaction, null);
	        	} else {
	        		changeZone(assault, assault.belligerentAttackFaction, null);
	        	}
	        	return;
	        }
		}
        CapturableZone zone = new CapturableZone(zoneLoc, faction, 
        		plugin.getConfig().getInt("capturable-zone.radius"), 
        		plugin.getConfig().getInt("capturable-zone.height"),
        		null);
        int idx = getAssaultIndex(assault);
        if(idx == -1) {
            Bukkit.getLogger().warning("Tried to change zone for an unknown assault.");
            return;
        }
        assaults.get(idx).changeZone(zone);
        
        for(Player p : assault.getAllPlayers()) {
    		String message = plugin.getConfig().getString("messages.zone-location")
        			.replace("%faction", faction.getTag())
        			.replace("%locX", String.valueOf(zoneLoc.getBlockX())
        			.replace("%locZ", String.valueOf(zoneLoc.getBlockZ())));
        	p.sendMessage(prefix + translateString(message));
        }
	}
	
	public static AssaultSide getSide(Faction fac) {
		boolean belligerentFaction = isFactionInAssault(fac);
		Assault assault = getFactionAssault(fac);
		if(assault == null) {
			return AssaultSide.NOASSAULT;
		}
		if(belligerentFaction) {
			if(assault.belligerentAttackFaction.equals(fac)) {
				return AssaultSide.ATTACK;
			}
			return AssaultSide.DEFENSE;
		} else {
			for(Faction f : assault.attackJoins) {
				if(f.equals(fac)) {
					return AssaultSide.ATTACK;
				}
			}
			return AssaultSide.DEFENSE;
		}
	}

	public static Assault getFactionAssault(Faction fac) {
		if(!isFactionInAssault(fac)) return null;
		for(Assault assault : assaults) {
			if(assault.belligerentAttackFaction.equals(fac) || assault.belligerentDefenseFaction.equals(fac)) {
				return assault;
			}
			for(Faction f : assault.attackJoins) {
				if(f.equals(fac)) {
					return assault;
				}
			}
			for(Faction f : assault.defenseJoins) {
				if(f.equals(fac)) {
					return assault;
				}
			}
		}
		return null;
	}

	public static void startCounter() {
		new BukkitRunnable() {
			@Override
			public void run() {
				float midAssault = AssaultPlugin.getInstance().getConfig().getInt("duration_of_assault") / 2;
				if(assaults.isEmpty()) return;
				for(Assault assault : assaults) {
					if(assault.secondsRemaining > 0) {
						assault.secondsRemaining -= 1;
					} else {
						if(assault.minutesRemaining > 0) {
							assault.secondsRemaining = 59;
							assault.minutesRemaining -= 1;
						} else {
							stopAssault(assault);
						}
					}
					float minutes = assault.minutesRemaining + (assault.secondsRemaining / 60);
					if(minutes == midAssault) {
						if(AssaultPlugin.getInstance().getConfig().getBoolean("capturable-zone.enable-zones") && AssaultPlugin.getInstance().getConfig().getBoolean("capturable-zone.rotate-zone-in-mid-assault")) {
							if(AssaultPlugin.getInstance().getConfig().getBoolean("capturable-zone.change-zone-territory-in-mid-assault")) {
								List<Player> players = assault.zone.getPlayers();
								AssaultManager.changeZone(assault, assault.belligerentAttackFaction, null);
								
								Assault updatedAssault = getFactionAssault(assault.belligerentAttackFaction);
								players.forEach(player -> {
									updatedAssault.zone.addPlayer(player);
								});
							} else {
								List<Player> players = assault.zone.getPlayers();
								AssaultManager.changeZone(assault, assault.belligerentDefenseFaction, null);
								
								Assault updatedAssault = getFactionAssault(assault.belligerentAttackFaction);
								players.forEach(player -> {
									updatedAssault.zone.addPlayer(player);
								});
							}
						}
					}
					if(AssaultPlugin.getInstance().getConfig().getBoolean("capturable-zone.enable-zones") && assault.zone != null) {
						assault.zone.renderZone();
					}
					updatePlayerScoreboards(assault);
					
					for(String plName : assault.attackDisconnectedPlayers.keySet()) {
						int i = assault.attackDisconnectedPlayers.get(plName).decrementAndGet();
						if(i <= 0) {
							processPlayerDisconnect(assault, plName);
						}
					}
					for(String plName : assault.defenseDisconnectedPlayers.keySet()) {
						int i = assault.defenseDisconnectedPlayers.get(plName).decrementAndGet();
						if(i <= 0) {
							processPlayerDisconnect(assault, plName);
						}
					}
					
					for(Player player : assault.attackTaggedPlayers.keySet()) {
						int i = assault.attackTaggedPlayers.get(player).decrementAndGet();
						if(i <= 0) {
							assault.attackTaggedPlayers.remove(player);
						}
					}
					for(Player player : assault.defenseTaggedPlayers.keySet()) {
						int i = assault.defenseTaggedPlayers.get(player).decrementAndGet();
						if(i <= 0) {
							assault.defenseTaggedPlayers.remove(player);
						}
					}
				}
			}
		}.runTaskTimer(AssaultPlugin.getInstance(), 0, 20L);
	}
	
	public static void startZoneCounter() {
	    new BukkitRunnable() {
	        @Override
	        public void run() {
	            if(assaults.isEmpty()) {
	                return;
	            }

	            AssaultPlugin plugin = AssaultPlugin.getInstance();
	            int ptsToAdd = plugin.getConfig().getInt("capturable-zone.given-points-per-period");
	            double requiredRatio = plugin.getConfig().getDouble("capturable-zone.required-minimal-ratio-for-give-points");

	            for(Assault assault : assaults) {
	                CapturableZone zone = assault.zone;

	                if(zone == null) {
	                    continue;
	                }

	                int attackCount = 0;
	                int defenseCount = 0;

	                for(Player player : assault.getAllPlayers()) {
	                    if(zone.isPlayerInside(player)) {
	                        Faction pFac = FPlayers.getInstance().getByPlayer(player).getFaction();
	                        AssaultSide side = getSide(pFac);
	                        if(side == AssaultSide.ATTACK) {
	                            attackCount++;
	                        } else if(side == AssaultSide.DEFENSE) {
	                            defenseCount++;
	                        }
	                    }
	                }

	                if(attackCount == defenseCount || (attackCount == 0 && defenseCount == 0)) {
	                    continue;
	                }

	                if(attackCount == 0 || defenseCount == 0) {
	                    if(attackCount == 0 && defenseCount >= requiredRatio) {
	                        assault.defenseScore += ptsToAdd;
	                    } else if(defenseCount == 0 && attackCount >= requiredRatio) {
	                        assault.attackScore += ptsToAdd;
	                    }
	                    continue;
	                }

	                double ratio = (double) Math.max(attackCount, defenseCount) / Math.min(attackCount, defenseCount);

	                if(ratio >= requiredRatio) {
	                    if(attackCount > defenseCount) {
	                        assault.attackScore += ptsToAdd;
	                    } else {
	                        assault.defenseScore += ptsToAdd;
	                    }
	                }
	            }
	        }
	    }.runTaskTimerAsynchronously(
	        AssaultPlugin.getInstance(),
	        0,
	        AssaultPlugin.getInstance().getConfig().getInt("capturable-zone.period-duration") * 20
	    );
	}

	public static void stopAssault(Assault assault) {
		AssaultPlugin plugin = AssaultPlugin.getInstance();
		String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";

		Faction attackFac = assault.belligerentAttackFaction;
		Faction defenseFac = assault.belligerentDefenseFaction;

		int attackScore = assault.attackScore;
		int defenseScore = assault.defenseScore;

		int pointToGive = plugin.getConfig().getInt("elo_points_to_give");
		int pointToGive2 = plugin.getConfig().getInt("elo_points_to_give_on_join");

		Faction winner = (attackScore > defenseScore) ? attackFac : defenseFac;
		Faction looser = attackFac.equals(winner) ? defenseFac : attackFac;
		
		int winnerScore = (winner.equals(attackFac)) ? attackScore : defenseScore;
		int looserScore = (winner.equals(attackFac)) ? defenseScore : attackScore;    

		List<Faction> attackJoin = assault.attackJoins;
		List<Faction> defenseJoin = assault.defenseJoins;

		if(attackScore > defenseScore || defenseScore > attackScore) {
			Bukkit.broadcastMessage(prefix + translateString(plugin.getConfig().getString("messages.assault_win").replace("%w", winner.getTag()).replace("%l", looser.getTag()).replace("%e", String.valueOf(pointToGive)).replace("%pw", String.valueOf(winnerScore)).replace("%pl", String.valueOf(looserScore))));

			if(plugin.getConfig().contains("ranking." + winner.getTag())) {
				int oldPoints = plugin.getConfig().getInt("ranking." + winner.getTag() + ".points");
				int newPoints = oldPoints + pointToGive;
				int oldWin = plugin.getConfig().getInt("ranking." + winner.getTag() + ".win");
				plugin.getConfig().set("ranking." + winner.getTag() + ".points", newPoints);
				plugin.getConfig().set("ranking." + winner.getTag() + ".win", oldWin + 1);
				plugin.saveConfig();
			} else {
				plugin.getConfig().set("ranking." + winner.getTag() + ".points", pointToGive);
				plugin.getConfig().set("ranking." + winner.getTag() + ".win", 1);
				plugin.getConfig().set("ranking." + winner.getTag() + ".loose", 0);
				plugin.saveConfig();
			}

			AssaultWinEvent winEvent = new AssaultWinEvent(winner, winnerScore, pointToGive);
			Bukkit.getServer().getPluginManager().callEvent(winEvent);

			if(pointToGive2 != 0) {
				boolean attackWinning = attackFac.equals(winner);

				for(int i = 0; i < attackJoin.size(); i++) {
					if(plugin.getConfig().contains("ranking." + attackJoin.get(i) + ".points")) {
						int oldPoints2 = plugin.getConfig().getInt("ranking." + attackJoin.get(i).getTag() + ".points");
						int newPoints2 = attackWinning ? oldPoints2 + pointToGive2 : oldPoints2 - pointToGive2;
						plugin.getConfig().set("ranking." + attackJoin.get(i).getTag() + ".points", newPoints2);
						plugin.saveConfig();
					} else {
						plugin.getConfig().set("ranking." + attackJoin.get(i).getTag() + ".points", pointToGive2);
						plugin.saveConfig();
					}
					attackJoin.get(i).getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_win_elo_on_join_assault").replace("%e", String.valueOf(pointToGive2)))));
				}

				for(int i = 0; i < defenseJoin.size(); i++) {
					if(plugin.getConfig().contains("ranking." + defenseJoin.get(i) + ".points")) {
						int oldPoints2 = plugin.getConfig().getInt("ranking." + defenseJoin.get(i).getTag() + ".points");
						int newPoints2 = attackWinning ?  oldPoints2 - pointToGive2 : oldPoints2 + pointToGive2;
						plugin.getConfig().set("ranking." + defenseJoin.get(i).getTag() + ".points", newPoints2);
						plugin.saveConfig();
					} else {
						plugin.getConfig().set("ranking." + defenseJoin.get(i).getTag() + ".points", pointToGive2);
						plugin.saveConfig();
					}
					defenseJoin.get(i).getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_win_elo_on_join_assault").replace("%e", String.valueOf(pointToGive2)))));
				}

				AssaultStopEvent stopEvent = new AssaultStopEvent(winner, looser, attackWinning ? attackJoin : defenseJoin, attackWinning ? defenseJoin : attackJoin, winnerScore, looserScore);
				Bukkit.getServer().getPluginManager().callEvent(stopEvent);
			}

			if(plugin.getConfig().contains("ranking." + looser.getTag())) {
				int oldPoints = plugin.getConfig().getInt("ranking." + looser.getTag() + ".points");
				int newPoints = 0;
				if(oldPoints - pointToGive <= 0) {
					newPoints = 0;
				} else {
					newPoints = oldPoints - pointToGive;
				}
				int oldLoose = plugin.getConfig().getInt("ranking." + looser.getTag() + ".loose");
				plugin.getConfig().set("ranking." + looser.getTag() + ".points", newPoints);
				plugin.getConfig().set("ranking." + looser.getTag() + ".loose", oldLoose + 1);
				plugin.saveConfig();
			} else {
				plugin.getConfig().set("ranking." + looser.getTag() + ".points", 0);
				plugin.getConfig().set("ranking." + looser.getTag() + ".win", 0);
				plugin.getConfig().set("ranking." + looser.getTag() + ".loose", 1);
				plugin.saveConfig();
			}

			AssaultLooseEvent looseEvent = new AssaultLooseEvent(looser, looserScore, pointToGive);
			Bukkit.getServer().getPluginManager().callEvent(looseEvent);
		} else {
			Bukkit.broadcastMessage(prefix + translateString(plugin.getConfig().getString("messages.assault_equality").replace("%d", defenseFac.getTag()).replace("%a", attackFac.getTag()).replace("%p", String.valueOf(attackScore))));
		}

		List<Player> allPlayers = assault.getAllPlayers();

		allPlayers.forEach(player -> {
			Scoreboard board = player.getScoreboard();
			if(board != null) {
				board.clearSlot(DisplaySlot.SIDEBAR);
			}
		});
		assaults.remove(assault);
	}

	private static void updatePlayerScoreboards(Assault assault) {
		Faction attackFac = assault.belligerentAttackFaction;
		Faction defenseFac = assault.belligerentDefenseFaction;

		String attackS = ChatColor.DARK_RED + "   • " + attackFac.getTag() + " : ";
		String defenseS = ChatColor.GOLD + "   • " + defenseFac.getTag() + " : ";

		int minutes = assault.minutesRemaining;
		int seconds = assault.secondsRemaining;
		
		String time = "";
		if(minutes > 0) {
			if(minutes < 10) {
				time = "0" + minutes + "m";
			} else {
				time = minutes + "m";
			}
		}
		
		if(seconds < 10) {
			time = time + "0" + seconds + "s";  
		} else {
			time = time + seconds + "s";
		}

		String scoreKey = ChatColor.YELLOW + "• Temps restant: ";
		String scoreValue = ChatColor.YELLOW + time;

		String allyKey = ChatColor.GREEN + "• Alliés Attaque: +";
		String eAllyKey = ChatColor.RED + "• Alliés Défense: +";

		for(Player player : assault.getAllPlayers()) {
			Scoreboard board = player.getScoreboard();
			if(board == null) {
				createScoreboard(assault, player, true);
				continue;
			}

			Objective objective = board.getObjective("assault");
			if(objective == null) continue;

			for(String entry : board.getEntries()) {
				if(entry.startsWith(attackS) || entry.startsWith(defenseS) || entry.startsWith(scoreKey) || entry.startsWith(allyKey) || entry.startsWith(eAllyKey)) {
					board.resetScores(entry);
				}
			}

			objective.getScore(attackS + assault.attackScore + " points").setScore(8);
			objective.getScore(defenseS + assault.defenseScore + " points").setScore(7);
			objective.getScore(scoreKey + scoreValue).setScore(2);
			objective.getScore(allyKey + assault.attackJoins.size()).setScore(5);
			objective.getScore(eAllyKey + assault.defenseJoins.size()).setScore(4);
		}
	}

	public static void createScoreboard(Assault assault, Player pp, boolean playerOnly) {
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();
		
		Faction attackFac = assault.belligerentAttackFaction;
		Faction defenseFac = assault.belligerentDefenseFaction;

		@SuppressWarnings("deprecation")
		Objective objective = board.registerNewObjective("assault", "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		String displayName = ChatColor.RED + "⚔ " + attackFac.getTag() + " VS " + defenseFac.getTag() + " ⚔";
		if(displayName.length() <= 32) {
			objective.setDisplayName(ChatColor.RED + "⚔ " + attackFac.getTag() + " VS " + defenseFac.getTag() + " ⚔");
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

		String startTime = assault.formattedStartTime;

		Score space4 = objective.getScore(ChatColor.GRAY + "    ");
		space4.setScore(1);

		Score startTimeScore = objective.getScore(ChatColor.YELLOW + "• Lancé à: " + startTime);
		startTimeScore.setScore(0);

		if(playerOnly) {
			pp.setScoreboard(board);
		} else {
			for(Player player : assault.getAllPlayers()) {
				player.setScoreboard(board);
			}
		}
	}

	public void createAllyScoreboard(Faction faction) {
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();

		Assault assault = getFactionAssault(faction);

		Faction attack = assault.belligerentAttackFaction;
		Faction defense = assault.belligerentDefenseFaction;

		@SuppressWarnings("deprecation")
		Objective objective = board.registerNewObjective("assault", "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		String displayName = ChatColor.RED + "⚔ " + attack.getTag() + " VS " + defense.getTag() + " ⚔";
		if(displayName.length() <= 32) {
			objective.setDisplayName(ChatColor.RED + "⚔ " + attack.getTag() + " VS " + defense.getTag() + " ⚔");
		} else {
			objective.setDisplayName(displayName.substring(0, 32));
		}

		Score space1 = objective.getScore("");
		space1.setScore(11);

		Score scoreString = objective.getScore(ChatColor.DARK_GRAY + "• Scores :");
		scoreString.setScore(10);

		Score space2 = objective.getScore(" ");
		space2.setScore(9);

		Score attackScore = objective.getScore(ChatColor.DARK_RED + "   • " + attack.getTag() + " : " + 0 + " points");
		attackScore.setScore(8);

		Score defenseScore = objective.getScore(ChatColor.GOLD + "   • " + defense.getTag() + " : " + 0 + " points");
		defenseScore.setScore(7);

		Score space3 = objective.getScore(ChatColor.GRAY + "   ");
		space3.setScore(6);

		Score allyScore = objective.getScore(ChatColor.GREEN + "• Alliés Attaque: +" + 0);
		allyScore.setScore(5);

		Score eAllyScore = objective.getScore(ChatColor.RED + "• Alliés Défense: +" + 0);
		eAllyScore.setScore(4);

		Score spaceX = objective.getScore(ChatColor.AQUA + "      ");
		spaceX.setScore(3);

		String startTime = assault.formattedStartTime;

		Score space4 = objective.getScore(ChatColor.GRAY + "    ");
		space4.setScore(1);

		Score startTimeScore = objective.getScore(ChatColor.YELLOW + "• Lancé à: " + startTime);
		startTimeScore.setScore(0);

		for(Player player : faction.getOnlinePlayers()) {
			player.setScoreboard(board);
		}
	}
	
	public static Location getRandomLocationForFaction(Faction fac) {
		List<Location> location = new ArrayList<>();
		World world = Bukkit.getWorld(AssaultPlugin.getInstance().getConfig().getString("capturable-zone.zone-world"));
		Random rd = new Random();
		boolean resolved = false;
		int maxAttempts = 150;
		Location finalLoc = new Location(world, 0, 0, 0);
		
		for(FLocation claim : fac.getAllClaims()) {
			if(!claim.getWorldName().equals(world.getName())) continue;
			int xStart = claim.getIntX() * 16;
			int zStart = claim.getIntZ() * 16;
			
			for(int x = 0; x < 16; x++) {
				for(int z = 0; z < 16; z++) {
					location.add(new Location(world, xStart + x, 60, zStart + z));
				}
			}
		}
		
		if(location.isEmpty()) return null;
		while(!resolved && maxAttempts > 0) {
			maxAttempts--;
			Location rdLoc = location.get(rd.nextInt(location.size()));
			
			int x = rdLoc.getBlockX();
			int z = rdLoc.getBlockZ();
			for(int i = 256; i > 0; i--) {
				if(!world.getBlockAt(x, i, z).getType().equals(Material.AIR)) {
					finalLoc = new Location(world, x, i + 1, z);
					resolved = true;
				}
			}
		}
		return finalLoc;
	}
	
	public static void processPlayerDisconnect(Assault assault, String playerName) {
		String prefix = AssaultPlugin.getInstance().getConfig().getBoolean("use-prefix") ? translateString(AssaultPlugin.getInstance().getConfig().getString("prefix")) : "";
		
		AssaultSide side = assault.attackDisconnectedPlayers.containsKey(playerName) ? AssaultSide.ATTACK : AssaultSide.DEFENSE;
        int scoreToAdd = AssaultPlugin.getInstance().getConfig().getInt("point-per-kill");
        
		if(side.equals(AssaultSide.ATTACK)) {
			assault.attackDisconnectedPlayers.remove(playerName);
			assault.defenseScore += scoreToAdd;
			for(Faction fac : assault.getAllSide(side)) {
				fac.getOnlinePlayers().forEach(player -> {
					player.sendMessage(prefix + translateString(AssaultPlugin.getInstance().getConfig().getString("messages.disconnect_kill_count_pf")));
				});
			}
			for(Faction fac : assault.getAllSide(AssaultSide.DEFENSE)) {
				fac.getOnlinePlayers().forEach(player -> {
					player.sendMessage(prefix + translateString(AssaultPlugin.getInstance().getConfig().getString("messages.disconnect_kill_count")));
				});
			}
		} else {
			assault.defenseDisconnectedPlayers.remove(playerName);
			assault.attackScore += scoreToAdd;
			for(Faction fac : assault.getAllSide(side)) {
				fac.getOnlinePlayers().forEach(player -> {
					player.sendMessage(prefix + translateString(AssaultPlugin.getInstance().getConfig().getString("messages.disconnect_kill_count_pf")));
				});
			}
			for(Faction fac : assault.getAllSide(AssaultSide.ATTACK)) {
				fac.getOnlinePlayers().forEach(player -> {
					player.sendMessage(prefix + translateString(AssaultPlugin.getInstance().getConfig().getString("messages.disconnect_kill_count")));
				});
			}
		}
	}

	public static String translateString(String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}
}
