package fr.Boulldogo.AssaultPlugin.Commands.Subcommands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.Boulldogo.AssaultPlugin.AssaultPlugin;
import fr.Boulldogo.AssaultPlugin.Commands.Abstract.AssaultSubcommand;
import fr.Boulldogo.AssaultPlugin.Utils.FactionRanking;

public class CmdRanking extends AssaultSubcommand {
	
	public CmdRanking() {
		this.setName("ranking")
		.setPermission("ranking");
	}

	@Override
	public void execute(Player player) {
		AssaultPlugin plugin = this.getPlugin();
        String header = translateString(plugin.getConfig().getString("ranking_header"));
        String lineTemplate = translateString(plugin.getConfig().getString("ranking_lines"));
        String footer = translateString(plugin.getConfig().getString("ranking_footer"));
        int maxEntries = plugin.getConfig().getInt("ranking_entries", 10);

        ConfigurationSection rankingSection = plugin.getConfig().getConfigurationSection("ranking");
        if(rankingSection == null) {
            player.sendMessage(prefix + translateString(plugin.getConfig().getString("ranking_no_entries")));
            return;
        }

        List<FactionRanking> rankings = new ArrayList<>();
        for(String factionName : rankingSection.getKeys(false)) {
            int points = rankingSection.getInt(factionName + ".points");
            int wins = rankingSection.getInt(factionName + ".win");
            int losses = rankingSection.getInt(factionName + ".loose");
            rankings.add(new FactionRanking(factionName, points, wins, losses));
        }
        
        if(rankings.size() == 0) {
            player.sendMessage(prefix + translateString(plugin.getConfig().getString("ranking_no_entries")));
            return;
        }

        rankings.sort((a, b) -> Integer.compare(b.getPoints(), a.getPoints()));

        int entriesToShow = Math.min(rankings.size(), maxEntries);

        StringBuilder rankingMessage = new StringBuilder();
        rankingMessage.append(header).append("\n");
        for(int i = 0; i < entriesToShow; i++) {
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

        player.sendMessage(rankingMessage.toString());
	}
}
