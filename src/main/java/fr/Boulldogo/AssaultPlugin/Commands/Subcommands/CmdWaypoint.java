package fr.Boulldogo.AssaultPlugin.Commands.Subcommands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;

import fr.Boulldogo.AssaultPlugin.AssaultPlugin;
import fr.Boulldogo.AssaultPlugin.Commands.Abstract.AssaultSubcommand;
import fr.Boulldogo.AssaultPlugin.Utils.Assault;
import fr.Boulldogo.AssaultPlugin.Utils.AssaultManager;

public class CmdWaypoint extends AssaultSubcommand {
	
	public CmdWaypoint() {
		this.setName("waypoint")
		    .setPermission("waypoint");
	}

	@Override
	public void execute(Player player) {
		AssaultPlugin plugin = this.getPlugin();
        Faction playerFac = FPlayers.getInstance().getByPlayer(player).getFaction();
        
        if(playerFac.isWilderness()) {
            player.sendMessage(prefix + ChatColor.RED + "You must be in a faction to execute that command !");
            return;
        }
        
        if(!AssaultManager.isFactionInAssault(playerFac)) {
            player.sendMessage(prefix + ChatColor.RED + "You can't execute this command because you are not in assault !");
            return;
        }
        
		if(!this.getPlugin().getConfig().getBoolean("capturable-zone.enable-zones")) {
			player.sendMessage(prefix + ChatColor.RED + "This feature is disabled !");
			return;
		}
		
        if(!plugin.getConfig().getBoolean("capturable-zone.create-random-waypoints")) {
			player.sendMessage(prefix + ChatColor.RED + "This feature is disabled !");
			return;
        }
        
        Assault assault = AssaultManager.getFactionAssault(playerFac);
        
        if(assault.zoneSide.equals(AssaultManager.getSide(playerFac))) {
			player.sendMessage(prefix + ChatColor.RED + "You can't teleport to assault waypoint when zone is on your side !");
			return;
        }
        player.teleport(assault.zoneWaypoint);
		player.sendMessage(prefix + ChatColor.GREEN + "Sever correctly teleport you at assault waypoint !");
		return;
	}
}
