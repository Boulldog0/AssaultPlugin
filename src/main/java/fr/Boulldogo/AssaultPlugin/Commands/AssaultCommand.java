package fr.Boulldogo.AssaultPlugin.Commands;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;

import fr.Boulldogo.AssaultPlugin.AssaultPlugin;
import fr.Boulldogo.AssaultPlugin.Commands.Abstract.AssaultSubcommand;
import fr.Boulldogo.AssaultPlugin.Events.AssaultStartEvent;
import fr.Boulldogo.AssaultPlugin.Utils.Assault;
import fr.Boulldogo.AssaultPlugin.Utils.AssaultManager;

public class AssaultCommand implements CommandExecutor, TabCompleter {

    private final AssaultPlugin plugin;

    public AssaultCommand(AssaultPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
        if(!(sender instanceof Player)) {
            plugin.getLogger().info("Only players can use that command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if(args.length < 1) {
        	player.sendMessage(ChatColor.RED + "Invalid command ! Type /assault help for more help");
        	return true;
        }
        
        String subcommand = args[0];
        
        for(AssaultSubcommand command : SubcommandManager.getCommands()) {
        	if(command.getName().equals(subcommand)) {
        		int requiredLenght = 1 + command.getRequiredArguments().size();
        		if(args.length < requiredLenght) {
        			LinkedList<String> arguments = command.getRequiredArguments();
        			StringBuilder b = new StringBuilder();
        			for(String argument : arguments) {
        				b.append("<" + argument + "> ");
        			}
        			player.sendMessage(ChatColor.RED + "Correct usage : /assault " + subcommand + " " + b.toString());
        			return true;
        		}
        		
        		command.setSender(player);
        		for(String argument : args) {
        			command.addArgument(argument);
        		}
        		if(!command.playerCanExecute(player)) {
        			player.sendMessage(ChatColor.RED + "You can't execute this command (Missing Permissions) !");
        			return true;
        		}
        		command.execute(player);
        		command.resetCommand();
        		return true;
        	}
        }
    	exeucteStartAssaultCmd(player, plugin, args);    
        return true;
    }
    
    private void exeucteStartAssaultCmd(Player player, AssaultPlugin plugin, String[] args) {	
    	String prefix = AssaultPlugin.getInstance().getConfig().getBoolean("use-prefix") ? translateString(AssaultPlugin.getInstance().getConfig().getString("prefix")) : "";
        String targetName = args[0];
        Player targetPlayer = Bukkit.getPlayer(targetName);
        Faction faction;
        
        if(targetPlayer == null || !targetPlayer.isOnline()) {
            faction = Factions.getInstance().getBestTagMatch(targetName);
        } else {
            faction = FPlayers.getInstance().getByPlayer(targetPlayer).getFaction();
        }

        Faction playerFac = FPlayers.getInstance().getByPlayer(player).getFaction();
        
        if(faction == null || faction.isWilderness() || faction.isWarZone()) {
            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.cannot_assault_staff_or_wild_faction")));
            return;
        }

        if(playerFac.isWilderness()) {
            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.cannot_assault_in_wilderness")));
            return;
        }

        int factionCount = faction.getOnlinePlayers().size();
        int playerFacCount = playerFac.getOnlinePlayers().size();

        if(factionCount < plugin.getConfig().getInt("minimum_defense_faction_connected_count")) {
            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.not_enought_online_players")));
            return;
        }
        
        Role playerRole = FPlayers.getInstance().getByPlayer(player).getRole();
        Role role = Role.fromString(plugin.getConfig().getString("minimum_role_allowed_for_assault"));
        if(!isRoleValid(playerRole, role)) {
            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_havnt_good_role")));
            return;
        }

        if(playerFacCount < plugin.getConfig().getInt("minimum_attack_faction_connected_count")) {
            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.not_enought_online_players_in_your_faction")));
            return;
        }

        if(AssaultManager.isFactionInAssaultOrJoinAssault(faction)) {
            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_cant_assault_if_it_are_in_assault")));
            return;
        }

        if(AssaultManager.isFactionInAssaultOrJoinAssault(playerFac)) {
            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_cant_assault_if_you_are_in_assault")));
            return;
        }

        if(plugin.getConfig().contains("assault.cooldown." + playerFac.getTag() + "." + faction.getTag())) {
            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_have_cooldown_for_assault_this_faction")
                    .replace("%m", String.valueOf(plugin.getConfig().getInt("assault.cooldown." + playerFac.getTag() + "." + faction.getTag() + ".min")))
                    .replace("%s", String.valueOf(plugin.getConfig().getInt("assault.cooldown." + playerFac.getTag() + "." + faction.getTag() + ".sec")))));
            return;
        }

