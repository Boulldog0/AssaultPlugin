package fr.Boulldogo.AssaultPlugin.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;

import fr.Boulldogo.AssaultPlugin.AssaultPlugin;
import fr.Boulldogo.AssaultPlugin.Utils.Assault;
import fr.Boulldogo.AssaultPlugin.Utils.AssaultManager;

public class PlayerListener implements Listener {
	
	private final AssaultPlugin plugin;
	
	public PlayerListener(AssaultPlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamagedEvent(EntityDamageByEntityEvent e) {
		if(e.isCancelled()) return;
		String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
		if(e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			Player damaged = (Player) e.getEntity();
			Player damager = (Player) e.getDamager();
			
			Faction damagedFac = FPlayers.getInstance().getByPlayer(damaged).getFaction();
			Faction damagerFac = FPlayers.getInstance().getByPlayer(damager).getFaction();
			
			if(plugin.getConfig().getBoolean("disable-default-faction-pvp")) {
				FLocation damagedFLoc = FLocation.wrap(FPlayers.getInstance().getByPlayer(damaged));	
				Faction whereIsDamaged = Board.getInstance().getFactionAt(damagedFLoc);
				
				if(damagedFac != null && !damagedFac.isWilderness()) {
					if(whereIsDamaged == damagedFac) {
						if(AssaultManager.assaults.isEmpty()) {
							e.setCancelled(true);
							damager.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.cant_hit_outside_assault")));
							return;
						} else if(!AssaultManager.isFactionInAssaultOrJoinAssault(damagedFac)) {
							e.setCancelled(true);
							damager.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.cant_hit_outside_assault")));
							return;
						} else {
							if(!AssaultManager.isFactionInAssaultOrJoinAssault(damagerFac)) {
								e.setCancelled(true);
								damager.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.cant_hit_outside_assault")));
								return;
							}
						}
					}
				}
			}
			
        	if(plugin.getConfig().getBoolean("disable-damages-in-same-side")) {
        		if(!AssaultManager.isFactionInAssaultOrJoinAssault(damagerFac) || !AssaultManager.isFactionInAssaultOrJoinAssault(damagedFac)) return;
        		if(!AssaultManager.isInSameAssaults(damaged, damager)) return;
            	if(AssaultManager.getSide(damagerFac).equals(AssaultManager.getSide(damagedFac))) {
            		e.setCancelled(true);
					damager.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you-cant-hit-same-side")));
					return;
            	}
        	}
			
			if(plugin.getConfig().getBoolean("disable-inter-assault-pvp")) {
				if(AssaultManager.assaults.isEmpty()) return;
				if(!AssaultManager.isInSameAssaults(damaged, damager)) {
					damager.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.cant_hit_player_of_another_assault")));
					e.setCancelled(true);
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if(!plugin.getConfig().getBoolean("capturable-zone.enable-zones")) return;
		Player player = e.getPlayer();
		Location toLoc = e.getTo();
		if(toLoc == null) return;
		if(e.getFrom().getBlock().equals(e.getTo().getBlock())) return;
		if(!player.getWorld().getName().equals(plugin.getConfig().getString("capturable-zone.zone-world"))) return;
		Faction playerFac = FPlayers.getInstance().getByPlayer(player).getFaction();
		if(!AssaultManager.isFactionInAssaultOrJoinAssault(playerFac)) return;
		
		Assault assault = AssaultManager.getFactionAssault(playerFac);
		if(assault == null || assault.zone == null) return;
		Location zoneLoc = assault.zone.getLoc();
		int radius = assault.zone.getRadius();
		int height = assault.zone.getHeight();
		
		boolean isInZone = false;
		if(toLoc.getBlockX() >= zoneLoc.getBlockX() - radius && toLoc.getBlockX() <= zoneLoc.getBlockX() + radius) {
			if(toLoc.getBlockZ() >= zoneLoc.getBlockZ() - radius && toLoc.getBlockZ() <= zoneLoc.getBlockZ() + radius) {
				if(toLoc.getBlockY() >= zoneLoc.getBlockY() && toLoc.getBlockY() <= zoneLoc.getBlockY() + height) {
					isInZone = true;
				}
			}
		}
		if(isInZone) {
			if(!assault.zone.isPlayerInside(player)) {
				assault.zone.addPlayer(player);
			}
		} else {
			if(assault.zone.isPlayerInside(player)) {
				assault.zone.removePlayer(player);
			}
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		if(!plugin.getConfig().getBoolean("capturable-zone.enable-zones")) return;
		Player player = e.getPlayer();
		Location toLoc = e.getTo();
		if(toLoc == null) return;
		if(e.getFrom().getBlock().equals(e.getTo().getBlock())) return;
		if(!player.getWorld().getName().equals(plugin.getConfig().getString("capturable-zone.zone-world"))) return;
		Faction playerFac = FPlayers.getInstance().getByPlayer(player).getFaction();
		if(!AssaultManager.isFactionInAssaultOrJoinAssault(playerFac)) return;
		
		Assault assault = AssaultManager.getFactionAssault(playerFac);
		if(assault == null || assault.zone == null) return;
		Location zoneLoc = assault.zone.getLoc();
		int radius = assault.zone.getRadius();
		int height = assault.zone.getHeight();
		
		boolean isInZone = false;
		if(toLoc.getBlockX() >= zoneLoc.getBlockX() - radius && toLoc.getBlockX() <= zoneLoc.getBlockX() + radius) {
			if(toLoc.getBlockZ() >= zoneLoc.getBlockZ() - radius && toLoc.getBlockZ() <= zoneLoc.getBlockZ() + radius) {
				if(toLoc.getBlockY() >= zoneLoc.getBlockY() && toLoc.getBlockY() <= zoneLoc.getBlockY() + height) {
					isInZone = true;
				}
			}
		}
		if(isInZone) {
			if(!assault.zone.isPlayerInside(player)) {
				assault.zone.addPlayer(player);
			}
		} else {
			if(assault.zone.isPlayerInside(player)) {
				assault.zone.removePlayer(player);
			}
		}
	}
	
    public String translateString(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
