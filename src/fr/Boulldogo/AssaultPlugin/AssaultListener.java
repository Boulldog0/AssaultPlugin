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
import com.massivecraft.factions.event.FPlayerJoinEvent;
import com.massivecraft.factions.event.FPlayerLeaveEvent;

public class AssaultListener implements Listener {
	
	private final Main plugin;
	
    public AssaultListener(Main plugin) {
        this.plugin = plugin;
    }
    
    public static List<Integer> attackScoreList = new ArrayList<>();
    public static List<Integer> defenseScoreList = new ArrayList<>();
	
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
            }
            updateScoreboard(index);
        }
    }
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		Faction playerFac = FPlayers.getInstance().getByPlayer(player).getFaction();
		if(!playerFac.isWilderness()) {
			if(!AssaultCommand.attackAssaultList.isEmpty() && AssaultCommand.attackAssaultList.contains(playerFac)) {
				int index = AssaultCommand.attackAssaultList.lastIndexOf(playerFac);
				createScoreboard(player, index);
			} else if(!AssaultCommand.defenseAssaultList.isEmpty() && AssaultCommand.defenseAssaultList.contains(playerFac)) {
				int index = AssaultCommand.defenseAssaultList.lastIndexOf(playerFac);
				createScoreboard(player, index);
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeaveFac(FPlayerLeaveEvent e) {
		Player player = e.getfPlayer().getPlayer();
		Faction playerFac = FPlayers.getInstance().getByPlayer(player).getFaction();
		if(plugin.getConfig().getBoolean("allow_assault_leave")) {
			if(!playerFac.isWilderness()) {
				if(!AssaultCommand.attackAssaultList.isEmpty() && AssaultCommand.attackAssaultList.contains(e.getFaction())) {
		            Scoreboard board = player.getScoreboard();
		            if (board != null) {
		                board.clearSlot(DisplaySlot.SIDEBAR);
		            }
				} else if(!AssaultCommand.defenseAssaultList.isEmpty() && AssaultCommand.defenseAssaultList.contains(e.getFaction())) {
		            Scoreboard board = player.getScoreboard();
		            if (board != null) {
		                board.clearSlot(DisplaySlot.SIDEBAR);
		            }
				}
			}
		} else {
	        String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
			e.setCancelled(true);
			player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_cant_leave_faction_in_assault")));
		}
	}
	
	@EventHandler
	public void onPlayerJoinFac(FPlayerJoinEvent e) {
		Player player = e.getfPlayer().getPlayer();
		Faction playerFac = FPlayers.getInstance().getByPlayer(player).getFaction();
		if(plugin.getConfig().getBoolean("allow_assault_join")) {
			if(!playerFac.isWilderness()) {
				if(!AssaultCommand.attackAssaultList.isEmpty() && AssaultCommand.attackAssaultList.contains(e.getFaction())) {
					int index = AssaultCommand.attackAssaultList.lastIndexOf(playerFac);
					createScoreboard(player, index);
				} else if(!AssaultCommand.defenseAssaultList.isEmpty() && AssaultCommand.defenseAssaultList.contains(e.getFaction())) {
					int index = AssaultCommand.attackAssaultList.lastIndexOf(playerFac);
					createScoreboard(player, index);
				}
			}
		} else {
	        String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
			e.setCancelled(true);
			player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_cant_join_faction_in_assault")));
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
            
            objective.getScore(attackS + attackScoreList.get(index) + " points").setScore(7);
            
            for (String entry : board.getEntries()) {
                if (entry.startsWith(defenseS)) {
                    board.resetScores(entry);
                    break;
                }
            }
            
            objective.getScore(defenseS + defenseScoreList.get(index) + " points").setScore(5);
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
            
            objective.getScore(attackS + attackScoreList.get(index) + " points").setScore(7);
            
            for (String entry : board.getEntries()) {
                if (entry.startsWith(defenseS)) {
                    board.resetScores(entry);
                    break;
                }
            }
            
            objective.getScore(defenseS + defenseScoreList.get(index) + " points").setScore(5);
        }
	}

	
    public void createScoreboard(Player player, int index) {
    	Faction attackFac = AssaultCommand.attackAssaultList.get(index);
    	Faction defenseFac = AssaultCommand.defenseAssaultList.get(index);
    	
    	int attackS = attackScoreList.get(index);
    	int defenseS = defenseScoreList.get(index);
    	
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        Objective objective = board.registerNewObjective("assault", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.GREEN + "Assault " + attackFac.getTag() + " VS " + defenseFac.getTag());

        Score space1 = objective.getScore("");
        space1.setScore(4);
        
        Score scoreString = objective.getScore(ChatColor.GRAY + "Scores :");
        scoreString.setScore(3);
        
        Score space2 = objective.getScore("");
        space2.setScore(2);

        Score attackScore = objective.getScore(ChatColor.RED + attackFac.getTag() + " : " + attackS);
        attackScore.setScore(1);

        Score defenseScore = objective.getScore(ChatColor.GREEN + defenseFac.getTag() + " : " + defenseS);
        defenseScore.setScore(0);

        Score space3 = objective.getScore("");
        space3.setScore(-1);
        
        String startTime = plugin.getConfig().getString("assault.start_time." + attackFac.getTag());
        
        Score space4 = objective.getScore("");
        space4.setScore(-3);

        Score startTimeScore = objective.getScore(ChatColor.YELLOW + "Lancé à: " + startTime);
        startTimeScore.setScore(-4);

        player.setScoreboard(board);
    }
    
    public String translateString(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

}
