package fr.Boulldogo.AssaultPlugin.Commands.Subcommands;

import org.bukkit.entity.Player;

import fr.Boulldogo.AssaultPlugin.Commands.Abstract.AssaultSubcommand;

public class CmdHelp extends AssaultSubcommand {
	
	public CmdHelp() {
		this.setName("help")
		.setPermission("help");
	}

	@Override
	public void execute(Player player) {
		for(String line : this.getPlugin().getConfig().getStringList("help-message")) {
			player.sendMessage(this.translateString(line));
		}
	}
}
