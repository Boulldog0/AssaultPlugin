package fr.Boulldogo.AssaultPlugin.Listeners;

import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.event.FPlayerJoinEvent;
import com.massivecraft.factions.event.FactionDisbandEvent;
import com.massivecraft.factions.event.FactionRelationWishEvent;

import fr.Boulldogo.AssaultPlugin.AssaultPlugin;
import fr.Boulldogo.AssaultPlugin.Events.KillInAssaultEvent;
import fr.Boulldogo.AssaultPlugin.Events.PlayerDisconnectInAssaultEvent;
import fr.Boulldogo.AssaultPlugin.Utils.Assault;
import fr.Boulldogo.AssaultPlugin.Utils.AssaultManager;
import fr.Boulldogo.AssaultPlugin.Utils.AssaultManager.AssaultSide;

public class AssaultListener implements Listener {
	
	private final AssaultPlugin plugin;
	
    public AssaultListener(AssaultPlugin plugin) {
        this.plugin = plugin;
    }
	
	@EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        Faction playerFac = FPlayers.getInstance().getByPlayer(player).getFaction();
        if(!playerFac.isWilderness()) {
            if(!AssaultManager.isFactionInAssaultOrJoinAssault(playerFac)) return;
            Assault assault = AssaultManager.getFactionAssault(playerFac);
            int scoreToAdd = plugin.getConfig().getInt("point-per-kill");
            AssaultSide side = AssaultManager.getSide(playerFac);
            
            if(side.equals(AssaultSide.ATTACK)) {
            	assault.defenseScore += scoreToAdd;
            } else {
            	assault.attackScore += scoreToAdd;
            }
            KillInAssaultEvent killEvent = new KillInAssaultEvent(player, scoreToAdd, side);
            Bukkit.getServer().getPluginManager().callEvent(killEvent);
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
    	String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
        if(e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
        	if(plugin.getConfig().getBoolean("enable_anti_deconnexion_system")) {
                Player damaged = (Player) e.getEntity();
                Player damager = (Player) e.getDamager();
                
                Faction damagedFac = FPlayers.getInstance().getByPlayer(damaged).getFaction();
                Faction damagerFac = FPlayers.getInstance().getByPlayer(damager).getFaction();
                
                if(AssaultManager.getSide(damagerFac).equals(AssaultManager.getSide(damagedFac))) {
                	if(plugin.getConfig().getBoolean("disable-damages-in-same-side")) {
                		e.setCancelled(true);
                		return;
                	}
                }
                
                Assault assault = AssaultManager.getFactionAssault(damagerFac);
                AssaultSide damagedSide = AssaultManager.getSide(damagedFac);
            	AtomicInteger aI = new AtomicInteger();
            	aI.set(plugin.getConfig().getInt("players-tagged-for"));
            	
            	if(!assault.attackTaggedPlayers.containsKey(damager) && !assault.defenseTaggedPlayers.containsKey(damager))  {
            		damager.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_are_tagged")));
            	}
            	if(!assault.attackTaggedPlayers.containsKey(damaged) && !assault.defenseTaggedPlayers.containsKey(damaged))  {
            		damaged.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_are_tagged")));
            	}
                
                if(damagedSide.equals(AssaultSide.ATTACK)) {
                	assault.attackTaggedPlayers.put(damaged, aI);
                	assault.defenseTaggedPlayers.put(damager, aI);
                } else {
                	assault.attackTaggedPlayers.put(damager, aI);
                	assault.defenseTaggedPlayers.put(damaged, aI);
                }
        	}
        }
    }
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		Faction playerFac = FPlayers.getInstance().getByPlayer(player).getFaction();
		if(!playerFac.isWilderness()) {
			if(AssaultManager.isFactionInAssaultOrJoinAssault(playerFac)) {
				Assault assault = AssaultManager.getFactionAssault(playerFac);
				AssaultSide side = AssaultManager.getSide(playerFac);
				if(side.equals(AssaultSide.ATTACK)) {
					if(assault.attackDisconnectedPlayers.containsKey(player.getName())) {
						assault.attackDisconnectedPlayers.remove(player.getName());
					}
				} else {
					if(assault.defenseDisconnectedPlayers.containsKey(player.getName())) {
						assault.defenseDisconnectedPlayers.remove(player.getName());
					}
				}
			}
		}
		
		if(player.hasPermission("assault.update")) {
			if(plugin.getConfig().get("latest-version") != AssaultPlugin.V) {
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
			if(AssaultManager.isFactionInAssaultOrJoinAssault(playerFac)) {
				String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
				Assault assault = AssaultManager.getFactionAssault(playerFac);
				AssaultSide side = AssaultManager.getSide(playerFac);
				AtomicInteger aI = new AtomicInteger();
				aI.set(plugin.getConfig().getInt("count_kill_after"));
				if(side.equals(AssaultSide.ATTACK)) {
					if(assault.attackTaggedPlayers.containsKey(player)) {
						assault.attackDisconnectedPlayers.put(player.getName(), aI);
						assault.attackTaggedPlayers.remove(player);
						assault.getAllSide(AssaultSide.ATTACK).forEach(faction -> {
							faction.getOnlinePlayers().forEach(member -> {
								member.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.player_disconnect_in_your_camp").replace("%s", String.valueOf(plugin.getConfig().getInt("count_kill_after")))));
							});
						});
						assault.getAllSide(AssaultSide.DEFENSE).forEach(faction -> {
							faction.getOnlinePlayers().forEach(member -> {
								member.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.player_disconnect_in_enemy_camp").replace("%s", String.valueOf(plugin.getConfig().getInt("count_kill_after")))));
							});
						});
		            	PlayerDisconnectInAssaultEvent diae = new PlayerDisconnectInAssaultEvent(player);
		            	Bukkit.getServer().getPluginManager().callEvent(diae);
					}
				} else {
					if(assault.defenseTaggedPlayers.containsKey(player)) {
						assault.defenseDisconnectedPlayers.put(player.getName(), aI);
						assault.defenseTaggedPlayers.remove(player);
						assault.getAllSide(AssaultSide.DEFENSE).forEach(faction -> {
							faction.getOnlinePlayers().forEach(member -> {
								member.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.player_disconnect_in_your_camp").replace("%s", String.valueOf(plugin.getConfig().getInt("count_kill_after")))));
							});
						});
						assault.getAllSide(AssaultSide.ATTACK).forEach(faction -> {
							faction.getOnlinePlayers().forEach(member -> {
								member.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.player_disconnect_in_enemy_camp").replace("%s", String.valueOf(plugin.getConfig().getInt("count_kill_after")))));
							});
						});
		            	PlayerDisconnectInAssaultEvent diae = new PlayerDisconnectInAssaultEvent(player);
		            	Bukkit.getServer().getPluginManager().callEvent(diae);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoinFac(FPlayerJoinEvent e) {
		Player player = e.getfPlayer().getPlayer();
		if(player.hasPermission("assault.bypass-join-restrictions")) return;
		if(!plugin.getConfig().getBoolean("allow_assault_join")) {
			if(AssaultManager.isFactionInAssaultOrJoinAssault(e.getFaction())) {
		        String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
				e.setCancelled(true);
				player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_cant_join_faction_in_assault")));
			}
		}
	}
	
	@EventHandler
	public void onFactionDisband(FactionDisbandEvent e) {
		String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
		Faction faction = e.getFaction();
		
		if(AssaultManager.isFactionInAssaultOrJoinAssault(faction)) {
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
	
    public String translateString(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

}