        Relation relation = playerFac.getRelationTo(faction);

        if(relation != Relation.ENEMY) {
            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_cant_in_enemy_with_this_faction")
                    .replace("%m", String.valueOf(plugin.getConfig().getInt("assault.cooldown." + playerFac.getTag() + "." + faction.getTag() + ".min")))
                    .replace("%s", String.valueOf(plugin.getConfig().getInt("assault.cooldown." + playerFac.getTag() + "." + faction.getTag() + ".sec")))));
            return;
        }
        
        if(plugin.getConfig().contains("cooldowns." + playerFac.getTag() + "." + faction.getTag())) {
            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.they_are_cooldown_with_this_faction")
                    .replace("%m", String.valueOf(plugin.getConfig().getInt("cooldowns." + playerFac.getTag() + "." + faction.getTag())))));
            return;
        }
        
        if(plugin.getConfig().getBoolean("use-countdown-of-non-assault")) {
            int dayWithoutAssault = plugin.getConfig().getInt("non-assault-countdown");

            long attackCreationTime = playerFac.getFoundedDate();
            long defenseCreationTime = faction.getFoundedDate();

            LocalDate attackCreationDate = Instant.ofEpochSecond(attackCreationTime)
                                                   .atZone(ZoneId.systemDefault())
                                                   .toLocalDate();
            LocalDate defenseCreationDate = Instant.ofEpochSecond(defenseCreationTime)
                                                    .atZone(ZoneId.systemDefault())
                                                    .toLocalDate();

            LocalDate currentDate = LocalDate.now();

            long daysSinceAttackCreation = ChronoUnit.DAYS.between(attackCreationDate, currentDate);
            long daysSinceDefenseCreation = ChronoUnit.DAYS.between(defenseCreationDate, currentDate);

            if(daysSinceDefenseCreation < dayWithoutAssault) {
            	player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.assault_non_agression_countdown")));
            	return;
            } 
            
            if(daysSinceAttackCreation < dayWithoutAssault) {
            	player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.assault_non_agression_countdown")));
            	return;
            } 
        }

        String startTime = (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > 10 ? Calendar.getInstance().get(Calendar.HOUR_OF_DAY) : "0" + Calendar.getInstance().get(Calendar.HOUR_OF_DAY))  + ":" + (Calendar.getInstance().get(Calendar.MINUTE) > 10 ? Calendar.getInstance().get(Calendar.MINUTE) : "0" +  Calendar.getInstance().get(Calendar.MINUTE));
        Bukkit.broadcastMessage(prefix + translateString(plugin.getConfig().getString("messages.start_assault")
                .replace("%f", playerFac.getTag())
                .replace("%df", faction.getTag())
                .replace("%h", startTime)));
        
        Assault assault = new Assault(playerFac, faction);
        assault.minutesRemaining = plugin.getConfig().getInt("duration_of_assault");
        assault.secondsRemaining = 0;       
        
        AssaultManager.assaults.add(assault);
        
        if(plugin.getConfig().getBoolean("play_sound")) {
            playerFac.getOnlinePlayers().forEach(member -> member.playSound(member.getLocation(), Sound.valueOf(plugin.getConfig().getString("played_sound")), 4.0F, 4.0F));
            faction.getOnlinePlayers().forEach(member -> member.playSound(member.getLocation(), Sound.valueOf(plugin.getConfig().getString("played_sound")), 4.0F, 4.0F));
        }

        plugin.getConfig().set("assault.start_time." + playerFac.getTag(), startTime);
        plugin.saveConfig();
        
        AssaultStartEvent startEvent = new AssaultStartEvent(playerFac, faction, player);
        Bukkit.getServer().getPluginManager().callEvent(startEvent);
        
        int attackAssault = plugin.getConfig().contains("stats." + playerFac.getTag() + ".total_assaults") ? plugin.getConfig().getInt("stats." + playerFac.getTag() + ".total_assaults") : 0;
        int defenseAssault = plugin.getConfig().contains("stats." + faction.getTag() + ".total_assaults") ? plugin.getConfig().getInt("stats." + faction.getTag() + ".total_assaults") : 0;
        int weeklyAttackAssault = plugin.getConfig().contains("stats." + playerFac.getTag() + ".weekly_assaults") ? plugin.getConfig().getInt("stats." + playerFac.getTag() + ".weekly_assaults") : 0;
        int weeklyDefenseAssault = plugin.getConfig().contains("stats." + faction.getTag() + ".weekly_assaults") ? plugin.getConfig().getInt("stats." + faction.getTag() + ".weekly_assaults") : 0;
        
        plugin.getConfig().set("stats." + playerFac.getTag() + ".total_assaults", attackAssault + 1);
        plugin.getConfig().set("stats." + faction.getTag() + ".total_assaults", defenseAssault + 1);
        plugin.getConfig().set("stats." + playerFac.getTag() + ".total_weekly", weeklyAttackAssault + 1);
        plugin.getConfig().set("stats." + faction.getTag() + ".total_weekly", weeklyDefenseAssault + 1);
        
        int cooldownTime = plugin.getConfig().getInt("cooldown_behind_assaults");
        plugin.getConfig().set("cooldowns." + playerFac.getTag() + "." + faction.getTag(), cooldownTime);
        plugin.saveConfig();

        if(plugin.getConfig().getBoolean("enable_assault_scoreboard")) {
            AssaultManager.createScoreboard(assault, null, false);
        }
        
        AssaultManager.changeZone(assault, faction, null);
        return;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> facTags = new ArrayList<>();
        
        Factions.getInstance().getAllFactions().forEach(faction -> {
        	facTags.add(faction.getTag());
        });
        
        if(args.length == 1) {
            completions.add("help");
            completions.add("list");
            completions.add("admin");
            completions.add("ranking");
            completions.add("join");
            completions.add("accept");
            completions.add("zone");
            completions.addAll(facTags);
            completions = getSortedCompletions(completions, args[0]);
        } else if(args[0].equals("admin")) {
            if(args.length == 2) {
                completions.add("stop");
                completions.add("resetcd");
                completions.add("setzone");
                completions.add("modifyelo");
                completions = getSortedCompletions(completions, args[1]);
            } else if(args.length == 3) {
                completions.addAll(getFactionNames());
                completions = getSortedCompletions(completions, args[2]);
            } else if(args.length == 4 && args[1].equals("resetcd")) {
                completions.addAll(getFactionNames());
                completions = getSortedCompletions(completions, args[3]);
            }
        }
        
        return completions;
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

    private List<String> getSortedCompletions(List<String> completions, final String input) {
        if(input == null || input.isEmpty()) {
            Collections.sort(completions, String.CASE_INSENSITIVE_ORDER);
            return completions;
        }
        Collections.sort(completions, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                boolean s1StartsWith = s1.toLowerCase().startsWith(input.toLowerCase());
                boolean s2StartsWith = s2.toLowerCase().startsWith(input.toLowerCase());
                
                if(s1StartsWith && !s2StartsWith) {
                    return -1;
                } else if(!s1StartsWith && s2StartsWith) {
                    return 1;
                } else {
                    return s1.compareToIgnoreCase(s2);
                }
            }
        });
        return completions;
    }

    private List<String> getFactionNames() {
        List<String> factionNames = new ArrayList<>();
        for (Faction faction : Factions.getInstance().getAllFactions()) {
            if(!faction.isWilderness() && !faction.isWarZone()) {
                factionNames.add(faction.getTag());
            }
        }
        factionNames.sort(String::compareToIgnoreCase); 
        return factionNames;
    }

    public String translateString(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}


