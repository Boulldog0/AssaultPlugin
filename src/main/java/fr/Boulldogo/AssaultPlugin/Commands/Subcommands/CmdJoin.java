package fr.Boulldogo.AssaultPlugin.Commands.Subcommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;

import fr.Boulldogo.AssaultPlugin.AssaultPlugin;
import fr.Boulldogo.AssaultPlugin.Commands.Abstract.AssaultSubcommand;
import fr.Boulldogo.AssaultPlugin.Utils.Assault;
import fr.Boulldogo.AssaultPlugin.Utils.AssaultManager;

public class CmdJoin extends AssaultSubcommand {
	
	public CmdJoin() {
		this.setName("join")
		    .setPermission("join")
		    .addRequiredArgument("faction");
	}

	@Override
	public void execute(Player player) {
        Faction faction = this.getArgAsFaction(0);
        AssaultPlugin plugin = this.getPlugin();
        if(faction == null || faction.isWilderness()) {
            player.sendMessage(prefix + ChatColor.RED + translateString(plugin.getConfig().getString("messages.this_faction_dosnt_exists")));
            return;
        }
        
        Assault assault = AssaultManager.getFactionAssault(faction);

        Faction playerFac = FPlayers.getInstance().getByPlayer(player).getFaction();

        if(playerFac.isWilderness()) {
            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.cannot_assault_in_wilderness")));
            return;
        }

        Role playerRole = FPlayers.getInstance().getByPlayer(player).getRole();
        Role role = Role.fromString(plugin.getConfig().getString("minimum_role_allowed_for_join_assault"));
        if(!isRoleValid(playerRole, role)) {
            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_havnt_good_role")));
            return;
        }

        Relation relation = playerFac.getRelationTo(faction);

        if(relation != Relation.ALLY) {
            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_must_be_ally_with_this_faction")));
            return;
        }

        if(AssaultManager.isFactionInAssaultOrJoinAssault(playerFac)) {
            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_cant_join_assaut_if_you_are_in_assault")));
            return;
        }

        if(assault.waitingAttackJoins.contains(playerFac) || assault.waitingDefenseJoins.contains(playerFac)) {
            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.already_sent_request")));
            return;
        }

        if(assault.belligerentAttackFaction.equals(faction)) { 
            assault.waitingAttackJoins.add(playerFac);
        } else {
            assault.waitingDefenseJoins.add(playerFac);
        }
        Bukkit.broadcastMessage(prefix + plugin.getConfig().getString("messages.faction_request_join_assault").replace("%f", faction.getTag()).replace("%pf", playerFac.getTag()));
	}

    private boolean isRoleValid(Role playerRole, Role role) {
    	if(role == Role.RECRUIT) {
    		return true;
    	}
    	
    	if(role == Role.NORMAL) {
    		return playerRole == Role.NORMAL || playerRole == Role.MODERATOR || playerRole == Role.COLEADER || playerRole == Role.LEADER;
    	}
    	
    	if(role == Role.MODERATOR) {
    		return playerRole == Role.MODERATOR || playerRole == Role.COLEADER || playerRole == Role.LEADER;
    	}
    	
    	if(role == Role.COLEADER) {
    		return playerRole == Role.COLEADER || playerRole == Role.LEADER;
    	}
    	
    	if(role == Role.LEADER) {
    		return playerRole == Role.LEADER;
    	}
    	
    	return false;
    }
}
