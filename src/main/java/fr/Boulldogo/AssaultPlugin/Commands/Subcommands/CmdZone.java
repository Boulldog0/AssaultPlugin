package fr.Boulldogo.AssaultPlugin.Commands.Subcommands;

import org.bukkit.entity.Player;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;

import fr.Boulldogo.AssaultPlugin.Commands.Abstract.AssaultSubcommand;
import fr.Boulldogo.AssaultPlugin.Utils.Assault;
import fr.Boulldogo.AssaultPlugin.Utils.AssaultManager;
import net.md_5.bungee.api.ChatColor;
public class CmdZone extends AssaultSubcommand {
	
	public CmdZone() {
		this.setName("zone")
		.setPermission("zone");
	}

	@Override
	public void execute(Player player) {
		Faction playerFac = FPlayers.getInstance().getByPlayer(player).getFaction();
		if(!AssaultManager.isFactionInAssaultOrJoinAssault(playerFac)) {
			player.sendMessage(prefix + ChatColor.RED + "You must be in assault for perform this command !");
			return;
		}
		
		if(!this.getPlugin().getConfig().getBoolean("capturable-zone.enable-zones")) {
			player.sendMessage(prefix + ChatColor.RED + "This feature is disabled !");
			return;
		}
		
		Assault assault = AssaultManager.getFactionAssault(playerFac);
		
		if(assault.zone == null) {
			player.sendMessage(prefix + ChatColor.RED + "No zone found at this time !");
			return;
		}
		
		String facName = assault.zone.getTerritory().getTag();
		String locX = String.valueOf(assault.zone.getLoc().getBlockX());
		String locZ = String.valueOf(assault.zone.getLoc().getBlockZ());
		String message = this.getPlugin().getConfig().getString("messages.zone-location").replace("%faction", facName).replace("%location", locX + "/" + locZ);
		player.sendMessage(prefix + translateString(message));
	}
}
