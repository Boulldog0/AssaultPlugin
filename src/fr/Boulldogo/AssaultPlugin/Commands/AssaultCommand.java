package fr.Boulldogo.AssaultPlugin.Commands;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.DisplaySlot;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.struct.Relation;
import com.massivecraft.factions.struct.Role;

import fr.Boulldogo.AssaultPlugin.Main;
import fr.Boulldogo.AssaultPlugin.Events.AssaultLooseEvent;
import fr.Boulldogo.AssaultPlugin.Events.AssaultStartEvent;
import fr.Boulldogo.AssaultPlugin.Events.AssaultStopEvent;
import fr.Boulldogo.AssaultPlugin.Events.AssaultWinEvent;
import fr.Boulldogo.AssaultPlugin.Listeners.AssaultListener;
import fr.Boulldogo.AssaultPlugin.Utils.FactionRanking;

public class AssaultCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;
    private BukkitRunnable assaultVerification = null;

    public static List<Faction> attackAssaultList = new ArrayList<>();
    public static List<Faction> defenseAssaultList = new ArrayList<>();
    public static List<Faction> attackJoinList = new ArrayList<>();
    public static List<Faction> defenseJoinList = new ArrayList<>();
    
    int operationCount = 0;

    public AssaultCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
        if(!(sender instanceof Player)) {
            plugin.getLogger().info("Only players can use that command!");
            return true;
        }

        String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
        Player player = (Player) sender;

        if(args.length < 1) {
            player.sendMessage(prefix + ChatColor.RED + "Please give correct argument. Type /assault help for more help.");
            return true;
        }

        String subCommand = args[0];

        if(subCommand.equals("help")) {
            player.sendMessage(translateString(plugin.getConfig().getString("help_header")));
            player.sendMessage(translateString(plugin.getConfig().getString("help_1")));
            player.sendMessage(translateString(plugin.getConfig().getString("help_2")));
            player.sendMessage(translateString(plugin.getConfig().getString("help_3")));
            player.sendMessage(translateString(plugin.getConfig().getString("help_4")));
            player.sendMessage(translateString(plugin.getConfig().getString("help_5")));
            player.sendMessage(translateString(plugin.getConfig().getString("help_6")));
            player.sendMessage(translateString(plugin.getConfig().getString("help_7")));
            player.sendMessage(translateString(plugin.getConfig().getString("help_footer")));
            return true;
        } else if(subCommand.equals("list")) {
            if(attackAssaultList.size() > 0) {
                for (int z = 0; z < attackAssaultList.size(); z++) {
                    Faction attackFac = attackAssaultList.get(z);
                    Faction defenseFac = defenseAssaultList.get(z);
                    int minutes = plugin.getConfig().getInt("assault.time_roaming." + attackFac.getTag() + ".min");
                    int seconds = plugin.getConfig().getInt("assault.time_roaming." + attackFac.getTag() + ".sec");
                    String timeRemaining = (minutes > 0 ? minutes + "m" + seconds + "s" : seconds + "s");
                    player.sendMessage(translateString("&7" + attackFac.getTag() + " (" + AssaultListener.attackScoreList.get(z) + ")" + " VS " + defenseFac.getTag() + " (" + AssaultListener.defenseScoreList.get(z) + ")" + " (" + timeRemaining + ")"));
                }
            } else {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.assault_list_any_assault")));
            }
            return true;
        } else if(subCommand.equals("admin")) {
            if(!player.hasPermission("assault.admin")) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("no-permission")));
                return true;
            }

            if(args.length < 2) {
                player.sendMessage(prefix + ChatColor.RED + "Admin arguments: /assault admin <stop/resetcd/modifyelo> <args>");
                return true;
            }

            String command = args[1];
            if(command.equals("stop")) {
                if(args.length < 3) {
                    player.sendMessage(prefix + ChatColor.RED + "Correct arguments: /assault admin stop <faction>");
                    return true;
                }

                String facName = args[2];
                Faction faction = Factions.getInstance().getByTag(facName);
                if(faction == null || faction.isWilderness()) {
                    player.sendMessage(prefix + ChatColor.RED + "You cannot stop a wilderness assault!");
                    return true;
                }

                if(!attackAssaultList.contains(faction) && !defenseAssaultList.contains(faction)) {
                    player.sendMessage(prefix + ChatColor.RED + "This faction is not in assault!");
                    return true;
                }
                
                boolean isAttack = attackAssaultList.contains(faction) ? true : false;
                Faction finalStopFaction = isAttack ? defenseAssaultList.get(attackAssaultList.indexOf(faction)) : faction;

                stopAssault(finalStopFaction);
                player.sendMessage(prefix + ChatColor.GREEN + "Assault stopped for faction " + faction.getTag() + "!");
            } else if(command.equals("resetcd")) {
                if(args.length < 4) {
                    player.sendMessage(prefix + ChatColor.RED + "Correct arguments: /assault admin resetcd <faction> <faction_assault_to_reset>");
                    return true;
                }

                String facName = args[2];
                Faction faction = Factions.getInstance().getByTag(facName);
                if(faction == null || faction.isWilderness()) {
                    player.sendMessage(prefix + ChatColor.RED + "You cannot reset assault cooldown for a wilderness faction!");
                    return true;
                }

                String aFacName = args[3];
                Faction aFaction = Factions.getInstance().getByTag(aFacName);
                if(aFaction == null || aFaction.isWilderness()) {
                    player.sendMessage(prefix + ChatColor.RED + "You cannot reset assault cooldown for a wilderness faction!");
                    return true;
                }

                if(!plugin.getConfig().contains("cooldowns." + facName + "." + aFacName)) {
                    player.sendMessage(prefix + ChatColor.RED + "This faction doesn't have cooldown for faction " + aFacName + "!");
                    return true;
                }

                plugin.getConfig().set("cooldowns." + facName + "." + aFacName, null);
                plugin.saveConfig();
                aFaction.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.reset_cooldown_by_faction").replace("%f", facName).replace("%s", player.getName()))));
                faction.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.reset_cooldown_for_faction").replace("%f", aFacName).replace("%s", player.getName()))));
                player.sendMessage(prefix + "Cooldown of faction " + facName + " for assault faction " + aFacName + " successfully reset!");
            } else if(command.equals("modifyelo")) {
                if(args.length < 5) {
                    player.sendMessage(prefix + ChatColor.RED + "Correct arguments: /assault admin modifyelo <faction> <add/remove> <win/loose/points> <value>");
                    return true;
                }

                String facName = args[2];
                Faction faction = Factions.getInstance().getByTag(facName);
                if(faction == null || faction.isWilderness()) {
                    player.sendMessage(prefix + ChatColor.RED + "You cannot reset assault cooldown for a wilderness faction!");
                    return true;
                }
                
                if(!plugin.getConfig().contains("ranking." + faction.getTag())) {
                    player.sendMessage(prefix + ChatColor.RED + "This faction does not have ranking data ! Server create that, retry to execute command !");
                    plugin.getConfig().set("ranking." + faction.getTag() + ".points", 0);
                    plugin.getConfig().set("ranking." + faction.getTag() + ".win", 0);
                    plugin.getConfig().set("ranking." + faction.getTag() + ".loose", 0);
                    plugin.saveConfig();
                    return true;
                }
                
                String sc = args[3];
                
                if(!sc.equals("add") && !sc.equals("remove")) {
                    player.sendMessage(prefix + ChatColor.RED + "Correct arguments: /assault admin modifyelo <faction> <add/remove> <win/loose/points> <value>");
                    return true;
                }
                
                String n = args[4];
                if(!n.equals("win") && !n.equals("loose") && !n.equals("points")) {
                    player.sendMessage(prefix + ChatColor.RED + "Correct arguments: /assault admin modifyelo <faction> <add/remove> <win/loose/points> <value>");
                    return true;
                }
                
                String value = args[5];
                int v = Integer.parseInt(value);
                
                if(v <= 0) {
                    player.sendMessage(prefix + ChatColor.RED + "Choose number lower of 0 !");
                    return true;
                }
                
                if(n.equals("add")) {
                	int finalValue = plugin.getConfig().getInt("ranking." + faction.getTag() + "." + n) + v;
                	player.sendMessage(prefix + ChatColor.GREEN + "You have correctly add " + v + " points to faction " + faction.getTag() + " for " + n + " value.");
                    plugin.getConfig().set("ranking." + faction.getTag() + "." + n, finalValue);
                    plugin.saveConfig();
                }
                
                if(n.equals("remove")) {
                	int finalValue = plugin.getConfig().getInt("ranking." + faction.getTag() + "." + n) - v;
                	if(finalValue < 0) {
                        player.sendMessage(prefix + ChatColor.RED + "You can't set this value, because result is lower than 0");
                        return true;
                	}
                	player.sendMessage(prefix + ChatColor.GREEN + "You have correctly remove " + v + " points to faction " + faction.getTag() + " for " + n + " value.");
                    plugin.getConfig().set("ranking." + faction.getTag() + "." + n, finalValue);
                    plugin.saveConfig();
                }
                
            } 
        } else if(subCommand.equals("ranking")) {
            String header = translateString(plugin.getConfig().getString("ranking_header"));
            String lineTemplate = translateString(plugin.getConfig().getString("ranking_lines"));
            String footer = translateString(plugin.getConfig().getString("ranking_footer"));
            int maxEntries = plugin.getConfig().getInt("ranking_entries", 10);

            ConfigurationSection rankingSection = plugin.getConfig().getConfigurationSection("ranking");
            if(rankingSection == null) {
                sender.sendMessage(ChatColor.RED + "Aucune donnée de ranking trouvée.");
                return true;
            }

            List<FactionRanking> rankings = new ArrayList<>();
            for (String factionName : rankingSection.getKeys(false)) {
                int points = rankingSection.getInt(factionName + ".points");
                int wins = rankingSection.getInt(factionName + ".win");
                int losses = rankingSection.getInt(factionName + ".loose");
                rankings.add(new FactionRanking(factionName, points, wins, losses));
            }

            rankings.sort((a, b) -> Integer.compare(b.getPoints(), a.getPoints()));

            int entriesToShow = Math.min(rankings.size(), maxEntries);

            StringBuilder rankingMessage = new StringBuilder();
            rankingMessage.append(header).append("\n");
            for (int i = 0; i < entriesToShow; i++) {
                FactionRanking ranking = rankings.get(i);
                String line = lineTemplate
                        .replace("%c", String.valueOf(i + 1))
                        .replace("%f", ranking.getFactionName())
                        .replace("%p", String.valueOf(ranking.getPoints()))
                        .replace("%w", String.valueOf(ranking.getWins()))
                        .replace("%l", String.valueOf(ranking.getLosses()));
                rankingMessage.append(line).append("\n");
            }
            rankingMessage.append(footer);

            sender.sendMessage(rankingMessage.toString());
            return true;
        } else if(subCommand.equals("join") || subCommand.equals("accept")) {
            if(args.length < 2) {
                player.sendMessage(prefix + ChatColor.RED + "Correct arguments: /assault join <faction>");
                return true;
            }

            if(!subCommand.equals("accept")) {
                String facName = args[1];

                Faction faction = Factions.getInstance().getBestTagMatch(facName);
                if(faction == null || faction.isWilderness()) {
                    player.sendMessage(prefix + ChatColor.RED + translateString(plugin.getConfig().getString("messages.this_faction_dosnt_exists")));
                    return true;
                }

                Faction playerFac = FPlayers.getInstance().getByPlayer(player).getFaction();

                if(playerFac.isWilderness()) {
                    player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.cannot_assault_in_wilderness")));
                    return true;
                }

                Role playerRole = FPlayers.getInstance().getByPlayer(player).getRole();
                Role role = Role.fromString(plugin.getConfig().getString("minimum_role_allowed_for_join_assault"));
                if(!isRoleValid(playerRole, role)) {
                    player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_havnt_good_role")));
                    return true;
                }

                Relation relation = playerFac.getRelationTo(faction);

                if(relation != Relation.ALLY) {
                    player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_must_be_ally_with_this_faction")));
                    return true;
                }

                if(attackAssaultList.contains(playerFac) || defenseAssaultList.contains(playerFac)) {
                    player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_cant_join_assaut_if_you_are_in_assault")));
                    return true;
                }

                if(attackJoinList.contains(playerFac) || defenseJoinList.contains(playerFac)) {
                    player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_cant_join_assaut_if_you_are_in_assault")));
                    return true;
                }

                if(plugin.getConfig().getStringList("assault.waiting_join.attack." + faction.getTag()).contains(playerFac.getTag())) {
                    player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.already_sent_request")));
                    return true;
                }

                List<String> list;
                if(attackAssaultList.contains(faction)) {
                    list = plugin.getConfig().getStringList("assault.waiting_join.attack." + faction.getTag());
                    if(!list.contains(playerFac.getTag())) {
                        list.add(playerFac.getTag());
                        plugin.getConfig().set("assault.waiting_join.attack." + faction.getTag(), list);
                        plugin.saveConfig();
                        Bukkit.broadcastMessage(prefix + translateString(plugin.getConfig().getString("messages.faction_request_join_assault").replace("%f", faction.getTag()).replace("%pf", playerFac.getTag())));
                    }
                } else if(defenseAssaultList.contains(faction)) {
                    list = plugin.getConfig().getStringList("assault.waiting_join.defense." + faction.getTag());
                    if(!list.contains(playerFac.getTag())) {
                        list.add(playerFac.getTag());
                        plugin.getConfig().set("assault.waiting_join.defense." + faction.getTag(), list);
                        plugin.saveConfig();
                        Bukkit.broadcastMessage(prefix + plugin.getConfig().getString("messages.faction_request_join_assault").replace("%f", faction.getTag()).replace("%pf", playerFac.getTag()));
                    }
                } else {
                    player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.this_facion_is_not_in_assault")));
                    return true;
                }
            } else {
                if(args.length < 2) {
                    player.sendMessage(prefix + ChatColor.RED + "Correct arguments: /assault accept <faction>");
                    return true;
                }

                String facName = args[1];

                Faction faction = Factions.getInstance().getBestTagMatch(facName);
                if(faction == null || faction.isWilderness()) {
                    player.sendMessage(prefix + ChatColor.RED + translateString(plugin.getConfig().getString("messages.this_faction_dosnt_exists")));
                    return true;
                }

                Faction playerFac = FPlayers.getInstance().getByPlayer(player).getFaction();

                if(playerFac.isWilderness()) {
                    player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.cannot_assault_in_wilderness")));
                    return true;
                }

                Role playerRole = FPlayers.getInstance().getByPlayer(player).getRole();
                Role role = Role.fromString(plugin.getConfig().getString("minimum_role_allowed_for_join_assault"));
                if(!isRoleValid(playerRole, role)) {
                    player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_havnt_good_role")));
                    return true;
                }

                Relation relation = playerFac.getRelationTo(faction);

                if(relation != Relation.ALLY) {
                    player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_must_be_ally_with_this_faction")));
                    return true;
                }

                String side = (attackAssaultList.contains(playerFac) ? "attack" : "defense");

                if(!plugin.getConfig().getStringList("assault.waiting_join." + side + "." + playerFac.getTag()).contains(faction.getTag())) {
                    player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.this_facion_are_not_request_join_assault")));
                    return true;
                }

                List<String> joinList = plugin.getConfig().getStringList("assault.join." + side + "." + playerFac.getTag());
                if(joinList.isEmpty() || !joinList.isEmpty() && !joinList.contains(faction.getTag())) {
                    joinList.add(faction.getTag());
                    plugin.getConfig().set("assault.join." + side + "." + playerFac.getTag(), joinList);
                    plugin.saveConfig();
                    Bukkit.broadcastMessage(prefix + translateString(plugin.getConfig().getString("messages.faction_join_assault").replace("%f", faction.getTag()).replace("%pf", playerFac.getTag())));
                    createAllyScoreboard(faction, side, playerFac);
                    if(attackAssaultList.contains(playerFac)) {
                    	attackJoinList.add(faction);
                    } else if(defenseAssaultList.contains(playerFac)) {
                    	defenseJoinList.add(faction);
                    }
                } else if(joinList.contains(faction.getTag())) {
                    player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_cant_join_assaut_if_you_are_in_assault")));
                    return true;
                }
            }
        } else {
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
                return true;
            }

            if(playerFac.isWilderness()) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.cannot_assault_in_wilderness")));
                return true;
            }

            int factionCount = faction.getOnlinePlayers().size();
            int playerFacCount = playerFac.getOnlinePlayers().size();

            if(factionCount < plugin.getConfig().getInt("minimum_defense_faction_connected_count")) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.not_enought_online_players")));
                return true;
            }
            
            Role playerRole = FPlayers.getInstance().getByPlayer(player).getRole();
            Role role = Role.fromString(plugin.getConfig().getString("minimum_role_allowed_for_assault"));
            if(!isRoleValid(playerRole, role)) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_havnt_good_role")));
                return true;
            }

            if(playerFacCount < plugin.getConfig().getInt("minimum_attack_faction_connected_count")) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.not_enought_online_players_in_your_faction")));
                return true;
            }

            if(attackAssaultList.contains(faction) || defenseAssaultList.contains(faction)) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_cant_assault_if_it_are_in_assault")));
                return true;
            }

            if(attackAssaultList.contains(playerFac) || defenseAssaultList.contains(playerFac)) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_cant_assault_if_you_are_in_assault")));
                return true;
            }
            
            if(attackJoinList.contains(playerFac) || defenseJoinList.contains(playerFac)) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_cant_assault_if_you_are_in_assault")));
                return true;
            }

            if(plugin.getConfig().contains("assault.cooldown." + playerFac.getTag() + "." + faction.getTag())) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_have_cooldown_for_assault_this_faction")
                        .replace("%m", String.valueOf(plugin.getConfig().getInt("assault.cooldown." + playerFac.getTag() + "." + faction.getTag() + ".min")))
                        .replace("%s", String.valueOf(plugin.getConfig().getInt("assault.cooldown." + playerFac.getTag() + "." + faction.getTag() + ".sec")))));
                return true;
            }

            Relation relation = playerFac.getRelationTo(faction);

            if(relation != Relation.ENEMY) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_cant_in_enemy_with_this_faction")
                        .replace("%m", String.valueOf(plugin.getConfig().getInt("assault.cooldown." + playerFac.getTag() + "." + faction.getTag() + ".min")))
                        .replace("%s", String.valueOf(plugin.getConfig().getInt("assault.cooldown." + playerFac.getTag() + "." + faction.getTag() + ".sec")))));
                return true;
            }
            
            if(plugin.getConfig().contains("cooldowns." + playerFac.getTag() + "." + faction.getTag())) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.they_are_cooldown_with_this_faction")
                        .replace("%m", String.valueOf(plugin.getConfig().getInt("cooldowns." + playerFac.getTag() + "." + faction.getTag())))));
                return true;
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
                	return true;
                } 
                
                if(daysSinceAttackCreation < dayWithoutAssault) {
                	player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.assault_non_agression_countdown")));
                	return true;
                } 
            }

            String startTime = (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > 10 ? Calendar.getInstance().get(Calendar.HOUR_OF_DAY) : "0" + Calendar.getInstance().get(Calendar.HOUR_OF_DAY))  + ":" + (Calendar.getInstance().get(Calendar.MINUTE) > 10 ? Calendar.getInstance().get(Calendar.MINUTE) : "0" +  Calendar.getInstance().get(Calendar.MINUTE));
            Bukkit.broadcastMessage(prefix + translateString(plugin.getConfig().getString("messages.start_assault")
                    .replace("%f", playerFac.getTag())
                    .replace("%df", faction.getTag())
                    .replace("%h", startTime)));
            
            attackAssaultList.add(playerFac);
            defenseAssaultList.add(faction);
            
            if(plugin.getConfig().getBoolean("play_sound")) {
                playerFac.getOnlinePlayers().forEach(member -> member.playSound(member.getLocation(), Sound.valueOf(plugin.getConfig().getString("played_sound")), 4.0F, 4.0F));
                faction.getOnlinePlayers().forEach(member -> member.playSound(member.getLocation(), Sound.valueOf(plugin.getConfig().getString("played_sound")), 4.0F, 4.0F));
            }
            
            int duration = plugin.getConfig().getInt("duration_of_assault");
            List<String> emptyList = new ArrayList<>();
            plugin.getConfig().set("assault.time_roaming." + playerFac.getTag() + ".min", duration);
            plugin.getConfig().set("assault.time_roaming." + playerFac.getTag() + ".sec", 0);
            plugin.getConfig().set("assault.time_roaming." + faction.getTag() + ".min", duration);
            plugin.getConfig().set("assault.time_roaming." + faction.getTag() + ".sec", 0);
            plugin.getConfig().set("assault.start_time." + playerFac.getTag(), startTime);
            plugin.getConfig().set("assault.join.attack." + playerFac.getTag(), emptyList);
            plugin.getConfig().set("assault.join.defense." + faction.getTag(), emptyList);
            plugin.saveConfig();
            
            AssaultStartEvent startEvent = new AssaultStartEvent(playerFac, faction, player);
	        Bukkit.getServer().getPluginManager().callEvent(startEvent);
            
            AssaultListener.attackScoreList.add(0);
            AssaultListener.defenseScoreList.add(0);
            
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
                createScoreboard(playerFac, faction);
            }

            startCounter();
        }

        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if(args.length == 1) {
            completions.add("help");
            completions.add("list");
            completions.add("admin");
            completions.add("ranking");
            completions.add("join");
            completions.add("accept");
            completions = getSortedCompletions(completions, args[0]);
        } else if(args[0].equals("admin")) {
            if(args.length == 2) {
                completions.add("stop");
                completions.add("resetcd");
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

     
    public boolean isRoleValid(Role playerRole, Role role) {
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

    public void createScoreboard(Faction playerFac, Faction faction) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        Objective objective = board.registerNewObjective("assault", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        String displayName = ChatColor.RED + "⚔ Assault " + playerFac.getTag() + " VS " + faction.getTag() + " ⚔";
        if(displayName.length() <= 32) {
           objective.setDisplayName(ChatColor.RED + "⚔ Assault " + playerFac.getTag() + " VS " + faction.getTag() + " ⚔");
        } else {
            objective.setDisplayName(displayName.substring(0, 32));
        }

        Score space1 = objective.getScore("");
        space1.setScore(11);
        
        Score scoreString = objective.getScore(ChatColor.DARK_GRAY + "• Scores :");
        scoreString.setScore(10);
        
        Score space2 = objective.getScore(" ");
        space2.setScore(9);

        Score attackScore = objective.getScore(ChatColor.DARK_RED + "   • " + playerFac.getTag() + " : " + 0 + " points");
        attackScore.setScore(8);

        Score defenseScore = objective.getScore(ChatColor.GOLD + "   • " + faction.getTag() + " : " + 0 + " points");
        defenseScore.setScore(7);

        Score space3 = objective.getScore(ChatColor.GRAY + "   ");
        space3.setScore(6);

        Score allyScore = objective.getScore(ChatColor.GREEN + "• Alliés Attaque: +" + 0);
        allyScore.setScore(5);
        
        Score eAllyScore = objective.getScore(ChatColor.RED + "• Alliés Défense: +" + 0);
        eAllyScore.setScore(4);
        
        String startTime = plugin.getConfig().getString("assault.start_time." + playerFac.getTag());
        
        Score space4 = objective.getScore(ChatColor.GRAY + "    ");
        space4.setScore(1);

        Score startTimeScore = objective.getScore(ChatColor.YELLOW + "• Lancé à: " + startTime);
        startTimeScore.setScore(0);

        for (Player p : playerFac.getOnlinePlayers()) {
            p.setScoreboard(board);
        }

        for (Player p : faction.getOnlinePlayers()) {
            p.setScoreboard(board);
        }
    }
    
    public void createAllyScoreboard(Faction nFaction, String side, Faction sideFactionJoined) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        
        int index = (side == "attack") ? attackAssaultList.lastIndexOf(sideFactionJoined) : defenseAssaultList.lastIndexOf(sideFactionJoined);
        if(index != -1) {
            Faction playerFac = attackAssaultList.get(index);
            Faction faction = defenseAssaultList.get(index);

        Objective objective = board.registerNewObjective("assault", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        String displayName = ChatColor.RED + "⚔ Assault " + playerFac.getTag() + " VS " + faction.getTag() + " ⚔";
        if(displayName.length() <= 32) {
           objective.setDisplayName(ChatColor.RED + "⚔ Assault " + playerFac.getTag() + " VS " + faction.getTag() + " ⚔");
        } else {
            objective.setDisplayName(displayName.substring(0, 32));
        }

        Score space1 = objective.getScore("");
        space1.setScore(11);
        
        Score scoreString = objective.getScore(ChatColor.DARK_GRAY + "• Scores :");
        scoreString.setScore(10);
        
        Score space2 = objective.getScore(" ");
        space2.setScore(9);

        Score attackScore = objective.getScore(ChatColor.DARK_RED + "   • " + playerFac.getTag() + " : " + 0 + " points");
        attackScore.setScore(8);

        Score defenseScore = objective.getScore(ChatColor.GOLD + "   • " + faction.getTag() + " : " + 0 + " points");
        defenseScore.setScore(7);

        Score space3 = objective.getScore(ChatColor.GRAY + "   ");
        space3.setScore(6);

        Score allyScore = objective.getScore(ChatColor.GREEN + "• Alliés Attaque: +" + 0);
        allyScore.setScore(5);
        
        Score eAllyScore = objective.getScore(ChatColor.RED + "• Alliés Défense: +" + 0);
        eAllyScore.setScore(4);
        
        Score spaceX = objective.getScore(ChatColor.AQUA + "      ");
        spaceX.setScore(3);
        
        String startTime = plugin.getConfig().getString("assault.start_time." + playerFac.getTag());
        
        Score space4 = objective.getScore(ChatColor.GRAY + "    ");
        space4.setScore(1);

        Score startTimeScore = objective.getScore(ChatColor.YELLOW + "• Lancé à: " + startTime);
        startTimeScore.setScore(0);

        for (Player p : nFaction.getOnlinePlayers()) {
            p.setScoreboard(board);
           }
        }
    }

    public void startCounter() {
            assaultVerification = new BukkitRunnable() {
                @Override
                public void run() {
                	
                    for(int i = 0; i < attackAssaultList.size(); i++) {
                        Faction faction = attackAssaultList.get(i);
                        String facTag = faction.getTag();
                        int minutes = plugin.getConfig().getInt("assault.time_roaming." + facTag + ".min");
                        int seconds = plugin.getConfig().getInt("assault.time_roaming." + facTag + ".sec");

                        if(seconds >= 5) {
                            seconds -= 5;
                        } else {
                            if(minutes > 0) {
                                minutes -= 1;
                                seconds = 55 + seconds;
                            }
                        }


                        plugin.getConfig().set("assault.time_roaming." + facTag + ".min", minutes);
                        plugin.getConfig().set("assault.time_roaming." + facTag + ".sec", seconds);
                        plugin.saveConfig();

                        updateScoreboard(i);
                    }
                    
                	for(int i = 0; i < defenseAssaultList.size(); i++) {
                		Faction faction = defenseAssaultList.get(i);
                        String facTag = faction.getTag();
                        int minutes = plugin.getConfig().getInt("assault.time_roaming." + facTag + ".min");
                        int seconds = plugin.getConfig().getInt("assault.time_roaming." + facTag + ".sec");
                        
                        if(seconds >= 5) {
                            seconds -= 5;
                        } else {
                            if(minutes > 0) {
                                minutes -= 1;
                                seconds = 55 + seconds;
                            } else {
                                if(seconds == 0) {
                                    stopAssault(faction);
                                    continue;
                                }
                            }
                        }
                        
                        plugin.getConfig().set("assault.time_roaming." + facTag + ".min", minutes);
                        plugin.getConfig().set("assault.time_roaming." + facTag + ".sec", seconds);
                        plugin.saveConfig();

                        updateScoreboard(i);
                	}
                  }
               };
            assaultVerification.runTaskTimer(plugin, 0, 100);
    }

    public void stopAssault(Faction faction) {
    	String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
        Faction playerFac = attackAssaultList.get(defenseAssaultList.indexOf(faction));
        int playerFacScore = AssaultListener.attackScoreList.get(attackAssaultList.indexOf(playerFac));
        int factionScore = AssaultListener.defenseScoreList.get(defenseAssaultList.indexOf(faction));
        
        int pointToGive = plugin.getConfig().getInt("elo_points_to_give");
        int pointToGive2 = plugin.getConfig().getInt("elo_points_to_give_on_join");
        
        Faction winner = (playerFacScore > factionScore) ? playerFac : faction;
        Faction looser = (winner == faction) ? playerFac : faction;
        
        Iterator<Map.Entry<Player, Faction>> iterator = AssaultListener.taggedPlayer.entrySet().iterator();
        while(iterator.hasNext()) {
            Entry<Player, Faction> entry = iterator.next();
            if(entry.getValue().equals(winner)) {
                iterator.remove();
            }
            if(entry.getValue().equals(looser)) {
            	iterator.remove();
            }
        }
        
        int winnerScore = (winner == faction) ? factionScore : playerFacScore;
        int looserScore = (winner == faction) ? playerFacScore : factionScore;

        if(playerFacScore > factionScore || factionScore > playerFacScore) {
            Bukkit.broadcastMessage(prefix + translateString(plugin.getConfig().getString("messages.assault_win").replace("%w", winner.getTag()).replace("%l", looser.getTag()).replace("%e", String.valueOf(pointToGive)).replace("%pw", String.valueOf(winnerScore)).replace("%pl", String.valueOf(looserScore))));
            
            if(plugin.getConfig().contains("ranking." + winner.getTag())) {
            	int oldPoints = plugin.getConfig().getInt("ranking." + winner.getTag() + ".points");
            	int newPoints = oldPoints + pointToGive;
            	int oldWin = plugin.getConfig().getInt("ranking." + winner.getTag() + ".win");
            	plugin.getConfig().set("ranking." + winner.getTag() + ".points", newPoints);
            	plugin.getConfig().set("ranking." + winner.getTag() + ".win", oldWin + 1);
            	plugin.saveConfig();
            } else {
            	plugin.getConfig().set("ranking." + winner.getTag() + ".points", pointToGive);
            	plugin.getConfig().set("ranking." + winner.getTag() + ".win", 1);
            	plugin.getConfig().set("ranking." + winner.getTag() + ".loose", 0);
            	plugin.saveConfig();
            }
            
            AssaultWinEvent winEvent = new AssaultWinEvent(winner, winnerScore, pointToGive);
            Bukkit.getServer().getPluginManager().callEvent(winEvent);
            
            if(pointToGive2 != 0) {
            	String sideWin = (winner == playerFac) ? "attack" : "defense";
            	String sideLoose = (winner == playerFac) ? "defense" : "attack";
            	List<String> joiningFaction = plugin.getConfig().getStringList("assault.join." + sideWin + "." + winner.getTag()); 
            	
            	List<Faction> winnerJoin = new ArrayList<>();
            	List<Faction> looserJoin = new ArrayList<>();
            	
            	for(int i = 0; i < joiningFaction.size(); i++) {
            		if(plugin.getConfig().contains("ranking." + joiningFaction.get(i) + ".points")) {
                    	int oldPoints2 = plugin.getConfig().getInt("ranking." + joiningFaction.get(i) + ".points");
                    	int newPoints2 = oldPoints2 + pointToGive2;
                    	plugin.getConfig().set("ranking." + joiningFaction.get(i) + ".points", newPoints2);
                    	plugin.saveConfig();
            		} else {
                    	plugin.getConfig().set("ranking." + joiningFaction.get(i) + ".points", pointToGive2);
                    	plugin.saveConfig();
            		}
            		Faction fw = Factions.getInstance().getByTag(joiningFaction.get(i));
            		winnerJoin.add(fw);
            		fw.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_win_elo_on_join_assault").replace("%e", String.valueOf(pointToGive2)))));
            	}
            	
            	List<String> joiningFactionLoose = plugin.getConfig().getStringList("assault.join." + sideLoose + "." + looser.getTag()); 
            	for(int i = 0; i < joiningFactionLoose.size(); i++) {
            		if(plugin.getConfig().contains("ranking." + joiningFactionLoose.get(i) + ".points")) {
                    	int oldPoints2 = plugin.getConfig().getInt("ranking." + joiningFactionLoose.get(i) + ".points");
                    	int newPoints2 = oldPoints2 - pointToGive2;
                    	plugin.getConfig().set("ranking." + joiningFactionLoose.get(i) + ".points", newPoints2);
                    	plugin.saveConfig();
            		} else {
                    	plugin.getConfig().set("ranking." + joiningFactionLoose.get(i) + ".points", 0);
                    	plugin.saveConfig();
            		}
            		Faction fw = Factions.getInstance().getByTag(joiningFactionLoose.get(i));
            		looserJoin.add(fw);
            		fw.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_loose_elo_on_join_assault").replace("%e", String.valueOf(pointToGive2)))));
            	}
            	
            	AssaultStopEvent stopEvent = new AssaultStopEvent(winner, looser, winnerJoin, looserJoin, winnerScore, looserScore);
            	Bukkit.getServer().getPluginManager().callEvent(stopEvent);
            }
            
            if(plugin.getConfig().contains("ranking." + looser.getTag())) {
            	int oldPoints = plugin.getConfig().getInt("ranking." + looser.getTag() + ".points");
            	int newPoints = 0;
            	if(oldPoints - pointToGive <= 0) {
            		newPoints = 0;
            	} else {
            		newPoints = oldPoints - pointToGive;
            	}
            	int oldLoose = plugin.getConfig().getInt("ranking." + looser.getTag() + ".loose");
            	plugin.getConfig().set("ranking." + looser.getTag() + ".points", newPoints);
            	plugin.getConfig().set("ranking." + looser.getTag() + ".loose", oldLoose + 1);
            	plugin.saveConfig();
            } else {
            	plugin.getConfig().set("ranking." + looser.getTag() + ".points", 0);
            	plugin.getConfig().set("ranking." + looser.getTag() + ".win", 0);
            	plugin.getConfig().set("ranking." + looser.getTag() + ".loose", 1);
            	plugin.saveConfig();
            }
            
            AssaultLooseEvent looseEvent = new AssaultLooseEvent(looser, looserScore, pointToGive);
            Bukkit.getServer().getPluginManager().callEvent(looseEvent);
        } else {
            Bukkit.broadcastMessage(prefix + translateString(plugin.getConfig().getString("messages.assault_equality").replace("%d", faction.getTag()).replace("%a", playerFac.getTag()).replace("%p", String.valueOf(playerFacScore))));
        }

        for (Player player : playerFac.getOnlinePlayers()) {
            Scoreboard board = player.getScoreboard();
            if(board != null) {
                board.clearSlot(DisplaySlot.SIDEBAR);
            }
        }

        for (Player player : faction.getOnlinePlayers()) {
            Scoreboard board = player.getScoreboard();
            if(board != null) {
                board.clearSlot(DisplaySlot.SIDEBAR);
            }
        }
        
        List<String> attackJoinFac = plugin.getConfig().getStringList("assault.join.attack." + playerFac.getTag());
        List<String> defenseJoinFac = plugin.getConfig().getStringList("assault.join.defense." + faction.getTag());
        
        if(!attackJoinList.isEmpty() && !attackJoinFac.isEmpty()) {
            for(int i = 0; i < attackJoinFac.size(); i++) {
            	Faction fact = Factions.getInstance().getByTag(attackJoinFac.get(i));
            	for(Player player : fact.getOnlinePlayers()) {
                    Scoreboard board = player.getScoreboard();
                    if(board != null) {
                        board.clearSlot(DisplaySlot.SIDEBAR);
                    }
            	}
            }
        }
        
        if(!defenseJoinList.isEmpty() && !defenseJoinFac.isEmpty()) {
            for(int i = 0; i < defenseJoinFac.size(); i++) {
            	Faction fact = Factions.getInstance().getByTag(defenseJoinFac.get(i));
            	for(Player player : fact.getOnlinePlayers()) {
                    Scoreboard board = player.getScoreboard();
                    if(board != null) {
                        board.clearSlot(DisplaySlot.SIDEBAR);
                    }
            	}
            }
        }
        
        processRemoveFactionsJoin(playerFac, "attack", false);
        processRemoveFactionsJoin(faction, "defense", false);
        processRemoveFactionsJoin(playerFac, "attack", true);
        processRemoveFactionsJoin(faction, "defense", true);
        
        if(operationCount >= 4) {
        	operationCount = 0;
            plugin.getConfig().set("assault.time_roaming." + playerFac.getTag(), null);
            plugin.getConfig().set("assault.time_roaming." + faction.getTag(), null);
            plugin.getConfig().set("assault.start_time." + playerFac.getTag(), null);
            plugin.getConfig().set("assault." + playerFac.getTag(), null);
            plugin.getConfig().set("assault." + faction.getTag(), null);
            plugin.saveConfig();

            int index = defenseAssaultList.lastIndexOf(faction);

            if(index != -1) {
                AssaultListener.attackScoreList.remove(index);
                AssaultListener.defenseScoreList.remove(index);
                attackAssaultList.remove(playerFac);
                defenseAssaultList.remove(faction);
            
            if(attackAssaultList.isEmpty()) {
            	assaultVerification.cancel();
                this.assaultVerification = null;
                }
            }
        }
    }
    
    public void processRemoveFactionsJoin(Faction targetFaction, String assaultSide, boolean waiting) {
        FileConfiguration config = plugin.getConfig();
        String targetTag = targetFaction.getTag();

        String configPath = "assault." + (waiting ? "waiting_" : "") + "join." + assaultSide + "." + targetTag;
        List<String> factionsToRemove = config.getStringList(configPath);

        List<Faction> allFactions = Factions.getInstance().getAllFactions();

        allFactions.removeIf(faction -> factionsToRemove.contains(faction.getTag()));
        if(!attackJoinList.isEmpty()) {
            attackJoinList.removeIf(faction -> factionsToRemove.contains(faction.getTag()));
        }
        
        if(!defenseJoinList.isEmpty()) {
            defenseJoinList.removeIf(faction -> factionsToRemove.contains(faction.getTag()));
        }

        config.set(configPath, null);
        plugin.saveConfig();
        operationCount++;
    }

    public void updateScoreboard(int index) {
        Faction attackFac = AssaultCommand.attackAssaultList.get(index);
        Faction defenseFac = AssaultCommand.defenseAssaultList.get(index);

        String attackS = ChatColor.DARK_RED + "   • " + attackFac.getTag() + " : ";
        String defenseS = ChatColor.GOLD + "   • " + defenseFac.getTag() + " : ";

        String facTag = attackFac.getTag();
        String dFacTag = defenseFac.getTag();

        int minutes = plugin.getConfig().getInt("assault.time_roaming." + facTag + ".min");
        int seconds = plugin.getConfig().getInt("assault.time_roaming." + facTag + ".sec");

        String timeRemaining = (minutes > 0 ? minutes + "m" + seconds + "s" : seconds + "s");
        String scoreKey = ChatColor.YELLOW + "• Temps restant: ";
        String scoreValue = ChatColor.YELLOW + timeRemaining;

        String allyKey = ChatColor.GREEN + "• Alliés Attaque: +";
        String eAllyKey = ChatColor.RED + "• Alliés Défense: +";

        updatePlayerScoreboards(attackFac, index, attackS, defenseS, scoreKey, scoreValue, allyKey, eAllyKey, facTag);
        updatePlayerScoreboards(defenseFac, index, attackS, defenseS, scoreKey, scoreValue, allyKey, eAllyKey, dFacTag);

        updateFactionAllies("assault.join.attack." + attackFac.getTag(), index, attackS, defenseS, scoreKey, scoreValue, allyKey, eAllyKey, facTag);
        updateFactionAllies("assault.join.defense." + defenseFac.getTag(), index, attackS, defenseS, scoreKey, scoreValue, allyKey, eAllyKey, dFacTag);
    }

    private void updatePlayerScoreboards(Faction faction, int index, String attackS, String defenseS, String scoreKey, String scoreValue, String allyKey, String eAllyKey, String facTag) {
        for (Player player : faction.getOnlinePlayers()) {
            Scoreboard board = player.getScoreboard();
            if(board == null) continue;

            Objective objective = board.getObjective("assault");
            if(objective == null) continue;

            for (String entry : board.getEntries()) {
                if(entry.startsWith(attackS) || entry.startsWith(defenseS) || entry.startsWith(scoreKey) || entry.startsWith(allyKey) || entry.startsWith(eAllyKey)) {
                    board.resetScores(entry);
                }
            }

            objective.getScore(attackS + AssaultListener.attackScoreList.get(index) + " points").setScore(8);
            objective.getScore(defenseS + AssaultListener.defenseScoreList.get(index) + " points").setScore(7);
            objective.getScore(scoreKey + scoreValue).setScore(2);
            objective.getScore(allyKey + plugin.getConfig().getStringList("assault.join.attack." + facTag).size()).setScore(5);
            objective.getScore(eAllyKey + plugin.getConfig().getStringList("assault.join.defense." + facTag).size()).setScore(4);
        }
    }

    private void updateFactionAllies(String configPath, int index, String attackS, String defenseS, String scoreKey, String scoreValue, String allyKey, String eAllyKey, String facTag) {
        if(plugin.getConfig().contains(configPath)) {
            List<String> facName = plugin.getConfig().getStringList(configPath);
            for (String name : facName) {
                Faction faction = Factions.getInstance().getByTag(name);
                updatePlayerScoreboards(faction, index, attackS, defenseS, scoreKey, scoreValue, allyKey, eAllyKey, facTag);
            }
        }
    }
    
    public String searchAssaultFac(Faction faction) {
    	for(int i = 0; i < attackAssaultList.size(); i++) {
    		Faction fac = attackAssaultList.get(i);
        	if(plugin.getConfig().contains("assault.join.attack." + fac.getTag())) {
        		List<String> facName = plugin.getConfig().getStringList("assault.join.attack." + fac.getTag());
        		if(!facName.isEmpty()) {
        			for(int z = 0; z < facName.size(); z++) {
        				Faction finalFac = Factions.getInstance().getBestTagMatch(facName.get(z));
        				if(finalFac == faction) {
        					return fac.getTag();
        				}
        			}
        		}
            }
    	}
    	
    	for(int i = 0; i < defenseAssaultList.size(); i++) {
    		Faction fac = defenseAssaultList.get(i);
        	if(plugin.getConfig().contains("assault.join.defense." + fac.getTag())) {
        		List<String> facName = plugin.getConfig().getStringList("assault.join.defense." + fac.getTag());
        		if(!facName.isEmpty()) {
        			for(int z = 0; z < facName.size(); z++) {
        				Faction finalFac = Factions.getInstance().getBestTagMatch(facName.get(z));
        				if(finalFac == faction) {
        					return fac.getTag();
        				}
        			}
        		}
            }
    	}
    	
    	return null;
    }

    public String translateString(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}


