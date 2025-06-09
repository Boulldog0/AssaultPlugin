package fr.Boulldogo.AssaultPlugin.Commands.Subcommands;

import java.util.List;

import org.bukkit.entity.Player;

import com.massivecraft.factions.Faction;

import fr.Boulldogo.AssaultPlugin.Commands.Abstract.AssaultSubcommand;
import fr.Boulldogo.AssaultPlugin.Utils.Assault;
import fr.Boulldogo.AssaultPlugin.Utils.AssaultManager;

public class CmdList extends AssaultSubcommand {
	
	public CmdList() {
		this.setName("list")
		.setPermission("list");
	}

	@Override
	public void execute(Player player) {
    	List<Assault> assaultList = AssaultManager.assaults;
        if(assaultList.size() > 0) {
            for (int z = 0; z < assaultList.size(); z++) {
            	Assault assault = assaultList.get(z);
                Faction attackFac = assault.belligerentAttackFaction;
                Faction defenseFac = assault.belligerentDefenseFaction;
                int minutes = assault.minutesRemaining;
                int seconds = assault.secondsRemaining;
                
                String timeRemaining = (minutes > 0 ? minutes + "m" + seconds + "s" : seconds + "s");
                player.sendMessage(translateString("&7" + attackFac.getTag() + " (" + assault.attackScore + ")" + " VS " + defenseFac.getTag() + " (" + assault.defenseScore + ")" + " (" + timeRemaining + ")"));
            }
        } else {
            player.sendMessage(prefix + translateString(this.getPlugin().getConfig().getString("messages.assault_list_any_assault")));
        }
	}
}
