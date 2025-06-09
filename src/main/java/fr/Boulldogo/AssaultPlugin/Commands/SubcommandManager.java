package fr.Boulldogo.AssaultPlugin.Commands;

import java.util.ArrayList;
import java.util.List;

import fr.Boulldogo.AssaultPlugin.Commands.Abstract.AssaultSubcommand;
import fr.Boulldogo.AssaultPlugin.Commands.Subcommands.CmdAccept;
import fr.Boulldogo.AssaultPlugin.Commands.Subcommands.CmdAdmin;
import fr.Boulldogo.AssaultPlugin.Commands.Subcommands.CmdHelp;
import fr.Boulldogo.AssaultPlugin.Commands.Subcommands.CmdJoin;
import fr.Boulldogo.AssaultPlugin.Commands.Subcommands.CmdList;
import fr.Boulldogo.AssaultPlugin.Commands.Subcommands.CmdRanking;
import fr.Boulldogo.AssaultPlugin.Commands.Subcommands.CmdZone;

public class SubcommandManager {
	private static List<AssaultSubcommand> subcommands = new ArrayList<>();
	
	private static final CmdAccept accept = new CmdAccept();
	private static final CmdAdmin admin = new CmdAdmin();
	private static final CmdHelp help = new CmdHelp();
	private static final CmdJoin join = new CmdJoin();
	private static final CmdList list = new CmdList();
	private static final CmdRanking ranking = new CmdRanking();
	private static final CmdZone zone = new CmdZone();
	
	public static void registerSubcommands() {
		subcommands.add(accept);
		subcommands.add(admin);
		subcommands.add(help);
		subcommands.add(join);
		subcommands.add(list);
		subcommands.add(ranking);
		subcommands.add(zone);
	}
	
	public static List<AssaultSubcommand> getCommands() {
		return subcommands;
	}
}
