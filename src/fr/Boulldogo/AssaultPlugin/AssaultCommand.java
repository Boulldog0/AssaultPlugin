package fr.Boulldogo.AssaultPlugin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
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

public class AssaultCommand implements CommandExecutor {

    private final Main plugin;
    private BukkitRunnable assaultVerification = null;
    private boolean hasAssaultVerif = false;

    public static List<Faction> attackAssaultList = new ArrayList<>();
    public static List<Faction> defenseAssaultList = new ArrayList<>();

    public AssaultCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getLogger().info("Only players can use that command!");
            return true;
        }

        String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(prefix + ChatColor.RED + "Please give correct argument. Type /assault help for more help.");
            return true;
        }

        String subCommand = args[0];

        if (subCommand.equals("help")) {
            player.sendMessage(translateString(plugin.getConfig().getString("help_header")));
            player.sendMessage(translateString(plugin.getConfig().getString("help_1")));
            player.sendMessage(translateString(plugin.getConfig().getString("help_2")));
            player.sendMessage(translateString(plugin.getConfig().getString("help_3")));
            player.sendMessage(translateString(plugin.getConfig().getString("help_4")));
            player.sendMessage(translateString(plugin.getConfig().getString("help_5")));
            player.sendMessage(translateString(plugin.getConfig().getString("help_footer")));
            return true;
        } else if (subCommand.equals("list")) {
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
        } else if (subCommand.equals("admin")) {
            if (!player.hasPermission("assault.admin")) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("no-permission")));
                return true;
            }

            if (args.length < 2) {
                player.sendMessage(prefix + ChatColor.RED + "Admin arguments: /assault admin <stop/resetcd> <args>");
                return true;
            }

            String command = args[1];
            if (command.equals("stop")) {
                if (args.length < 3) {
                    player.sendMessage(prefix + ChatColor.RED + "Correct arguments: /assault admin stop <faction>");
                    return true;
                }

                String facName = args[2];
                Faction faction = Factions.getInstance().getByTag(facName);
                if (faction == null || faction.isWilderness()) {
                    player.sendMessage(prefix + ChatColor.RED + "You cannot stop a wilderness assault!");
                    return true;
                }

                if (!attackAssaultList.contains(faction) && !defenseAssaultList.contains(faction)) {
                    player.sendMessage(prefix + ChatColor.RED + "This faction is not in assault!");
                    return true;
                }

                stopAssault(faction);
                player.sendMessage(prefix + ChatColor.GREEN + "Assault stopped for faction " + faction.getTag() + "!");
            } else if (command.equals("resetcd")) {
                if (args.length < 4) {
                    player.sendMessage(prefix + ChatColor.RED + "Correct arguments: /assault admin resetcd <faction> <faction_assault_to_reset>");
                    return true;
                }

                String facName = args[2];
                Faction faction = Factions.getInstance().getByTag(facName);
                if (faction == null || faction.isWilderness()) {
                    player.sendMessage(prefix + ChatColor.RED + "You cannot reset assault cooldown for a wilderness faction!");
                    return true;
                }

                String aFacName = args[3];
                Faction aFaction = Factions.getInstance().getByTag(aFacName);
                if (aFaction == null || aFaction.isWilderness()) {
                    player.sendMessage(prefix + ChatColor.RED + "You cannot reset assault cooldown for a wilderness faction!");
                    return true;
                }

                if (!plugin.getConfig().contains("cooldowns." + facName + "." + aFacName)) {
                    player.sendMessage(prefix + ChatColor.RED + "This faction doesn't have cooldown for faction " + aFacName + "!");
                    return true;
                }

                plugin.getConfig().set("assault.cooldown." + facName + "." + aFacName, null);
                plugin.saveConfig();
                aFaction.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.reset_cooldown_by_faction").replace("%f", facName).replace("%s", player.getName()))));
                faction.getOnlinePlayers().forEach(member -> member.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.reset_cooldown_for_faction").replace("%f", aFacName).replace("%s", player.getName()))));
                player.sendMessage(prefix + "Cooldown of faction " + facName + " for assault faction " + aFacName + " successfully reset!");
            }
        } else if (subCommand.equals("ranking")) {
            String header = translateString(plugin.getConfig().getString("ranking_header"));
            String lineTemplate = translateString(plugin.getConfig().getString("ranking_lines"));
            String footer = translateString(plugin.getConfig().getString("ranking_footer"));
            int maxEntries = plugin.getConfig().getInt("ranking_entries", 10);

            ConfigurationSection rankingSection = plugin.getConfig().getConfigurationSection("ranking");
            if (rankingSection == null) {
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
        } else {
            String facName = args[0];
            Faction faction = Factions.getInstance().getBestTagMatch(facName);
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

            if (factionCount < plugin.getConfig().getInt("minimum_defense_faction_connected_count")) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.not_enought_online_players")));
                return true;
            }
            
            Role playerRole = FPlayers.getInstance().getByPlayer(player).getRole();
            Role role = Role.fromString(plugin.getConfig().getString("minimum_role_allowed_for_assault"));
            if(!isRoleValid(playerRole, role)) {
            	player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_havnt_good_role")));
            	return true;
            }

            if (playerFacCount < plugin.getConfig().getInt("minimum_attack_faction_connected_count")) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.not_enought_online_players_in_your_faction")));
                return true;
            }

            if (attackAssaultList.contains(faction) || defenseAssaultList.contains(faction)) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_cant_assault_if_it_are_in_assault")));
                return true;
            }

            if (attackAssaultList.contains(playerFac) || defenseAssaultList.contains(playerFac)) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_cant_assault_if_you_are_in_assault")));
                return true;
            }

            if (plugin.getConfig().contains("assault.cooldown." + playerFac.getTag() + "." + faction.getTag())) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_have_cooldown_for_assault_this_faction").replace("%m", String.valueOf(plugin.getConfig().getInt("assault.cooldown." + playerFac.getTag() + "." + faction.getTag() + ".min")).replace("%s", String.valueOf(plugin.getConfig().getInt("assault.cooldown." + playerFac.getTag() + "." + faction.getTag() + ".sec"))))));
                return true;
            }
            
            Relation relation = playerFac.getRelationTo(faction); 

            if (relation != Relation.ENEMY) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.you_cant_in_enemy_with_this_faction").replace("%m", String.valueOf(plugin.getConfig().getInt("assault.cooldown." + playerFac.getTag() + "." + faction.getTag() + ".min")).replace("%s", String.valueOf(plugin.getConfig().getInt("assault.cooldown." + playerFac.getTag() + "." + faction.getTag() + ".sec"))))));
                return true;
            }
            
            if(plugin.getConfig().contains("cooldowns." + playerFac.getTag() + "." + faction.getTag())) {
                player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.they_are_cooldown_with_this_faction").replace("%m", String.valueOf(plugin.getConfig().getInt("cooldowns." + playerFac.getTag() + "." + faction.getTag())))));
                return true;
            }

            String startTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":" + (Calendar.getInstance().get(Calendar.MINUTE) > 10 ? Calendar.getInstance().get(Calendar.MINUTE) : "0" +  Calendar.getInstance().get(Calendar.MINUTE));
            Bukkit.broadcastMessage(prefix + translateString(plugin.getConfig().getString("messages.start_assault").replace("%f", playerFac.getTag()).replace("%df", faction.getTag()).replace("%h", startTime)));
            attackAssaultList.add(playerFac);
            defenseAssaultList.add(faction);
            if (plugin.getConfig().getBoolean("play_sound")) {
                playerFac.getOnlinePlayers().forEach(member -> member.playSound(member.getLocation(), Sound.valueOf(plugin.getConfig().getString("played_sound")), 4.0F, 4.0F));
                faction.getOnlinePlayers().forEach(member -> member.playSound(member.getLocation(), Sound.valueOf(plugin.getConfig().getString("played_sound")), 4.0F, 4.0F));
            }
            int duration = plugin.getConfig().getInt("duration_of_assault");
            plugin.getConfig().set("assault.time_roaming." + playerFac.getTag() + ".min", duration);
            plugin.getConfig().set("assault.time_roaming." + playerFac.getTag() + ".sec", 0);
            plugin.getConfig().set("assault.time_roaming." + faction.getTag() + ".min", duration);
            plugin.getConfig().set("assault.time_roaming." + faction.getTag() + ".sec", 0);
            plugin.getConfig().set("assault.start_time." + playerFac.getTag(), startTime);
            plugin.saveConfig();
            
            AssaultListener.attackScoreList.add(0);
            AssaultListener.defenseScoreList.add(0);
            
            int cooldownTime = plugin.getConfig().getInt("cooldown_behind_assaults");
            plugin.getConfig().set("cooldowns." + playerFac.getTag() + "." + faction.getTag(), cooldownTime);
            plugin.saveConfig();

            if (plugin.getConfig().getBoolean("enable_assault_scoreboard")) {
                createScoreboard(playerFac, faction);
            }

            startCounter();
        }

        return true;
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
        objective.setDisplayName(ChatColor.RED + "⚔ Assault " + playerFac.getTag() + " VS " + faction.getTag() + " ⚔");

        Score space1 = objective.getScore("");
        space1.setScore(10);
        
        Score scoreString = objective.getScore(ChatColor.DARK_GRAY + "• Scores :");
        scoreString.setScore(9);
        
        Score space2 = objective.getScore(" ");
        space2.setScore(8);

        Score attackScore = objective.getScore(ChatColor.DARK_RED + "   • " + playerFac.getTag() + " : " + 0 + " points");
        attackScore.setScore(7);
        
        Score spacee = objective.getScore("  ");
        spacee.setScore(6);

        Score defenseScore = objective.getScore(ChatColor.GOLD + "   • " + faction.getTag() + " : " + 0 + " points");
        defenseScore.setScore(5);

        Score space3 = objective.getScore(ChatColor.GRAY + "   ");
        space3.setScore(4);
        
        Score space5 = objective.getScore(ChatColor.GRAY + "      ");
        space3.setScore(3);
        
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

    public void startCounter() {
        if (!hasAssaultVerif) {
            hasAssaultVerif = true;

            assaultVerification = new BukkitRunnable() {
                @Override
                public void run() {
                    for (Faction faction : attackAssaultList) {
                        String facTag = faction.getTag();
                        int minutes = plugin.getConfig().getInt("assault.time_roaming." + facTag + ".min");
                        int seconds = plugin.getConfig().getInt("assault.time_roaming." + facTag + ".sec");

                        if (minutes == 0 && seconds == 0) {
                            stopAssault(faction);
                            continue;
                        }

                        plugin.getConfig().set("assault.time_roaming." + facTag + ".min", minutes);
                        plugin.getConfig().set("assault.time_roaming." + facTag + ".sec", seconds);
                        plugin.saveConfig();

                        updateScoreboards(faction);
                    }
                    
                    for (Faction faction : defenseAssaultList) {
                        String facTag = faction.getTag();
                        int minutes = plugin.getConfig().getInt("assault.time_roaming." + facTag + ".min");
                        int seconds = plugin.getConfig().getInt("assault.time_roaming." + facTag + ".sec");

                        if (seconds >= 5) {
                            seconds -= 5;
                        } else {
                            if (minutes > 0) {
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

                        updateScoreboards(faction);
                    }
                }
            };

            assaultVerification.runTaskTimer(plugin, 0, 100);
        }
    }

    public void stopAssault(Faction faction) {
    	String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
    	if(attackAssaultList.size() == 0) {
    		hasAssaultVerif = false;
    	}
        Faction playerFac = attackAssaultList.get(defenseAssaultList.indexOf(faction));
        int playerFacScore = AssaultListener.attackScoreList.get(attackAssaultList.indexOf(playerFac));
        int factionScore = AssaultListener.defenseScoreList.get(defenseAssaultList.indexOf(faction));
        
        int pointToGive = plugin.getConfig().getInt("elo_points_to_give");
        
        Faction winner = (playerFacScore > factionScore) ? playerFac : faction;
        Faction looser = (winner == faction) ? playerFac : faction;
        
        int winnerScore = (winner == faction) ? factionScore : playerFacScore;
        int looserScore = (winner == faction) ? playerFacScore : factionScore;

        if (playerFacScore > factionScore || factionScore > playerFacScore) {
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
        } else {
            Bukkit.broadcastMessage(prefix + translateString(plugin.getConfig().getString("messages.assault_equality").replace("%d", faction.getTag()).replace("%a", playerFac.getTag()).replace("%p", String.valueOf(playerFacScore))));
        }

        plugin.getConfig().set("assault.time_roaming." + playerFac.getTag(), null);
        plugin.getConfig().set("assault.time_roaming." + faction.getTag(), null);
        plugin.getConfig().set("assault.start_time." + playerFac.getTag(), null);
        plugin.getConfig().set("assault." + playerFac.getTag(), null);
        plugin.getConfig().set("assault." + faction.getTag(), null);
        plugin.saveConfig();

        for (Player player : playerFac.getOnlinePlayers()) {
            Scoreboard board = player.getScoreboard();
            if (board != null) {
                board.clearSlot(DisplaySlot.SIDEBAR);
            }
        }

        for (Player player : faction.getOnlinePlayers()) {
            Scoreboard board = player.getScoreboard();
            if (board != null) {
                board.clearSlot(DisplaySlot.SIDEBAR);
            }
        }
        
        int index = attackAssaultList.indexOf(playerFac);

        attackAssaultList.remove(playerFac);
        defenseAssaultList.remove(faction);
        AssaultListener.attackScoreList.remove(index);
        AssaultListener.defenseScoreList.remove(index);
        
    }

    public void updateScoreboards(Faction faction) {
        String facTag = faction.getTag();
        int minutes = plugin.getConfig().getInt("assault.time_roaming." + facTag + ".min");
        int seconds = plugin.getConfig().getInt("assault.time_roaming." + facTag + ".sec");

        String timeRemaining = (minutes > 0 ? minutes + "m" + seconds + "s" : seconds + "s");
        String scoreKey = ChatColor.YELLOW + "• Temps restant: ";
        String scoreValue = ChatColor.YELLOW + timeRemaining;

        for (Player player : faction.getOnlinePlayers()) {
            Scoreboard board = player.getScoreboard();
            if (board == null) continue;

            Objective objective = board.getObjective("assault");
            if (objective == null) continue;

            for (String entry : board.getEntries()) {
                if (entry.startsWith(scoreKey)) {
                    board.resetScores(entry);
                    break;
                }
            }
            
            objective.getScore(scoreKey + scoreValue).setScore(2);
        }
    }


    public String translateString(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}


