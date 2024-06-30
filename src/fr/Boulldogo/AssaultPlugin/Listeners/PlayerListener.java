package fr.Boulldogo.AssaultPlugin.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;

import fr.Boulldogo.AssaultPlugin.Main;
import fr.Boulldogo.AssaultPlugin.Commands.AssaultCommand;

public class PlayerListener implements Listener {
	
	private final Main plugin;
	
	public PlayerListener(Main plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamagedEvent(EntityDamageByEntityEvent e) {
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
						if(AssaultCommand.attackAssaultList.isEmpty()) {
							e.setCancelled(true);
							damager.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.cant_hit_outside_assault")));
							return;
						} else if(!AssaultCommand.attackAssaultList.contains(damagedFac)
						&& !AssaultCommand.defenseAssaultList.contains(damagedFac)
						&& !AssaultCommand.attackJoinList.contains(damagedFac)
						&& !AssaultCommand.defenseJoinList.contains(damagedFac)) {
							e.setCancelled(true);
							damager.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.cant_hit_outside_assault")));
							return;
						} else {
							if(!AssaultCommand.attackAssaultList.contains(damagerFac)
									&& !AssaultCommand.defenseAssaultList.contains(damagerFac)
									&& !AssaultCommand.attackJoinList.contains(damagerFac)
									&& !AssaultCommand.defenseJoinList.contains(damagerFac)) {
								e.setCancelled(true);
								damager.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.cant_hit_outside_assault")));
								return;
							}
						}
					}
				}
			}
			
			if(plugin.getConfig().getBoolean("disable-inter-assault-pvp")) {
				
				if(AssaultCommand.attackAssaultList.isEmpty()) return;
				
				else if(!AssaultCommand.attackAssaultList.contains(damagedFac)
				&& !AssaultCommand.defenseAssaultList.contains(damagedFac)
				&& !AssaultCommand.attackJoinList.contains(damagedFac)
				&& !AssaultCommand.defenseJoinList.contains(damagedFac)) {
					boolean isOriginalFaction = AssaultCommand.attackAssaultList.contains(damagedFac) || AssaultCommand.defenseAssaultList.contains(damagedFac);
					boolean attackSide = isOriginalFaction && AssaultCommand.attackAssaultList.contains(damagedFac);
					
					if(isOriginalFaction) {
						int index = 0;
						if(attackSide) {
							index = AssaultCommand.attackAssaultList.lastIndexOf(damagedFac);
						} else {
							index = AssaultCommand.defenseAssaultList.lastIndexOf(damagedFac);
						}
						
						Faction attackFac = AssaultCommand.attackAssaultList.get(index);
						Faction defenseFac = AssaultCommand.defenseAssaultList.get(index);
						
						if(AssaultCommand.attackAssaultList.contains(damagerFac) || AssaultCommand.defenseAssaultList.contains(damagerFac)) {
							if(!damagerFac.equals(attackFac) && !damagerFac.equals(defenseFac)) {
								e.setCancelled(true);
								damager.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.cant_hit_player_of_another_assault")));
							}
						}
					} else {
						
						if(!AssaultCommand.attackAssaultList.isEmpty()) {
							boolean attackJoinSide = AssaultCommand.attackJoinList.contains(damagedFac);
							
							int index = 0;
							if(attackJoinSide) {
								for(Faction fac : Factions.getInstance().getAllFactions()) {
									if(plugin.getConfig().getStringList("assault.join.attack." + fac.getTag()).contains(damagedFac.getTag())) {
										index = AssaultCommand.attackAssaultList.lastIndexOf(fac);
										break;
									}
								}
							} else {
								if(!AssaultCommand.defenseJoinList.isEmpty()) {
									for(Faction fac : Factions.getInstance().getAllFactions()) {
										if(plugin.getConfig().getStringList("assault.join.defense." + fac.getTag()).contains(damagedFac.getTag())) {
											index = AssaultCommand.defenseAssaultList.lastIndexOf(fac);
											break;
										}
									}
								}
							}
							
							Faction attackSideFac = AssaultCommand.attackAssaultList.get(index);
							Faction defenseSideFac = AssaultCommand.defenseAssaultList.get(index);
							
							if(AssaultCommand.attackAssaultList.contains(damagerFac) || AssaultCommand.defenseAssaultList.contains(damagerFac)) {
								if(!damagerFac.equals(attackSideFac) && !damagerFac.equals(defenseSideFac)) {
									e.setCancelled(true);
									damager.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.cant_hit_player_of_another_assault")));
									return;
								}
							}
							
							boolean damagerAttackJoinSide = AssaultCommand.attackJoinList.contains(damagedFac);
							
							int damagerIndex = 0;
							if(damagerAttackJoinSide) {
								for(Faction fac : Factions.getInstance().getAllFactions()) {
									if(plugin.getConfig().getStringList("assault.join.attack." + fac.getTag()).contains(damagedFac.getTag())) {
										damagerIndex = AssaultCommand.attackAssaultList.lastIndexOf(fac);
										break;
									}
								}
							} else {
								if(!AssaultCommand.defenseJoinList.isEmpty()) {
									for(Faction fac : Factions.getInstance().getAllFactions()) {
										if(plugin.getConfig().getStringList("assault.join.defense." + fac.getTag()).contains(damagedFac.getTag())) {
											damagerIndex = AssaultCommand.defenseAssaultList.lastIndexOf(fac);
											break;
										}
									}
								}
							}
							
							Faction damagerAttackSide = AssaultCommand.attackAssaultList.get(damagerIndex);
							Faction damagerDefenseSide = AssaultCommand.defenseAssaultList.get(damagerIndex);
							
							if(!attackSideFac.equals(damagerAttackSide) && !defenseSideFac.equals(damagerDefenseSide)) {
								e.setCancelled(true);
								damager.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.cant_hit_player_of_another_assault")));
								return;
							}
						}
					}
				}
			}
		}
	}
	
    public String translateString(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
