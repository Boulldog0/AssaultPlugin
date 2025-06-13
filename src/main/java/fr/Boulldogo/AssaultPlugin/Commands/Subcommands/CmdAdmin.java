package fr.Boulldogo.AssaultPlugin.Commands.Subcommands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;

import fr.Boulldogo.AssaultPlugin.AssaultPlugin;
import fr.Boulldogo.AssaultPlugin.Commands.Abstract.AssaultSubcommand;
import fr.Boulldogo.AssaultPlugin.Utils.Assault;
import fr.Boulldogo.AssaultPlugin.Utils.AssaultManager;
import fr.Boulldogo.AssaultPlugin.Utils.AssaultManager.AssaultSide;

public class CmdAdmin extends AssaultSubcommand {
	
	public CmdAdmin() {
		this.setName("admin")
		    .setPermission("admin")
		    .addRequiredArgument("subcommand");
	}

	@Override
	public void execute(Player player) {
		AssaultPlugin plugin = this.getPlugin();
        if(!this.playerCanExecute(player)) {
            player.sendMessage(prefix + translateString(plugin.getConfig().getString("no-permission")));
            return;
        }
        
        String command = this.getArgAsString(0);
        
        if(command.equals("stop")) {
            if(!this.isArgSet(2)) {
                player.sendMessage(prefix + ChatColor.RED + "Correct arguments: /assault admin stop <faction>");
                return;
            }

            Faction faction = this.getArgAsFaction(1);
            if(faction == null || faction.isWilderness()) {
                player.sendMessage(prefix + ChatColor.RED + "You cannot stop a wilderness assault!");
                return;
            }

            if(!AssaultManager.isFactionInAssault(faction)) {
                player.sendMessage(prefix + ChatColor.RED + "This faction is not in assault!");
                return;
            }
            
            Assault assault = AssaultManager.getFactionAssault(faction);
            AssaultManager.stopAssault(assault, true);
            player.sendMessage(prefix + ChatColor.GREEN + "Assault stopped for faction " + faction.getTag() + "!");
        } else if(command.equals("resetcd")) {
            if(!this.isArgSet(3)) {
                player.sendMessage(prefix + ChatColor.RED + "Correct arguments: /assault admin resetcd <faction> <faction_assault_to_reset>");
                return;
            }

            Faction faction = this.getArgAsFaction(1);
            if(faction == null || faction.isWilderness()) {
                player.sendMessage(prefix + ChatColor.RED + "You cannot reset assault cooldown for a wilderness faction!");
                return;
            }

            Faction aFaction = this.getArgAsFaction(2);
            if(aFaction == null || aFaction.isWilderness()) {
                player.sendMessage(prefix + ChatColor.RED + "You cannot reset assault cooldown for a wilderness faction!");
                return;
            }

            if(!plugin.getConfig().contains("cooldowns." + faction.getTag() + "." + aFaction.getTag())) {
                player.sendMessage(prefix + ChatColor.RED + "This faction doesn't have cooldown for faction " + aFaction.getTag() + "!");
                return;
            }

            plugin.getConfig().set("cooldowns." + faction.getTag() + "." + aFaction.getTag(), null);
            plugin.saveConfig();
            aFaction.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.reset_cooldown_by_faction").replace("%f", faction.getTag()).replace("%s", player.getName()))));
            faction.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.reset_cooldown_for_faction").replace("%f", aFaction.getTag()).replace("%s", player.getName()))));
            player.sendMessage(prefix + "Cooldown of faction " + faction.getTag() + " for assault faction " + aFaction.getTag() + " successfully reset!");
        } else if(command.equals("modifyelo")) {
        	if (!this.isArgSet(5)) {
        	    player.sendMessage(prefix + ChatColor.RED + "Usage: /assault admin modifyelo <faction> <add/remove> <win/loose/points> <value>");
        	    return;
        	}

        	Faction faction = this.getArgAsFaction(1);

        	if(faction.isWilderness()) {
        	    player.sendMessage(prefix + ChatColor.RED + "You can't modify elo of wilderness faction!");
        	    return;
        	}

        	String path = "ranking." + faction.getTag();
        	if(!plugin.getConfig().contains(path)) {
        	    plugin.getConfig().set(path + ".points", 0);
        	    plugin.getConfig().set(path + ".win", 0);
        	    plugin.getConfig().set(path + ".loose", 0);
        	    plugin.saveConfig();
        	    player.sendMessage(prefix + ChatColor.YELLOW + "Ranking data initialized. Retry the command.");
        	    return;
        	}

        	String action = this.getArgAsString(2).toLowerCase();
        	if(!action.equals("add") && !action.equals("remove")) {
        	    player.sendMessage(prefix + ChatColor.RED + "Invalid action: " + action + ". Must be 'add' or 'remove'");
        	    return;
        	}

        	String type = this.getArgAsString(3).toLowerCase();
        	if(!type.equals("win") && !type.equals("loose") && !type.equals("points")) {
        	    player.sendMessage(prefix + ChatColor.RED + "Invalid type: " + type + ". Must be 'win', 'loose' or 'points'");
        	    return;
        	}

        	int value = this.getArgAsInt(4);
        	if(value <= 0) {
        	    player.sendMessage(prefix + ChatColor.RED + "Value must be > 0. Got: " + value);
        	    return;
        	}

        	String fullPath = path + "." + type;
        	int current = plugin.getConfig().getInt(fullPath);
        	int result = action.equals("add") ? (current + value) : (current - value);

        	player.sendMessage(prefix + ChatColor.GRAY + "Current " + type + ": " + current + ", Operation: " + action + " " + value + " -> Result: " + result);

        	if(result < 0) {
        	    player.sendMessage(prefix + ChatColor.RED + "Cannot apply operation. Result is negative.");
        	    return;
        	}

        	plugin.getConfig().set(fullPath, result);
        	plugin.saveConfig();
        	player.sendMessage(prefix + ChatColor.GREEN + "Successfully " + (action.equals("add") ? "added" : "removed") + " " + value + " to " + type + " for faction " + faction.getTag() + ".");
        } else if(command.equals("setzone")) {
            if(!this.isArgSet(2)) {
                player.sendMessage(prefix + ChatColor.RED + "Correct arguments: /assault admin setzone <one_of_faction_in_assault>");
                return;
            }
            
    		if(!this.getPlugin().getConfig().getBoolean("capturable-zone.enable-zones")) {
    			player.sendMessage(prefix + ChatColor.RED + "This feature is disabled !");
    			return;
    		}

            Faction faction = this.getArgAsFaction(1);
            if(faction == null || faction.isWilderness()) {
                player.sendMessage(prefix + ChatColor.RED + "You cannot set a zone for a null faction !");
                return;
            }
            
            if(!AssaultManager.isFactionInAssault(faction)) {
                player.sendMessage(prefix + ChatColor.RED + "This faction ins'nt an assault belligerent faction !");
                return;
            }
            
            Assault assault = AssaultManager.getFactionAssault(faction);
            Location loc = player.getLocation();
            
            Faction territory = Board.getInstance().getFactionAt(FLocation.wrap(loc));
            
            if(!AssaultManager.isFactionInAssault(territory)) {
                player.sendMessage(prefix + ChatColor.RED + "This faction ins'nt an assault belligerent faction !");
                return;
            }
            
            if(!AssaultManager.isSameAssaultFaction(faction, territory)) {
                player.sendMessage(prefix + ChatColor.RED + "This territory don't belongs to a facion who is'nt in the same assault than given faction !");
                return;
            }
            AssaultManager.changeZone(assault, territory, loc);
            player.sendMessage(prefix + ChatColor.GREEN + "Capture zone correctly moved at your position.");
        } else if(command.equals("changewaypoint")) {
            if(!this.isArgSet(2)) {
                player.sendMessage(prefix + ChatColor.RED + "Correct arguments: /assault admin changewaypoint <one_of_faction_in_assault>");
                return;
            }

            Faction faction = this.getArgAsFaction(1);
            if(faction == null || faction.isWilderness()) {
                player.sendMessage(prefix + ChatColor.RED + "You cannot change waypoint for a null faction !");
                return;
            }
            
            if(!AssaultManager.isFactionInAssault(faction)) {
                player.sendMessage(prefix + ChatColor.RED + "This faction ins'nt an assault belligerent faction !");
                return;
            }
            
            Assault assault = AssaultManager.getFactionAssault(faction);
            
    		if(!this.getPlugin().getConfig().getBoolean("capturable-zone.enable-zones")) {
    			player.sendMessage(prefix + ChatColor.RED + "This feature is disabled !");
    			return;
    		}
    		
            if(!plugin.getConfig().getBoolean("capturable-zone.create-random-waypoints")) {
    			player.sendMessage(prefix + ChatColor.RED + "This feature is disabled !");
    			return;
            }
            Faction territory = faction;
            AssaultSide side = AssaultManager.getSide(faction);   
            if(side.equals(assault.zoneSide)) {
            	if(side.equals(AssaultSide.ATTACK)) {
            		territory = assault.belligerentDefenseFaction;
            	} else {
            		territory = assault.belligerentAttackFaction;
            	}
            }
        	Location randomLoc = AssaultManager.getRandomLocationForFaction(territory);
        	if(randomLoc == null) {
        		assault.getAllPlayers().forEach(p -> {
        			p.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.no-loc-avaiable-for-waypoint")));
        		});
                player.sendMessage(prefix + ChatColor.RED + "No avaiable location found in faction territory.");
        		return;
        	}
        	
        	int attempts = 150;			
    		boolean isInZone = true;
    		while(isInZone && attempts > 0) {
    			attempts--;
    			randomLoc = AssaultManager.getRandomLocationForFaction(territory);
    			if(!assault.zone.isLocationInZone(randomLoc)) {
    				isInZone = false;
    			}
    		}
    		if(isInZone) {
        		assault.getAllPlayers().forEach(p -> {
        			p.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.no-loc-avaiable-for-waypoint")));
        		});
        		return;
    		}
    		
    		assault.zoneWaypoint = randomLoc;
    		int bX = randomLoc.getBlockX();
    		int bZ = randomLoc.getBlockZ();
    		String tname = territory.getTag();
    		assault.getAllPlayers().forEach(p -> {
    			if(!AssaultManager.getSide(FPlayers.getInstance().getByPlayer(p).getFaction()).equals(assault.zoneSide)) {
    				p.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.waypoint-loc").replace("%location", bX + "/" + bZ).replace("%faction", tname)));
    			} else {
    				p.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.waypoint-disappear")));
    			}
    		});
            player.sendMessage(prefix + ChatColor.GREEN + "Assault waypoint correctly moved at a new random position (" + bX + "/" + bZ + ").");
        }
	}

}
