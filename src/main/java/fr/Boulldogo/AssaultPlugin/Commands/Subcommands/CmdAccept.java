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

public class CmdAccept extends AssaultSubcommand {
	
	public CmdAccept() {
		this.setName("accept")
		    .setPermission("accept")
		    .addRequiredArgument("faction");
	}

	@Override
	public void execute(Player player) {
		AssaultPlugin plugin = this.getPlugin();

        Faction faction = this.getArgAsFaction(0);
        if(faction == null || faction.isWilderness()) {
            player.sendMessage(prefix + ChatColor.RED + translateString(plugin.getConfig().getString("messages.this_faction_dosnt_exists")));
            return;
        }

        Faction playerFac = FPlayers.getInstance().getByPlayer(player).getFaction();
        
        if(!AssaultManager.isFactionInAssault(playerFac)) {
            player.sendMessage(prefix + ChatColor.RED + "You can't accept this faction you are not in assault !");
            return;
        }
        
        Assault assault = AssaultManager.getFactionAssault(playerFac);

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
        
        boolean attackSide = assault.belligerentAttackFaction.equals(playerFac);

        if(attackSide ? assault.waitingAttackJoins.contains(faction) : assault.waitingDefenseJoins.contains(faction)) {
            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.this_facion_are_not_request_join_assault")));
            return;
        }
        
        if(AssaultManager.isFactionInAssaultOrJoinAssault(faction)) {
            player.sendMessage(prefix + ChatColor.RED + "You can't accept this faction because it is already in assault !");
            if(attackSide) {
            	assault.waitingAttackJoins.remove(faction);
            } else {
            	assault.waitingDefenseJoins.remove(faction);
            }
            return;
        }
        
        if(attackSide) {
        	assault.attackJoins.add(faction);
        	assault.waitingAttackJoins.remove(faction);
        } else {
        	assault.defenseJoins.add(faction);
        	assault.waitingDefenseJoins.remove(faction);
        }
        Bukkit.broadcastMessage(prefix + translateString(plugin.getConfig().getString("messages.faction_join_assault").replace("%f", faction.getTag()).replace("%pf", playerFac.getTag())));
        for(Player p : faction.getOnlinePlayers()) {
        	AssaultManager.createScoreboard(assault, p, true);
        }
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
