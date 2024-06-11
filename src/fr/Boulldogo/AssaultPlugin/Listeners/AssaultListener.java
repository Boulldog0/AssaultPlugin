package fr.Boulldogo.AssaultPlugin.Listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
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

import fr.Boulldogo.AssaultPlugin.Main;
import fr.Boulldogo.AssaultPlugin.Commands.AssaultCommand;
import fr.Boulldogo.AssaultPlugin.Events.KillInAssaultEvent;
import fr.Boulldogo.AssaultPlugin.Events.PlayerDisconnectInAssaultEvent;

public class AssaultListener implements Listener {
	
	private final Main plugin;
	
    public AssaultListener(Main plugin) {
        this.plugin = plugin;
    }
    
    public static List<Integer> attackScoreList = new ArrayList<>();
    public static List<Integer> defenseScoreList = new ArrayList<>();
    
    public static Map<Player, Integer> disconnectedPlayer = new HashMap<>();
    public static Map<Player, Faction> taggedPlayer = new HashMap<>();
    
    boolean disconnectCounter = false;
    public BukkitTask disconnectVerif;
	
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
                Faction faction = AssaultCommand.defenseAssaultList.get(index);
                KillInAssaultEvent killEvent = new KillInAssaultEvent(player, scoreToAdd, playerFac, faction);
                Bukkit.getServer().getPluginManager().callEvent(killEvent);
            } else if (!AssaultCommand.defenseAssaultList.isEmpty() && AssaultCommand.defenseAssaultList.contains(playerFac)) {
                index = AssaultCommand.defenseAssaultList.indexOf(playerFac);
                int scoreToAdd = plugin.getConfig().getInt("point-per-kill");
                attackScoreList.set(index, attackScoreList.get(index) + scoreToAdd);
                Faction faction = AssaultCommand.attackAssaultList.get(index);
                KillInAssaultEvent killEvent = new KillInAssaultEvent(player, scoreToAdd, playerFac, faction);
                Bukkit.getServer().getPluginManager().callEvent(killEvent);
            } else if(!AssaultCommand.attackJoinList.isEmpty() && AssaultCommand.attackJoinList.contains(playerFac)) {
				for(int i = 0; i < AssaultCommand.attackAssaultList.size(); i++) {
					if(plugin.getConfig().getStringList("assault.join.attack." + AssaultCommand.attackAssaultList.get(i).getTag()).contains(playerFac)) {
						index = AssaultCommand.attackAssaultList.lastIndexOf(AssaultCommand.attackAssaultList.get(i));
					}
				}
                int scoreToAdd = plugin.getConfig().getInt("point-per-kill");
                Faction faction = AssaultCommand.defenseAssaultList.get(index);
                KillInAssaultEvent killEvent = new KillInAssaultEvent(player, scoreToAdd, playerFac, faction);
                Bukkit.getServer().getPluginManager().callEvent(killEvent);
                defenseScoreList.set(index, defenseScoreList.get(index) + scoreToAdd);
			} else if(!AssaultCommand.defenseJoinList.isEmpty() && AssaultCommand.defenseJoinList.contains(playerFac)) {
				for(int i = 0; i < AssaultCommand.defenseAssaultList.size(); i++) {
					if(plugin.getConfig().getStringList("assault.join.defense." + AssaultCommand.defenseAssaultList.get(i).getTag()).contains(playerFac)) {
						index = AssaultCommand.defenseAssaultList.lastIndexOf(AssaultCommand.defenseAssaultList.get(i));
					}
				}
                int scoreToAdd = plugin.getConfig().getInt("point-per-kill");
                Faction faction = AssaultCommand.defenseAssaultList.get(index);
                KillInAssaultEvent killEvent = new KillInAssaultEvent(player, scoreToAdd, playerFac, faction);
                Bukkit.getServer().getPluginManager().callEvent(killEvent);
                attackScoreList.set(index, attackScoreList.get(index) + scoreToAdd);
			}
            updateScoreboard(index);
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    	String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
        	if(plugin.getConfig().getBoolean("enable_anti_deconnexion_system")) {
                Player damaged = (Player) event.getEntity();
                Player damager = (Player) event.getDamager();
                
                if(!taggedPlayer.containsKey(damaged)) {
                    Faction playerFac = FPlayers.getInstance().getByPlayer(damaged).getFaction();
                    if(!AssaultCommand.attackAssaultList.isEmpty() && AssaultCommand.attackAssaultList.contains(playerFac)
                     || !AssaultCommand.defenseAssaultList.isEmpty() && AssaultCommand.defenseAssaultList.contains(playerFac)
                     || !AssaultCommand.attackJoinList.isEmpty() && AssaultCommand.attackJoinList.contains(playerFac)
                     || !AssaultCommand.defenseJoinList.isEmpty() && AssaultCommand.defenseAssaultList.contains(playerFac)) {
                    	boolean isntJoin = AssaultCommand.attackAssaultList.contains(playerFac) || AssaultCommand.defenseAssaultList.contains(playerFac);
                    	
                    	if(isntJoin) {
                    		taggedPlayer.put(damaged, playerFac);
                    		damaged.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_are_tagged")));
                        } else {
                        	if(AssaultCommand.attackJoinList.contains(playerFac)) {
                            	int index = 0;
                				for(int i = 0; i < AssaultCommand.attackAssaultList.size(); i++) {
                					if(plugin.getConfig().getStringList("assault.join.attack." + AssaultCommand.attackAssaultList.get(i).getTag()).contains(playerFac.getTag())) {
                						index = AssaultCommand.attackAssaultList.lastIndexOf(AssaultCommand.attackAssaultList.get(i));
                					}
                				}
                				Faction faction = AssaultCommand.attackAssaultList.get(index);
                        		taggedPlayer.put(damaged, faction);
                        		damaged.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_are_tagged")));
                        	} else if(AssaultCommand.defenseJoinList.contains(playerFac)) {
                            	int index = 0;
                				for(int i = 0; i < AssaultCommand.defenseAssaultList.size(); i++) {
                					if(plugin.getConfig().getStringList("assault.join.attack." + AssaultCommand.attackAssaultList.get(i).getTag()).contains(playerFac.getTag())) {
                						index = AssaultCommand.defenseAssaultList.lastIndexOf(AssaultCommand.attackAssaultList.get(i));
                					}
                				}
                				Faction faction = AssaultCommand.defenseAssaultList.get(index);
                        		taggedPlayer.put(damaged, faction);
                        		damaged.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_are_tagged")));
                        	 }
                         }
                     }
        	     }
                
                if(!taggedPlayer.containsKey(damager)) {
                    Faction playerFac = FPlayers.getInstance().getByPlayer(damager).getFaction();
                    if(!AssaultCommand.attackAssaultList.isEmpty() && AssaultCommand.attackAssaultList.contains(playerFac)
                     || !AssaultCommand.defenseAssaultList.isEmpty() && AssaultCommand.defenseAssaultList.contains(playerFac)
                     || !AssaultCommand.attackJoinList.isEmpty() && AssaultCommand.attackJoinList.contains(playerFac)
                     || !AssaultCommand.defenseJoinList.isEmpty() && AssaultCommand.defenseAssaultList.contains(playerFac)) {
                    	boolean isntJoin = AssaultCommand.attackAssaultList.contains(playerFac) || AssaultCommand.defenseAssaultList.contains(playerFac);
                    	
                    	if(isntJoin) {
                    		taggedPlayer.put(damager, playerFac);
                    		damager.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_are_tagged")));
                        } else {
                        	if(AssaultCommand.attackJoinList.contains(playerFac)) {
                            	int index = 0;
                				for(int i = 0; i < AssaultCommand.attackAssaultList.size(); i++) {
                					if(plugin.getConfig().getStringList("assault.join.attack." + AssaultCommand.attackAssaultList.get(i).getTag()).contains(playerFac.getTag())) {
                						index = AssaultCommand.attackAssaultList.lastIndexOf(AssaultCommand.attackAssaultList.get(i));
                					}
                				}
                				Faction faction = AssaultCommand.attackAssaultList.get(index);
                        		taggedPlayer.put(damager, faction);
                        		damager.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_are_tagged")));
                        	} else if(AssaultCommand.defenseJoinList.contains(playerFac)) {
                            	int index = 0;
                				for(int i = 0; i < AssaultCommand.defenseAssaultList.size(); i++) {
                					if(plugin.getConfig().getStringList("assault.join.attack." + AssaultCommand.attackAssaultList.get(i).getTag()).contains(playerFac.getTag())) {
                						index = AssaultCommand.defenseAssaultList.lastIndexOf(AssaultCommand.attackAssaultList.get(i));
                					}
                				}
                				Faction faction = AssaultCommand.defenseAssaultList.get(index);
                        		taggedPlayer.put(damager, faction);
                        		damager.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_are_tagged")));
                        	 }
                         }
                     }
        	     }
        	 } 
         }
    }
	
	@SuppressWarnings("unlikely-arg-type")
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		Faction playerFac = FPlayers.getInstance().getByPlayer(player).getFaction();
		if(!playerFac.isWilderness()) {
			if(disconnectedPlayer.containsKey(player)) {
				disconnectedPlayer.remove(player);
			}
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
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		Faction playerFac = FPlayers.getInstance().getByPlayer(player).getFaction();
		if(!playerFac.isWilderness()) {
            int index = 0;
            if (!AssaultCommand.attackAssaultList.isEmpty() && AssaultCommand.attackAssaultList.contains(playerFac)) {
                index = AssaultCommand.attackAssaultList.indexOf(playerFac);
                Faction faction = AssaultCommand.defenseAssaultList.get(index);
                
            	PlayerDisconnectInAssaultEvent diae = new PlayerDisconnectInAssaultEvent(player, playerFac, faction);
            	Bukkit.getServer().getPluginManager().callEvent(diae);
            } else if (!AssaultCommand.defenseAssaultList.isEmpty() && AssaultCommand.defenseAssaultList.contains(playerFac)) {
                index = AssaultCommand.defenseAssaultList.indexOf(playerFac);
                Faction faction = AssaultCommand.attackAssaultList.get(index);
                
            	PlayerDisconnectInAssaultEvent diae = new PlayerDisconnectInAssaultEvent(player, playerFac, faction);
            	Bukkit.getServer().getPluginManager().callEvent(diae);
            } else if(!AssaultCommand.attackJoinList.isEmpty() && AssaultCommand.attackJoinList.contains(playerFac)) {
				for(int i = 0; i < AssaultCommand.attackAssaultList.size(); i++) {
					if(plugin.getConfig().getStringList("assault.join.attack." + AssaultCommand.attackAssaultList.get(i).getTag()).contains(playerFac.getTag())) {
						index = AssaultCommand.attackAssaultList.lastIndexOf(AssaultCommand.attackAssaultList.get(i));
					}
				}
                Faction faction = AssaultCommand.defenseAssaultList.get(index);
                
            	PlayerDisconnectInAssaultEvent diae = new PlayerDisconnectInAssaultEvent(player, playerFac, faction);
            	Bukkit.getServer().getPluginManager().callEvent(diae);
			} else if(!AssaultCommand.defenseJoinList.isEmpty() && AssaultCommand.defenseJoinList.contains(playerFac)) {
				for(int i = 0; i < AssaultCommand.defenseAssaultList.size(); i++) {
					if(plugin.getConfig().getStringList("assault.join.defense." + AssaultCommand.defenseAssaultList.get(i).getTag()).contains(playerFac.getTag())) {
						index = AssaultCommand.defenseAssaultList.lastIndexOf(AssaultCommand.defenseAssaultList.get(i));
					}
				}
                Faction faction = AssaultCommand.attackAssaultList.get(index);
                
            	PlayerDisconnectInAssaultEvent diae = new PlayerDisconnectInAssaultEvent(player, playerFac, faction);
            	Bukkit.getServer().getPluginManager().callEvent(diae);
			}
		}
	}
	
	@EventHandler
	public void onPlayerDisconnectInAssault(PlayerDisconnectInAssaultEvent e) {
		String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
		if(plugin.getConfig().getBoolean("enable_anti_deconnexion_system")) {
			Player player = e.getPlayer();
			if(taggedPlayer.containsKey(player)) {
				Faction playerSideFac = e.getPlayerSideFaction();
				Faction enemySideFac = e.getEnemySideFaction();
				
				int seconds = plugin.getConfig().getInt("count_kill_after");
				
				disconnectedPlayer.put(player, seconds);
				if(!disconnectCounter) {
					startDisconnectCounter();
				}
				
				playerSideFac.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.player_disconnect_in_your_camp").replace("%s", String.valueOf(seconds)))));
				enemySideFac.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.player_disconnect_in_enemy_camp").replace("%s", String.valueOf(seconds)))));
			}
		}
	}
	
	public void startDisconnectCounter() {
		disconnectVerif = new BukkitRunnable() {
			
			@Override
			public void run() {
				disconnectCounter = true;
				if(!disconnectedPlayer.isEmpty()) {
			        for (Map.Entry<Player, Integer> entry : disconnectedPlayer.entrySet()) {
			            Player player = entry.getKey();
			            Integer time = entry.getValue();
			            disconnectedPlayer.put(player, time - 1);
			            if((time - 1) <= 0) {
			            	disconnectedPlayer.remove(player);
			            	processPlayerDisconnect(player);
			            }
			        }
				} else {
					disconnectVerif.cancel();
					disconnectVerif = null;
					disconnectCounter = false;
				}
			}
		}.runTaskTimer(plugin, 0, 20L);
	}
	
	public void processPlayerDisconnect(Player player) {
		String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
		Faction playerFac = FPlayers.getInstance().getByPlayer(player).getFaction();
    	
       	if(AssaultCommand.attackAssaultList.isEmpty()) return;
		
       	boolean isntJoin = AssaultCommand.attackAssaultList.contains(playerFac) || AssaultCommand.defenseAssaultList.contains(playerFac);
       	
    	if(isntJoin) {
    		int index = 0;
    		if(AssaultCommand.attackAssaultList.contains(playerFac)) {
                index = AssaultCommand.attackAssaultList.indexOf(playerFac);
                int scoreToAdd = plugin.getConfig().getInt("point-per-kill");
                defenseScoreList.set(index, defenseScoreList.get(index) + scoreToAdd);
                Faction faction = AssaultCommand.defenseAssaultList.get(index);
                playerFac.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString("messages.disconnect_kill_count_pf")));
                faction.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString("messages.disconnect_kill_count")));
                KillInAssaultEvent killEvent = new KillInAssaultEvent(player, scoreToAdd, playerFac, faction);
                Bukkit.getServer().getPluginManager().callEvent(killEvent);
                
                List<String> attackJoin = plugin.getConfig().getStringList("assault.join.attack." + playerFac.getTag());
                List<String> defenseJoin = plugin.getConfig().getStringList("assault.join.defense." + faction.getTag());
                
                if(!attackJoin.isEmpty()) {
                	for(int i = 0; i < attackJoin.size(); i++) {
                		Faction f = Factions.getInstance().getByTag(attackJoin.get(i));
                		f.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString("messages.disconnect_kill_count_pf")));
                	}
                }
                
                if(!defenseJoin.isEmpty()) {
                	for(int i = 0; i < attackJoin.size(); i++) {
                		Faction f = Factions.getInstance().getByTag(defenseJoin.get(i));
                		f.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString("messages.disconnect_kill_count")));
                	}
                }
    		} else if(AssaultCommand.defenseAssaultList.contains(playerFac)) {
                index = AssaultCommand.defenseAssaultList.indexOf(playerFac);
                int scoreToAdd = plugin.getConfig().getInt("point-per-kill");
                defenseScoreList.set(index, attackScoreList.get(index) + scoreToAdd);
                Faction faction = AssaultCommand.attackAssaultList.get(index);
                playerFac.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString("messages.disconnect_kill_count_pf")));
                faction.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString("messages.disconnect_kill_count")));
                KillInAssaultEvent killEvent = new KillInAssaultEvent(player, scoreToAdd, playerFac, faction);
                Bukkit.getServer().getPluginManager().callEvent(killEvent);
                
                List<String> attackJoin = plugin.getConfig().getStringList("assault.join.attack." + faction.getTag());
                List<String> defenseJoin = plugin.getConfig().getStringList("assault.join.defense." + playerFac.getTag());
                
                if(!attackJoin.isEmpty()) {
                	for(int i = 0; i < attackJoin.size(); i++) {
                		Faction f = Factions.getInstance().getByTag(attackJoin.get(i));
                		f.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString("messages.disconnect_kill_count")));
                	}
                }
                
                if(!defenseJoin.isEmpty()) {
                	for(int i = 0; i < attackJoin.size(); i++) {
                		Faction f = Factions.getInstance().getByTag(defenseJoin.get(i));
                		f.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString("messages.disconnect_kill_count_pf")));
                	}
                }
    		} else {
    			return;
    		}
        } else {
        	if(AssaultCommand.attackJoinList.contains(playerFac)) {
            	int index = 0;
				for(int i = 0; i < AssaultCommand.attackAssaultList.size(); i++) {
					if(plugin.getConfig().getStringList("assault.join.attack." + AssaultCommand.attackAssaultList.get(i).getTag()).contains(playerFac.getTag())) {
						index = AssaultCommand.attackAssaultList.lastIndexOf(AssaultCommand.attackAssaultList.get(i));
					}
				}
				Faction pFac = AssaultCommand.attackAssaultList.get(index);
                int scoreToAdd = plugin.getConfig().getInt("point-per-kill");
                defenseScoreList.set(index, attackScoreList.get(index) + scoreToAdd);
                Faction faction = AssaultCommand.defenseAssaultList.get(index);
                pFac.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString("messages.disconnect_kill_count_pf")));
                faction.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString("messages.disconnect_kill_count")));
                KillInAssaultEvent killEvent = new KillInAssaultEvent(player, scoreToAdd, pFac, faction);
                Bukkit.getServer().getPluginManager().callEvent(killEvent);
                
                List<String> attackJoin = plugin.getConfig().getStringList("assault.join.attack." + pFac.getTag());
                List<String> defenseJoin = plugin.getConfig().getStringList("assault.join.defense." + faction.getTag());
                
                if(!attackJoin.isEmpty()) {
                	for(int i = 0; i < attackJoin.size(); i++) {
                		Faction f = Factions.getInstance().getByTag(attackJoin.get(i));
                		f.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString("messages.disconnect_kill_count_pf")));
                	}
                }
                
                if(!defenseJoin.isEmpty()) {
                	for(int i = 0; i < attackJoin.size(); i++) {
                		Faction f = Factions.getInstance().getByTag(defenseJoin.get(i));
                		f.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString("messages.disconnect_kill_count")));
                	}
                }
        	} else if(AssaultCommand.defenseJoinList.contains(playerFac)) {
            	int index = 0;
				for(int i = 0; i < AssaultCommand.defenseAssaultList.size(); i++) {
					if(plugin.getConfig().getStringList("assault.join.attack." + AssaultCommand.defenseAssaultList.get(i).getTag()).contains(playerFac.getTag())) {
						index = AssaultCommand.defenseAssaultList.lastIndexOf(AssaultCommand.defenseAssaultList.get(i));
					}
				}
				Faction pFac = AssaultCommand.defenseAssaultList.get(index);
                int scoreToAdd = plugin.getConfig().getInt("point-per-kill");
                defenseScoreList.set(index, attackScoreList.get(index) + scoreToAdd);
                Faction faction = AssaultCommand.attackAssaultList.get(index);
                pFac.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString("messages.disconnect_kill_count_pf")));
                faction.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString("messages.disconnect_kill_count")));
                KillInAssaultEvent killEvent = new KillInAssaultEvent(player, scoreToAdd, pFac, faction);
                Bukkit.getServer().getPluginManager().callEvent(killEvent);
                
                List<String> attackJoin = plugin.getConfig().getStringList("assault.join.attack." + faction.getTag());
                List<String> defenseJoin = plugin.getConfig().getStringList("assault.join.defense." + pFac.getTag());
                
                if(!attackJoin.isEmpty()) {
                	for(int i = 0; i < attackJoin.size(); i++) {
                		Faction f = Factions.getInstance().getByTag(attackJoin.get(i));
                		f.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString("messages.disconnect_kill_count")));
                	}
                }
                
                if(!defenseJoin.isEmpty()) {
                	for(int i = 0; i < attackJoin.size(); i++) {
                		Faction f = Factions.getInstance().getByTag(defenseJoin.get(i));
                		f.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString("messages.disconnect_kill_count_pf")));
                	}
                }
        	 } else {
        		 return;
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
